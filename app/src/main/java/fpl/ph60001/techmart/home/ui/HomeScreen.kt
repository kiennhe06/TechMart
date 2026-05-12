package fpl.ph60001.techmart.home.ui

import androidx.compose.animation.core.*
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import fpl.ph60001.techmart.ui.theme.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun HomeScreen() {
    Scaffold(
        topBar = { HomeTopBar() },
        containerColor = TechDark
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentPadding = PaddingValues(bottom = 24.dp)
        ) {
            // 1. Auto-sliding Banners
            item { AutoSlidingBanner() }

            // 2. Category Section
            item { CategorySection() }

            // 3. Flash Sale Section
            item { FlashSaleSection() }

            // 4. Featured Section Header
            item {
                SectionHeader(title = "Sản phẩm mới nhất", onSeeAllClick = {})
            }

            // Mock Product Grid Placeholder
            item {
                Spacer(modifier = Modifier.height(200.dp)) // Placeholder for product items
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeTopBar() {
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
            IconButton(onClick = {}) {
                Icon(Icons.Default.Notifications, contentDescription = null, tint = WhitePure)
            }
            IconButton(onClick = {}) {
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
fun AutoSlidingBanner() {
    val banners = listOf(
        "Sale tựu trường - Giảm giá Laptop tới 50%",
        "iPhone 16 Pro Max - Sẵn hàng giá hời",
        "Phụ kiện Gaming - Mua 1 tặng 1"
    )
    val pagerState = rememberPagerState(pageCount = { banners.size })

    // Auto-scroll logic
    LaunchedEffect(key1 = pagerState.currentPage) {
        launch {
            delay(3000)
            val nextPage = (pagerState.currentPage + 1) % banners.size
            pagerState.animateScrollToPage(nextPage)
        }
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(180.dp)
            .padding(16.dp)
            .clip(RoundedCornerShape(20.dp))
            .background(
                brush = Brush.linearGradient(
                    colors = listOf(BluePrimary, BlueSecondary)
                )
            ),
        contentAlignment = Alignment.Center
    ) {
        HorizontalPager(
            state = pagerState,
            modifier = Modifier.fillMaxSize()
        ) { page ->
            Column(
                modifier = Modifier.fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = banners[page],
                    style = MaterialTheme.typography.headlineSmall.copy(
                        color = WhitePure,
                        fontWeight = FontWeight.Bold
                    ),
                    modifier = Modifier.padding(horizontal = 32.dp),
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                )
            }
        }

        // Pager Indicators
        Row(
            Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 12.dp)
        ) {
            repeat(banners.size) { iteration ->
                val color = if (pagerState.currentPage == iteration) WhitePure else WhitePure.copy(alpha = 0.5f)
                Box(
                    modifier = Modifier
                        .padding(2.dp)
                        .clip(CircleShape)
                        .background(color)
                        .size(if (pagerState.currentPage == iteration) 12.dp else 8.dp)
                )
            }
        }
    }
}

data class Category(val name: String, val icon: ImageVector)

@Composable
fun CategorySection() {
    val categories = listOf(
        Category("Điện thoại", Icons.Default.Smartphone),
        Category("Laptop", Icons.Default.Laptop),
        Category("Tablet", Icons.Default.TabletAndroid),
        Category("Âm thanh", Icons.Default.Headphones),
        Category("Phụ kiện", Icons.Default.SettingsInputHdmi)
    )

    Column(modifier = Modifier.padding(vertical = 16.dp)) {
        SectionHeader(title = "Danh mục", onSeeAllClick = {})
        LazyRow(
            contentPadding = PaddingValues(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            items(categories) { category ->
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.clickable { }
                ) {
                    Box(
                        modifier = Modifier
                            .size(64.dp)
                            .clip(RoundedCornerShape(16.dp))
                            .background(TechSlate),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = category.icon,
                            contentDescription = null,
                            tint = BluePrimary,
                            modifier = Modifier.size(32.dp)
                        )
                    }
                    Text(
                        text = category.name,
                        style = MaterialTheme.typography.bodySmall.copy(color = WhitePure),
                        modifier = Modifier.padding(top = 8.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun FlashSaleSection() {
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
        
        // Mock Flash Sale Products
        LazyRow(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            items(3) {
                FlashSaleItem()
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
fun FlashSaleItem() {
    Column(
        modifier = Modifier
            .width(140.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(TechDark)
            .padding(8.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(100.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(TechSlate),
            contentAlignment = Alignment.Center
        ) {
            Icon(Icons.Default.Devices, contentDescription = null, tint = TechGray, modifier = Modifier.size(48.dp))
        }
        Text(
            text = "Laptop Gaming Neo",
            style = MaterialTheme.typography.bodyMedium.copy(color = WhitePure),
            modifier = Modifier.padding(top = 8.dp),
            maxLines = 1
        )
        Text(
            text = "19.990.000đ",
            style = MaterialTheme.typography.bodySmall.copy(
                color = Color(0xFFFF4D4D),
                fontWeight = FontWeight.Bold
            )
        )
        // Progress bar for stock
        LinearProgressIndicator(
            progress = 0.7f,
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 8.dp)
                .height(6.dp)
                .clip(CircleShape),
            color = Color(0xFFFF4D4D),
            trackColor = TechSlate
        )
        Text(
            text = "Đã bán 70%",
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
