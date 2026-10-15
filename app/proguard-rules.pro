# Keep YouTube API models
-keep class com.youtubecc.app.data.model.** { *; }
-keep class com.google.api.services.youtube.** { *; }
-keep class com.google.api.services.youtubeAnalytics.** { *; }

# Retrofit / Gson
-keepattributes Signature
-keepattributes *Annotation*
-dontwarn okhttp3.**
-dontwarn retrofit2.**
