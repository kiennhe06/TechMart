package fpl.ph60001.techmart.product.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import fpl.ph60001.techmart.network.RetrofitClient
import fpl.ph60001.techmart.cart.viewmodel.CartViewModel
import fpl.ph60001.techmart.ui.theme.*
import fpl.ph60001.techmart.network.ProductDetailDto
import androidx.compose.material.icons.filled.Favorite
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class ProductDetailViewModel : ViewModel() {
    private val _product = MutableStateFlow<ProductDetailDto?>(null)
    val product: StateFlow<ProductDetailDto?> = _product

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    fun loadProduct(id: String) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val detail = RetrofitClient.apiService.getProductDetail(id)
                _product.value = detail
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                _isLoading.value = false
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProductDetailScreen(
    productId: String,
    onBackClick: () -> Unit,
    viewModel: ProductDetailViewModel = viewModel(),
    cartViewModel: CartViewModel = viewModel()
) {
    val product by viewModel.product.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val favoriteIds by cartViewModel.favoriteIds.collectAsState()
    val isFavorite = favoriteIds.contains(productId)
    
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    LaunchedEffect(productId) {
        viewModel.loadProduct(productId)
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { Text("Chi tiết sản phẩm", color = WhitePure) },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.Default.ArrowBack, contentDescription = null, tint = WhitePure)
                    }
                },
                actions = {
                    IconButton(onClick = { cartViewModel.toggleFavorite(productId) }) {
                        Icon(
                            imageVector = if (isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                            contentDescription = null,
                            tint = if (isFavorite) Color.Red else WhitePure
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = TechDark)
            )
        },
        bottomBar = {
            product?.let { item ->
                BottomPurchaseBar(
                    price = item.price,
                    onAddToCart = {
                        cartViewModel.addToCart(item)
                        scope.launch {
                            snackbarHostState.showSnackbar("Đã thêm vào giỏ hàng!")
                        }
                    }
                )
            }
        },
        containerColor = TechDark
    ) { padding ->
        if (isLoading) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = BluePrimary)
            }
        } else {
            product?.let { item ->
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding)
                        .verticalScroll(rememberScrollState())
                ) {
                    // Image Section
                    AsyncImage(
                        model = item.image,
                        contentDescription = null,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(350.dp)
                            .background(TechSlate)
                    )

                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = item.name,
                            style = MaterialTheme.typography.headlineMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = WhitePure
                            )
                        )
                        
                        Text(
                            text = item.price,
                            style = MaterialTheme.typography.titleLarge.copy(
                                color = BluePrimary,
                                fontWeight = FontWeight.Black
                            ),
                            modifier = Modifier.padding(vertical = 8.dp)
                        )

                        HorizontalDivider(
                            modifier = Modifier.padding(vertical = 16.dp),
                            color = TechSlate
                        )

                        Text(
                            text = "Mô tả sản phẩm",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = WhitePure
                            )
                        )
                        Text(
                            text = item.description,
                            style = MaterialTheme.typography.bodyMedium.copy(color = TechGray),
                            modifier = Modifier.padding(top = 8.dp)
                        )

                        Spacer(modifier = Modifier.height(24.dp))

                        Text(
                            text = "Thông số kỹ thuật",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = WhitePure
                            )
                        )
                        
                        item.specifications.forEach { spec ->
                            SpecificationRow(spec.key, spec.value)
                        }
                        
                        Spacer(modifier = Modifier.height(32.dp))
                    }
                }
            }
        }
    }
}

@Composable
fun SpecificationRow(key: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(key, color = TechGray)
        Text(value, color = WhitePure, fontWeight = FontWeight.SemiBold)
    }
}

@Composable
fun BottomPurchaseBar(price: String, onAddToCart: () -> Unit) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = TechDark,
        tonalElevation = 8.dp,
        border = androidx.compose.foundation.BorderStroke(1.dp, TechSlate)
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .navigationBarsPadding(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            OutlinedButton(
                onClick = onAddToCart,
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = BluePrimary)
            ) {
                Icon(Icons.Default.ShoppingCart, contentDescription = null)
                Spacer(Modifier.width(8.dp))
                Text("Giỏ hàng")
            }
            
            Button(
                onClick = {},
                modifier = Modifier.weight(1.5f),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = BluePrimary)
            ) {
                Text("Mua ngay", fontWeight = FontWeight.Bold)
            }
        }
    }
}
