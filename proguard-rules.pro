# Add project specific ProGuard rules here.
# You can control the set of applied configuration files using the
# proguardFiles setting in build.gradle.

# Keep Room entities
-keep class com.qrbarcode.studio.data.** { *; }

# Keep ZXing
-keep class com.google.zxing.** { *; }

# Keep ML Kit
-keep class com.google.mlkit.** { *; }

# Keep CameraX
-keep class androidx.camera.** { *; }
