package com.nexora.player.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FolderOpen
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.PlaylistPlay
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.VideoLibrary
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import com.nexora.player.ui.navigation.NexoraDestination
import com.nexora.player.ui.theme.NexoraRadius
import com.nexora.player.ui.theme.nexoraColors

data class BottomNavItem(val destination: NexoraDestination, val label: String, val icon: ImageVector)

val BOTTOM_NAV_ITEMS = listOf(
    BottomNavItem(NexoraDestination.Home, "Home", Icons.Filled.Home),
    BottomNavItem(NexoraDestination.Library, "Videos", Icons.Filled.VideoLibrary),
    BottomNavItem(NexoraDestination.Folders, "Folders", Icons.Filled.FolderOpen),
    BottomNavItem(NexoraDestination.Playlists, "Playlists", Icons.Filled.PlaylistPlay),
    BottomNavItem(NexoraDestination.Settings, "Settings", Icons.Filled.Settings),
)

@Composable
fun PremiumBottomNav(
    currentRoute: String?,
    onNavigate: (NexoraDestination) -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = MaterialTheme.nexoraColors
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 10.dp)
            .neumorphic(cornerRadius = NexoraRadius.Pill)
            .clip(RoundedCornerShape(NexoraRadius.Pill))
            .background(colors.surface)
            .padding(vertical = 10.dp, horizontal = 6.dp),
        horizontalArrangement = Arrangement.SpaceEvenly,
    ) {
        BOTTOM_NAV_ITEMS.forEach { item ->
            val selected = currentRoute == item.destination.route
            NavItem(item = item, selected = selected, onClick = { onNavigate(item.destination) })
        }
    }
}

@Composable
private fun NavItem(item: BottomNavItem, selected: Boolean, onClick: () -> Unit) {
    val colors = MaterialTheme.nexoraColors
    val interactionSource = remember { MutableInteractionSource() }
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .clickable(indication = null, interactionSource = interactionSource, onClick = onClick)
            .padding(horizontal = 6.dp, vertical = 2.dp),
    ) {
        // Selected tab reads as a pressed-in ("inset") neumorphic pill behind the
        // icon — the classic soft-UI way to show an active control, in place of
        // a flat accent-colored underline.
        Box(
            modifier = Modifier
                .size(40.dp)
                .then(
                    if (selected) {
                        Modifier
                            .neumorphic(cornerRadius = NexoraRadius.Pill, elevation = 4.dp, pressed = true)
                            .clip(RoundedCornerShape(NexoraRadius.Pill))
                            .background(colors.surface)
                    } else {
                        Modifier
                    },
                ),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                item.icon,
                contentDescription = item.label,
                tint = if (selected) colors.accent else colors.textTertiary,
                modifier = Modifier.size(22.dp),
            )
        }
        Text(
            text = item.label,
            style = MaterialTheme.typography.labelMedium,
            color = if (selected) colors.textPrimary else colors.textTertiary,
        )
    }
}
