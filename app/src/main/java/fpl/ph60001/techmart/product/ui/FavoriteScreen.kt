package fpl.ph60001.techmart.product.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import fpl.ph60001.techmart.cart.viewmodel.CartViewModel
import fpl.ph60001.techmart.home.viewmodel.HomeViewModel
import fpl.ph60001.techmart.network.SimpleProductDto
import fpl.ph60001.techmart.ui.theme.*

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
                title = {
                    Column {
                        Text("Sản phẩm yêu thích", color = WhitePure, fontWeight = FontWeight.Bold)
                        if (favoriteProducts.isNotEmpty()) {
                            Text(
                                "${favoriteProducts.size} sản phẩm",
                                color = TechGray,
                                fontSize = 12.sp
                            )
                        }
                    }
                },
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
            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentPadding = PaddingValues(12.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(favoriteProducts) { product ->
                    FavoriteProductCard(
                        product = product,
                        onClick = { onProductClick(product.id) },
                        onRemove = { cartViewModel.toggleFavorite(product.id) }
                    )
                }
            }
        }
    }
}

@Composable
fun FavoriteProductCard(
    product: SimpleProductDto,
    onClick: () -> Unit,
    onRemove: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = TechSlate)
    ) {
        Box {
            Column {
                // Product Image
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(150.dp)
                        .clip(RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp))
                        .background(TechDark)
                ) {
                    AsyncImage(
                        model = product.image,
                        contentDescription = null,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                }

                // Product Info
                Column(modifier = Modifier.padding(12.dp)) {
                    Text(
                        text = product.name,
                        color = WhitePure,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 14.sp,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                    Spacer(Modifier.height(4.dp))
                    Text(
                        text = product.price,
                        color = BluePrimary,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                    )
                }
            }

            // Heart remove button
            IconButton(
                onClick = onRemove,
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(4.dp)
                    .size(32.dp)
                    .background(Color.Black.copy(alpha = 0.5f), CircleShape)
            ) {
                Icon(
                    Icons.Default.Favorite,
                    contentDescription = null,
                    tint = Color.Red,
                    modifier = Modifier.size(18.dp)
                )
            }
        }
    }
}
