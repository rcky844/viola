-keepclassmembers class tipz.viola.* {
    <fields>;
    <init>();
    <methods>;
}
-assumenosideeffects class android.util.Log {
    public static int d(...);
    public static int v(...);
    public static int i(...);
#    public static int w(...);
#    public static int e(...);
#    public static int wtf(...);
}