package com.nexora.player.domain.model

/** Detected HDR transfer/format, surfaced as a badge in the library and player. */
enum class HdrType(val label: String) {
    NONE("SDR"),
    HDR10("HDR10"),
    HDR10_PLUS("HDR10+"),
    HLG("HLG"),
    DOLBY_VISION("DV");

    companion object {
        fun fromStored(value: String): HdrType = entries.find { it.name == value } ?: NONE
    }
}

enum class Resolution(val label: String, val minHeight: Int) {
    UHD_4K("4K", 2000),
    QHD_1440P("1440P", 1350),
    FHD_1080P("1080P", 1000),
    HD_720P("720P", 650),
    SD_480P("480P", 0);

    companion object {
        /** Buckets a raw pixel height into the nearest marketing resolution label. */
        fun fromHeight(height: Int): Resolution =
            entries.firstOrNull { height >= it.minHeight } ?: SD_480P
    }
}

enum class AspectRatioMode(val label: String) {
    FIT("Fit"),
    FILL("Fill"),
    CROP("Crop"),
    RATIO_16_9("16:9"),
    RATIO_4_3("4:3"),
    ORIGINAL("Original"),
}

val PLAYBACK_SPEEDS = listOf(0.25f, 0.5f, 0.75f, 1.0f, 1.25f, 1.5f, 1.75f, 2.0f, 2.5f, 3.0f)

enum class SortOption(val label: String) {
    RECENTLY_ADDED("Recently Added"),
    RECENTLY_PLAYED("Recently Played"),
    NAME("Name"),
    DURATION("Duration"),
    FILE_SIZE("File Size"),
    RESOLUTION("Resolution"),
}

enum class LibraryFilter(val label: String) {
    ALL("All"),
    UHD_4K("4K"),
    FHD_1080P("1080P"),
    HDR("HDR"),
    FAVORITES("Favorites"),
    WATCHED("Watched"),
    UNWATCHED("Unwatched"),
}
