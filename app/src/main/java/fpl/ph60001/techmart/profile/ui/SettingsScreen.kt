package fpl.ph60001.techmart.profile.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import fpl.ph60001.techmart.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(onBackClick: () -> Unit) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Cài đặt", color = WhitePure, fontWeight = FontWeight.Bold) },
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
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Giao diện
            SettingsSection(title = "Giao diện") {
                var darkMode by remember { mutableStateOf(true) }
                SettingsToggleItem(
                    icon = Icons.Default.DarkMode,
                    title = "Chế độ tối",
                    subtitle = "Giao diện tối giúp bảo vệ mắt",
                    checked = darkMode,
                    onCheckedChange = { darkMode = it }
                )
            }

            // Thông báo
            SettingsSection(title = "Thông báo") {
                var pushEnabled by remember { mutableStateOf(true) }
                var orderNotif by remember { mutableStateOf(true) }
                var promoNotif by remember { mutableStateOf(false) }

                SettingsToggleItem(
                    icon = Icons.Default.Notifications,
                    title = "Thông báo đẩy",
                    subtitle = "Nhận thông báo từ ứng dụng",
                    checked = pushEnabled,
                    onCheckedChange = { pushEnabled = it }
                )
                HorizontalDivider(color = TechDark, modifier = Modifier.padding(vertical = 4.dp))
                SettingsToggleItem(
                    icon = Icons.Default.LocalShipping,
                    title = "Cập nhật đơn hàng",
                    subtitle = "Thông báo khi đơn hàng thay đổi trạng thái",
                    checked = orderNotif,
                    onCheckedChange = { orderNotif = it }
                )
                HorizontalDivider(color = TechDark, modifier = Modifier.padding(vertical = 4.dp))
                SettingsToggleItem(
                    icon = Icons.Default.Campaign,
                    title = "Khuyến mãi",
                    subtitle = "Nhận ưu đãi và mã giảm giá",
                    checked = promoNotif,
                    onCheckedChange = { promoNotif = it }
                )
            }

            // Thông tin ứng dụng
            SettingsSection(title = "Thông tin") {
                SettingsInfoItem(icon = Icons.Default.Info, title = "Phiên bản", value = "1.0.0")
                HorizontalDivider(color = TechDark, modifier = Modifier.padding(vertical = 4.dp))
                SettingsInfoItem(icon = Icons.Default.Shield, title = "Chính sách bảo mật", value = "")
                HorizontalDivider(color = TechDark, modifier = Modifier.padding(vertical = 4.dp))
                SettingsInfoItem(icon = Icons.Default.Description, title = "Điều khoản sử dụng", value = "")
            }

            Spacer(Modifier.height(16.dp))
        }
    }
}

@Composable
private fun SettingsSection(title: String, content: @Composable ColumnScope.() -> Unit) {
    Column {
        Text(
            title,
            color = BluePrimary,
            fontSize = 13.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 1.sp,
            modifier = Modifier.padding(bottom = 8.dp, start = 4.dp)
        )
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = TechSlate),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(Modifier.padding(16.dp)) {
                content()
            }
        }
    }
}

@Composable
private fun SettingsToggleItem(
    icon: ImageVector,
    title: String,
    subtitle: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(icon, null, tint = BluePrimary, modifier = Modifier.size(22.dp))
        Spacer(Modifier.width(14.dp))
        Column(Modifier.weight(1f)) {
            Text(title, color = WhitePure, fontSize = 14.sp, fontWeight = FontWeight.Medium)
            Text(subtitle, color = TechGray, fontSize = 12.sp)
        }
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(
                checkedThumbColor = WhitePure,
                checkedTrackColor = BluePrimary,
                uncheckedThumbColor = TechGray,
                uncheckedTrackColor = TechDark
            )
        )
    }
}

@Composable
private fun SettingsInfoItem(icon: ImageVector, title: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .clickable { }
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(icon, null, tint = BluePrimary, modifier = Modifier.size(22.dp))
        Spacer(Modifier.width(14.dp))
        Text(title, color = WhitePure, fontSize = 14.sp, fontWeight = FontWeight.Medium, modifier = Modifier.weight(1f))
        if (value.isNotBlank()) {
            Text(value, color = TechGray, fontSize = 13.sp)
        } else {
            Icon(Icons.Default.ChevronRight, null, tint = TechGray, modifier = Modifier.size(20.dp))
        }
    }
}
