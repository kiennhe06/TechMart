package fpl.ph60001.techmart.product.ui

import androidx.compose.foundation.background
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import fpl.ph60001.techmart.home.ui.CountdownBox
import fpl.ph60001.techmart.home.ui.FlashSaleItem
import fpl.ph60001.techmart.home.viewmodel.HomeViewModel
import fpl.ph60001.techmart.ui.theme.*
import kotlinx.coroutines.delay

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FlashSaleListScreen(
    onBackClick: () -> Unit,
    onProductClick: (String) -> Unit,
    homeViewModel: HomeViewModel = viewModel()
) {
    val homeData by homeViewModel.homeData.collectAsState()
    val isLoading by homeViewModel.isLoading.collectAsState()
    
    // Countdown logic
    var timeLeft by remember { mutableStateOf(36000) }
    LaunchedEffect(key1 = true) {
        while (timeLeft > 0) {
            delay(1000)
            timeLeft--
        }
    }
    val hours = timeLeft / 3600
    val minutes = (timeLeft % 3600) / 60
    val seconds = timeLeft % 60

    Scaffold(
        topBar = {
            Column(modifier = Modifier.background(TechDark)) {
                TopAppBar(
                    title = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text("FLASH SALE", color = Color(0xFFFF4D4D), fontWeight = FontWeight.Black)
                            Spacer(Modifier.width(12.dp))
                            CountdownBox(time = String.format("%02d", hours))
                            Text(":", color = WhitePure, modifier = Modifier.padding(horizontal = 2.dp))
                            CountdownBox(time = String.format("%02d", minutes))
                            Text(":", color = WhitePure, modifier = Modifier.padding(horizontal = 2.dp))
                            CountdownBox(time = String.format("%02d", seconds))
                        }
                    },
                    navigationIcon = {
                        IconButton(onClick = onBackClick) {
                            Icon(Icons.Default.ArrowBack, contentDescription = null, tint = WhitePure)
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(containerColor = TechDark)
                )
            }
        },
        containerColor = TechDark
    ) { padding ->
        if (isLoading && homeData == null) {
            Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = BluePrimary)
            }
        } else {
            val flashProducts = homeData?.flashSale ?: emptyList()
            
            if (flashProducts.isEmpty()) {
                Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                    Text("Hiện không có chương trình Flash Sale nào", color = TechGray)
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
                    items(flashProducts) { product ->
                        // Tái sử dụng FlashSaleItem nhưng điều chỉnh kích thước cho phù hợp lưới
                        Box(modifier = Modifier.fillMaxWidth()) {
                            FlashSaleItem(
                                product = product,
                                onClick = { onProductClick(product.id) }
                            )
                        }
                    }
                }
            }
        }
    }
}
