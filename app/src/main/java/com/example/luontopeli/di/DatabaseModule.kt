package com.example.luontopeli.di

import android.content.Context
import androidx.room.Room
import com.example.luontopeli.data.AppDatabase
import com.example.luontopeli.data.local.dao.NatureSpotDao
import com.example.luontopeli.data.local.dao.RoutePointDao
import com.example.luontopeli.data.local.dao.WalkSessionDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): AppDatabase {
        return Room.databaseBuilder(
            context,
            AppDatabase::class.java,
            "luontopeli_database"
        )
        .fallbackToDestructiveMigration()
        .build()
    }

    @Provides
    fun provideNatureSpotDao(database: AppDatabase): NatureSpotDao {
        return database.natureSpotDao()
    }

    @Provides
    fun provideWalkSessionDao(database: AppDatabase): WalkSessionDao {
        return database.walkSessionDao()
    }

    @Provides
    fun provideRoutePointDao(database: AppDatabase): RoutePointDao {
        return database.routePointDao()
    }
}
