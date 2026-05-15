package fpl.ph60001.techmart.order.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import fpl.ph60001.techmart.order.Order
import fpl.ph60001.techmart.order.OrderRepository
import fpl.ph60001.techmart.ui.theme.*
import fpl.ph60001.techmart.network.RetrofitClient
import fpl.ph60001.techmart.utils.PreferenceManager
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OrderHistoryScreen(onBackClick: () -> Unit, onOrderClick: (String) -> Unit) {
    val context = LocalContext.current
    val prefManager = remember { PreferenceManager(context) }
    val userPhone = remember { prefManager.getShippingPhone() }
    
    var orders by remember { mutableStateOf<List<Order>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }

    LaunchedEffect(Unit) {
        val orderRepo = OrderRepository(context)
        val localOrders = orderRepo.getOrders()
        
        try {
            // 1. Lấy toàn bộ đơn hàng từ server
            val serverOrders = RetrofitClient.apiService.getOrders()
            
            // 2. Gộp dữ liệu: Nếu đơn hàng có trên server, lấy trạng thái từ server. 
            // Nếu chỉ có ở local, vẫn giữ lại để hiển thị.
            val mergedOrders = mutableListOf<Order>()
            val serverOrderMap = serverOrders.associateBy { it.id }
            
            // Ưu tiên các đơn hàng từ server có cùng số điện thoại
            serverOrders.filter { it.customerPhone == userPhone }.forEach { 
                mergedOrders.add(it)
            }
            
            // Thêm các đơn hàng local mà chưa có trong danh sách server (hoặc khác ID)
            localOrders.forEach { local ->
                val serverVersion = serverOrderMap[local.id]
                if (serverVersion != null) {
                    // Nếu trùng ID, dùng bản server để có status mới nhất
                    if (mergedOrders.none { it.id == local.id }) {
                        mergedOrders.add(serverVersion)
                    }
                } else {
                    // Nếu local có mà server không có (chưa sync xong), vẫn hiện local
                    if (mergedOrders.none { it.id == local.id }) {
                        mergedOrders.add(local)
                    }
                }
            }
            
            // Sắp xếp theo ngày mới nhất
            orders = mergedOrders.sortedByDescending { it.orderDate }
            
        } catch (e: Exception) {
            // Nếu lỗi mạng, hiển thị dữ liệu local và thông báo
            orders = localOrders.sortedByDescending { it.orderDate }
            android.widget.Toast.makeText(context, "Không thể đồng bộ với máy chủ. Đang hiển thị offline.", android.widget.Toast.LENGTH_SHORT).show()
        } finally {
            isLoading = false
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Đơn hàng của tôi", color = WhitePure, fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.Default.ArrowBack, null, tint = WhitePure)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = TechDark)
            )
        },
        containerColor = TechDark
    ) { padding ->
        if (isLoading) {
            Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = BluePrimary)
            }
        } else if (orders.isEmpty()) {
            // Trạng thái trống
            Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Box(
                        Modifier.size(80.dp).clip(CircleShape)
                            .background(Brush.linearGradient(listOf(BluePrimary.copy(0.2f), CyberCyan.copy(0.2f)))),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.ShoppingCart, null, tint = TechGray, modifier = Modifier.size(40.dp))
                    }
                    Spacer(Modifier.height(16.dp))
                    Text("Chưa có đơn hàng nào", color = WhitePure, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                    Spacer(Modifier.height(4.dp))
                    Text("Hãy mua sắm để xem lịch sử đơn hàng", color = TechGray, fontSize = 14.sp)
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize().padding(padding),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(orders) { order ->
                    OrderCard(order, onClick = { onOrderClick(order.id) })
                }
            }
        }
    }
}

@Composable
private fun OrderCard(order: Order, onClick: () -> Unit) {
    val dateStr = remember(order.orderDate) {
        SimpleDateFormat("HH:mm - dd/MM/yyyy", Locale.getDefault()).format(Date(order.orderDate))
    }
    val statusInfo = remember(order.status) {
        when (order.status) {
            0 -> Triple("Đã đặt hàng", Icons.Default.CheckCircle, BluePrimary)
            1 -> Triple("Đang xử lý", Icons.Default.Inventory, CyberCyan)
            2 -> Triple("Đang giao hàng", Icons.Default.LocalShipping, CyberCyan)
            3 -> Triple("Đã giao hàng", Icons.Default.Done, BluePrimary)
            else -> Triple("Không xác định", Icons.Default.Help, TechGray)
        }
    }

    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = TechSlate),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(Modifier.padding(16.dp)) {
            // Header: Mã đơn + trạng thái
            Row(verticalAlignment = Alignment.CenterVertically) {
                Column(Modifier.weight(1f)) {
                    Text("Đơn hàng #${order.id.takeLast(10)}", color = WhitePure, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                    Text(dateStr, color = TechGray, fontSize = 12.sp)
                }
                Surface(
                    shape = RoundedCornerShape(20.dp),
                    color = statusInfo.third.copy(alpha = 0.15f)
                ) {
                    Row(Modifier.padding(horizontal = 10.dp, vertical = 4.dp), verticalAlignment = Alignment.CenterVertically) {
                        Icon(statusInfo.second, null, tint = statusInfo.third, modifier = Modifier.size(14.dp))
                        Spacer(Modifier.width(4.dp))
                        Text(statusInfo.first, color = statusInfo.third, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                    }
                }
            }

            Spacer(Modifier.height(12.dp))
            HorizontalDivider(color = TechDark)
            Spacer(Modifier.height(12.dp))

            // Danh sách sản phẩm (tối đa 3, còn lại hiện "+N sản phẩm khác")
            val displayItems = order.items.take(3)
            displayItems.forEach { item ->
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(vertical = 4.dp)) {
                    AsyncImage(
                        model = item.image, contentDescription = null, contentScale = ContentScale.Crop,
                        modifier = Modifier.size(44.dp).clip(RoundedCornerShape(8.dp)).background(TechDark)
                    )
                    Spacer(Modifier.width(10.dp))
                    Column(Modifier.weight(1f)) {
                        Text(item.name, color = WhitePure, fontSize = 13.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
                        Text(item.price, color = BluePrimary, fontSize = 12.sp)
                    }
                    Text("x${item.quantity}", color = TechGray, fontSize = 13.sp)
                }
            }
            if (order.items.size > 3) {
                Text(
                    "+${order.items.size - 3} sản phẩm khác",
                    color = TechGray, fontSize = 12.sp,
                    modifier = Modifier.padding(top = 4.dp)
                )
            }

            Spacer(Modifier.height(12.dp))
            HorizontalDivider(color = TechDark)
            Spacer(Modifier.height(8.dp))

            // Footer: Tổng + Địa chỉ
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Column {
                    Text("Tổng thanh toán", color = TechGray, fontSize = 12.sp)
                    Text(order.total, color = BluePrimary, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text(order.paymentMethod, color = TechGray, fontSize = 11.sp, maxLines = 1)
                }
            }
        }
    }
}
