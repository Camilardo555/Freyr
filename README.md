# Freyr — Native Android Application

**Freyr** is a minimalist and modern personal and family financial management application built natively for Android using Kotlin and Jetpack Compose.

## Color Palette
- **Primary:** `#69042A`
- **Secondary:** `#E1CCD4`

---

## Opening and Running in Android Studio

1. **Launch Android Studio** (Hedgehog, Iguana, Jellyfish, Koala, Ladybug, or Meerkat).
2. Click **Open** (or **File > Open**) and select the root directory of this repository:
   ```
   Freyr/
   ```
3. Android Studio will automatically detect the `settings.gradle.kts` and start **Gradle Sync**.
4. Once the Gradle sync finishes:
   - Select an Android Virtual Device (AVD Emulator with API 24+) or connect a physical Android device.
   - Click the green **Run 'app'** button (or press `Shift + F10`).
5. The application will compile and launch directly on the device.

---

## Project Structure & Architecture

```
Freyr/
├── settings.gradle.kts                # Root project definition (Freyr)
├── build.gradle.kts                   # Root build configuration
├── gradle.properties                  # AndroidX & JVM memory settings
├── gradlew / gradlew.bat              # Gradle wrapper scripts
├── gradle/wrapper/                    # Gradle wrapper distribution
└── app/
    ├── build.gradle.kts               # Android app configuration & dependencies
    ├── proguard-rules.pro             # Proguard rules
    └── src/main/
        ├── AndroidManifest.xml        # Freyr app manifest
        ├── res/
        │   ├── values/strings.xml     # App name "Freyr"
        │   ├── values/colors.xml      # Color palette (#69042A, #E1CCD4)
        │   ├── values/themes.xml      # Freyr theme
        │   └── drawable/              # Vector drawables & launcher icons
        └── java/com/freyr/app/
            ├── FreyrApplication.kt   # App instance & Room DB initialization
            ├── MainActivity.kt        # Main Entry activity
            ├── data/
            │   ├── model/Models.kt    # Room entities (User, FamilyGroup, Debt, Payment, Invitation)
            │   ├── local/Daos.kt      # DAOs & Converters
            │   ├── local/FreyrDatabase.kt # Room database with seed data
            │   └── repository/FreyrRepository.kt # Repository & business logic
            └── ui/
                ├── theme/             # Freyr Compose theme & typography
                ├── navigation/        # Screen definitions & FreyrNavHost
                ├── viewmodel/         # FreyrViewModel
                └── screens/
                    ├── auth/          # LoginScreen & RegisterScreen
                    ├── dashboard/     # DashboardScreen (4 Main options & summary)
                    ├── debt/          # AddDebtScreen (Fixed input fields), PersonalFinancesScreen, DebtDetailScreen, AddPaymentScreen
                    ├── group/         # FamilyGroupsScreen, CreateFamilyGroupScreen, FamilyGroupDetailScreen, AcceptInvitationsScreen
                    └── profile/       # EditProfileScreen
```

---

## Key Features & Bug Fixes

### 1. Add Debt Screen (Bug Fix Completely Resolved)
- All input fields (`Debt Name`, `Total Amount`, `Description`) use standard native `OutlinedTextField` components.
- Fully focusable, opens the keyboard upon touch, supports text deletion, backspace, and cursor positioning.
- Dedicated numeric input keyboard (`KeyboardType.Decimal`) for the amount.
- Real-time form validation ensuring positive amounts and non-empty titles.

### 2. Main Dashboard (4 Key Actions)
1. **Edit Profile**: Modify user credentials.
2. **Add Debt**: Create personal or family debts.
3. **Create Family Group**: Form collaborative household expense pools.
4. **Accept Family Group Invitation**: Accept or decline group invitations.

### 3. Financial Activity Summary
- **Total Amount Pending**: Highlighted focal metric.
- **Pending Debts Count**
- **Paid Debts Count**
- **Personal Payments Total**
- **Family Debts Total**

### 4. Personal Debts & Partial Installments
- Independent tracking with automatic calculation:
  $$\text{Remaining} = \text{Total Debt} - \sum \text{Installments}$$
- Automatically marks debts as **Paid** once the remaining balance reaches $0.00.

### 5. Family Groups & Shared Debts
- Group creators can invite users, assign debts to specific members or the entire family.
- Member payment logging with complete history.

### 6. Persistence
- Fully backed by a local **Room Database** (`FreyrDatabase`) with initial sample data for test accounts (**Juan**, **María**, and **Carlos**).
