package com.myspy.android.data.local.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.myspy.android.data.local.db.dao.*
import com.myspy.android.data.local.db.entities.*

/**
 * App Database
 * @author Michael KOJDL
 * @version 1.0.0
 */
@Database(
    entities = [
        SmsEntity::class,
        CallEntity::class,
        LocationEntity::class,
        AppEntity::class,
        BrowserHistoryEntity::class,
        MediaEntity::class,
        ScreenshotEntity::class
    ],
    version = 2,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun smsDao(): SmsDao
    abstract fun callsDao(): CallsDao
    abstract fun locationDao(): LocationDao
    abstract fun appsDao(): AppsDao
    abstract fun browserHistoryDao(): BrowserHistoryDao
    abstract fun mediaDao(): MediaDao
    abstract fun screenshotDao(): ScreenshotDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "myspy_database"
                )
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
