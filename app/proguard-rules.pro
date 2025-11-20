
 # Add project specific ProGuard rules here.
# You can control the set of applied configuration files using the
# proguardFiles setting in build.gradle.
#
# For more details, see
#   http://developer.android.com/guide/developing/tools/proguard.html

# If your project uses WebView with JS, uncomment the following
# and specify the fully qualified class name to the JavaScript interface
# class:
#-keepclassmembers class fqcn.of.javascript.interface.for.webview {
#   public *;
#}

# Uncomment this to preserve the line number information for
# debugging stack traces.
#-keepattributes SourceFile,LineNumberTable

# If you keep the line number information, uncomment this to
# hide the original source file name.
#-renamesourcefileattribute SourceFile
#---------------------------
# Room Database
#---------------------------
-keep class androidx.room.** { *; }
-keepattributes *Annotation*

# Garder les classes de données de ton projet
-keep class com.example.projetn2.model.** { *; }
-keepclassmembers class com.example.projetn2.model.** { *; }

# Si tu utilises des DAO
-keep interface com.example.projetn2.data.** { *; }

#---------------------------
# MPAndroidChart
#---------------------------
-keep class com.github.mikephil.charting.** { *; }

#---------------------------
# Kotlin
#---------------------------
-keepclassmembers class kotlin.Metadata { *; }

#---------------------------
# ViewBinding / DataBinding
#---------------------------
-keep class androidx.databinding.** { *; }

#---------------------------
# WorkManager
#---------------------------
-keep class androidx.work.** { *; }

#---------------------------
# Notifications / BroadcastReceiver
#---------------------------
-keep class com.example.projetn2.receiver.** { *; }

#---------------------------
# Gson / JSON (si utilisé)
#---------------------------
-keep class com.example.projetn2.data.** { *; }
-keepclassmembers class com.example.projetn2.data.** { *; }

#---------------------------
# Général / Activités et Fragments
#---------------------------
-keep class com.example.projetn2.ui.** { *; }
-keepclassmembers class com.example.projetn2.ui.** { *; }

#---------------------------
# Règles supplémentaires pour éviter les crashs
#---------------------------
-dontwarn kotlin.**
-dontwarn androidx.**
-dontwarn com.github.mikephil.charting.**