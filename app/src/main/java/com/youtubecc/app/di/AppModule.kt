package com.youtubecc.app.di

import android.content.Context
import androidx.room.Room
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.youtubecc.app.data.api.YouTubeApiService
import com.youtubecc.app.data.local.*
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideGson(): Gson = GsonBuilder().create()

    @Provides
    @Singleton
    fun provideOkHttpClient(): OkHttpClient {
        val logging = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.NONE
        }
        return OkHttpClient.Builder()
            .addInterceptor(logging)
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .build()
    }

    @Provides
    @Singleton
    fun provideYouTubeApi(client: OkHttpClient, gson: Gson): YouTubeApiService {
        return Retrofit.Builder()
            .baseUrl("https://www.googleapis.com/youtube/v3/")
            .client(client)
            .addConverterFactory(GsonConverterFactory.create(gson))
            .build()
            .create(YouTubeApiService::class.java)
    }

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): AppDatabase {
        return Room.databaseBuilder(
            context,
            AppDatabase::class.java,
            "youtube_cc.db"
        ).build()
    }

    @Provides fun provideCompetitorDao(db: AppDatabase): CompetitorDao = db.competitorDao()
    @Provides fun provideSavedVideoDao(db: AppDatabase): SavedVideoDao = db.savedVideoDao()
    @Provides fun provideCacheDao(db: AppDatabase): CacheDao = db.cacheDao()
    @Provides fun provideOpportunityDao(db: AppDatabase): OpportunityDao = db.opportunityDao()
    @Provides fun provideVideoProjectDao(db: AppDatabase): VideoProjectDao = db.videoProjectDao()
    @Provides fun provideTitleVariantDao(db: AppDatabase): TitleVariantDao = db.titleVariantDao()
    @Provides fun provideDescriptionVariantDao(db: AppDatabase): DescriptionVariantDao = db.descriptionVariantDao()
    @Provides fun provideThumbnailVariantDao(db: AppDatabase): ThumbnailVariantDao = db.thumbnailVariantDao()
    @Provides fun provideProductionTaskDao(db: AppDatabase): ProductionTaskDao = db.productionTaskDao()
    @Provides fun provideViewSnapshotDao(db: AppDatabase): ViewSnapshotDao = db.viewSnapshotDao()
    @Provides fun provideCompetitorNoteDao(db: AppDatabase): CompetitorNoteDao = db.competitorNoteDao()
    @Provides fun provideSavedVideoMetaDao(db: AppDatabase): SavedVideoMetaDao = db.savedVideoMetaDao()
    @Provides fun provideRecommendationDao(db: AppDatabase): RecommendationDao = db.recommendationDao()
    @Provides fun provideChannelSnapshotDao(db: AppDatabase): ChannelSnapshotDao = db.channelSnapshotDao()
    @Provides fun provideAnalyzedChannelDao(db: AppDatabase): AnalyzedChannelDao = db.analyzedChannelDao()
}
