# QR Barcode Studio — Android App

A complete, production-ready QR & Barcode Scanner + Generator app for Android.

---

## 📋 Requirements

| Item             | Version                  |
|------------------|--------------------------|
| Android Studio   | Hedgehog (2023.1.1) or newer |
| Min SDK          | 26 (Android 8.0 Oreo)   |
| Target SDK       | 34 (Android 14)          |
| Build System     | Gradle with Kotlin DSL   |
| Language         | Java (no Kotlin)         |
| JDK              | 17                       |

---

## 🚀 Getting Started

### Step 1 — Open in Android Studio
1. Unzip `QRBarcodeStudio.zip`
2. Open Android Studio → **File → Open** → Select the `QRBarcodeStudio` folder
3. Wait for Gradle sync to complete (requires internet to download dependencies)

### Step 2 — SDK Path
Android Studio will auto-configure `local.properties` with your SDK path.  
If it doesn't, open `local.properties` and set:
```
sdk.dir=/path/to/your/Android/Sdk
```

### Step 3 — Run
- Connect a physical Android device (recommended for camera) or use an emulator with camera support
- Click ▶ **Run** or press `Shift + F10`

---

## 📱 Features

### 📷 Screen 1 — Live Scanner
- Real-time CameraX + ML Kit barcode scanning
- Supports: **QR Code, Code 128, Code 39, EAN-13, EAN-8, UPC-A, PDF417, DataMatrix, Aztec**
- Animated laser scan line overlay
- Bottom sheet result with **Copy / Share / Open URL / Save to History**
- Flashlight toggle + Front/Back camera switch
- Vibration feedback on scan

### ✨ Screen 2 — QR Code Generator
- Input tabs: **URL, Text, Email, Phone, WiFi, vCard, SMS**
- Live QR preview as you type
- Customize: foreground color, background color, size slider
- Add a center logo from gallery
- Save to gallery (MediaStore) + Share

### 📊 Screen 3 — Barcode Generator
- Formats: **Code 128, Code 39, EAN-13, EAN-8, UPC-A**
- Live barcode preview
- Format validation with helpful hints
- Save to gallery + Share

### 🕓 Screen 4 — History
- Room database persistence
- Filter: All / Scanned / Generated
- Search bar
- Swipe to delete + Long-press multi-select
- Tap to view detail, copy, share, or delete

### ⚙️ Screen 5 — Settings
- Vibrate on scan toggle
- Beep on scan toggle
- Auto-open URLs toggle
- Clear all history button
- Dark mode support (Material You)

---

## 🏗️ Architecture

```
com.qrbarcode.studio/
├── MainActivity.java           ← BottomNavigationView host
├── ui/
│   ├── scanner/
│   │   ├── ScannerFragment.java
│   │   └── ScanResultBottomSheet.java
│   ├── qrgenerator/
│   │   └── QrGeneratorFragment.java
│   ├── barcodegenerator/
│   │   └── BarcodeGeneratorFragment.java
│   ├── history/
│   │   ├── HistoryFragment.java
│   │   └── HistoryAdapter.java
│   └── settings/
│       └── SettingsFragment.java
├── data/
│   ├── ScanRecord.java         ← Room Entity
│   ├── ScanDao.java            ← Room DAO
│   └── AppDatabase.java        ← Room Database
├── repository/
│   └── ScanRepository.java
├── viewmodel/
│   └── ScanViewModel.java
└── utils/
    └── QRCodeUtils.java        ← QR/Barcode generation + gallery save
```

---

## 📦 Dependencies (build.gradle.kts)

| Library                             | Version  | Purpose               |
|-------------------------------------|----------|-----------------------|
| `androidx.camera:camera-*`          | 1.3.1    | Live camera preview   |
| `com.google.mlkit:barcode-scanning` | 17.2.0   | Barcode detection     |
| `com.google.zxing:core`             | 3.5.2    | Code generation       |
| `com.journeyapps:zxing-android-embedded` | 4.3.0 | ZXing Android wrapper |
| `androidx.room:room-*`              | 2.6.1    | Local database        |
| `androidx.lifecycle:lifecycle-*`    | 2.7.0    | MVVM LiveData         |
| `com.google.android.material`       | 1.11.0   | Material Design 3     |

---

## 🛑 Permissions

| Permission                    | Usage                          |
|-------------------------------|--------------------------------|
| `CAMERA`                      | Live barcode scanning          |
| `VIBRATE`                     | Haptic feedback on scan        |
| `WRITE_EXTERNAL_STORAGE`      | Save images (API ≤ 28 only)   |
| `READ_MEDIA_IMAGES`           | Pick logo from gallery (API 33+) |

---

## 🐛 Troubleshooting

**Gradle sync fails?**
- Check internet connection
- File → Invalidate Caches → Restart

**Camera not working on emulator?**
- Use a physical device for best results
- Or enable camera in AVD settings

**`local.properties` error?**
- Let Android Studio auto-generate it, or manually set `sdk.dir`
