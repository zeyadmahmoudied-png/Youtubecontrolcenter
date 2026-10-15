package com.youtubecc.app.data

import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

data class ChannelInfo(
    val id: String,
    val title: String,
    val handle: String,
    val description: String,
    val thumbnail: String,
    val subscribers: Long,
    val views: Long,
    val videos: Long,
    val publishedAt: String
)

data class VideoInfo(
    val id: String,
    val title: String,
    val channelTitle: String,
    val channelId: String,
    val thumbnail: String,
    val publishedAt: String,
    val views: Long,
    val likes: Long,
    val comments: Long
)

class YouTubeRepository(private val prefs: PreferencesManager) {
    private val api: YouTubeApi by lazy {
        val log = HttpLoggingInterceptor().apply { level = HttpLoggingInterceptor.Level.NONE }
        val client = OkHttpClient.Builder()
            .connectTimeout(20, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .addInterceptor(log)
            .build()
        Retrofit.Builder()
            .baseUrl("https://www.googleapis.com/youtube/v3/")
            .client(client)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(YouTubeApi::class.java)
    }

    private fun LongStr(s: String?) = s?.toLongOrNull() ?: 0L

    suspend fun resolveChannel(input: String): ChannelInfo {
        val key = prefs.getApiKey().ifBlank {
            throw IllegalStateException("أدخل مفتاح YouTube Data API من الإعدادات")
        }
        val q = input.trim()
        val resp = when {
            q.startsWith("UC") && q.length >= 20 ->
                api.channels(id = q, key = key)
            q.startsWith("@") ->
                api.channels(forHandle = q.removePrefix("@"), key = key)
            q.contains("youtube.com") || q.contains("youtu.be") -> {
                val handle = Regex("@([\\w.-]+)").find(q)?.groupValues?.getOrNull(1)
                val id = Regex("(channel/)(UC[\\w-]+)").find(q)?.groupValues?.getOrNull(2)
                when {
                    id != null -> api.channels(id = id, key = key)
                    handle != null -> api.channels(forHandle = handle, key = key)
                    else -> throw IllegalArgumentException("رابط غير معروف")
                }
            }
            else -> api.channels(forHandle = q.removePrefix("@"), key = key)
        }
        val item = resp.items?.firstOrNull()
            ?: throw IllegalArgumentException("لم يتم العثور على القناة")
        val sn = item.snippet
        val st = item.statistics
        return ChannelInfo(
            id = item.id ?: "",
            title = sn?.title ?: "",
            handle = sn?.customUrl ?: "",
            description = sn?.description ?: "",
            thumbnail = sn?.thumbnails?.high?.url ?: sn?.thumbnails?.medium?.url ?: "",
            subscribers = if (st?.hiddenSubscriberCount == true) -1 else LongStr(st?.subscriberCount),
            views = LongStr(st?.viewCount),
            videos = LongStr(st?.videoCount),
            publishedAt = sn?.publishedAt ?: ""
        )
    }

    suspend fun channelVideos(channelId: String, max: Int = 15): List<VideoInfo> {
        val key = prefs.getApiKey().ifBlank { throw IllegalStateException("مفتاح API مطلوب") }
        val search = api.search(channelId = channelId, order = "date", maxResults = max, key = key)
        val ids = search.items?.mapNotNull { it.id?.videoId }.orEmpty()
        if (ids.isEmpty()) return emptyList()
        val details = api.videos(id = ids.joinToString(","), key = key)
        return details.items.orEmpty().map { v ->
            VideoInfo(
                id = v.id ?: "",
                title = v.snippet?.title ?: "",
                channelTitle = v.snippet?.channelTitle ?: "",
                channelId = v.snippet?.channelId ?: "",
                thumbnail = v.snippet?.thumbnails?.medium?.url ?: "",
                publishedAt = v.snippet?.publishedAt ?: "",
                views = LongStr(v.statistics?.viewCount),
                likes = LongStr(v.statistics?.likeCount),
                comments = LongStr(v.statistics?.commentCount)
            )
        }
    }

    suspend fun trending(region: String = "EG"): List<VideoInfo> {
        val key = prefs.getApiKey().ifBlank { throw IllegalStateException("مفتاح API مطلوب") }
        val resp = api.chart(regionCode = region, key = key)
        return resp.items.orEmpty().map { v ->
            VideoInfo(
                id = v.id ?: "",
                title = v.snippet?.title ?: "",
                channelTitle = v.snippet?.channelTitle ?: "",
                channelId = v.snippet?.channelId ?: "",
                thumbnail = v.snippet?.thumbnails?.medium?.url ?: "",
                publishedAt = v.snippet?.publishedAt ?: "",
                views = LongStr(v.statistics?.viewCount),
                likes = LongStr(v.statistics?.likeCount),
                comments = LongStr(v.statistics?.commentCount)
            )
        }
    }

    suspend fun searchVideos(query: String): List<VideoInfo> {
        val key = prefs.getApiKey().ifBlank { throw IllegalStateException("مفتاح API مطلوب") }
        val search = api.search(q = query, order = "viewCount", maxResults = 15, key = key)
        val ids = search.items?.mapNotNull { it.id?.videoId }.orEmpty()
        if (ids.isEmpty()) return emptyList()
        val details = api.videos(id = ids.joinToString(","), key = key)
        return details.items.orEmpty().map { v ->
            VideoInfo(
                id = v.id ?: "",
                title = v.snippet?.title ?: "",
                channelTitle = v.snippet?.channelTitle ?: "",
                channelId = v.snippet?.channelId ?: "",
                thumbnail = v.snippet?.thumbnails?.medium?.url ?: "",
                publishedAt = v.snippet?.publishedAt ?: "",
                views = LongStr(v.statistics?.viewCount),
                likes = LongStr(v.statistics?.likeCount),
                comments = LongStr(v.statistics?.commentCount)
            )
        }
    }
}
