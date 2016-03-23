# Add project specific ProGuard rules here.
# By default, the flags in this file are appended to flags specified
# in /Applications/Android Studio.app/sdk/tools/proguard/proguard-android.txt
# You can edit the include path and order by changing the ProGuard
# include property in project.properties.
#
# For more details, see
#   http://developer.android.com/guide/developing/tools/proguard.html

# Add any project specific keep options here:

# If your project uses WebView with JS, uncomment the following
# and specify the fully qualified class first_name to the JavaScript interface
# class:
-keepclassmembers class fqcn.of.javascript.interface.for.refresh {
   public *;
}


##---------------Begin: proguard configuration for EventBus  ----------

-keepclassmembers class ** {
    public void onEvent*(**);
}


##---------------Begin: proguard configuration for Gson  ----------

# Gson uses generic type information stored in a class file when working with fields. Proguard
# removes such information by default, so configure it to keep all of it.
-keepattributes Signature

# Gson specific classes
-keep class sun.misc.Unsafe { *; }
-keep class com.google.gson.stream.** { *; }

# Application classes that will be serialized/deserialized over Gson
-keep class de.xikolo.data.entities.** { *; }


##---------------Begin: proguard configuration for Samsung SDK  ----------
-dontwarn com.samsung.**
-keep class com.samsung.** { *; }


##---------------Begin: proguard configuration for Glide  ----------
-keep public class * implements com.bumptech.glide.module.GlideModule
-keep public enum com.bumptech.glide.load.resource.bitmap.ImageHeaderParser$** {
  **[] $VALUES;
  public *;
}


##---------------Begin: proguard configuration for CastCompanionLibrary  ----------
-keep class android.support.v7.** { *; }
