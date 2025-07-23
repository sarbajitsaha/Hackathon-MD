## Overview

Free, offline Android application designed to support children aged 5-13 with intellectual disabilities, such as autism spectrum disorder (ASD), Down syndrome, or developmental delays. Developed for a non-governmental organization (NGO) targeting low-income families, the app emphasizes simplicity, small file size (<50MB target, <10MB for POC), and complete offline functionality. It draws inspiration from apps like AutiSpark, focusing on visual, interactive tools to foster cognitive development, daily routines, emotional regulation, and progress tracking.

The app is bilingual (English and Bengali) to cater to regional users in areas like Bangladesh or India. It supports non-verbal and illiterate children through image-based interfaces, audio cues, and touch interactions. Parents can customize content locally without internet access.

This project is built as a proof-of-concept (POC) by a team of 5 developers in 2 days, with plans for expansion. It is open-source and licensed under MIT for easy adoption by NGOs.

## Key Features

### Core Modules
- **Daily Activity**: Create and follow daily routines using drag-and-drop icons (e.g., wake up, brush teeth, eat). Includes timers and audio narration for predictability and anxiety reduction.
- **Flashcards (Learning)**: Interactive cards for building vocabulary, colors, shapes, animals, and emotions. Tap to flip, hear pronunciations, or match pairs. Supports visual-only mode for non-readers.
- **Calming Ideas**: Sensory activities like soothing sounds, or simple animations to help manage overstimulation or meltdowns.
- **IQ Test**: Visual-based quizzes (e.g., pattern recognition, memory games) to track cognitive progress. Automated scoring with local charts; not a clinical tool but for parental insights.
- **Customization**: Parents can add personal flashcards or schedules from the device gallery or recordings (offline). Simple editor for images, text, and audio.
- **Parental Dashboard**: View progress logs, export reports as text files.

### Additional Highlights
- **Bilingual Support**: Toggle between English and Bengali via settings. Uses localized strings and pre-recorded audio.
- **Offline Operation**: All data stored locally using Room Database; no internet required.
- **Accessibility**: Large icons, high-contrast colors, voice-over compatibility, and positive reinforcement (stars, sounds).
- **Target Users**: Children who may be non-verbal or non-literate initially; low-tech parents in resource-poor settings.

The POC includes a basic dashboard with buttons for "Daily Activity", "IQ", "Learning", and "Calm", plus prototypes for each feature.

## Technology Stack

- **Platform**: Android
- **Language**: Kotlin.
- **UI Framework**: Jetpack Compose (lightweight, declarative UI).
- **Database**: Room (SQLite for local storage of schedules, flashcards, progress).
- **Navigation**: Navigation Compose.
- **Media**: MediaPlayer for audio playback.
- **Localization**: Android's built-in resources (strings.xml for English/Bengali).
- **Build Tools**: Android Studio, Gradle with R8/ProGuard for APK shrinking.
- **Assets**: AI-generated images/animations (e.g., via Craiyon, Bing Image Creator) for flashcards and icons.

The app is optimized for low-end devices (1GB+ RAM) and small APK size through compressed assets (WebP images, short MP3 audio).

## Usage

1. Launch the app: You'll see the home dashboard with 4 large buttons:
   - **Daily Activity**: Build/view routines.
   - **IQ**: Take a simple visual quiz and view scores.
   - **Learning**: Browse/interact with flashcards.
   - **Calm**: Access relaxation tools.
2. Toggle language in Settings (gear icon on dashboard).
3. Customize: In Learning or Daily Activity, use the "+" button to add from gallery or record audio.
4. Track Progress: After quizzes, view charts in the IQ section.

For parents: No login required; all data is local and private.

### Screenshots


## Development Guide

### Project Structure
- `MainActivity.kt`: Entry point with Compose setup.
- `HomeScreen.kt`: Starting screen with 4 buttons
- `data/`: Room DB entities and DAOs.
- `assets/`: Pre-loaded images, audio.
- `res/values/`: Localization files (strings.xml, strings_bn.xml).
