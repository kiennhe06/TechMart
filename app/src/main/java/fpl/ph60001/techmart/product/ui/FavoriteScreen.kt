package fpl.ph60001.techmart.product.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import fpl.ph60001.techmart.cart.viewmodel.CartViewModel
import fpl.ph60001.techmart.home.ui.SimpleProductItem
import fpl.ph60001.techmart.home.viewmodel.HomeViewModel
import fpl.ph60001.techmart.ui.theme.*

import fpl.ph60001.techmart.network.SimpleProductDto

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FavoriteScreen(
    onBackClick: () -> Unit,
    onProductClick: (String) -> Unit,
    cartViewModel: CartViewModel = viewModel(),
    homeViewModel: HomeViewModel = viewModel()
) {
    val favoriteIds by cartViewModel.favoriteIds.collectAsState()
    val homeData by homeViewModel.homeData.collectAsState()
    val isLoading by homeViewModel.isLoading.collectAsState()
    
    val favoriteProducts = remember(homeData, favoriteIds) {
        val all = mutableListOf<SimpleProductDto>()
        homeData?.let { data ->
            all.addAll(data.allProducts)
            data.flashSale.forEach { flash ->
                if (all.none { it.id == flash.id }) {
                    all.add(SimpleProductDto(flash.id, flash.name, flash.price, flash.image, 0))
                }
            }
        }
        all.filter { it.id in favoriteIds }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Sản phẩm yêu thích", color = WhitePure) },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.Default.ArrowBack, contentDescription = null, tint = WhitePure)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = TechDark)
            )
        },
        containerColor = TechDark
    ) { padding ->
        if (isLoading && homeData == null) {
            Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = BluePrimary)
            }
        } else if (favoriteIds.isEmpty() || favoriteProducts.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        Icons.Default.Favorite,
                        contentDescription = null,
                        modifier = Modifier.size(80.dp),
                        tint = TechGray.copy(alpha = 0.3f)
                    )
                    Spacer(Modifier.height(16.dp))
                    Text("Bạn chưa yêu thích sản phẩm nào", color = TechGray)
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                items(favoriteProducts) { product ->
                    SimpleProductItem(
                        product = product,
                        onClick = { onProductClick(product.id) }
                    )
                }
            }
        }
    }
}
