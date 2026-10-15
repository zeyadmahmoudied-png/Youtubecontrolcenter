package com.youtubecc.app.data

import retrofit2.http.GET
import retrofit2.http.Query

interface YouTubeApi {
    @GET("channels")
    suspend fun channels(
        @Query("part") part: String = "snippet,statistics,contentDetails",
        @Query("id") id: String? = null,
        @Query("forHandle") forHandle: String? = null,
        @Query("key") key: String
    ): ChannelsResponse

    @GET("search")
    suspend fun search(
        @Query("part") part: String = "snippet",
        @Query("q") q: String? = null,
        @Query("channelId") channelId: String? = null,
        @Query("type") type: String = "video",
        @Query("order") order: String = "viewCount",
        @Query("maxResults") maxResults: Int = 15,
        @Query("regionCode") regionCode: String? = null,
        @Query("key") key: String
    ): SearchResponse

    @GET("videos")
    suspend fun videos(
        @Query("part") part: String = "snippet,statistics,contentDetails",
        @Query("id") id: String,
        @Query("key") key: String
    ): VideosResponse

    @GET("videos")
    suspend fun chart(
        @Query("part") part: String = "snippet,statistics",
        @Query("chart") chart: String = "mostPopular",
        @Query("regionCode") regionCode: String = "EG",
        @Query("maxResults") maxResults: Int = 15,
        @Query("key") key: String
    ): VideosResponse
}

data class ChannelsResponse(val items: List<ChannelItem>?)
data class ChannelItem(
    val id: String?,
    val snippet: ChannelSnippet?,
    val statistics: ChannelStatistics?
)
data class ChannelSnippet(
    val title: String?,
    val description: String?,
    val customUrl: String?,
    val thumbnails: Thumbnails?,
    val publishedAt: String?
)
data class ChannelStatistics(
    val viewCount: String?,
    val subscriberCount: String?,
    val videoCount: String?,
    val hiddenSubscriberCount: Boolean?
)
data class SearchResponse(val items: List<SearchItem>?)
data class SearchItem(val id: SearchId?, val snippet: VideoSnippet?)
data class SearchId(val videoId: String?)
data class VideosResponse(val items: List<VideoItem>?)
data class VideoItem(
    val id: String?,
    val snippet: VideoSnippet?,
    val statistics: VideoStatistics?,
    val contentDetails: ContentDetails?
)
data class VideoSnippet(
    val title: String?,
    val description: String?,
    val channelTitle: String?,
    val channelId: String?,
    val publishedAt: String?,
    val thumbnails: Thumbnails?
)
data class VideoStatistics(
    val viewCount: String?,
    val likeCount: String?,
    val commentCount: String?
)
data class ContentDetails(val duration: String?)
data class Thumbnails(
    val default: Thumb?,
    val medium: Thumb?,
    val high: Thumb?
)
data class Thumb(val url: String?)
