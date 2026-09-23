package com.nexora.player.ui.library

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nexora.player.data.repository.VideoRepository
import com.nexora.player.domain.model.HdrType
import com.nexora.player.domain.model.LibraryFilter
import com.nexora.player.domain.model.Resolution
import com.nexora.player.domain.model.SortOption
import com.nexora.player.domain.model.Video
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

/** One folder's worth of videos, in the "All Videos" folder-wise grouping. */
data class FolderSection(
    val path: String,
    val displayName: String,
    val videos: List<Video>,
)

data class LibraryUiState(
    val videos: List<Video> = emptyList(),
    val folderSections: List<FolderSection> = emptyList(),
    /** true only for the unfiltered "All Videos" screen — a single-folder view (opened
     * from Folders) always stays a flat grid, since a header would just repeat itself. */
    val groupByFolder: Boolean = false,
    val sortOption: SortOption = SortOption.RECENTLY_ADDED,
    val activeFilters: Set<LibraryFilter> = emptySet(),
    val isLoading: Boolean = true,
)

class LibraryViewModel(
    private val videoRepository: VideoRepository,
    initialFolderPath: String? = null,
) : ViewModel() {

    private val groupByFolder = initialFolderPath == null

    private val sortOption = MutableStateFlow(SortOption.RECENTLY_ADDED)
    private val activeFilters = MutableStateFlow<Set<LibraryFilter>>(emptySet())

    private val sourceFlow = if (initialFolderPath != null) {
        videoRepository.observeByFolder(initialFolderPath)
    } else {
        videoRepository.observeLibrary()
    }

    val uiState: StateFlow<LibraryUiState> = combine(
        sourceFlow,
        sortOption,
        activeFilters,
    ) { videos, sort, filters ->
        val sorted = applySortAndFilter(videos, sort, filters)
        LibraryUiState(
            videos = sorted,
            folderSections = if (groupByFolder) buildFolderSections(sorted) else emptyList(),
            groupByFolder = groupByFolder,
            sortOption = sort,
            activeFilters = filters,
            isLoading = false,
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), LibraryUiState())

    /** Groups the already sorted/filtered list by folder (order within each folder keeps
     * the user's chosen sort), then orders the folders themselves alphabetically so the
     * section order stays stable no matter which video sort is active. */
    private fun buildFolderSections(videos: List<Video>): List<FolderSection> =
        videos.groupBy { it.folderPath }
            .map { (path, videosInFolder) ->
                FolderSection(
                    path = path,
                    displayName = path.trimEnd('/').substringAfterLast('/').ifBlank { path },
                    videos = videosInFolder,
                )
            }
            .sortedBy { it.displayName.lowercase() }

    fun setSortOption(option: SortOption) {
        sortOption.value = option
    }

    fun toggleFilter(filter: LibraryFilter) {
        activeFilters.value = if (filter in activeFilters.value) {
            activeFilters.value - filter
        } else {
            activeFilters.value + filter
        }
    }

    fun toggleFavorite(video: Video) {
        viewModelScope.launch { videoRepository.toggleFavorite(video.id, !video.isFavorite) }
    }

    private fun applySortAndFilter(videos: List<Video>, sort: SortOption, filters: Set<LibraryFilter>): List<Video> {
        var result = videos
        if (filters.isNotEmpty()) {
            result = result.filter { video ->
                filters.all { filter ->
                    when (filter) {
                        LibraryFilter.ALL -> true
                        LibraryFilter.UHD_4K -> video.resolution == Resolution.UHD_4K
                        LibraryFilter.FHD_1080P -> video.resolution == Resolution.FHD_1080P
                        LibraryFilter.HDR -> video.hdrType != HdrType.NONE
                        LibraryFilter.FAVORITES -> video.isFavorite
                        LibraryFilter.WATCHED -> video.isCompleted
                        LibraryFilter.UNWATCHED -> video.watchProgress == null
                    }
                }
            }
        }
        return when (sort) {
            SortOption.RECENTLY_ADDED -> result.sortedByDescending { it.dateAddedEpochMs }
            SortOption.RECENTLY_PLAYED -> result.sortedByDescending { it.watchProgress?.lastPlayedAtEpochMs ?: 0 }
            SortOption.NAME -> result.sortedBy { it.filename.lowercase() }
            SortOption.DURATION -> result.sortedByDescending { it.durationMs }
            SortOption.FILE_SIZE -> result.sortedByDescending { it.sizeBytes }
            SortOption.RESOLUTION -> result.sortedByDescending { it.height }
        }
    }
}
