package fpl.ph60001.techmart.order.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import kotlinx.coroutines.launch
import fpl.ph60001.techmart.network.RetrofitClient
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
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
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OrderDetailScreen(orderId: String, onBackClick: () -> Unit) {
    val context = LocalContext.current
    var order by remember { mutableStateOf<Order?>(null) }
    var isLoading by remember { mutableStateOf(true) }
    val scope = rememberCoroutineScope()
    
    var showRatingDialog by remember { mutableStateOf(false) }
    var rating by remember { mutableIntStateOf(5) }
    var comment by remember { mutableStateOf("") }

    LaunchedEffect(orderId) {
        try {
            // Lấy từ server để có status mới nhất
            val serverOrders = RetrofitClient.apiService.getOrders()
            order = serverOrders.find { it.id == orderId }
            if (order == null) {
                // Nếu không thấy trên server, thử tìm ở local (phòng trường hợp đơn mới chưa kịp sync hết)
                val orderRepo = OrderRepository(context)
                order = orderRepo.getOrders().find { it.id == orderId }
            }
        } catch (e: Exception) {
            val orderRepo = OrderRepository(context)
            order = orderRepo.getOrders().find { it.id == orderId }
        } finally {
            isLoading = false
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Chi tiết đơn hàng", color = WhitePure, fontWeight = FontWeight.Bold) },
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
        val currentOrder = order
        if (isLoading) {
            Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = BluePrimary)
            }
        } else if (currentOrder == null) {
            Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                Text("Không tìm thấy đơn hàng", color = TechGray)
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // 1. Trạng thái vận chuyển (Timeline)
                StatusTimeline(currentOrder.status)
 
                // 2. Thông tin nhận hàng
                OrderSection(Icons.Default.LocationOn, "Địa chỉ nhận hàng") {
                    Text(currentOrder.address, color = WhitePure, fontSize = 14.sp, lineHeight = 20.sp)
                }
 
                // 3. Danh sách sản phẩm
                OrderSection(Icons.Default.ShoppingBag, "Sản phẩm") {
                    currentOrder.items.forEach { item ->
                        OrderItemRow(item)
                        if (item != currentOrder.items.last()) {
                            HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp), color = TechDark)
                        }
                    }
                }
 
                // 4. Thanh toán
                OrderSection(Icons.Default.Payment, "Thông tin thanh toán") {
                    PaymentRow("Phương thức thanh toán", currentOrder.paymentMethod)
                    PaymentRow("Mã đơn hàng", "#${currentOrder.id.takeLast(10)}")
                    PaymentRow("Ngày đặt", SimpleDateFormat("HH:mm - dd/MM/yyyy", Locale.getDefault()).format(Date(currentOrder.orderDate)))
                    HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp), color = TechDark)
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Tổng thanh toán", color = WhitePure, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                        Text(currentOrder.total, color = BluePrimary, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                    }
                }

                // 5. Nút hành động (Hủy đơn / Đánh giá)
                if (currentOrder.status == 0) {
                    var showCancelDialog by remember { mutableStateOf(false) }
                    
                    if (showCancelDialog) {
                        AlertDialog(
                            onDismissRequest = { showCancelDialog = false },
                            containerColor = TechSlate,
                            title = { Text("Xác nhận hủy đơn", color = WhitePure) },
                            text = { Text("Bạn có chắc chắn muốn hủy đơn hàng này không?", color = TechGray) },
                            confirmButton = {
                                TextButton(onClick = {
                                    showCancelDialog = false
                                    scope.launch {
                                        try {
                                            val res = RetrofitClient.apiService.cancelOrder(currentOrder.id)
                                            if (res.isSuccessful) {
                                                android.widget.Toast.makeText(context, "Đã hủy đơn hàng thành công", android.widget.Toast.LENGTH_SHORT).show()
                                                // Refresh data
                                                isLoading = true
                                                val serverOrders = RetrofitClient.apiService.getOrders()
                                                order = serverOrders.find { it.id == orderId }
                                                isLoading = false
                                            } else {
                                                android.widget.Toast.makeText(context, "Hủy thất bại: ${res.message()}", android.widget.Toast.LENGTH_SHORT).show()
                                            }
                                        } catch (e: Exception) {
                                            android.widget.Toast.makeText(context, "Lỗi kết nối: ${e.message}", android.widget.Toast.LENGTH_SHORT).show()
                                        }
                                    }
                                }) {
                                    Text("Đồng ý", color = ErrorRed, fontWeight = FontWeight.Bold)
                                }
                            },
                            dismissButton = {
                                TextButton(onClick = { showCancelDialog = false }) {
                                    Text("Quay lại", color = TechGray)
                                }
                            }
                        )
                    }

                    Button(
                        onClick = { showCancelDialog = true },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
                        border = androidx.compose.foundation.BorderStroke(1.dp, ErrorRed),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("Hủy đơn hàng", color = ErrorRed, fontWeight = FontWeight.Bold)
                    }
                } else if (currentOrder.status == 3 && currentOrder.review == null) {
                    Button(
                        onClick = { showRatingDialog = true },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(containerColor = BluePrimary),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(Icons.Default.Star, null)
                        Spacer(Modifier.width(8.dp))
                        Text("Đánh giá sản phẩm", fontWeight = FontWeight.Bold)
                    }
                } else if (currentOrder.review != null) {
                    OrderSection(Icons.Default.Star, "Đánh giá của bạn") {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            repeat(5) { i ->
                                Icon(
                                    Icons.Default.Star, null,
                                    tint = if (i < currentOrder.review.rating) Color(0xFFFFD700) else TechGray,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }
                        Text(currentOrder.review.comment, color = WhitePure, fontSize = 13.sp, modifier = Modifier.padding(top = 4.dp))
                        
                        if (!currentOrder.review.adminReply.isNullOrBlank()) {
                            Box(
                                modifier = Modifier
                                    .padding(top = 12.dp)
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(BluePrimary.copy(alpha = 0.1f))
                                    .padding(8.dp)
                            ) {
                                Column {
                                    Text(
                                        "Phản hồi từ TechMart:",
                                        color = BluePrimary,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 11.sp
                                    )
                                    Text(
                                        currentOrder.review.adminReply,
                                        color = WhitePure,
                                        fontSize = 12.sp,
                                        modifier = Modifier.padding(top = 2.dp)
                                    )
                                }
                            }
                        }
                    }
                }
                
                Spacer(Modifier.height(24.dp))
            }
        }
    }

    // Rating Dialog
    if (showRatingDialog) {
        AlertDialog(
            onDismissRequest = { showRatingDialog = false },
            containerColor = TechSlate,
            title = { Text("Đánh giá đơn hàng", color = WhitePure) },
            text = {
                Column {
                    Text("Bạn cảm thấy sản phẩm thế nào?", color = TechGray, fontSize = 14.sp)
                    Row(
                        Modifier.fillMaxWidth().padding(vertical = 16.dp),
                        horizontalArrangement = Arrangement.Center
                    ) {
                        repeat(5) { i ->
                            IconButton(onClick = { rating = i + 1 }) {
                                Icon(
                                    imageVector = if (i < rating) Icons.Default.Star else Icons.Default.StarBorder,
                                    contentDescription = null,
                                    tint = if (i < rating) Color(0xFFFFD700) else TechGray,
                                    modifier = Modifier.size(32.dp)
                                )
                            }
                        }
                    }
                    OutlinedTextField(
                        value = comment, onValueChange = { comment = it },
                        placeholder = { Text("Nhập cảm nhận của bạn...") },
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = WhitePure, unfocusedTextColor = WhitePure,
                            focusedBorderColor = BluePrimary, unfocusedBorderColor = TechDark
                        )
                    )
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    scope.launch {
                        try {
                            val review = fpl.ph60001.techmart.order.OrderReview(rating, comment)
                            val res = RetrofitClient.apiService.submitReview(orderId, review)
                            if (res.isSuccessful) {
                                android.widget.Toast.makeText(context, "Cảm ơn bạn đã đánh giá!", android.widget.Toast.LENGTH_SHORT).show()
                                showRatingDialog = false
                                // Refresh
                                isLoading = true
                                val serverOrders = RetrofitClient.apiService.getOrders()
                                order = serverOrders.find { it.id == orderId }
                                isLoading = false
                            } else {
                                android.widget.Toast.makeText(context, "Lỗi: ${res.message()}", android.widget.Toast.LENGTH_SHORT).show()
                            }
                        } catch (e: Exception) {
                            android.widget.Toast.makeText(context, "Lỗi kết nối: ${e.message}", android.widget.Toast.LENGTH_SHORT).show()
                        }
                    }
                }) {
                    Text("Gửi đánh giá", color = BluePrimary, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showRatingDialog = false }) {
                    Text("Hủy", color = TechGray)
                }
            }
        )
    }
}

@Composable
private fun StatusTimeline(currentStatus: Int) {
    val steps = listOf(
        "Chờ xác nhận" to Icons.Default.Inventory,
        "Đang xử lý" to Icons.Default.Settings,
        "Đang giao" to Icons.Default.LocalShipping,
        "Hoàn tất" to Icons.Default.CheckCircle
    )

    val finalStatus = if (currentStatus == -1) "Đã hủy" else steps.getOrNull(currentStatus)?.first ?: "Không xác định"
    val isCancelled = currentStatus == -1

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = TechSlate),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(Modifier.padding(16.dp)) {
            Text("Trạng thái đơn hàng", color = WhitePure, fontWeight = FontWeight.Bold, fontSize = 15.sp)
            Spacer(Modifier.height(20.dp))
            
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                steps.forEachIndexed { index, step ->
                    val isActive = index <= currentStatus
                    val color = if (isActive) BluePrimary else TechGray
                    
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.weight(1f)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            // Line connecting circles
                            if (index < steps.size - 1) {
                                Box(
                                    Modifier
                                        .offset(x = 40.dp) // Adjust based on item width
                                        .height(2.dp)
                                        .fillMaxWidth()
                                        .background(if (index < currentStatus) BluePrimary else TechDark)
                                )
                            }
                            
                            Box(
                                Modifier
                                    .size(32.dp)
                                    .clip(CircleShape)
                                    .background(if (isCancelled) ErrorRed.copy(0.2f) else if (isActive) BluePrimary.copy(0.2f) else TechDark),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    if (isCancelled && index == 0) Icons.Default.Cancel else step.second, 
                                    null, 
                                    tint = if (isCancelled) ErrorRed else color, 
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }
                        Spacer(Modifier.height(8.dp))
                        Text(
                            if (isCancelled && index == 0) "Đã hủy" else step.first, 
                            color = if (isCancelled && index == 0) ErrorRed else color, 
                            fontSize = 10.sp, 
                            fontWeight = if (isActive || (isCancelled && index == 0)) FontWeight.Bold else FontWeight.Normal
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun OrderSection(icon: ImageVector, title: String, content: @Composable () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = TechSlate),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(icon, null, tint = BluePrimary, modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(8.dp))
                Text(title, color = WhitePure, fontWeight = FontWeight.Bold, fontSize = 15.sp)
            }
            Spacer(Modifier.height(12.dp))
            content()
        }
    }
}

@Composable
private fun OrderItemRow(item: fpl.ph60001.techmart.order.OrderItem) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        AsyncImage(
            model = item.image, contentDescription = null, contentScale = ContentScale.Crop,
            modifier = Modifier.size(60.dp).clip(RoundedCornerShape(12.dp)).background(TechDark)
        )
        Spacer(Modifier.width(12.dp))
        Column(Modifier.weight(1f)) {
            Text(item.name, color = WhitePure, fontSize = 14.sp, maxLines = 2, overflow = TextOverflow.Ellipsis)
            Text(item.price, color = BluePrimary, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
        }
        Text("x${item.quantity}", color = TechGray, fontSize = 14.sp)
    }
}

@Composable
private fun PaymentRow(label: String, value: String) {
    Row(Modifier.fillMaxWidth().padding(vertical = 4.dp), horizontalArrangement = Arrangement.SpaceBetween) {
        Text(label, color = TechGray, fontSize = 13.sp)
        Text(value, color = WhitePure, fontSize = 13.sp)
    }
}
