# Standard Android rules are included via getDefaultProguardFile

# NewPipe / Rhino missing dependencies
-dontwarn java.beans.**

# Media3 / ExoPlayer
-keep class androidx.media3.common.** { *; }
-keep class androidx.media3.exoplayer.** { *; }
-keep class androidx.media3.session.** { *; }

# Coil
-keep class coil.** { *; }

# NewPipe Extractor
-keep class org.schabi.newpipe.extractor.** { *; }

# Kotlin Coroutines
-keepnames class kotlinx.coroutines.internal.MainDispatcherFactory {}
-keepnames class kotlinx.coroutines.CoroutineExceptionHandler {}
-keepnames class kotlinx.coroutines.android.AndroidExceptionPreHandler {}
-keepnames class kotlinx.coroutines.android.AndroidDispatcherFactory {}

# WorkManager
-keep class androidx.work.** { *; }

# JSON
-keep class org.json.** { *; }

# Preserve Zubeen FM Models for Serialization
-keep class com.amairatech.zubeenfm.data.model.** { *; }
