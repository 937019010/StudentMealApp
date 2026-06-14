# Add project specific ProGuard rules here.
-keepattributes *Annotation*
-keepattributes SourceFile,LineNumberTable
-keep public class * extends java.lang.Exception

# Room
-keep class * extends androidx.room.RoomDatabase
-keep @androidx.room.Entity class *
-dontwarn androidx.room.paging.**

# Apache POI
-keep class org.apache.poi.** { *; }
-keep class org.apache.xmlbeans.** { *; }
-dontwarn org.apache.xmlbeans.**
-dontwarn org.apache.poi.**
-dontwarn org.apache.logging.log4j.**

# OpenCSV
-keep class com.opencsv.** { *; }
-dontwarn com.opencsv.**
