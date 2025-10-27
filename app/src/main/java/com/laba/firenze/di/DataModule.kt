package com.laba.firenze.di

import android.content.Context
import com.laba.firenze.data.NotificationManager
import com.laba.firenze.data.TopicManager
import com.laba.firenze.data.repository.LessonCalendarRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DataModule {
    
    @Provides
    @Singleton
    fun provideTopicManager(
        @ApplicationContext context: Context
    ): TopicManager {
        return TopicManager(context)
    }
    
    @Provides
    @Singleton
    fun provideLessonCalendarRepository(
        @ApplicationContext context: Context
    ): LessonCalendarRepository {
        return LessonCalendarRepository(context)
    }
}

