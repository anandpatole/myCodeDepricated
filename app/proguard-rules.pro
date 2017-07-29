# Add project specific ProGuard rules here.
# By default, the flags in this file are appended to flags specified
# in /home/pankaj/Android/NewSdk/Sdk/tools/proguard/proguard-android.txt
# You can edit the include path and order by changing the proguardFiles
# directive in build.gradle.
#
# For more details, see
#   http://developer.android.com/guide/developing/tools/proguard.html

# Add any project specific keep options here:

# If your project uses WebView with JS, uncomment the following
# and specify the fully qualified class name to the JavaScript interface
# class:
-keepclassmembers class fqcn.of.javascript.interface.for.webview {
   public *;
}

# your duplicated classes (e.g. SslError) are presented in both android.jar and org.apache.http.legacy.jar.
#There is no need to keep the classes as long as they are located in library jar (phone's library actually).
#dontwarn doesn't work because it's not a warning, it's a note.
-dontnote android.net.http.*
-dontnote org.apache.commons.codec.**
-dontnote org.apache.http.**


#can't find referenced class com.google.android.gms.R
-keep public class com.google.android.gms.* { public *; }
-dontwarn com.google.android.gms.**

#AppsFlyer
-dontwarn com.appsflyer.GcmInstanceIdListener

#Freshdesk
-dontwarn com.freshdesk.hotline.common.*
-dontwarn com.mixpanel.android.mpmetrics.Tweaks

#App Specific
-keep public class * extends android.app.Activity
-keep public class * extends android.app.Application
-keep public class * extends android.app.Service
-keep public class * extends android.content.BroadcastReceiver
-keep public class * extends android.content.ContentProvider
-keep public class * extends android.app.backup.BackupAgent
-keep public class * extends android.preference.Preference
-keep public class * extends android.support.v4.app.Fragment
-keep public class * extends android.app.Fragment
-keep class com.cheep.** { public *; }
-keep public class * extends android.view.View {
    public <init>(android.content.Context);
    public <init>(android.content.Context, android.util.AttributeSet);
    public <init>(android.content.Context, android.util.AttributeSet, int);
    public void set*(...);
}
-keepclasseswithmembers class * {
    public <init>(android.content.Context, android.util.AttributeSet);
}
-keepclasseswithmembers class * {
    public <init>(android.content.Context, android.util.AttributeSet, int);
}
-keepclassmembers class * extends android.app.Activity {
   public void *(android.view.View);
}
# The support library contains references to newer platform versions.
# Don't warn about those in case this app is linking against an older
# platform version.  We know about them, and they are safe.
-dontwarn android.support.**
# Needed by google-http-client to keep generic types and @Key annotations accessed via reflection
-keepclassmembers class * {
  @com.google.api.client.util.Key <fields>;
}
# Needed just to be safe in terms of keeping Google API service model classes
-keep class com.google.api.services.*.model.*
-keep class com.google.api.client.**
-keepattributes Signature,RuntimeVisibleAnnotations,AnnotationDefault
# See https://groups.google.com/forum/#!topic/guava-discuss/YCZzeCiIVoI
-dontwarn com.google.common.collect.MinMaxPriorityQueue
# Make sure that Google Analytics doesn't get removed
-keep class com.google.analytics.tracking.android.CampaignTrackingReceiver

# Keep app compat vector animation class
-keep class android.support.graphics.drawable.VectorDrawableCompat
-keep class android.support.graphics.drawable.VectorDrawableCompat$* {
    *;
}

# Other settings
-keep class com.android.**
-keep class com.google.android.**
-keep class com.google.android.gms.**
-keep class com.google.android.gms.location.**
-keep class com.google.api.client.**
-keep class com.google.maps.android.**
-keep class libcore.**

# Firebase
# See: http://stackoverflow.com/questions/26273929/what-proguard-configuration-do-i-need-for-firebase-on-android
-keepnames class com.google.samples.apps.iosched.sync.userdata.** { *; }
-keep class com.firebase.** { *; }
-keep class org.apache.** { *; }
-keepnames class com.fasterxml.jackson.** { *; }
-keepnames class javax.servlet.** { *; }
-keepnames class org.ietf.jgss.** { *; }
-dontwarn org.w3c.dom.**
-dontwarn org.joda.time.**
-dontwarn org.shaded.apache.**
-dontwarn org.ietf.jgss.**


#Event Bus[Start]
-keepclassmembers class ** {
    @org.greenrobot.eventbus.Subscribe <methods>;
}
-keep enum org.greenrobot.eventbus.ThreadMode { *; }
# Only required if you use AsyncExecutor
-keepclassmembers class * extends org.greenrobot.eventbus.util.ThrowableFailureEvent {
    <init>(java.lang.Throwable);
}
#Event Bus[End]