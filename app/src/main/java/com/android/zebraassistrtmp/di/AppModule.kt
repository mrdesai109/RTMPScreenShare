package com.android.zebraassistrtmp.di

import android.content.Context
import android.media.projection.MediaProjectionManager
import androidx.core.content.ContextCompat
import com.android.zebraassistrtmp.util.SharedPreferencesUtil
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@InstallIn(SingletonComponent::class)
@Module
object AppModule {

    @Singleton
    @Provides
    fun providesMediaProjectionManager(@ApplicationContext context: Context): MediaProjectionManager =
        ContextCompat.getSystemService(context, MediaProjectionManager::class.java)!!

    @Singleton
    @Provides
    fun providesSharedPreferencesUtil(@ApplicationContext context: Context): SharedPreferencesUtil =
        SharedPreferencesUtil(context)
}