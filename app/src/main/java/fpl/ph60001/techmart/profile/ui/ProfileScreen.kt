
package fpl.ph60001.techmart.profile.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import fpl.ph60001.techmart.ui.theme.*
import kotlin.text.take
import kotlin.text.uppercase
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.layout.ContentScale
import coil.compose.AsyncImage
import fpl.ph60001.techmart.utils.PreferenceManager
import android.net.Uri

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    onLogoutClick: () -> Unit,
    onFavoriteClick: () -> Unit = {},
    onPersonalInfoClick: () -> Unit = {},
    onOrderHistoryClick: () -> Unit = {},
    onSettingsClick: () -> Unit = {}
) {
    val context = LocalContext.current
    val prefManager = remember { PreferenceManager(context) }
    val user = Firebase.auth.currentUser
    
    val localName = prefManager.getShippingName()
    val localEmail = prefManager.getSavedEmail()
    
    val displayName = user?.displayName?.takeIf { it.isNotBlank() } ?: localName.takeIf { it.isNotBlank() } ?: "Người dùng TechMart"
    val displayEmail = user?.email?.takeIf { it.isNotBlank() } ?: localEmail?.takeIf { it.isNotBlank() } ?: "Chưa cập nhật email"
    
    var avatarUri by remember { mutableStateOf(user?.photoUrl?.toString() ?: prefManager.getAvatar()) }
    
    val launcher = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        uri?.let {
            avatarUri = it.toString()
            prefManager.saveAvatar(it.toString())
        }
    }

    // Biến trạng thái để kiểm soát việc hiển thị thông báo xác nhận đăng xuất
    var showLogoutDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Hồ sơ của tôi", fontWeight = FontWeight.Bold) },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = TechDark,
                    titleContentColor = WhitePure
                )
            )
        },
        containerColor = TechDark
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Avatar Section
            Box(
                modifier = Modifier
                    .size(100.dp)
                    .clip(CircleShape)
                    .background(
                        brush = Brush.linearGradient(
                            colors = listOf(BluePrimary, CyberCyan)
                        )
                    )
                    .clickable { launcher.launch("image/*") },
                contentAlignment = Alignment.Center
            ) {
                if (avatarUri != null) {
                    AsyncImage(
                        model = avatarUri,
                        contentDescription = "Avatar",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize().clip(CircleShape)
                    )
                } else {
                    Text(
                        text = displayName.take(1).uppercase(),
                        style = MaterialTheme.typography.headlineLarge.copy(
                            color = WhitePure,
                            fontWeight = FontWeight.Bold
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = displayName,
                style = MaterialTheme.typography.titleLarge.copy(
                    color = WhitePure,
                    fontWeight = FontWeight.Bold
                )
            )
            Text(
                text = displayEmail,
                style = MaterialTheme.typography.bodyMedium.copy(color = TechGray)
            )

            Spacer(modifier = Modifier.height(32.dp))

            // Profile Options
            ProfileOptionItem(icon = Icons.Default.Person, title = "Thông tin cá nhân", onClick = onPersonalInfoClick)
            ProfileOptionItem(icon = Icons.Default.History, title = "Lịch sử mua hàng", onClick = onOrderHistoryClick)
            ProfileOptionItem(icon = Icons.Default.Favorite, title = "Sản phẩm yêu thích", onClick = onFavoriteClick)
            ProfileOptionItem(icon = Icons.Default.Settings, title = "Cài đặt", onClick = onSettingsClick)

            Spacer(modifier = Modifier.weight(1f))

            // Nút Đăng xuất
            Button(
                onClick = { showLogoutDialog = true }, // Khi nhấn thì hiện thông báo
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = TechSlate,
                    contentColor = Color.Red.copy(alpha = 0.8f)
                )
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.AutoMirrored.Filled.ExitToApp, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        "Đăng xuất",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                    )
                }
            }

            Spacer(modifier = Modifier.height(32.dp))
        }

        // Hộp thoại xác nhận đăng xuất
        if (showLogoutDialog) {
            AlertDialog(
                onDismissRequest = { showLogoutDialog = false },
                containerColor = TechSlate,
                title = {
                    Text(
                        text = "Xác nhận đăng xuất",
                        color = WhitePure,
                        fontWeight = FontWeight.Bold
                    )
                },
                text = {
                    Text(
                        text = "Bạn có chắc chắn muốn đăng xuất khỏi TechMart không?",
                        color = TechGray
                    )
                },
                confirmButton = {
                    TextButton(
                        onClick = {
                            showLogoutDialog = false
                            onLogoutClick()
                        }
                    ) {
                        Text("Đăng xuất", color = Color.Red, fontWeight = FontWeight.Bold)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showLogoutDialog = false }) {
                        Text("Hủy", color = WhitePure)
                    }
                }
            )
        }
    }
}

@Composable
fun ProfileOptionItem(icon: ImageVector, title: String, onClick: () -> Unit = {}) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(TechSlate.copy(alpha = 0.3f))
            .clickable { onClick() }
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(icon, contentDescription = null, tint = BluePrimary, modifier = Modifier.size(24.dp))
        Spacer(modifier = Modifier.width(16.dp))
        Text(
            text = title,
            style = MaterialTheme.typography.bodyLarge.copy(color = WhitePure, fontWeight = FontWeight.Medium),
            modifier = Modifier.weight(1f)
        )
        Icon(Icons.Default.ChevronRight, contentDescription = null, tint = TechGray)
    }
}