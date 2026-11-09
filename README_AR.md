# Nova Widgets — Android source project

A native Kotlin Android starter for a premium dark-and-gold home-screen widget collection.

## Included
- Clock, date, quote, weather placeholder, battery, and prayer-time placeholder widgets.
- Simple font library screen: import `.ttf` / `.otf` files using Android's document picker.
- Imported fonts are copied into app-private storage, validated, previewed, and selected.
- The selected font is rendered into widget bitmaps, so it can appear in Android home-screen widgets.
- Dark premium visual style with gold accents.

## Build an APK
Open this folder in a recent Android Studio installation, allow Gradle sync, then select:
**Build > Build Bundle(s) / APK(s) > Build APK(s)**

Requirements: JDK 17+, Android SDK 35, internet for first Gradle dependency resolution.

## Important status
This is source code, not a compiled APK. The current execution environment has no Android SDK or Gradle installation, so an APK cannot be built or verified here.
Weather and prayer widgets are placeholders; they need a live weather API and a prayer-time calculation library/settings (city, calculation method, madhhab) before they can show accurate live data. The clock widget refresh interval also needs production tuning and device testing.

## Font import
From the app, tap **Import a font**, choose a `.ttf` or `.otf` file from the phone, then select it from the font library. The active font is applied to widget body text.
