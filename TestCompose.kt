import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.window.PopupProperties

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun Test() {
    ExposedDropdownMenuBox(expanded = true, onExpandedChange = {}) {
        ExposedDropdownMenu(
            expanded = true,
            onDismissRequest = {},
            properties = PopupProperties(focusable = false)
        ) {}
    }
}
