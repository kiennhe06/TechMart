import androidx.compose.material3.*
import androidx.compose.runtime.Composable
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun Test() {
    ExposedDropdownMenuBox(expanded = true, onExpandedChange = {}) {
        // Can we pass properties?
    }
}
