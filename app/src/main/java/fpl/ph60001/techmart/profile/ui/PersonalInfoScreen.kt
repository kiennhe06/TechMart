package fpl.ph60001.techmart.profile.ui

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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import fpl.ph60001.techmart.ui.theme.*
import fpl.ph60001.techmart.utils.PreferenceManager

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PersonalInfoScreen(onBackClick: () -> Unit) {
    val user = Firebase.auth.currentUser
    val context = LocalContext.current
    val prefManager = remember { PreferenceManager(context) }

    val displayName = user?.displayName ?: prefManager.getShippingName().ifBlank { "Người dùng TechMart" }
    val email = user?.email ?: "Chưa cập nhật"
    val phone = prefManager.getShippingPhone()
    val province = prefManager.getShippingProvince()
    val ward = prefManager.getShippingWard()
    val addressDetail = prefManager.getShippingAddressDetail()
    val fullAddress = listOf(addressDetail, ward, province).filter { it.isNotBlank() }.joinToString(", ")

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Thông tin cá nhân", color = WhitePure, fontWeight = FontWeight.Bold) },
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
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Avatar
            Box(
                modifier = Modifier
                    .size(100.dp)
                    .clip(CircleShape)
                    .background(
                        brush = Brush.linearGradient(listOf(BluePrimary, CyberCyan))
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = displayName.take(1).uppercase(),
                    fontSize = 36.sp,
                    color = WhitePure,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            Text(displayName, color = WhitePure, fontSize = 20.sp, fontWeight = FontWeight.Bold)
            Text(email, color = TechGray, fontSize = 14.sp)

            Spacer(modifier = Modifier.height(32.dp))

            // Info Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = TechSlate),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(Modifier.padding(20.dp)) {
                    Text(
                        "Thông tin tài khoản",
                        color = WhitePure,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp
                    )
                    Spacer(Modifier.height(20.dp))

                    InfoRow(icon = Icons.Default.Person, label = "Họ và tên", value = displayName)
                    HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp), color = TechDark)
                    InfoRow(icon = Icons.Default.Email, label = "Email", value = email)
                    HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp), color = TechDark)
                    InfoRow(icon = Icons.Default.Phone, label = "Số điện thoại", value = phone.ifBlank { "Chưa cập nhật" })
                    HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp), color = TechDark)
                    InfoRow(icon = Icons.Default.LocationOn, label = "Địa chỉ", value = fullAddress.ifBlank { "Chưa cập nhật" })
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Auth Provider
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = TechSlate),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(Modifier.padding(20.dp)) {
                    Text(
                        "Phương thức đăng nhập",
                        color = WhitePure,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp
                    )
                    Spacer(Modifier.height(16.dp))

                    val provider = user?.providerData?.lastOrNull()?.providerId ?: "unknown"
                    val providerName = when {
                        provider.contains("google") -> "Google"
                        provider.contains("facebook") -> "Facebook"
                        provider.contains("password") -> "Email/Mật khẩu"
                        else -> "Không xác định"
                    }
                    val providerIcon = when {
                        provider.contains("google") -> Icons.Default.AccountCircle
                        provider.contains("facebook") -> Icons.Default.Public
                        else -> Icons.Default.Lock
                    }

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(providerIcon, null, tint = BluePrimary, modifier = Modifier.size(22.dp))
                        Spacer(Modifier.width(12.dp))
                        Column {
                            Text(providerName, color = WhitePure, fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
                            Text("Đang sử dụng", color = TechGray, fontSize = 12.sp)
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

@Composable
private fun InfoRow(icon: ImageVector, label: String, value: String) {
    Row(verticalAlignment = Alignment.Top) {
        Icon(icon, null, tint = BluePrimary, modifier = Modifier.size(20.dp))
        Spacer(Modifier.width(14.dp))
        Column {
            Text(label, color = TechGray, fontSize = 12.sp)
            Spacer(Modifier.height(2.dp))
            Text(value, color = WhitePure, fontSize = 15.sp, fontWeight = FontWeight.Medium)
        }
    }
}
