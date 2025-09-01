
---

# 🎙️ Voice-to-Text Keyboard (Android)

A custom Android keyboard powered by **Groq’s Whisper API**, enabling seamless **speech-to-text transcription** directly within any app. Simply press, hold, speak, and release — your voice becomes text instantly.

---

## ✨ Features

* 🎤 **Voice Recording** – Record audio using your microphone.
* 📝 **Real-time Transcription** – Converts speech to text using **Whisper Large v3**.
* ⌨️ **Custom Keyboard** – Works across all apps as a system input method.
* 🔄 **Undo / Redo / Select All** support for quick text editing.
* ⚡ **Modern UI** – Built with **Jetpack Compose** for a clean look.

---

## 📂 Project Structure

```
app/
 ├── AudioRecorder.kt        # Handles audio recording
 ├── MainActivity.kt         # App entry point & setup instructions
 ├── VoiceKeyboardIME.kt     # Core Input Method Service (keyboard logic)
 ├── WhisperApiService.kt    # Handles API calls to Groq Whisper
 ├── res/
 │   ├── keyboard_view.xml   # Keyboard layout container
 │   └── method.xml          # Input method declaration
 └── gradle.properties       # Stores API keys & build settings
```

---

## 🚀 Getting Started

### 1️⃣ Prerequisites

* Android Studio (latest version)
* Android SDK 24+
* Groq API Key (Whisper transcription service)

### 2️⃣ Clone the Repository

```bash
git clone https://github.com/your-username/voice-to-text-keyboard.git
cd voice-to-text-keyboard
```

### 3️⃣ Set Up API Key

Add your **Groq Whisper API Key** to `gradle.properties`:

```properties
WHISPER_API_KEY=your_api_key_here
```

### 4️⃣ Build & Run

* Open project in **Android Studio**
* Sync Gradle
* Run on an emulator or device

---

## 🛠️ Usage

1. Open the app and grant **microphone permission**.
2. Go to **Keyboard Settings** and enable **Voice Keyboard**.
3. Select it as your **default input method**.
4. Long-press the **mic button** → Speak → Release to transcribe.

---

## 📡 API Details

* **Base URL:** `https://api.groq.com/`
* **Endpoint:** `/openai/v1/audio/transcriptions`
* **Model Used:** `whisper-large-v3`

---

## 📸 Screenshots (Optional)
https://github.com/user-attachments/assets/61a9ad7f-d29a-40bf-9d17-ad24c827b027

---

## 🤝 Contributing

Contributions are welcome! Please fork the repo and submit a PR.

---

## 📜 License

This project is licensed under the **MIT License**.

---

Would you like me to also create a **badges & visuals enhanced version** (with shields.io badges like Kotlin, Jetpack Compose, API, License, etc.) to make your README look even more professional?
