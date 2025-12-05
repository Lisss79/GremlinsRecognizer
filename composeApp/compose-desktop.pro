# Основное: сохраняем точку входа в приложение
-keepclasseswithmembers public class ru.lisss79.gremlins_recognizer.MainKt {
    public static void main(java.lang.String[]);
}

# Критично: сохраняем основные классы Android, Kotlin и их рантайм
-keep class kotlin.** { *; }
-keep class android.** { *; }

-keep class ru.lisss79.gremlins_recognizer.** { *; }

-keep class ** { *; }
-dontwarn **