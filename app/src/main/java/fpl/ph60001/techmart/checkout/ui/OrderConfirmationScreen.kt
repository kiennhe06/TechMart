package fpl.ph60001.techmart.checkout.ui

import androidx.compose.animation.core.*
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import fpl.ph60001.techmart.ui.theme.*
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OrderConfirmationScreen(
    orderTotal: String,
    shippingAddress: String,
    paymentMethod: String,
    itemCount: Int,
    onBackToHome: () -> Unit
) {
    // Mã đơn hàng ngẫu nhiên
    val orderCode = remember {
        "TM" + SimpleDateFormat("yyMMdd", Locale.getDefault()).format(Date()) +
                (100000..999999).random()
    }
    val orderDate = remember {
        SimpleDateFormat("HH:mm - dd/MM/yyyy", Locale("vi")).format(Date())
    }
    // Ngày giao dự kiến (3-5 ngày)
    val deliveryDate = remember {
        val cal = Calendar.getInstance()
        cal.add(Calendar.DAY_OF_MONTH, (3..5).random())
        SimpleDateFormat("dd/MM/yyyy", Locale("vi")).format(cal.time)
    }

    // Animation cho icon check
    val scale = remember { Animatable(0f) }
    LaunchedEffect(Unit) {
        scale.animateTo(1f, animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessLow))
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Xác nhận đơn hàng", color = WhitePure, fontWeight = FontWeight.Bold) },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = TechDark)
            )
        },
        bottomBar = {
            Surface(Modifier.fillMaxWidth(), color = TechDark, border = androidx.compose.foundation.BorderStroke(1.dp, TechSlate)) {
                Button(
                    onClick = onBackToHome,
                    modifier = Modifier.fillMaxWidth().padding(16.dp).navigationBarsPadding(),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = BluePrimary)
                ) {
                    Icon(Icons.Default.Home, null)
                    Spacer(Modifier.width(8.dp))
                    Text("Về trang chủ", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                }
            }
        },
        containerColor = TechDark
    ) { padding ->
        Column(
            modifier = Modifier.fillMaxSize().padding(padding)
                .verticalScroll(rememberScrollState()).padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Spacer(Modifier.height(8.dp))

            // === ICON THÀNH CÔNG ===
            Box(
                modifier = Modifier.size(96.dp).scale(scale.value)
                    .clip(CircleShape)
                    .background(Brush.linearGradient(listOf(BluePrimary, CyberCyan))),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Default.CheckCircle, null, tint = WhitePure, modifier = Modifier.size(56.dp))
            }

            Text(
                "Đặt hàng thành công!",
                color = WhitePure, fontWeight = FontWeight.Bold, fontSize = 24.sp
            )
            Text(
                "Cảm ơn bạn đã mua hàng tại TechMart",
                color = TechGray, fontSize = 14.sp, textAlign = TextAlign.Center
            )

            Spacer(Modifier.height(8.dp))

            // === THÔNG TIN ĐƠN HÀNG ===
            Card(
                Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = TechSlate),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Receipt, null, tint = BluePrimary, modifier = Modifier.size(22.dp))
                        Spacer(Modifier.width(8.dp))
                        Text("Thông tin đơn hàng", color = WhitePure, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    }
                    HorizontalDivider(color = TechDark)
                    InfoRow("Mã đơn hàng", orderCode)
                    InfoRow("Thời gian đặt", orderDate)
                    InfoRow("Số lượng sản phẩm", "$itemCount sản phẩm")
                    InfoRow("Tổng thanh toán", orderTotal, valueColor = BluePrimary)
                }
            }

            // === ĐỊA CHỈ GIAO HÀNG ===
            Card(
                Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = TechSlate),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.LocalShipping, null, tint = BluePrimary, modifier = Modifier.size(22.dp))
                        Spacer(Modifier.width(8.dp))
                        Text("Giao hàng", color = WhitePure, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    }
                    HorizontalDivider(color = TechDark)
                    InfoRow("Địa chỉ", shippingAddress)
                    InfoRow("Phương thức", paymentMethod)
                    InfoRow("Dự kiến giao", deliveryDate, valueColor = CyberCyan)
                }
            }

            // === TRẠNG THÁI ĐƠN HÀNG ===
            Card(
                Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = TechSlate),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(0.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Timeline, null, tint = BluePrimary, modifier = Modifier.size(22.dp))
                        Spacer(Modifier.width(8.dp))
                        Text("Trạng thái đơn hàng", color = WhitePure, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    }
                    Spacer(Modifier.height(16.dp))
                    StatusStep(icon = Icons.Default.CheckCircle, title = "Đã đặt hàng", subtitle = orderDate, isActive = true, isLast = false)
                    StatusStep(icon = Icons.Default.Inventory, title = "Đang xử lý", subtitle = "Chờ xác nhận từ cửa hàng", isActive = false, isLast = false)
                    StatusStep(icon = Icons.Default.LocalShipping, title = "Đang giao hàng", subtitle = "Dự kiến $deliveryDate", isActive = false, isLast = false)
                    StatusStep(icon = Icons.Default.Done, title = "Đã giao hàng", subtitle = "", isActive = false, isLast = true)
                }
            }

            Spacer(Modifier.height(16.dp))
        }
    }
}

@Composable
private fun InfoRow(label: String, value: String, valueColor: Color = WhitePure) {
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
        Text(label, color = TechGray, fontSize = 14.sp, modifier = Modifier.weight(1f))
        Text(value, color = valueColor, fontWeight = FontWeight.SemiBold, fontSize = 14.sp,
            textAlign = TextAlign.End, modifier = Modifier.weight(1.2f))
    }
}

@Composable
private fun StatusStep(icon: ImageVector, title: String, subtitle: String, isActive: Boolean, isLast: Boolean) {
    Row(modifier = Modifier.fillMaxWidth()) {
        // Icon + line
        Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.width(40.dp)) {
            Box(
                modifier = Modifier.size(32.dp)
                    .clip(CircleShape)
                    .background(if (isActive) BluePrimary else TechDark),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, null, tint = if (isActive) WhitePure else TechGray, modifier = Modifier.size(18.dp))
            }
            if (!isLast) {
                Box(
                    Modifier.width(2.dp).height(32.dp)
                        .background(if (isActive) BluePrimary else TechDark)
                )
            }
        }
        Spacer(Modifier.width(12.dp))
        // Text
        Column(modifier = Modifier.padding(top = 4.dp)) {
            Text(title, color = if (isActive) WhitePure else TechGray, fontWeight = if (isActive) FontWeight.Bold else FontWeight.Normal, fontSize = 14.sp)
            if (subtitle.isNotEmpty()) {
                Text(subtitle, color = TechGray, fontSize = 12.sp)
            }
        }
    }
}
