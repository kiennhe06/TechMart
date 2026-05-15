package fpl.ph60001.techmart.product.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import fpl.ph60001.techmart.home.ui.SimpleProductItem
import fpl.ph60001.techmart.home.viewmodel.HomeViewModel
import fpl.ph60001.techmart.ui.theme.*

import androidx.compose.foundation.background
import androidx.compose.foundation.shape.RoundedCornerShape

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AllProductsScreen(
    onBackClick: () -> Unit,
    onProductClick: (String) -> Unit,
    homeViewModel: HomeViewModel = viewModel()
) {
    val homeData by homeViewModel.homeData.collectAsState()
    val isLoading by homeViewModel.isLoading.collectAsState()
    var searchQuery by remember { mutableStateOf("") }

    val filteredProducts = homeData?.allProducts?.filter {
        it.name.contains(searchQuery, ignoreCase = true)
    } ?: emptyList()

    Scaffold(
        topBar = {
            Column(modifier = Modifier.background(TechDark)) {
                TopAppBar(
                    title = { Text("Tất cả sản phẩm", color = WhitePure) },
                    navigationIcon = {
                        IconButton(onClick = onBackClick) {
                            Icon(Icons.Default.ArrowBack, contentDescription = null, tint = WhitePure)
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(containerColor = TechDark)
                )
                
                // Search Bar
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    placeholder = { Text("Tìm kiếm sản phẩm...", color = TechGray) },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = TechGray) },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = BluePrimary,
                        unfocusedBorderColor = TechSlate,
                        focusedContainerColor = TechSlate.copy(alpha = 0.5f),
                        unfocusedContainerColor = TechSlate.copy(alpha = 0.3f),
                        focusedTextColor = WhitePure,
                        unfocusedTextColor = WhitePure
                    ),
                    shape = RoundedCornerShape(12.dp),
                    singleLine = true
                )
                Spacer(Modifier.height(8.dp))
            }
        },
        containerColor = TechDark
    ) { padding ->
        if (isLoading && homeData == null) {
            Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = BluePrimary)
            }
        } else if (filteredProducts.isEmpty()) {
            Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                Text("Không tìm thấy sản phẩm nào", color = TechGray)
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
                items(filteredProducts) { product ->
                    SimpleProductItem(
                        product = product,
                        onClick = { onProductClick(product.id) }
                    )
                }
            }
        }
    }
}
