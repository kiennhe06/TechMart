package fpl.ph60001.techmart.cart.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
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
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import fpl.ph60001.techmart.cart.viewmodel.CartItem
import fpl.ph60001.techmart.cart.viewmodel.CartViewModel
import fpl.ph60001.techmart.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CartScreen(
    onBackClick: () -> Unit,
    onCheckoutClick: () -> Unit,
    viewModel: CartViewModel = viewModel()
) {
    val cartItems by viewModel.cartItems.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Giỏ hàng của tôi", color = WhitePure) },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.Default.ArrowBack, contentDescription = null, tint = WhitePure)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = TechDark)
            )
        },
        bottomBar = {
            if (cartItems.isNotEmpty()) {
                CartBottomBar(cartItems, onCheckoutClick)
            }
        },
        containerColor = TechDark
    ) { padding ->
        if (cartItems.isEmpty()) {
            EmptyCartView(modifier = Modifier.padding(padding))
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                items(cartItems) { item ->
                    CartItemRow(
                        item = item,
                        onIncrease = { viewModel.updateQuantity(item.id, 1) },
                        onDecrease = { viewModel.updateQuantity(item.id, -1) },
                        onRemove = { viewModel.removeFromCart(item.id) }
                    )
                }
            }
        }
    }
}

@Composable
fun CartItemRow(
    item: CartItem,
    onIncrease: () -> Unit,
    onDecrease: () -> Unit,
    onRemove: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = TechSlate),
        shape = RoundedCornerShape(16.dp)
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            AsyncImage(
                model = item.image,
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .size(80.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(TechDark)
            )
            
            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 12.dp)
            ) {
                Text(
                    text = item.name,
                    style = MaterialTheme.typography.bodyLarge.copy(
                        fontWeight = FontWeight.Bold,
                        color = WhitePure
                    ),
                    maxLines = 1
                )
                Text(
                    text = item.price,
                    style = MaterialTheme.typography.bodyMedium.copy(color = BluePrimary)
                )
                
                Row(
                    modifier = Modifier.padding(top = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    IconButton(
                        onClick = onDecrease,
                        modifier = Modifier
                            .size(28.dp)
                            .background(TechDark, RoundedCornerShape(4.dp))
                    ) {
                        Icon(Icons.Default.Remove, contentDescription = null, tint = WhitePure, modifier = Modifier.size(16.dp))
                    }
                    
                    Text(text = item.quantity.toString(), color = WhitePure, fontWeight = FontWeight.Bold)
                    
                    IconButton(
                        onClick = onIncrease,
                        modifier = Modifier
                            .size(28.dp)
                            .background(BluePrimary, RoundedCornerShape(4.dp))
                    ) {
                        Icon(Icons.Default.Add, contentDescription = null, tint = WhitePure, modifier = Modifier.size(16.dp))
                    }
                }
            }
            
            IconButton(onClick = onRemove) {
                Icon(Icons.Default.Delete, contentDescription = null, tint = Color.Red.copy(alpha = 0.7f))
            }
        }
    }
}

@Composable
fun EmptyCartView(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            Icons.Default.ShoppingCart,
            contentDescription = null,
            modifier = Modifier.size(100.dp),
            tint = TechGray.copy(alpha = 0.3f)
        )
        Spacer(Modifier.height(16.dp))
        Text("Giỏ hàng của bạn đang trống", color = TechGray)
    }
}

@Composable
fun CartBottomBar(items: List<CartItem>, onCheckoutClick: () -> Unit) {
    val total = items.sumOf { 
        val priceNum = it.price.replace("[^\\d]".toRegex(), "").toLongOrNull() ?: 0L
        priceNum * it.quantity
    }
    val formattedTotal = String.format("%,dđ", total).replace(",", ".")

    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = TechDark,
        border = androidx.compose.foundation.BorderStroke(1.dp, TechSlate)
    ) {
        Column(modifier = Modifier.padding(16.dp).navigationBarsPadding()) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Tổng thanh toán:", color = WhitePure)
                Text(
                    text = formattedTotal,
                    style = MaterialTheme.typography.titleLarge.copy(
                        color = BluePrimary,
                        fontWeight = FontWeight.Black
                    )
                )
            }
            Spacer(Modifier.height(16.dp))
            Button(
                onClick = onCheckoutClick,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = BluePrimary)
            ) {
                Text("Tiến hành thanh toán", fontWeight = FontWeight.Bold, modifier = Modifier.padding(vertical = 4.dp))
            }
        }
    }
}
