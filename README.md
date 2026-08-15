<div align="center">

# 🪔 ZUBEEN FM

### হৃদয়ত জীয়াই থকা এক সুৰ

<p>
  <img src="docs/assets/zubeen-portrait.jpg"
       alt="Zubeen Garg"
       width="280"
       style="border-radius: 20px;">
</p>

<p>
  <img src="docs/assets/diya.gif"
       alt="Animated Diya"
       width="90">
</p>

## শ্ৰদ্ধাঞ্জলী

### ❤️ Heartthrob ZUBEEN DA

**18-11-1972 — 19-09-2025**

> **“তোমাৰ সুৰে আমাক সদায় জীয়াই থকাৰ সাহস দিব, জুবিন দা।”**

</div>

---

## 🎵 About ZUBEEN FM

**ZUBEEN FM** is a personal, non-commercial Android music and tribute
project created as a heartfelt dedication to the music, memories and
cultural contribution of **Zubeen Garg**.

The project is designed to provide a modern music experience while
keeping Zubeen Garg's musical legacy at its heart.

---

## ✨ Features

### 📻 ZUBEEN FM Radio

- Dedicated Zubeen Garg radio experience
- Universal synchronized radio station
- Deterministic station schedule
- Continuous playback
- Modern radio player
- Audio visualization
- Play / Pause controls
- Background playback
- Lock-screen media controls
- Android media notification
- No advertisements
- No RJ interruptions

### 🎶 Assamese Music Library

Normal Songs mode focuses on Assamese-language recordings.

Includes discovery for:

- Songs
- Albums
- Artists
- Genres
- Languages
- Search
- Favorites
- Recently played

The catalogue is designed to cover Assamese music from older generations
through contemporary artists.

### ⭐ Zubeen Garg Collection

Zubeen Garg has a dedicated artist catalogue.

Unlike the general Assamese catalogue, the Zubeen Garg artist section can
contain verified recordings in their **original languages**.

Language information is preserved rather than automatically translated.

### 🪔 Tribute Experience

The Tribute section includes:

- Animated diya
- Zubeen Garg portrait
- Memorial design
- ❤️ Heartthrob ZUBEEN DA
- Rose-petal interaction
- Assamese tribute message
- Assamese facts
- Assamese stories about Zubeen Garg
- Interactive memorial experience

---

## 🌹 A Tribute, Not a Commercial Service

ZUBEEN FM is an independent personal fan and tribute project.

It is **not affiliated with, endorsed by, or officially associated with**
Zubeen Garg, his family, estate, record labels, music publishers, or
music platforms unless explicitly stated.

The project does not claim ownership of Zubeen Garg's music or other
copyrighted recordings.

All music, recordings, artwork and other copyrighted material remain the
property of their respective rights holders.

---

## 🔒 Privacy

ZUBEEN FM is designed as a personal, non-commercial application.

The project does not intentionally provide:

- Commercial advertising
- Paid subscriptions
- In-app purchases
- User-data monetization

Music discovery and streaming may require communication with third-party
services used by the application.

The application does not claim ownership or control over those
third-party services.

---

## 🚫 Non-Commercial Policy

ZUBEEN FM is intended for personal and non-commercial use.

The project does not sell music, subscriptions or advertising.

**Important:** Non-commercial use does not itself grant copyright,
streaming or distribution rights.

Users are responsible for respecting applicable copyright and
third-party service terms.

---

## 🌐 Open Source

ZUBEEN FM uses open-source software and libraries.

Major technologies used by the project include:

- Kotlin
- Jetpack Compose
- AndroidX
- Material 3
- Media3 / ExoPlayer
- Kotlin Coroutines
- Coil
- NewPipe Extractor
- Other open-source dependencies included in the project

See:

**[`ATTRIBUTIONS.md`](docs/ATTRIBUTIONS.md)**

for the complete dependency, copyright and license information used by
the actual project.

---

## 🏗️ Architecture

```text
                    ZUBEEN FM
                        │
          ┌─────────────┴─────────────┐
          │                           │
     NORMAL MODE                  RADIO MODE
          │                           │
          ▼                           ▼
 Assamese Catalogue             Zubeen Catalogue
          │                           │
 Songs / Albums / Artists       Verified Zubeen
 Genres / Search                     │
          │                           ▼
          │                    Station Manifest
          │                           │
          │                           ▼
          │                    RadioStationClock
          │                           │
          └─────────────┬─────────────┘
                        ▼
                 Playback Engine
                        │
          ┌─────────────┼─────────────┐
          ▼             ▼             ▼
       Android       Notification   Lock Screen
       Player          Controls       Controls
