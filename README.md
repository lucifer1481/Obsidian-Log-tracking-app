# Obsidian Log 🦇

Obsidian Log is a premium, cinematic media tracker designed to keep all your entertainment in one place. Featuring a sleek, custom "Lucifer" Red & Pure Black aesthetic, it allows you to effortlessly track your progress across Anime, Manga, Movies, Series, and AAA Games.

## ✨ Features
* **Cinematic Dark Theme:** A completely custom pure black and red interface optimized for OLED screens.
* **Universal Tracking:** 
  * Anime & Manga tracking (Powered by MyAnimeList)
  * Movies & Series tracking
  * AAA Game tracking (Powered by RAWG API)
* **Cloud Sync:** Custom libraries synced securely via Supabase.
* **Explore Page:** Endless scrolling grids, trending charts, and recommendations.
* **Sleek Animations:** Custom splash screens and smooth transitions.

## 📥 Download & Install
You can download the latest version of Obsidian Log directly to your Android device:
1. Go to the **[Releases](../../releases/latest)** page.
2. Download the `Obsidian_Log.apk` file.
3. Open the file on your phone to install (you may need to allow "Install from unknown sources").

## ⚠️ Troubleshooting & Network Setup (Private DNS)
Because TMDB and certain media APIs can occasionally be restricted by specific internet service providers (ISPs) or regional network configurations, you may need to configure a Private DNS on your test device or emulator for the Movies and Series databases to load correctly:
1. Go to your device **Settings** -> **Network & Internet** -> **Private DNS** -> **Private DNS provider hostname**.
2. Set it to `dns.adguard.com` (or `cloudflare-dns.com`). 
3. This bypasses local domain blocks to ensure smooth image loading and metadata fetching for all custom media items.

## 🛠️ Built With
* **Kotlin & Jetpack Compose** - UI and core logic.
* **Supabase** - Cloud database.
* **Coil** - Image loading.
* **Ktor** - Networking.
* *Note: This project is an extensively customized and enhanced fork based on the open-source MoeList tracker.*
