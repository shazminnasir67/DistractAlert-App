# DriveAlert - Driver Drowsiness Detection App

## 🎯 Project Overview
DriveAlert is an Android application designed to detect driver drowsiness using computer vision and machine learning. The app is built with a modular architecture following MVVM pattern and uses Jetpack Compose for the UI.

## 🚧 Development Phases

### ✅ Phase 1: Login & Camera Permission (MVP base) - COMPLETED
- **Login Screen**: Clean Material3 design with camera preview
- **Camera Permission**: Automatic permission request and handling
- **Face Detection**: ML Kit integration for basic face presence detection
- **Navigation**: Seamless flow from login to start trip screen

#### Phase 1 Features:
- ✅ Jetpack Compose UI with Material3 design
- ✅ MVVM architecture with StateFlow
- ✅ CameraX integration with front camera
- ✅ ML Kit face detection
- ✅ Permission handling
- ✅ Navigation between screens
- ✅ Error handling and loading states

## 🏗️ Project Structure

```
app/src/main/java/com/example/drivealert/
├── data/
│   ├── model/
│   │   └── LoginState.kt
│   └── repository/
│       └── FaceDetectionRepository.kt
├── ui/
│   ├── navigation/
│   │   ├── DriveAlertNavGraph.kt
│   │   └── Screen.kt
│   ├── screens/
│   │   ├── login/
│   │   │   ├── LoginScreen.kt
│   │   │   └── LoginViewModel.kt
│   │   └── starttrip/
│   │       └── StartTripScreen.kt
│   └── theme/
│       ├── Color.kt
│       ├── Theme.kt
│       └── Type.kt
├── utils/
│   └── CameraUtils.kt
└── MainActivity.kt
```

## 🛠️ Technical Stack

### Core Technologies:
- **Kotlin**: Primary programming language
- **Jetpack Compose**: Modern UI toolkit
- **Material3**: Design system
- **MVVM**: Architecture pattern
- **StateFlow**: Reactive state management

### Camera & ML:
- **CameraX**: Camera library
- **ML Kit**: Face detection
- **Coroutines**: Asynchronous programming

### Navigation:
- **Navigation Compose**: Type-safe navigation

## 📱 Screens

### Login Screen
- Clean, modern design with app branding
- Camera preview with face detection overlay
- Permission handling with user-friendly prompts
- Loading states and error handling
- Automatic navigation on successful face detection

### Start Trip Screen
- Success confirmation screen
- Phase completion indicator
- Ready for next phase integration

## 🔧 Setup & Installation

### Prerequisites:
- Android Studio Arctic Fox or later
- Android SDK 24+ (API level 24)
- Kotlin 1.9.0+

### Dependencies:
The app uses the following key dependencies:
- Jetpack Compose BOM 2024.02.00
- CameraX 1.3.1
- ML Kit Face Detection 16.1.5
- Navigation Compose 2.7.6
- Coroutines 1.7.3

### Building:
1. Clone the repository
2. Open in Android Studio
3. Sync Gradle files
4. Build and run on device/emulator

## 🔐 Permissions

The app requires the following permissions:
- `CAMERA`: For face detection and preview
- Front camera hardware feature

## 🎨 UI/UX Features

### Design Principles:
- **Material3**: Following Google's latest design guidelines
- **Accessibility**: Proper contrast ratios and touch targets
- **Responsive**: Adapts to different screen sizes
- **Intuitive**: Clear user flow and feedback

### Color Scheme:
- Dynamic color support (Android 12+)
- Fallback to custom purple theme
- Proper light/dark mode support

## 🚀 Next Steps - Phase 2

Phase 2 will extend the current implementation with:
- Face recognition using FaceNet/TensorFlow Lite
- MongoDB Atlas integration for user authentication
- Embedding extraction and matching
- Enhanced security and user management

## 📊 Performance Considerations

### Phase 1 Optimizations:
- Efficient camera preview rendering
- Minimal ML Kit processing overhead
- Proper lifecycle management
- Memory-efficient image analysis

### Battery Optimization:
- Camera preview only when needed
- Efficient coroutine usage
- Proper resource cleanup

## 🧪 Testing

### Current Test Coverage:
- Basic UI component testing
- Navigation flow testing
- Permission handling testing

### Future Testing Plans:
- Unit tests for ViewModels
- Integration tests for face detection
- UI automation tests

## 📝 Development Notes

### Architecture Decisions:
- **MVVM**: Chosen for separation of concerns and testability
- **Repository Pattern**: For data layer abstraction
- **StateFlow**: For reactive UI updates
- **Compose**: For modern, declarative UI

### Code Quality:
- Kotlin best practices
- Clean architecture principles
- Proper error handling
- Comprehensive documentation

## 🤝 Contributing

This is a final year project. For questions or contributions, please contact the development team.

## 📄 License

This project is developed for educational purposes as part of a final year project.

---

**Phase 1 Status**: ✅ COMPLETED  
**Ready for Phase 2**: Face Recognition with MongoDB 