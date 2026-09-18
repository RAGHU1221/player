package com.nexora.player.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.VideoLibrary
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import com.nexora.player.ui.theme.nexoraColors

/** Used by every screen instead of a blank/broken view when there's nothing to show yet (section 34/35). */
@Composable
fun EmptyState(
    title: String,
    subtitle: String,
    modifier: Modifier = Modifier,
    icon: ImageVector = Icons.Outlined.VideoLibrary,
    primaryActionLabel: String? = null,
    onPrimaryAction: (() -> Unit)? = null,
    secondaryActionLabel: String? = null,
    onSecondaryAction: (() -> Unit)? = null,
) {
    val colors = MaterialTheme.nexoraColors
    Column(
        modifier = modifier.padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        GlassCard(cornerRadius = 100.dp) {
            Icon(
                icon,
                contentDescription = null,
                tint = colors.textSecondary,
                modifier = Modifier.padding(28.dp),
            )
        }
        androidx.compose.foundation.layout.Spacer(Modifier.padding(top = 20.dp))
        Text(title, style = MaterialTheme.typography.titleLarge, color = colors.textPrimary)
        androidx.compose.foundation.layout.Spacer(Modifier.padding(top = 6.dp))
        Text(
            subtitle,
            style = MaterialTheme.typography.bodyMedium,
            color = colors.textSecondary,
            textAlign = androidx.compose.ui.text.style.TextAlign.Center,
        )
        if (primaryActionLabel != null || secondaryActionLabel != null) {
            androidx.compose.foundation.layout.Spacer(Modifier.padding(top = 20.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                if (primaryActionLabel != null && onPrimaryAction != null) {
                    Button(
                        onClick = onPrimaryAction,
                        colors = ButtonDefaults.buttonColors(containerColor = colors.accent, contentColor = Color.White),
                    ) { Text(primaryActionLabel) }
                }
                if (secondaryActionLabel != null && onSecondaryAction != null) {
                    OutlinedButton(onClick = onSecondaryAction) { Text(secondaryActionLabel, color = colors.textPrimary) }
                }
            }
        }
    }
}
