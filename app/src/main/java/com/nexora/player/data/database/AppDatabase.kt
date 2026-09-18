package com.nexora.player.data.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import com.nexora.player.data.database.dao.FavoriteDao
import com.nexora.player.data.database.dao.FolderDao
import com.nexora.player.data.database.dao.PlaybackSettingsDao
import com.nexora.player.data.database.dao.PlaylistDao
import com.nexora.player.data.database.dao.VideoDao
import com.nexora.player.data.database.dao.WatchHistoryDao
import com.nexora.player.data.database.entity.FavoriteEntity
import com.nexora.player.data.database.entity.FolderEntity
import com.nexora.player.data.database.entity.PlaybackSettingsEntity
import com.nexora.player.data.database.entity.PlaylistEntity
import com.nexora.player.data.database.entity.PlaylistItemEntity
import com.nexora.player.data.database.entity.VideoEntity
import com.nexora.player.data.database.entity.WatchHistoryEntity

@Database(
    entities = [
        VideoEntity::class,
        WatchHistoryEntity::class,
        FavoriteEntity::class,
        PlaylistEntity::class,
        PlaylistItemEntity::class,
        FolderEntity::class,
        PlaybackSettingsEntity::class,
    ],
    version = 1,
    exportSchema = true,
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun videoDao(): VideoDao
    abstract fun watchHistoryDao(): WatchHistoryDao
    abstract fun favoriteDao(): FavoriteDao
    abstract fun playlistDao(): PlaylistDao
    abstract fun folderDao(): FolderDao
    abstract fun playbackSettingsDao(): PlaybackSettingsDao

    companion object {
        private const val DB_NAME = "nexora_player.db"

        @Volatile
        private var instance: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase =
            instance ?: synchronized(this) {
                instance ?: build(context).also { instance = it }
            }

        private fun build(context: Context): AppDatabase =
            Room.databaseBuilder(context.applicationContext, AppDatabase::class.java, DB_NAME)
                .addMigrations(*ALL_MIGRATIONS)
                // Room throws instead of silently wiping data if a future version ships
                // without a matching migration in ALL_MIGRATIONS below.
                .build()

        /**
         * Real migrations are added here as the schema evolves, e.g.:
         *
         * ```
         * val MIGRATION_1_2 = object : Migration(1, 2) {
         *     override fun migrate(db: SupportSQLiteDatabase) {
         *         db.execSQL("ALTER TABLE videos ADD COLUMN dolbyVisionProfile INTEGER NOT NULL DEFAULT 0")
         *     }
         * }
         * ```
         *
         * and then appended to this array. Version 1 (current) has no predecessor,
         * so the array starts empty rather than shipping a fake no-op migration.
         */
        private val ALL_MIGRATIONS: Array<Migration> = arrayOf()
    }
}
