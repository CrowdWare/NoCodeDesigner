package at.crowdware.nocodedesigner.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import at.crowdware.nocodedesigner.theme.ExtendedTheme

@Composable
fun CheckboxItem(modifier: Modifier = Modifier, label: String, checked: Boolean, onCheckedChange: (Boolean) -> Unit) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier
            .fillMaxWidth()
            .clickable { onCheckedChange(checked) }
    ) {
        Checkbox(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = CheckboxDefaults.colors(checkedColor = ExtendedTheme.colors.accentColor)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(text = label)
    }
}