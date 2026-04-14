package com.example.luontopeli.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.luontopeli.data.local.dao.NatureSpotDao
import com.example.luontopeli.data.local.dao.RoutePointDao
import com.example.luontopeli.data.local.dao.WalkSessionDao
import com.example.luontopeli.data.local.entity.NatureSpot
import com.example.luontopeli.data.local.entity.RoutePoint
import com.example.luontopeli.data.local.entity.WalkSession

@Database(
    entities = [NatureSpot::class, WalkSession::class, RoutePoint::class],
    version = 3,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun natureSpotDao(): NatureSpotDao
    abstract fun walkSessionDao(): WalkSessionDao
    abstract fun routePointDao(): RoutePointDao

    companion object {
        @Volatile 
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "luontopeli_database"
                )
                    .fallbackToDestructiveMigration()
                    .build().also { INSTANCE = it }
            }
        }
    }
}
