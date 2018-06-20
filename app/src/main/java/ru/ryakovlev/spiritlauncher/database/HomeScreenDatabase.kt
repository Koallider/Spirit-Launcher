package ru.ryakovlev.spiritlauncher.database

import android.arch.persistence.room.Database
import android.arch.persistence.room.Room
import android.arch.persistence.room.RoomDatabase
import android.arch.persistence.room.TypeConverters
import android.content.Context
import ru.ryakovlev.spiritlauncher.dao.HomeScreenIconDao
import ru.ryakovlev.spiritlauncher.domain.HomeScreenIcon
import ru.ryakovlev.spiritlauncher.util.StringListConverters

/**
 * Created by roma on 05.06.2018.
 */
@Database(entities = arrayOf(HomeScreenIcon::class), version = 1)
@TypeConverters(value = StringListConverters::class)
abstract class HomeScreenDatabase : RoomDatabase() {

    abstract fun homeScreenIconDao(): HomeScreenIconDao

    companion object {
        private var INSTANCE: HomeScreenDatabase? = null

        fun getInstance(context: Context): HomeScreenDatabase? {
            if (INSTANCE == null) {
                synchronized(HomeScreenDatabase::class) {
                    INSTANCE = Room.databaseBuilder(context.getApplicationContext(),
                            HomeScreenDatabase::class.java, "homeScreen.db")
                            .build()
                }
            }
            return INSTANCE
        }

        fun destroyInstance() {
            INSTANCE = null
        }
    }
}