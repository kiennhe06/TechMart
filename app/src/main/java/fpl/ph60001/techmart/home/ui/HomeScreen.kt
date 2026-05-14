package fpl.ph60001.techmart.home.ui

import androidx.compose.animation.core.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import fpl.ph60001.techmart.R
import fpl.ph60001.techmart.home.viewmodel.HomeViewModel
import fpl.ph60001.techmart.network.BannerDto
import fpl.ph60001.techmart.network.CategoryDto
import fpl.ph60001.techmart.network.ProductDto
import fpl.ph60001.techmart.network.SimpleProductDto
import fpl.ph60001.techmart.ui.theme.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import coil.compose.AsyncImage
import fpl.ph60001.techmart.cart.viewmodel.CartViewModel

@Composable
fun HomeScreen(
    onProductClick: (String) -> Unit = {},
    onCategoryClick: (String) -> Unit = {},
    onNotificationClick: () -> Unit = {},
    onCartClick: () -> Unit = {},
    homeViewModel: HomeViewModel = viewModel(),
    cartViewModel: CartViewModel = viewModel()
) {
    val homeData by homeViewModel.homeData.collectAsState()
    val isLoading by homeViewModel.isLoading.collectAsState()
    val selectedCategoryId by homeViewModel.selectedCategoryId.collectAsState()

    Scaffold(
        topBar = { 
            HomeTopBar(
                onNotificationClick = onNotificationClick,
                onCartClick = onCartClick
            ) 
        },
        containerColor = TechDark
    ) { paddingValues ->
        if (isLoading) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = BluePrimary)
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentPadding = PaddingValues(bottom = 24.dp)
            ) {
                // 1. Auto-sliding Banners
                homeData?.banners?.let { banners ->
                    item { AutoSlidingBanner(banners) }
                }

                // 2. Category Section
                homeData?.categories?.let { categories ->
                    item { 
                        CategorySection(
                            categories = categories, 
                            selectedCategoryId = selectedCategoryId,
                            onCategoryClick = { category ->
                                homeViewModel.selectCategory(category.id)
                                onCategoryClick(category.name)
                            }
                        ) 
                    }
                }

                // 3. Flash Sale Section
                homeData?.flashSale?.let { products ->
                    item { FlashSaleSection(products, onProductClick) }
                }

                // 4. Featured Section Header
                item {
                    SectionHeader(title = "Sản phẩm mới nhất", onSeeAllClick = { /* Điều hướng tới DS sản phẩm */ })
                }

                // 5. Product Grid
                homeData?.allProducts?.let { products ->
                    item {
                        val filteredProducts = if (selectedCategoryId != null) {
                            products.filter { it.categoryId == selectedCategoryId }
                        } else {
                            products
                        }
                        
                        if (filteredProducts.isEmpty()) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(32.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    "Không có sản phẩm nào trong danh mục này",
                                    style = MaterialTheme.typography.bodyMedium.copy(color = TechGray)
                                )
                            }
                        } else {
                            ProductGrid(filteredProducts, onProductClick)
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeTopBar(
    onNotificationClick: () -> Unit,
    onCartClick: () -> Unit
) {
    TopAppBar(
        title = {
            Column {
                Text(
                    "TechMart",
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontWeight = FontWeight.Bold,
                        color = BluePrimary,
                        letterSpacing = 1.sp
                    )
                )
                Text(
                    "Khám phá công nghệ mới",
                    style = MaterialTheme.typography.bodySmall.copy(color = TechGray)
                )
            }
        },
        actions = {
            IconButton(onClick = onNotificationClick) {
                Icon(Icons.Default.Notifications, contentDescription = null, tint = WhitePure)
            }
            IconButton(onClick = onCartClick) {
                Icon(Icons.Default.ShoppingCart, contentDescription = null, tint = WhitePure)
            }
        },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = TechDark,
            titleContentColor = WhitePure
        )
    )
}

@Composable
fun AutoSlidingBanner(banners: List<BannerDto>) {
    val pagerState = rememberPagerState(pageCount = { banners.size })

    LaunchedEffect(key1 = pagerState.currentPage) {
        launch {
            delay(4000)
            val nextPage = (pagerState.currentPage + 1) % banners.size
            pagerState.animateScrollToPage(nextPage)
        }
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(200.dp)
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .clip(RoundedCornerShape(20.dp))
            .background(TechSlate),
        contentAlignment = Alignment.Center
    ) {
        HorizontalPager(
            state = pagerState,
            modifier = Modifier.fillMaxSize()
        ) { page ->
            AsyncImage(
                model = banners[page].imageUrl,
                contentDescription = null,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )
        }

        // Pager Indicators
        Row(
            Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 12.dp)
                .background(Color.Black.copy(alpha = 0.3f), CircleShape)
                .padding(horizontal = 8.dp, vertical = 4.dp)
        ) {
            repeat(banners.size) { iteration ->
                val color = if (pagerState.currentPage == iteration) BluePrimary else WhitePure.copy(alpha = 0.5f)
                Box(
                    modifier = Modifier
                        .padding(2.dp)
                        .clip(CircleShape)
                        .background(color)
                        .size(if (pagerState.currentPage == iteration) 10.dp else 6.dp)
                )
            }
        }
    }
}

@Composable
fun CategorySection(
    categories: List<CategoryDto>, 
    selectedCategoryId: Int?,
    onCategoryClick: (CategoryDto) -> Unit
) {
    val iconMap = mapOf(
        "Smartphone" to Icons.Default.Smartphone,
        "Laptop" to Icons.Default.Laptop,
        "TabletAndroid" to Icons.Default.TabletAndroid,
        "Headphones" to Icons.Default.Headphones,
        "SettingsInputHdmi" to Icons.Default.SettingsInputHdmi
    )

    Column(modifier = Modifier.padding(vertical = 16.dp)) {
        SectionHeader(title = "Danh mục", onSeeAllClick = {})
        LazyRow(
            contentPadding = PaddingValues(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            items(categories) { category ->
                val isSelected = selectedCategoryId == category.id
                val bgColor = if (isSelected) BluePrimary else TechSlate
                val iconColor = if (isSelected) WhitePure else BluePrimary
                val textColor = if (isSelected) BluePrimary else WhitePure

                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.clickable { onCategoryClick(category) }
                ) {
                    Box(
                        modifier = Modifier
                            .size(64.dp)
                            .clip(RoundedCornerShape(16.dp))
                            .background(bgColor),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = iconMap[category.icon] ?: Icons.Default.Devices,
                            contentDescription = null,
                            tint = iconColor,
                            modifier = Modifier.size(32.dp)
                        )
                    }
                    Text(
                        text = category.name,
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = textColor,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                        ),
                        modifier = Modifier.padding(top = 8.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun FlashSaleSection(products: List<ProductDto>, onProductClick: (String) -> Unit) {
    var timeLeft by remember { mutableStateOf(36000) } // 10 hours in seconds

    LaunchedEffect(key1 = true) {
        while (timeLeft > 0) {
            delay(1000)
            timeLeft--
        }
    }

    val hours = timeLeft / 3600
    val minutes = (timeLeft % 3600) / 60
    val seconds = timeLeft % 60

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .clip(RoundedCornerShape(20.dp))
            .background(TechSlate.copy(alpha = 0.5f))
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = "FLASH SALE",
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontWeight = FontWeight.Black,
                        color = Color(0xFFFF4D4D),
                        letterSpacing = 1.sp
                    )
                )
                Spacer(modifier = Modifier.width(12.dp))
                CountdownBox(time = String.format("%02d", hours))
                Text(":", color = WhitePure, modifier = Modifier.padding(horizontal = 4.dp))
                CountdownBox(time = String.format("%02d", minutes))
                Text(":", color = WhitePure, modifier = Modifier.padding(horizontal = 4.dp))
                CountdownBox(time = String.format("%02d", seconds))
            }
            Text(
                text = "Xem tất cả >",
                style = MaterialTheme.typography.bodySmall.copy(color = BluePrimary),
                modifier = Modifier.clickable { }
            )
        }

        Spacer(modifier = Modifier.height(16.dp))
        
        LazyRow(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            items(products) { product ->
                FlashSaleItem(
                    product = product,
                    onClick = { onProductClick(product.id) }
                )
            }
        }
    }
}

@Composable
fun CountdownBox(time: String) {
    Box(
        modifier = Modifier
            .size(30.dp)
            .clip(RoundedCornerShape(4.dp))
            .background(Color(0xFFFF4D4D)),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = time,
            style = MaterialTheme.typography.bodyMedium.copy(
                fontWeight = FontWeight.Bold,
                color = WhitePure
            )
        )
    }
}

@Composable
fun FlashSaleItem(product: ProductDto, onClick: () -> Unit) {
    Column(
        modifier = Modifier
            .width(140.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(TechDark)
            .clickable(onClick = onClick)
            .padding(8.dp)
    ) {
        AsyncImage(
            model = product.image,
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .fillMaxWidth()
                .height(100.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(TechSlate)
        )
        Text(
            text = product.name,
            style = MaterialTheme.typography.bodyMedium.copy(color = WhitePure),
            modifier = Modifier.padding(top = 8.dp),
            maxLines = 1
        )
        Text(
            text = product.price,
            style = MaterialTheme.typography.bodySmall.copy(
                color = Color(0xFFFF4D4D),
                fontWeight = FontWeight.Bold
            )
        )
        LinearProgressIndicator(
            progress = { product.soldProgress },
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 8.dp)
                .height(6.dp)
                .clip(CircleShape),
            color = Color(0xFFFF4D4D),
            trackColor = TechSlate
        )
        Text(
            text = "Đã bán ${(product.soldProgress * 100).toInt()}%",
            style = MaterialTheme.typography.labelSmall.copy(color = TechGray, fontSize = 10.sp),
            modifier = Modifier.padding(top = 4.dp)
        )
    }
}

@Composable
fun SectionHeader(title: String, onSeeAllClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium.copy(
                fontWeight = FontWeight.Bold,
                color = WhitePure
            )
        )
        TextButton(onClick = onSeeAllClick) {
            Text(
                text = "Xem tất cả",
                style = MaterialTheme.typography.bodySmall.copy(color = BluePrimary)
            )
        }
    }
}

@Composable
fun ProductGrid(products: List<SimpleProductDto>, onProductClick: (String) -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        val chunkedProducts = products.chunked(2)
        for (row in chunkedProducts) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                for (product in row) {
                    Box(modifier = Modifier.weight(1f)) {
                        SimpleProductItem(product = product, onClick = { onProductClick(product.id) })
                    }
                }
                // Handle odd number of items
                if (row.size == 1) {
                    Spacer(modifier = Modifier.weight(1f))
                }
            }
        }
    }
}

@Composable
fun SimpleProductItem(product: SimpleProductDto, onClick: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(TechDark)
            .clickable(onClick = onClick)
            .padding(8.dp)
    ) {
        AsyncImage(
            model = product.image,
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .fillMaxWidth()
                .height(140.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(TechSlate)
        )
        Text(
            text = product.name,
            style = MaterialTheme.typography.bodyMedium.copy(color = WhitePure),
            modifier = Modifier.padding(top = 8.dp),
            maxLines = 2
        )
        Text(
            text = product.price,
            style = MaterialTheme.typography.bodySmall.copy(
                color = BluePrimary,
                fontWeight = FontWeight.Bold
            ),
            modifier = Modifier.padding(top = 4.dp)
        )
    }
}
