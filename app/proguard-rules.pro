# Nexora Player release rules

# Keep Media3 / ExoPlayer public API surface used via reflection for extractors & codecs.
-keep class androidx.media3.** { *; }
-dontwarn androidx.media3.**

# Room
-keep class androidx.room.** { *; }
-keep @androidx.room.Entity class * { *; }
-dontwarn androidx.room.paging.**

# Keep data/domain models (Room entities, Parcelable-ish nav args) from being renamed
# in ways that break reflection-based serialization.
-keep class com.nexora.player.data.database.entity.** { *; }
-keep class com.nexora.player.domain.model.** { *; }

# Kotlin coroutines
-dontwarn kotlinx.coroutines.**

# Compose keeps handled by AGP/Compose compiler defaults; nothing extra needed.
