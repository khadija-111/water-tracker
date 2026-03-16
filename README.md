1-# Project Structure

app/
├── manifests/
│   └── AndroidManifest.xml
│
├── kotlin+java/
│   └── com.khadija.bankapp/
│       ├── AuthWorker.kt
│       ├── CompteFragment.kt
│       ├── ConseilsFragment.kt
│       ├── DashboardFragment.kt
│       ├── LoginActivity.kt
│       ├── ObjectifsFragment.kt
│       ├── NotificationReceiver.kt
│       ├── RegisterActivity.kt
│       ├── SplashOnBoardingActivity.kt
│       ├── ViewPagerAdapter.kt
│       ├── MainActivity.kt
│       │
│       └── data/
│           ├── AppDatabase.kt
│           ├── GoalDao.kt
│           ├── GoalEntry.kt
│           ├── WaterDao.kt
│           └── WaterEntry.kt
│
└── res/
    ├── color/
    ├── drawable/
    └── layout/
        ├── activity_login.xml
        ├── activity_main.xml
        ├── activity_register.xml
        ├── activity_splash_onboarding.xml
        ├── fragment_conseils.xml
        ├── fragment_dashboard.xml
        ├── fragment_objectifs.xml
        ├── fragment_compte.xml
        └── onborading_page.xml

2-# Technologies Used

- Kotlin
- Android Studio
- Room Database
- WorkManager
- ViewPager
- BroadcastReceiver (Notifications)
- Material Design Components
- Android Fragments
- JSON / Local Data Storage

3-# Features

- User authentication (Login & Register)
- Dashboard to display user information
- Goals management
- Water tracking
- Tips and advice section
- Notifications system
- Onboarding screen for new users
- Local data storage using Room Database
- Navigation between fragments using ViewPager
