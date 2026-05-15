package fpl.ph60001.techmart

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.facebook.CallbackManager
import com.facebook.FacebookCallback
import com.facebook.FacebookException
import com.facebook.login.LoginManager
import com.facebook.login.LoginResult
import com.google.firebase.auth.FacebookAuthProvider
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import fpl.ph60001.techmart.auth.data.GoogleAuthUiClient
import fpl.ph60001.techmart.auth.ui.LoginScreen
import fpl.ph60001.techmart.auth.ui.RegisterScreen
import fpl.ph60001.techmart.auth.ui.SplashScreen
import fpl.ph60001.techmart.home.ui.HomeScreen
import fpl.ph60001.techmart.profile.ui.ProfileScreen
import fpl.ph60001.techmart.product.ui.ProductDetailScreen
import fpl.ph60001.techmart.product.ui.FavoriteScreen
import fpl.ph60001.techmart.product.ui.AllProductsScreen
import fpl.ph60001.techmart.product.ui.FlashSaleListScreen
import fpl.ph60001.techmart.cart.ui.CartScreen
import fpl.ph60001.techmart.cart.viewmodel.CartViewModel
import fpl.ph60001.techmart.checkout.ui.CheckoutScreen
import fpl.ph60001.techmart.ui.theme.*
import fpl.ph60001.techmart.utils.PreferenceManager
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class MainActivity : ComponentActivity() {
    private val callbackManager = CallbackManager.Factory.create()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            TechMartTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    TechMartApp(callbackManager)
                }
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        callbackManager.onActivityResult(requestCode, resultCode, data)
    }
}

@Composable
fun TechMartApp(callbackManager: CallbackManager) {
    val navController = rememberNavController()
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val preferenceManager = remember { PreferenceManager(context) }
    val googleAuthUiClient = remember { GoogleAuthUiClient(context) }
    val cartViewModel: CartViewModel = viewModel(factory = object : androidx.lifecycle.ViewModelProvider.Factory {
        override fun <T : androidx.lifecycle.ViewModel> create(modelClass: Class<T>): T {
            return CartViewModel(preferenceManager) as T
        }
    })

    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination

    // Facebook Login Logic
    DisposableEffect(Unit) {
        LoginManager.getInstance().registerCallback(callbackManager, object : FacebookCallback<LoginResult> {
            override fun onSuccess(result: LoginResult) {
                scope.launch {
                    val credential = FacebookAuthProvider.getCredential(result.accessToken.token)
                    Firebase.auth.signInWithCredential(credential).await()
                    Toast.makeText(context, "Đăng nhập Facebook thành công!", Toast.LENGTH_SHORT).show()
                    navController.navigate("home") {
                        popUpTo("login") { inclusive = true }
                    }
                }
            }
            override fun onCancel() {
                Toast.makeText(context, "Đã hủy đăng nhập Facebook", Toast.LENGTH_SHORT).show()
            }
            override fun onError(error: FacebookException) {
                Toast.makeText(context, "Lỗi Facebook: ${error.message}", Toast.LENGTH_SHORT).show()
            }
        })
        onDispose {
            LoginManager.getInstance().unregisterCallback(callbackManager)
        }
    }

    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult(),
        onResult = { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                scope.launch {
                    val signInResult = googleAuthUiClient.signInWithGoogle(result.data ?: return@launch)
                    if (signInResult.isSuccess) {
                        Toast.makeText(context, "Đăng nhập Google thành công!", Toast.LENGTH_SHORT).show()
                        navController.navigate("home") {
                            popUpTo("login") { inclusive = true }
                        }
                    } else {
                        Toast.makeText(context, "Lỗi đăng nhập Google", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }
    )

    val showBottomBar = currentDestination?.route in listOf("home", "profile")

    Scaffold(
        bottomBar = {
            if (showBottomBar) {
                NavigationBar(
                    containerColor = TechSlate,
                    contentColor = WhitePure
                ) {
                    NavigationBarItem(
                        icon = { Icon(Icons.Default.Home, contentDescription = null) },
                        label = { Text("Trang chủ") },
                        selected = currentDestination?.hierarchy?.any { it.route == "home" } == true,
                        onClick = {
                            navController.navigate("home") {
                                popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                                launchSingleTop = true
                                restoreState = true
                            }
                        },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = BluePrimary,
                            selectedTextColor = BluePrimary,
                            unselectedIconColor = TechGray,
                            unselectedTextColor = TechGray,
                            indicatorColor = TechDark
                        )
                    )
                    NavigationBarItem(
                        icon = { Icon(Icons.Default.Person, contentDescription = null) },
                        label = { Text("Hồ sơ") },
                        selected = currentDestination?.hierarchy?.any { it.route == "profile" } == true,
                        onClick = {
                            navController.navigate("profile") {
                                popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                                launchSingleTop = true
                                restoreState = true
                            }
                        },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = BluePrimary,
                            selectedTextColor = BluePrimary,
                            unselectedIconColor = TechGray,
                            unselectedTextColor = TechGray,
                            indicatorColor = TechDark
                        )
                    )
                }
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = "splash",
            modifier = Modifier.padding(innerPadding)
        ) {
            composable("splash") {
                SplashScreen(onTimeout = {
                    if (preferenceManager.isRemembered()) {
                        navController.navigate("home") {
                            popUpTo("splash") { inclusive = true }
                        }
                    } else {
                        navController.navigate("login") {
                            popUpTo("splash") { inclusive = true }
                        }
                    }
                })
            }
            composable("login") {
                LoginScreen(
                    onLoginClick = { email, password, rememberMe ->
                        if (email.isNotEmpty() && password.isNotEmpty()) {
                            preferenceManager.saveLoginState(rememberMe, email)
                            Toast.makeText(context, "Đăng nhập thành công!", Toast.LENGTH_SHORT).show()
                            navController.navigate("home") {
                                popUpTo("login") { inclusive = true }
                            }
                        } else {
                            Toast.makeText(context, "Vui lòng nhập đầy đủ thông tin", Toast.LENGTH_SHORT).show()
                        }
                    },
                    onRegisterClick = { navController.navigate("register") },
                    onForgotPasswordClick = {
                        Toast.makeText(context, "Tính năng đang phát triển", Toast.LENGTH_SHORT).show()
                    },
                    onGoogleSignInClick = {
                        launcher.launch(googleAuthUiClient.getSignInIntent())
                    },
                    onFacebookSignInClick = {
                        LoginManager.getInstance().logInWithReadPermissions(
                            context as androidx.activity.result.ActivityResultRegistryOwner,
                            callbackManager,
                            listOf("email", "public_profile")
                        )
                    }
                )
            }
            composable("register") {
                RegisterScreen(
                    onRegisterClick = { name, email, phone, password ->
                        if (name.isNotEmpty() && email.isNotEmpty() && phone.isNotEmpty() && password.isNotEmpty()) {
                            Toast.makeText(context, "Đăng ký thành công!", Toast.LENGTH_SHORT).show()
                            navController.popBackStack()
                        } else {
                            Toast.makeText(context, "Vui lòng điền đầy đủ các trường", Toast.LENGTH_SHORT).show()
                        }
                    },
                    onBackToLoginClick = { navController.popBackStack() }
                )
            }
            composable("home") {
                HomeScreen(
                    onProductClick = { productId ->
                        navController.navigate("product_detail/$productId")
                    },
                    onCategoryClick = { categoryName ->
                        Toast.makeText(context, "Bấm vào danh mục: $categoryName", Toast.LENGTH_SHORT).show()
                    },
                    onNotificationClick = {
                        Toast.makeText(context, "Mở thông báo", Toast.LENGTH_SHORT).show()
                    },
                    onCartClick = {
                        navController.navigate("cart")
                    },
                    onSeeAllProductsClick = {
                        navController.navigate("all_products")
                    },
                    onSeeAllFlashSaleClick = {
                        navController.navigate("flash_sale_list")
                    },
                    cartViewModel = cartViewModel
                )
            }
            composable("all_products") {
                AllProductsScreen(
                    onBackClick = { navController.popBackStack() },
                    onProductClick = { productId ->
                        navController.navigate("product_detail/$productId")
                    }
                )
            }
            composable("flash_sale_list") {
                FlashSaleListScreen(
                    onBackClick = { navController.popBackStack() },
                    onProductClick = { productId ->
                        navController.navigate("product_detail/$productId")
                    }
                )
            }
            composable("cart") {
                CartScreen(
                    onBackClick = { navController.popBackStack() },
                    onCheckoutClick = { selectedItems ->
                        cartViewModel.setCheckoutItems(selectedItems)
                        navController.navigate("checkout")
                    },
                    viewModel = cartViewModel
                )
            }
            composable("checkout") {
                CheckoutScreen(
                    onBackClick = { navController.popBackStack() },
                    onOrderSuccess = {
                        navController.navigate("home") {
                            popUpTo("home") { inclusive = true }
                        }
                    },
                    cartViewModel = cartViewModel
                )
            }
            composable("profile") {
                ProfileScreen(
                    onLogoutClick = {
                        Firebase.auth.signOut()
                        LoginManager.getInstance().logOut()
                        preferenceManager.clearLoginState()
                        Toast.makeText(context, "Đã đăng xuất", Toast.LENGTH_SHORT).show()
                        navController.navigate("login") {
                            popUpTo(0) { inclusive = true }
                        }
                    },
                    onFavoriteClick = {
                        navController.navigate("favorites")
                    }
                )
            }
            composable("favorites") {
                FavoriteScreen(
                    onBackClick = { navController.popBackStack() },
                    onProductClick = { productId ->
                        navController.navigate("product_detail/$productId")
                    },
                    cartViewModel = cartViewModel
                )
            }
            composable("product_detail/{productId}") { backStackEntry ->
                val productId = backStackEntry.arguments?.getString("productId") ?: ""
                ProductDetailScreen(
                    productId = productId,
                    onBackClick = { navController.popBackStack() },
                    onBuyNowClick = { itemToBuy ->
                        cartViewModel.setCheckoutItems(itemToBuy)
                        navController.navigate("checkout")
                    },
                    cartViewModel = cartViewModel
                )
            }
        }
    }
}