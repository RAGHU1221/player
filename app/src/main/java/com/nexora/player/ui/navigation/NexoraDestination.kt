package com.nexora.player.ui.navigation

/** Every screen in the app, as a typed route builder rather than raw strings scattered across the codebase. */
sealed class NexoraDestination(val route: String) {
    data object Home : NexoraDestination("home")
    data object Library : NexoraDestination("library")
    data object Folders : NexoraDestination("folders")
    data object FolderContents : NexoraDestination("folder/{folderPath}") {
        fun createRoute(folderPath: String) = "folder/${java.net.URLEncoder.encode(folderPath, "UTF-8")}"
        const val ARG_FOLDER_PATH = "folderPath"
    }
    data object Playlists : NexoraDestination("playlists")
    data object PlaylistDetail : NexoraDestination("playlist/{playlistId}") {
        fun createRoute(playlistId: Long) = "playlist/$playlistId"
        const val ARG_PLAYLIST_ID = "playlistId"
    }
    data object Favorites : NexoraDestination("favorites")
    data object History : NexoraDestination("history")
    data object Search : NexoraDestination("search")
    data object Settings : NexoraDestination("settings")
    data object VideoDetails : NexoraDestination("video/{videoId}/details") {
        fun createRoute(videoId: Long) = "video/$videoId/details"
        const val ARG_VIDEO_ID = "videoId"
    }
    data object Player : NexoraDestination("video/{videoId}/play") {
        fun createRoute(videoId: Long) = "video/$videoId/play"
        const val ARG_VIDEO_ID = "videoId"
    }

    companion object {
        /** Routes that show the bottom nav + mini player chrome. */
        val TOP_LEVEL_ROUTES = setOf(Home.route, Library.route, Folders.route, Playlists.route, Settings.route)
    }
}
