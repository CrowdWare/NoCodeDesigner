package at.crowdware.nocodedesigner.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.RadioButton
import androidx.compose.material.RadioButtonDefaults
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import at.crowdware.nocodedesigner.theme.ExtendedTheme

@Composable
fun RadioButtonItem(modifier: Modifier = Modifier, label: String, selected: Boolean, onClick: () -> Unit) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier
            .fillMaxWidth()
            .clickable { onClick() }
    ) {
        RadioButton(
            selected = selected,
            onClick = onClick,
            colors = RadioButtonDefaults.colors(selectedColor = ExtendedTheme.colors.accentColor)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(text = label)
    }
}