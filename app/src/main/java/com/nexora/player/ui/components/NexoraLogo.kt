package com.nexora.player.ui.components

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.nexora.player.R
import com.nexora.player.ui.theme.nexoraColors

@Composable
fun NexoraLogo(modifier: Modifier = Modifier, showWordmark: Boolean = true) {
    Row(modifier = modifier, verticalAlignment = Alignment.CenterVertically) {
        androidx.compose.foundation.Image(
            painter = painterResource(R.drawable.ic_nexora_logo),
            contentDescription = "Nexora Player",
            modifier = Modifier.size(28.dp),
        )
        if (showWordmark) {
            androidx.compose.foundation.layout.Spacer(Modifier.width(8.dp))
            Text(
                text = "NEXORA",
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.nexoraColors.textPrimary,
            )
        }
    }
}
