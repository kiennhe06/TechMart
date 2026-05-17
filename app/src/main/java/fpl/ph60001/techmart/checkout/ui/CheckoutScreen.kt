package fpl.ph60001.techmart.checkout.ui

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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.text.KeyboardOptions
import coil.compose.AsyncImage
import fpl.ph60001.techmart.cart.viewmodel.CartItem
import fpl.ph60001.techmart.cart.viewmodel.CartViewModel
import fpl.ph60001.techmart.network.ProvinceDto
import fpl.ph60001.techmart.network.RetrofitClient
import fpl.ph60001.techmart.network.WardDto
import fpl.ph60001.techmart.ui.theme.*
import fpl.ph60001.techmart.utils.PreferenceManager
import fpl.ph60001.techmart.order.Order
import fpl.ph60001.techmart.order.OrderItem
import fpl.ph60001.techmart.order.OrderRepository
import kotlinx.coroutines.launch
import androidx.compose.ui.window.PopupProperties

// Kiểm tra số điện thoại Việt Nam hợp lệ
private fun isValidPhone(phone: String): Boolean {
    return phone.matches(Regex("^(0[3|5|7|8|9])[0-9]{8}$"))
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CheckoutScreen(
    onBackClick: () -> Unit,
    onNavigateToConfirmation: (total: String, address: String, payment: String, itemCount: Int) -> Unit,
    cartViewModel: CartViewModel
) {
    val context = LocalContext.current
    val prefManager = remember { PreferenceManager(context) }
    val orderRepo = remember { OrderRepository(context) }
    val scope = rememberCoroutineScope()
    val cartItems by cartViewModel.checkoutItems.collectAsState()

    var selectedPayment by remember { mutableIntStateOf(0) }
    var isShippingCollapsed by remember { mutableStateOf(prefManager.getShippingName().isNotBlank() && prefManager.getShippingPhone().isNotBlank()) }

    var cardNumber by remember { mutableStateOf("") }
    var cardName by remember { mutableStateOf("") }
    var cardExpiry by remember { mutableStateOf("") }
    var cardCvv by remember { mutableStateOf("") }

    var isProcessingPayment by remember { mutableStateOf(false) }
    var paymentStepText by remember { mutableStateOf("") }

    // Thông tin giao hàng - auto-fill từ lần trước
    var fullName by remember { mutableStateOf(prefManager.getShippingName()) }
    var phone by remember { mutableStateOf(prefManager.getShippingPhone()) }
    var addressDetail by remember { mutableStateOf(prefManager.getShippingAddressDetail()) }
    var note by remember { mutableStateOf("") }
    var phoneError by remember { mutableStateOf<String?>(null) }

    // Dữ liệu địa chỉ từ API
    var provinces by remember { mutableStateOf<List<ProvinceDto>>(emptyList()) }
    var wards by remember { mutableStateOf<List<WardDto>>(emptyList()) }
    var isLoadingProvinces by remember { mutableStateOf(true) }
    var isLoadingWards by remember { mutableStateOf(false) }

    // Lựa chọn hiện tại
    var selectedProvince by remember { mutableStateOf<ProvinceDto?>(null) }
    var selectedWard by remember { mutableStateOf<WardDto?>(null) }

    // Trạng thái text search
    var provinceQuery by remember { mutableStateOf("") }
    var wardQuery by remember { mutableStateOf("") }

    // Tên đã lưu từ lần trước (dùng để auto-fill)
    val savedProvinceName = remember { prefManager.getShippingProvince() }
    val savedWardName = remember { prefManager.getShippingWard() }

    // Trạng thái dropdown
    var expandedProvince by remember { mutableStateOf(false) }
    var expandedWard by remember { mutableStateOf(false) }

    // Tải danh sách tỉnh/thành khi mở màn hình
    LaunchedEffect(Unit) {
        try {
            provinces = RetrofitClient.locationApiService.getProvinces()
            // Auto-fill tỉnh từ lần trước
            if (savedProvinceName.isNotEmpty()) {
                val matched = provinces.find { it.name == savedProvinceName }
                if (matched != null) {
                    selectedProvince = matched
                    provinceQuery = matched.name
                    // Tải phường của tỉnh đã lưu
                    val provinceDetail = RetrofitClient.locationApiService.getWardsByProvince(matched.code)
                    wards = provinceDetail.wards ?: emptyList()
                    // Auto-fill phường
                    if (savedWardName.isNotEmpty()) {
                        val matchedWard = wards.find { it.name == savedWardName }
                        if (matchedWard != null) {
                            selectedWard = matchedWard
                            wardQuery = matchedWard.name
                        }
                    }
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            isLoadingProvinces = false
        }
    }

    // Tính tổng tiền
    val subtotal = cartItems.sumOf {
        val priceNum = it.price.replace("[^\\d]".toRegex(), "").toLongOrNull() ?: 0L
        priceNum * it.quantity
    }
    val shippingFee = if (subtotal > 500000) 0L else 30000L
    val total = subtotal + shippingFee
    val fmtSub = String.format("%,dđ", subtotal).replace(",", ".")
    val fmtShip = if (shippingFee == 0L) "Miễn phí" else String.format("%,dđ", shippingFee).replace(",", ".")
    val fmtTotal = String.format("%,dđ", total).replace(",", ".")

    // Kiểm tra form hợp lệ
    val isCardValid = if (selectedPayment == 2) {
        cardNumber.replace(" ", "").length == 16 &&
                cardName.isNotBlank() &&
                cardExpiry.matches(Regex("^(0[1-9]|1[0-2])/[0-9]{2}$")) &&
                cardCvv.length == 3
    } else true

    val isFormValid = fullName.isNotBlank() && isValidPhone(phone) &&
            selectedProvince != null && selectedWard != null &&
            addressDetail.isNotBlank() && cartItems.isNotEmpty() && isCardValid

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
            val paymentLabels = listOf("Thanh toán khi nhận hàng (COD)", "Chuyển khoản ngân hàng", "Thẻ tín dụng / Ghi nợ")
            CheckoutBottomBar(total = fmtTotal, enabled = isFormValid, onPlaceOrder = {
                scope.launch {
                    try {
                        if (selectedPayment > 0) {
                            isProcessingPayment = true
                            val gateway = if (selectedPayment == 1) "Techcombank" else "Visa/Mastercard"
                            paymentStepText = "Đang kết nối cổng thanh toán $gateway..."
                            kotlinx.coroutines.delay(1200)
                            paymentStepText = "Đang xác thực giao dịch & số dư..."
                            kotlinx.coroutines.delay(1200)
                            paymentStepText = "Xác thực thành công! Đang đồng bộ hóa hóa đơn..."
                            kotlinx.coroutines.delay(800)
                        }
                        
                        val address = "$addressDetail, ${selectedWard?.name ?: ""}, ${selectedProvince?.name ?: ""}"
                        val payment = paymentLabels.getOrElse(selectedPayment) { paymentLabels[0] }
                        val orderItems = cartItems.map { OrderItem(it.name, it.price, it.image, it.quantity) }
                        
                        val order = Order(
                            id = "TM" + System.currentTimeMillis(),
                            items = orderItems,
                            total = fmtTotal,
                            address = address,
                            paymentMethod = payment,
                            customerName = fullName,
                            customerEmail = prefManager.getSavedEmail() ?: "",
                            customerPhone = phone
                        )

                        // 1. Gửi lên Server
                        val response = RetrofitClient.apiService.createOrder(order)
                        if (response.isSuccessful) {
                            // 2. Lưu cục bộ để xem offline
                            orderRepo.saveOrder(order)
                            
                            // 3. Lưu thông tin giao hàng cho lần sau
                            prefManager.saveShippingInfo(
                                fullName, phone,
                                selectedProvince?.name ?: "",
                                selectedWard?.name ?: "", addressDetail
                            )
                            
                            val itemCount = cartItems.sumOf { it.quantity }
                            cartViewModel.clearCart()
                            
                            if (selectedPayment > 0) {
                                paymentStepText = "Đặt hàng thành công!"
                                kotlinx.coroutines.delay(600)
                            }
                            isProcessingPayment = false
                            onNavigateToConfirmation(fmtTotal, address, payment, itemCount)
                        } else {
                            isProcessingPayment = false
                            android.widget.Toast.makeText(context, "Lỗi đặt hàng: ${response.message()}", android.widget.Toast.LENGTH_LONG).show()
                        }
                    } catch (e: Exception) {
                        isProcessingPayment = false
                        android.widget.Toast.makeText(context, "Lỗi kết nối server: ${e.message}", android.widget.Toast.LENGTH_LONG).show()
                    }
                }
            })
        },
        containerColor = TechDark
    ) { padding ->
        if (isProcessingPayment) {
            androidx.compose.ui.window.Dialog(
                onDismissRequest = {},
                properties = androidx.compose.ui.window.DialogProperties(
                    dismissOnBackPress = false,
                    dismissOnClickOutside = false
                )
            ) {
                Box(
                    modifier = Modifier
                        .size(280.dp, 200.dp)
                        .clip(RoundedCornerShape(24.dp))
                        .background(TechSlate)
                        .border(1.5.dp, BluePrimary.copy(alpha = 0.4f), RoundedCornerShape(24.dp))
                        .padding(24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        CircularProgressIndicator(
                            color = BluePrimary,
                            strokeWidth = 4.dp,
                            modifier = Modifier.size(50.dp)
                        )
                        Spacer(Modifier.height(24.dp))
                        Text(
                            text = paymentStepText,
                            color = WhitePure,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Medium,
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                            lineHeight = 20.sp
                        )
                    }
                }
            }
        }

        Column(
            modifier = Modifier.fillMaxSize().padding(padding)
                .verticalScroll(rememberScrollState()).padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // === 1. THÔNG TIN NHẬN HÀNG ===
            SectionCard(Icons.Default.LocationOn, "Thông tin nhận hàng") {
                if (isShippingCollapsed) {
                    // Hiển thị tóm tắt khi thu gọn
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth().clickable { isShippingCollapsed = false }.padding(vertical = 4.dp)
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(text = "$fullName | $phone", color = WhitePure, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                            Text(
                                text = "$addressDetail, $wardQuery, $provinceQuery",
                                color = TechGray, fontSize = 13.sp, maxLines = 1, overflow = TextOverflow.Ellipsis
                            )
                        }
                        TextButton(onClick = { isShippingCollapsed = false }) {
                            Text("Thay đổi", color = BluePrimary, fontWeight = FontWeight.Bold)
                        }
                    }
                } else {
                    // Họ và tên
                OutlinedTextField(
                    value = fullName, onValueChange = { fullName = it },
                    label = { Text("Họ và tên *") }, singleLine = true,
                    leadingIcon = { Icon(Icons.Default.Person, null, tint = TechGray, modifier = Modifier.size(20.dp)) },
                    modifier = Modifier.fillMaxWidth(), colors = tfColors(), shape = RoundedCornerShape(12.dp)
                )
                Spacer(Modifier.height(8.dp))

                // Số điện thoại
                OutlinedTextField(
                    value = phone,
                    onValueChange = {
                        val filtered = it.filter { c -> c.isDigit() }.take(10)
                        phone = filtered
                        phoneError = when {
                            filtered.isEmpty() -> null
                            filtered.length < 10 -> "Số điện thoại cần 10 chữ số"
                            !isValidPhone(filtered) -> "Số điện thoại không hợp lệ"
                            else -> null
                        }
                    },
                    label = { Text("Số điện thoại *") }, singleLine = true,
                    leadingIcon = { Icon(Icons.Default.Phone, null, tint = TechGray, modifier = Modifier.size(20.dp)) },
                    isError = phoneError != null,
                    supportingText = phoneError?.let { { Text(it, color = ErrorRed, fontSize = 12.sp) } },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                    modifier = Modifier.fillMaxWidth(), colors = tfColors(), shape = RoundedCornerShape(12.dp)
                )
                Spacer(Modifier.height(8.dp))

                 // Gõ tìm Tỉnh/Thành phố
                val filteredProvinces = remember(provinceQuery, provinces) {
                    if (provinceQuery.isBlank() || provinceQuery == selectedProvince?.name) provinces
                    else provinces.filter { it.name.contains(provinceQuery, ignoreCase = true) }
                }
                Box(modifier = Modifier.fillMaxWidth()) {
                    OutlinedTextField(
                        value = provinceQuery,
                        onValueChange = { provinceQuery = it; selectedProvince = null; selectedWard = null; expandedProvince = true },
                        label = { Text(if (isLoadingProvinces) "Đang tải..." else "Tỉnh / Thành phố *") },
                        singleLine = true,
                        trailingIcon = {
                            if (isLoadingProvinces) {
                                CircularProgressIndicator(Modifier.size(20.dp), color = BluePrimary, strokeWidth = 2.dp)
                            } else {
                                IconButton(onClick = {
                                    if (provinceQuery.isNotEmpty()) {
                                        provinceQuery = ""; selectedProvince = null
                                    } else {
                                        expandedProvince = !expandedProvince
                                    }
                                }) {
                                    Icon(
                                        if (provinceQuery.isNotEmpty()) Icons.Default.Clear else Icons.Default.ArrowDropDown,
                                        contentDescription = null,
                                        tint = TechGray
                                    )
                                }
                            }
                        },
                        leadingIcon = { Icon(Icons.Default.LocationCity, null, tint = TechGray, modifier = Modifier.size(20.dp)) },
                        modifier = Modifier.fillMaxWidth(), colors = tfColors(), shape = RoundedCornerShape(12.dp)
                    )
                    DropdownMenu(
                        expanded = filteredProvinces.isNotEmpty() && expandedProvince, 
                        onDismissRequest = { expandedProvince = false }, 
                        properties = PopupProperties(focusable = false),
                        modifier = Modifier.fillMaxWidth(0.9f).background(TechSlate)
                    ) {
                        filteredProvinces.forEach { province ->
                            DropdownMenuItem(text = { Text(province.name, color = WhitePure) }, onClick = {
                                selectedProvince = province; provinceQuery = province.name
                                selectedWard = null; wardQuery = ""
                                wards = emptyList()
                                expandedProvince = false
                                scope.launch {
                                    isLoadingWards = true
                                    try { 
                                        wards = (RetrofitClient.locationApiService.getWardsByProvince(province.code)).wards ?: emptyList() 
                                        if (wards.isNotEmpty()) expandedWard = true
                                    }
                                    catch (_: Exception) {} finally { isLoadingWards = false }
                                }
                            })
                        }
                    }
                }
                Spacer(Modifier.height(8.dp))

                // Gõ tìm Phường/Xã
                val filteredWards = remember(wardQuery, wards) {
                    if (wardQuery.isBlank() || wardQuery == selectedWard?.name) wards
                    else wards.filter { it.name.contains(wardQuery, ignoreCase = true) }
                }
                Box(modifier = Modifier.fillMaxWidth()) {
                    OutlinedTextField(
                        value = wardQuery,
                        onValueChange = { wardQuery = it; selectedWard = null; expandedWard = true },
                        label = { Text(if (isLoadingWards) "Đang tải..." else "Phường / Xã *") },
                        singleLine = true,
                        trailingIcon = {
                            if (isLoadingWards) {
                                CircularProgressIndicator(Modifier.size(20.dp), color = BluePrimary, strokeWidth = 2.dp)
                            } else {
                                IconButton(onClick = {
                                    if (wardQuery.isNotEmpty()) {
                                        wardQuery = ""; selectedWard = null
                                    } else {
                                        expandedWard = !expandedWard
                                    }
                                }) {
                                    Icon(
                                        if (wardQuery.isNotEmpty()) Icons.Default.Clear else Icons.Default.ArrowDropDown,
                                        contentDescription = null,
                                        tint = TechGray
                                    )
                                }
                            }
                        },
                        leadingIcon = { Icon(Icons.Default.Place, null, tint = TechGray, modifier = Modifier.size(20.dp)) },
                        enabled = selectedProvince != null,
                        modifier = Modifier.fillMaxWidth(), colors = tfColors(), shape = RoundedCornerShape(12.dp)
                    )
                    DropdownMenu(
                        expanded = filteredWards.isNotEmpty() && expandedWard, 
                        onDismissRequest = { expandedWard = false }, 
                        properties = PopupProperties(focusable = false),
                        modifier = Modifier.fillMaxWidth(0.9f).background(TechSlate)
                    ) {
                        filteredWards.forEach { ward ->
                            DropdownMenuItem(text = { Text(ward.name, color = WhitePure) }, onClick = {
                                selectedWard = ward; wardQuery = ward.name; expandedWard = false
                            })
                        }
                    }
                }
                Spacer(Modifier.height(8.dp))

                // Số nhà, tên đường
                OutlinedTextField(
                    value = addressDetail, onValueChange = { addressDetail = it },
                    label = { Text("Số nhà, tên đường *") }, singleLine = true,
                    leadingIcon = { Icon(Icons.Default.Home, null, tint = TechGray, modifier = Modifier.size(20.dp)) },
                    modifier = Modifier.fillMaxWidth(), colors = tfColors(), shape = RoundedCornerShape(12.dp)
                )
                Spacer(Modifier.height(8.dp))

                // Ghi chú
                OutlinedTextField(
                    value = note, onValueChange = { note = it },
                    label = { Text("Ghi chú cho người bán") }, minLines = 2,
                    leadingIcon = { Icon(Icons.Default.Edit, null, tint = TechGray, modifier = Modifier.size(20.dp)) },
                    modifier = Modifier.fillMaxWidth(), colors = tfColors(), shape = RoundedCornerShape(12.dp)
                )

                if (!isShippingCollapsed && fullName.isNotBlank() && phone.isNotBlank() && selectedWard != null) {
                    Spacer(Modifier.height(12.dp))
                    Button(
                        onClick = { isShippingCollapsed = true },
                        modifier = Modifier.align(Alignment.End),
                        colors = ButtonDefaults.buttonColors(containerColor = TechDark),
                        shape = RoundedCornerShape(8.dp),
                        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 0.dp)
                    ) {
                        Text("Lưu & Thu gọn", color = BluePrimary, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

            // === 2. SẢN PHẨM ĐẶT MUA ===
            SectionCard(Icons.Default.ShoppingBag, "Sản phẩm đặt mua (${cartItems.size})") {
                cartItems.forEachIndexed { i, item ->
                    CheckoutItemRow(item)
                    if (i < cartItems.size - 1) HorizontalDivider(Modifier.padding(vertical = 8.dp), color = TechDark.copy(0.5f))
                }
            }

            // === 3. PHƯƠNG THỨC THANH TOÁN ===
            SectionCard(Icons.Default.Payment, "Phương thức thanh toán") {
                PaymentOption(Icons.Default.Money, "Thanh toán khi nhận hàng (COD)", selectedPayment == 0) { selectedPayment = 0 }
                Spacer(Modifier.height(8.dp))
                
                PaymentOption(Icons.Default.AccountBalance, "Chuyển khoản ngân hàng", selectedPayment == 1) { selectedPayment = 1 }
                if (selectedPayment == 1) {
                    BankTransferDetails(fmtTotal, phone)
                }
                Spacer(Modifier.height(8.dp))
                
                PaymentOption(Icons.Default.CreditCard, "Thẻ tín dụng / Ghi nợ", selectedPayment == 2) { selectedPayment = 2 }
                if (selectedPayment == 2) {
                    CreditCardForm(
                        cardNumber = cardNumber,
                        onCardNumberChange = { cardNumber = it },
                        cardName = cardName,
                        onCardNameChange = { cardName = it },
                        cardExpiry = cardExpiry,
                        onCardExpiryChange = { cardExpiry = it },
                        cardCvv = cardCvv,
                        onCardCvvChange = { cardCvv = it }
                    )
                }
            }

            // === 4. CHI TIẾT THANH TOÁN ===
            SectionCard(Icons.Default.Receipt, "Chi tiết thanh toán") {
                SummaryRow("Tạm tính", fmtSub)
                Spacer(Modifier.height(4.dp))
                SummaryRow("Phí vận chuyển", fmtShip)
                if (shippingFee == 0L) {
                    Text("Miễn phí vận chuyển cho đơn trên 500.000đ", color = CyberCyan,
                        style = MaterialTheme.typography.labelSmall, modifier = Modifier.padding(start = 4.dp, top = 2.dp))
                }
                HorizontalDivider(Modifier.padding(vertical = 8.dp), color = TechDark.copy(0.5f))
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Text("Tổng thanh toán", color = WhitePure, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    Text(fmtTotal, color = BluePrimary, fontWeight = FontWeight.Black, fontSize = 20.sp)
                }
            }
            Spacer(Modifier.height(8.dp))
        }
    }

}

// --- CÁC COMPOSABLE PHỤ TRỢ ---

@Composable
private fun SectionCard(icon: ImageVector, title: String, content: @Composable ColumnScope.() -> Unit) {
    Card(Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = TechSlate), shape = RoundedCornerShape(16.dp)) {
        Column(Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(bottom = 12.dp)) {
                Icon(icon, null, tint = BluePrimary, modifier = Modifier.size(22.dp))
                Spacer(Modifier.width(8.dp))
                Text(title, color = WhitePure, fontWeight = FontWeight.Bold, fontSize = 16.sp)
            }
            content()
        }
    }
}

@Composable
private fun CheckoutItemRow(item: CartItem) {
    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
        AsyncImage(model = item.image, contentDescription = null, contentScale = ContentScale.Crop,
            modifier = Modifier.size(56.dp).clip(RoundedCornerShape(10.dp)).background(TechDark))
        Spacer(Modifier.width(12.dp))
        Column(Modifier.weight(1f)) {
            Text(item.name, color = WhitePure, fontWeight = FontWeight.SemiBold, maxLines = 1, overflow = TextOverflow.Ellipsis, fontSize = 14.sp)
            Text(item.price, color = BluePrimary, fontSize = 13.sp)
        }
        Text("x${item.quantity}", color = TechGray, fontWeight = FontWeight.Medium, fontSize = 14.sp)
    }
}

@Composable
private fun PaymentOption(icon: ImageVector, label: String, selected: Boolean, onClick: () -> Unit) {
    Row(verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(12.dp))
            .background(if (selected) BluePrimary.copy(alpha = 0.1f) else Color.Transparent)
            .border(1.5.dp, if (selected) BluePrimary else TechDark, RoundedCornerShape(12.dp))
            .clickable(onClick = onClick).padding(12.dp)
    ) {
        Icon(icon, null, tint = if (selected) BluePrimary else TechGray, modifier = Modifier.size(24.dp))
        Spacer(Modifier.width(12.dp))
        Text(label, color = if (selected) WhitePure else TechGray, fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal, modifier = Modifier.weight(1f))
        RadioButton(selected, onClick, colors = RadioButtonDefaults.colors(selectedColor = BluePrimary, unselectedColor = TechGray))
    }
}

@Composable
private fun SummaryRow(label: String, value: String) {
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
        Text(label, color = TechGray, fontSize = 14.sp); Text(value, color = WhitePure, fontSize = 14.sp)
    }
}

@Composable
private fun CheckoutBottomBar(total: String, enabled: Boolean, onPlaceOrder: () -> Unit) {
    Surface(Modifier.fillMaxWidth(), color = TechDark, border = androidx.compose.foundation.BorderStroke(1.dp, TechSlate)) {
        Row(Modifier.padding(16.dp).navigationBarsPadding(), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            Column(Modifier.weight(1f)) {
                Text("Tổng thanh toán", color = TechGray, fontSize = 12.sp)
                Text(total, color = BluePrimary, fontWeight = FontWeight.Black, fontSize = 20.sp)
            }
            Button(onClick = onPlaceOrder, enabled = enabled, modifier = Modifier.weight(1f), shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = BluePrimary, disabledContainerColor = TechGray.copy(alpha = 0.3f))
            ) {
                Icon(Icons.Default.ShoppingCartCheckout, null); Spacer(Modifier.width(8.dp))
                Text("Đặt hàng", fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
private fun tfColors() = OutlinedTextFieldDefaults.colors(
    focusedTextColor = WhitePure, unfocusedTextColor = WhitePure, cursorColor = BluePrimary,
    focusedBorderColor = BluePrimary, unfocusedBorderColor = TechDark,
    focusedLabelColor = BluePrimary, unfocusedLabelColor = TechGray,
    disabledTextColor = TechGray, disabledBorderColor = TechDark.copy(0.5f), disabledLabelColor = TechGray.copy(0.5f),
    errorBorderColor = ErrorRed, errorLabelColor = ErrorRed
)

@Composable
private fun CreditCardPreview(
    cardNumber: String,
    cardName: String,
    cardExpiry: String
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(180.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(
                brush = Brush.linearGradient(
                    colors = listOf(Color(0xFF0F2027), Color(0xFF203A43), Color(0xFF2C5364))
                )
            )
            .border(1.dp, Color.White.copy(alpha = 0.2f), RoundedCornerShape(16.dp))
            .padding(20.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(36.dp, 28.dp)
                        .clip(RoundedCornerShape(6.dp))
                        .background(
                            brush = Brush.linearGradient(
                                colors = listOf(Color(0xFFE5A93B), Color(0xFFFFF2A3))
                            )
                        )
                )
                Text(
                    text = "VISA",
                    color = Color.White,
                    fontWeight = FontWeight.Black,
                    fontSize = 20.sp,
                    fontStyle = androidx.compose.ui.text.font.FontStyle.Italic
                )
            }
            
            val displayNum = cardNumber.padEnd(16, '•')
                .chunked(4)
                .joinToString("   ")
            
            Text(
                text = displayNum,
                color = Color.White,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 2.sp
            )
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text("CHỦ THẺ", color = Color.White.copy(alpha = 0.6f), fontSize = 10.sp)
                    Text(
                        text = cardName.ifBlank { "NO NAME" }.uppercase(),
                        color = Color.White,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                
                Column(horizontalAlignment = Alignment.End) {
                    Text("HẠN DÙNG", color = Color.White.copy(alpha = 0.6f), fontSize = 10.sp)
                    Text(
                        text = cardExpiry.ifBlank { "MM/YY" },
                        color = Color.White,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

@Composable
private fun CreditCardForm(
    cardNumber: String,
    onCardNumberChange: (String) -> Unit,
    cardName: String,
    onCardNameChange: (String) -> Unit,
    cardExpiry: String,
    onCardExpiryChange: (String) -> Unit,
    cardCvv: String,
    onCardCvvChange: (String) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 12.dp, bottom = 8.dp)
    ) {
        CreditCardPreview(cardNumber, cardName, cardExpiry)
        Spacer(Modifier.height(16.dp))
        
        OutlinedTextField(
            value = cardNumber,
            onValueChange = { input ->
                val filtered = input.filter { it.isDigit() }.take(16)
                onCardNumberChange(filtered)
            },
            label = { Text("Số thẻ (16 chữ số)") },
            leadingIcon = { Icon(Icons.Default.CreditCard, null, tint = BluePrimary) },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            colors = tfColors(),
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )
        Spacer(Modifier.height(8.dp))
        
        OutlinedTextField(
            value = cardName,
            onValueChange = { onCardNameChange(it.take(30)) },
            label = { Text("Họ tên chủ thẻ") },
            leadingIcon = { Icon(Icons.Default.Person, null, tint = BluePrimary) },
            colors = tfColors(),
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )
        Spacer(Modifier.height(8.dp))
        
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            OutlinedTextField(
                value = cardExpiry,
                onValueChange = { input ->
                    val filtered = input.replace("/", "").filter { it.isDigit() }.take(4)
                    val formatted = if (filtered.length >= 3) {
                        filtered.substring(0, 2) + "/" + filtered.substring(2)
                    } else {
                        filtered
                    }
                    onCardExpiryChange(formatted)
                },
                label = { Text("Hạn dùng (MM/YY)") },
                leadingIcon = { Icon(Icons.Default.DateRange, null, tint = BluePrimary) },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                colors = tfColors(),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.weight(1f),
                singleLine = true
            )
            
            OutlinedTextField(
                value = cardCvv,
                onValueChange = { input ->
                    val filtered = input.filter { it.isDigit() }.take(3)
                    onCardCvvChange(filtered)
                },
                label = { Text("Mã CVV (3 số)") },
                leadingIcon = { Icon(Icons.Default.Lock, null, tint = BluePrimary) },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                colors = tfColors(),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.weight(1f),
                singleLine = true
            )
        }
    }
}

@Composable
private fun BankTransferDetails(totalAmount: String, userPhone: String) {
    val context = LocalContext.current
    val clipboardManager = androidx.compose.ui.platform.LocalClipboardManager.current
    val accountNumber = "99999999990203"
    val transferContent = "TM ${userPhone.takeLast(6)}"
    
    val amountRaw = totalAmount.replace("[^\\d]".toRegex(), "")
    val transferContentEncoded = try {
        java.net.URLEncoder.encode(transferContent, "UTF-8")
    } catch (e: Exception) {
        transferContent
    }
    
    val qrUrl = "https://img.vietqr.io/image/tcb-99999999990203-qr_only.png?amount=$amountRaw&addInfo=$transferContentEncoded&accountName=PHAM%20DUC%20KIEN"

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 12.dp, bottom = 8.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(TechDark)
            .border(1.dp, BluePrimary.copy(alpha = 0.3f), RoundedCornerShape(16.dp))
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .size(160.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(Color.White)
                .padding(8.dp),
            contentAlignment = Alignment.Center
        ) {
            AsyncImage(
                model = qrUrl,
                contentDescription = "VietQR",
                contentScale = ContentScale.Fit,
                modifier = Modifier.fillMaxSize()
            )
        }
        
        Spacer(Modifier.height(12.dp))
        Text("QUÉT MÃ QR ĐỂ THANH TOÁN CHUYỂN KHOẢN", color = CyberCyan, fontSize = 11.sp, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(16.dp))
        
        BankInfoRow("Ngân hàng", "Techcombank")
        BankInfoRow("Chủ tài khoản", "PHAM DUC KIEN")
        BankInfoRow("Số tài khoản", accountNumber) {
            clipboardManager.setText(AnnotatedString(accountNumber))
            android.widget.Toast.makeText(context, "Đã sao chép số tài khoản", android.widget.Toast.LENGTH_SHORT).show()
        }
        BankInfoRow("Số tiền", totalAmount) {
            val amountRaw = totalAmount.replace("[^\\d]".toRegex(), "")
            clipboardManager.setText(AnnotatedString(amountRaw))
            android.widget.Toast.makeText(context, "Đã sao chép số tiền", android.widget.Toast.LENGTH_SHORT).show()
        }
        BankInfoRow("Nội dung CK", transferContent) {
            clipboardManager.setText(AnnotatedString(transferContent))
            android.widget.Toast.makeText(context, "Đã sao chép nội dung chuyển khoản", android.widget.Toast.LENGTH_SHORT).show()
        }
    }
}

@Composable
private fun BankInfoRow(label: String, value: String, onCopy: (() -> Unit)? = null) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(label, color = TechGray, fontSize = 13.sp)
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(value, color = WhitePure, fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
            if (onCopy != null) {
                Icon(
                    imageVector = Icons.Default.ContentCopy,
                    contentDescription = "Copy",
                    tint = BluePrimary,
                    modifier = Modifier
                        .size(16.dp)
                        .clickable { onCopy() }
                )
            }
        }
    }
}
