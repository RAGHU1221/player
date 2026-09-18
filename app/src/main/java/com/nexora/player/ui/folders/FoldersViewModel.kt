package com.nexora.player.ui.folders

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nexora.player.data.database.entity.FolderEntity
import com.nexora.player.data.repository.VideoRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class FolderRow(val path: String, val displayName: String, val isManaged: Boolean, val managedEntity: FolderEntity?)

class FoldersViewModel(private val videoRepository: VideoRepository) : ViewModel() {

    val folders: StateFlow<List<FolderRow>> = combine(
        videoRepository.observeFolderPaths(),
        videoRepository.observeManagedFolders(),
    ) { mediaStoreFolders, managed ->
        val rows = mutableListOf<FolderRow>()
        mediaStoreFolders.forEach { path ->
            rows += FolderRow(path = path, displayName = path.trimEnd('/').substringAfterLast('/'), isManaged = false, managedEntity = null)
        }
        managed.forEach { folder ->
            rows += FolderRow(path = folder.uriString, displayName = folder.displayName, isManaged = true, managedEntity = folder)
        }
        rows.sortedBy { it.displayName.lowercase() }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun addSafFolder(uri: Uri) {
        viewModelScope.launch {
            videoRepository.addSafFolder(uri)
            videoRepository.scanLibrary()
        }
    }

    fun removeFolder(folder: FolderEntity) {
        viewModelScope.launch { videoRepository.removeFolder(folder) }
    }

    fun rescan() {
        viewModelScope.launch {
            videoRepository.scanLibrary()
            videoRepository.probeUnprocessedVideos()
        }
    }
}
