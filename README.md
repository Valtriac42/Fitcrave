# Fitcrave — Android Gym App

Android Studio project for a gym/fitness app that matches the supplied Fitcrave UI exactly, with:
- Supabase for auth + data (`profiles`, `daily_stats`, `workouts` tables)
- A WebView screen that opens a Vercel-hosted dashboard

## Project layout

```
GYM/
├── build.gradle.kts            (root)
├── settings.gradle.kts
├── gradle.properties
├── gradle/wrapper/             (wrapper props — Android Studio will fill the jar)
├── local.properties.example    (copy → local.properties, fill in secrets)
├── app/
│   ├── build.gradle.kts
│   ├── proguard-rules.pro
│   └── src/main/
│       ├── AndroidManifest.xml
│       ├── java/com/fitcrave/app/
│       │   ├── FitcraveApp.kt
│       │   ├── MainActivity.kt
│       │   ├── activities/  (Splash, Workout, Diet, Report, WebView)
│       │   ├── auth/        (Login, Signup)
│       │   ├── data/        (SupabaseProvider, FitcraveRepository, models)
│       │   ├── fragments/   (Training, Settings, Profile)
│       │   └── ui/          (RecyclerView adapters)
│       └── res/             (layouts, drawables, values, menu, mipmap)
├── supabase/schema.sql      (run this in your Supabase SQL editor)
└── web/                     (a tiny Next.js starter you can deploy to Vercel)
```

## 1. Open in Android Studio

1. Open Android Studio → **Open** → choose this `GYM` folder.
2. Android Studio will report that `gradle-wrapper.jar` is missing — click **OK** when it offers to generate the wrapper, or from a terminal in the project root run `gradle wrapper` once (any Gradle 8+ install works). Android Studio's bundled JDK is fine.
3. Let Gradle sync — it will fetch Supabase Kotlin SDK, Material, Ktor, coroutines.

## 2. Configure secrets

Copy `local.properties.example` to `local.properties` and fill in:

```
SUPABASE_URL=https://xxxx.supabase.co
SUPABASE_ANON_KEY=eyJhbGciOi...
VERCEL_URL=https://your-fitcrave.vercel.app
```

These are read by `app/build.gradle.kts` and exposed as `BuildConfig.*` constants at compile time. **Never commit `local.properties`.**

## 3. Set up Supabase

1. Create a project at supabase.com.
2. In **SQL editor**, paste & run `supabase/schema.sql` (included). It creates:
   - `profiles` (user metadata)
   - `daily_stats` (workouts / kcal / minutes per day)
   - `workouts` (per-day completion log)
   - Row-Level-Security policies so each user only sees their own rows.
3. Under **Authentication → Providers**, ensure **Email** is enabled.
4. Copy the **Project URL** and **anon key** into `local.properties`.

## 4. Set up Vercel

The `web/` folder is a minimal Next.js page you can deploy to Vercel — replace its content with your real dashboard later. The Android `WebViewActivity` simply loads `BuildConfig.VERCEL_URL`, so any web URL works.

Quick deploy:
```
cd web
npm install
vercel       # follow prompts; copy the URL into local.properties as VERCEL_URL
```

## 5. Run the app

- Sync Gradle in Android Studio.
- Press **Run ▶** with an emulator or device.
- First screen → **Login / Signup** → creates a Supabase user → opens Home (Training tab).
- The Workout/Diet cards open detail screens; **Mark Day Complete** writes a row to `workouts`.
- **Settings → Open Web Dashboard** loads your Vercel URL inside a WebView.

## UI mapping vs the screenshot

| Screenshot element              | Implementation file |
| --                              | -- |
| "Fitcrave" + fire + crown header| `fragment_training.xml` top bar |
| 0 / 0 / 0 stats row             | `tvWorkoutsCount`, `tvKcal`, `tvMinutes` |
| Weekly Progress 1..7 chips      | `weeklyRow` + `bg_weekly_chip_active.xml` |
| Tasks → Workout DAY 0 START     | `cardWorkout` + `ic_bodybuilder.xml` |
| Tasks → Diet DAY ... START      | `cardDiet` |
| REPORT block                    | `cardReport` + `ic_report_hero.xml` |
| Bottom nav (Training/Settings/Profile) | `bottom_nav_menu.xml` |

The vector illustrations are stylised placeholders; drop your own PNGs into `res/drawable-xxxhdpi/` and reference them in the layouts to swap.

## Troubleshooting

- **Gradle sync fails on Ktor** — make sure your JDK is 17 (Android Studio bundled JBR is fine).
- **`SUPABASE_URL` placeholder error at runtime** — you forgot to fill in `local.properties` and rebuild.
- **WebView shows the placeholder URL** — set `VERCEL_URL` in `local.properties` then rebuild.

---
Built around a single Activity host (`MainActivity`) + three Fragments, with detail Activities reused for Workout / Diet / Report / WebView.
