package com.nexora.player.ui.folders

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.FolderOff
import androidx.compose.material.icons.filled.CreateNewFolder
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.nexora.player.ui.components.EmptyState
import com.nexora.player.ui.components.GlassCard
import com.nexora.player.ui.theme.nexoraColors
import com.nexora.player.util.GenericViewModelFactory
import com.nexora.player.util.nexoraApp

@Composable
fun FoldersScreen(onFolderClick: (path: String) -> Unit) {
    val context = LocalContext.current
    val app = context.nexoraApp()
    val viewModel: FoldersViewModel = viewModel(factory = GenericViewModelFactory { FoldersViewModel(app.videoRepository) })
    val folders by viewModel.folders.collectAsState()
    val colors = MaterialTheme.nexoraColors

    val pickFolderLauncher = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocumentTree()) { uri ->
        uri?.let { viewModel.addSafFolder(it) }
    }

    Scaffold(
        containerColor = Color.Transparent,
        floatingActionButton = {
            FloatingActionButton(onClick = { pickFolderLauncher.launch(null) }, containerColor = colors.accent) {
                Icon(Icons.Filled.CreateNewFolder, contentDescription = "Choose Folder", tint = Color.White)
            }
        },
    ) { padding ->
        Column(modifier = Modifier.fillMaxSize().padding(padding)) {
            Text(
                "Folders",
                style = MaterialTheme.typography.headlineMedium,
                color = colors.textPrimary,
                modifier = Modifier.padding(horizontal = 20.dp, vertical = 16.dp),
            )
            if (folders.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    EmptyState(
                        title = "No folders yet",
                        subtitle = "Choose a folder to include it in your library",
                        icon = Icons.Filled.FolderOff,
                        primaryActionLabel = "Choose Folder",
                        onPrimaryAction = { pickFolderLauncher.launch(null) },
                    )
                }
            } else {
                LazyColumn(contentPadding = PaddingValues(horizontal = 16.dp, vertical = 4.dp), modifier = Modifier.fillMaxSize()) {
                    items(folders, key = { it.path }) { folder ->
                        GlassCard(modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp)) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(14.dp),
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                Icon(Icons.Filled.Folder, contentDescription = null, tint = colors.accent)
                                Column(
                                    modifier = Modifier
                                        .weight(1f)
                                        .padding(start = 12.dp)
                                        .clickable { onFolderClick(folder.path) },
                                ) {
                                    Text(folder.displayName, style = MaterialTheme.typography.titleMedium, color = colors.textPrimary)
                                    if (folder.isManaged) {
                                        Text("Custom folder", style = MaterialTheme.typography.bodyMedium, color = colors.textTertiary)
                                    }
                                }
                                val managedEntity = folder.managedEntity
                                if (folder.isManaged && managedEntity != null) {
                                    IconButton(onClick = { viewModel.removeFolder(managedEntity) }) {
                                        Icon(Icons.Filled.Delete, contentDescription = "Remove", tint = colors.textSecondary)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
