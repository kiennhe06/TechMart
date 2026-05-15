package fpl.ph60001.techmart.checkout.ui

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import fpl.ph60001.techmart.cart.viewmodel.CartItem
import fpl.ph60001.techmart.cart.viewmodel.CartViewModel
import fpl.ph60001.techmart.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CheckoutScreen(
    onBackClick: () -> Unit,
    onOrderSuccess: () -> Unit,
    cartViewModel: CartViewModel
) {
    val cartItems by cartViewModel.checkoutItems.collectAsState()

    var selectedPayment by remember { mutableIntStateOf(0) }
    var showSuccessDialog by remember { mutableStateOf(false) }

    // Shipping address fields
    var fullName by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var address by remember { mutableStateOf("") }
    // Calculate totals
    val subtotal = cartItems.sumOf {
        val priceNum = it.price.replace("[^\\d]".toRegex(), "").toLongOrNull() ?: 0L
        priceNum * it.quantity
    }
    val shippingFee = if (subtotal > 500000) 0L else 30000L
    val total = subtotal + shippingFee
    val formattedSubtotal = String.format("%,dđ", subtotal).replace(",", ".")
    val formattedShipping = if (shippingFee == 0L) "Miễn phí" else String.format("%,dđ", shippingFee).replace(",", ".")
    val formattedTotal = String.format("%,dđ", total).replace(",", ".")

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Thanh toán", color = WhitePure, fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.Default.ArrowBack, contentDescription = null, tint = WhitePure)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = TechDark)
            )
        },
        bottomBar = {
            CheckoutBottomBar(
                total = formattedTotal,
                enabled = fullName.isNotBlank() && phone.isNotBlank() && address.isNotBlank() && cartItems.isNotEmpty(),
                onPlaceOrder = { showSuccessDialog = true }
            )
        },
        containerColor = TechDark
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // 1. Shipping Address Section
            CheckoutSectionCard(
                icon = Icons.Default.LocationOn,
                title = "Địa chỉ nhận hàng"
            ) {
                OutlinedTextField(
                    value = fullName,
                    onValueChange = { fullName = it },
                    label = { Text("Họ và tên") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    colors = checkoutTextFieldColors(),
                    shape = RoundedCornerShape(12.dp)
                )
                Spacer(Modifier.height(8.dp))
                OutlinedTextField(
                    value = phone,
                    onValueChange = { phone = it },
                    label = { Text("Số điện thoại") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    colors = checkoutTextFieldColors(),
                    shape = RoundedCornerShape(12.dp)
                )
                Spacer(Modifier.height(8.dp))
                OutlinedTextField(
                    value = address,
                    onValueChange = { address = it },
                    label = { Text("Địa chỉ chi tiết") },
                    minLines = 2,
                    modifier = Modifier.fillMaxWidth(),
                    colors = checkoutTextFieldColors(),
                    shape = RoundedCornerShape(12.dp)
                )
            }

            // 2. Order Items Section
            CheckoutSectionCard(
                icon = Icons.Default.ShoppingBag,
                title = "Sản phẩm đặt mua (${cartItems.size})"
            ) {
                cartItems.forEachIndexed { index, item ->
                    CheckoutItemRow(item)
                    if (index < cartItems.size - 1) {
                        HorizontalDivider(
                            modifier = Modifier.padding(vertical = 8.dp),
                            color = TechDark.copy(alpha = 0.5f)
                        )
                    }
                }
            }

            // 3. Payment Method Section
            CheckoutSectionCard(
                icon = Icons.Default.Payment,
                title = "Phương thức thanh toán"
            ) {
                PaymentMethodOption(
                    icon = Icons.Default.Money,
                    label = "Thanh toán khi nhận hàng (COD)",
                    selected = selectedPayment == 0,
                    onClick = { selectedPayment = 0 }
                )
                Spacer(Modifier.height(8.dp))
                PaymentMethodOption(
                    icon = Icons.Default.AccountBalance,
                    label = "Chuyển khoản ngân hàng",
                    selected = selectedPayment == 1,
                    onClick = { selectedPayment = 1 }
                )
                Spacer(Modifier.height(8.dp))
                PaymentMethodOption(
                    icon = Icons.Default.CreditCard,
                    label = "Thẻ tín dụng / Ghi nợ",
                    selected = selectedPayment == 2,
                    onClick = { selectedPayment = 2 }
                )
            }

            // 4. Order Summary Section
            CheckoutSectionCard(
                icon = Icons.Default.Receipt,
                title = "Chi tiết thanh toán"
            ) {
                SummaryRow("Tạm tính", formattedSubtotal)
                Spacer(Modifier.height(4.dp))
                SummaryRow("Phí vận chuyển", formattedShipping)
                if (shippingFee == 0L) {
                    Text(
                        "Miễn phí vận chuyển cho đơn trên 500.000đ",
                        color = CyberCyan,
                        style = MaterialTheme.typography.labelSmall,
                        modifier = Modifier.padding(start = 4.dp, top = 2.dp)
                    )
                }
                HorizontalDivider(
                    modifier = Modifier.padding(vertical = 8.dp),
                    color = TechDark.copy(alpha = 0.5f)
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        "Tổng thanh toán",
                        color = WhitePure,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp
                    )
                    Text(
                        formattedTotal,
                        color = BluePrimary,
                        fontWeight = FontWeight.Black,
                        fontSize = 20.sp
                    )
                }
            }

            Spacer(Modifier.height(8.dp))
        }
    }

    // Success Dialog
    if (showSuccessDialog) {
        AlertDialog(
            onDismissRequest = {},
            containerColor = TechSlate,
            icon = {
                Box(
                    modifier = Modifier
                        .size(64.dp)
                        .clip(CircleShape)
                        .background(
                            Brush.linearGradient(listOf(BluePrimary, CyberCyan))
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Default.CheckCircle,
                        contentDescription = null,
                        tint = WhitePure,
                        modifier = Modifier.size(40.dp)
                    )
                }
            },
            title = {
                Text(
                    "Đặt hàng thành công!",
                    color = WhitePure,
                    fontWeight = FontWeight.Bold,
                    fontSize = 20.sp
                )
            },
            text = {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        "Cảm ơn bạn đã mua hàng tại TechMart",
                        color = TechGray,
                        fontSize = 14.sp
                    )
                    Spacer(Modifier.height(4.dp))
                    Text(
                        "Đơn hàng của bạn sẽ được xử lý trong thời gian sớm nhất.",
                        color = TechGray,
                        fontSize = 14.sp
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        cartViewModel.clearCart()
                        onOrderSuccess()
                    },
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = BluePrimary),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Về trang chủ", fontWeight = FontWeight.Bold)
                }
            }
        )
    }
}

@Composable
private fun CheckoutSectionCard(
    icon: ImageVector,
    title: String,
    content: @Composable ColumnScope.() -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = TechSlate),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(bottom = 12.dp)
            ) {
                Icon(
                    icon,
                    contentDescription = null,
                    tint = BluePrimary,
                    modifier = Modifier.size(22.dp)
                )
                Spacer(Modifier.width(8.dp))
                Text(
                    title,
                    color = WhitePure,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                )
            }
            content()
        }
    }
}

@Composable
private fun CheckoutItemRow(item: CartItem) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.fillMaxWidth()
    ) {
        AsyncImage(
            model = item.image,
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .size(56.dp)
                .clip(RoundedCornerShape(10.dp))
                .background(TechDark)
        )
        Spacer(Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                item.name,
                color = WhitePure,
                fontWeight = FontWeight.SemiBold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                fontSize = 14.sp
            )
            Text(
                item.price,
                color = BluePrimary,
                fontSize = 13.sp
            )
        }
        Text(
            "x${item.quantity}",
            color = TechGray,
            fontWeight = FontWeight.Medium,
            fontSize = 14.sp
        )
    }
}

@Composable
private fun PaymentMethodOption(
    icon: ImageVector,
    label: String,
    selected: Boolean,
    onClick: () -> Unit
) {
    val borderColor = if (selected) BluePrimary else TechDark
    val bgColor = if (selected) BluePrimary.copy(alpha = 0.1f) else Color.Transparent

    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(bgColor)
            .border(1.5.dp, borderColor, RoundedCornerShape(12.dp))
            .clickable(onClick = onClick)
            .padding(12.dp)
    ) {
        Icon(
            icon,
            contentDescription = null,
            tint = if (selected) BluePrimary else TechGray,
            modifier = Modifier.size(24.dp)
        )
        Spacer(Modifier.width(12.dp))
        Text(
            label,
            color = if (selected) WhitePure else TechGray,
            fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal,
            modifier = Modifier.weight(1f)
        )
        RadioButton(
            selected = selected,
            onClick = onClick,
            colors = RadioButtonDefaults.colors(
                selectedColor = BluePrimary,
                unselectedColor = TechGray
            )
        )
    }
}

@Composable
private fun SummaryRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(label, color = TechGray, fontSize = 14.sp)
        Text(value, color = WhitePure, fontSize = 14.sp)
    }
}

@Composable
private fun CheckoutBottomBar(
    total: String,
    enabled: Boolean,
    onPlaceOrder: () -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = TechDark,
        border = androidx.compose.foundation.BorderStroke(1.dp, TechSlate)
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .navigationBarsPadding(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text("Tổng thanh toán", color = TechGray, fontSize = 12.sp)
                Text(
                    total,
                    color = BluePrimary,
                    fontWeight = FontWeight.Black,
                    fontSize = 20.sp
                )
            }
            Button(
                onClick = onPlaceOrder,
                enabled = enabled,
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = BluePrimary,
                    disabledContainerColor = TechGray.copy(alpha = 0.3f)
                )
            ) {
                Icon(Icons.Default.ShoppingCartCheckout, contentDescription = null)
                Spacer(Modifier.width(8.dp))
                Text("Đặt hàng", fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
private fun checkoutTextFieldColors() = OutlinedTextFieldDefaults.colors(
    focusedTextColor = WhitePure,
    unfocusedTextColor = WhitePure,
    cursorColor = BluePrimary,
    focusedBorderColor = BluePrimary,
    unfocusedBorderColor = TechDark,
    focusedLabelColor = BluePrimary,
    unfocusedLabelColor = TechGray
)
