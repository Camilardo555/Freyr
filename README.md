# Freyr — Native Android Java Application

**Freyr** is a minimalist and modern personal and family financial management application built natively for Android using **pure Java**, **XML layouts**, and **Android Studio**.

## Color Palette
- **Primary:** `#69042A`
- **Secondary:** `#E1CCD4`

---

## Opening and Running in Android Studio

1. **Launch Android Studio** (Hedgehog, Iguana, Jellyfish, Koala, Ladybug, Meerkat, or newer).
2. Click **Open** (or **File > Open**) and select the root directory of this repository:
   ```
   Freyr/
   ```
3. Android Studio will automatically detect `settings.gradle` and execute **Gradle Sync**.
4. Once the Gradle sync finishes:
   - Select an Android Virtual Device (AVD Emulator with API 24+) or connect a physical Android device.
   - Click the green **Run 'app'** button (or press `Shift + F10`).
5. The application will compile using standard Java 17 and launch directly on the device.

---

## 100% Pure Java Architecture & Structure

This project is built strictly in **Java** (no Kotlin `.kt` files, no Kotlin libraries, and standard Groovy Gradle configuration):

```
Freyr/
├── settings.gradle                    # Project settings & repositories (Groovy)
├── build.gradle                       # Root build configuration (Groovy)
├── gradle.properties                  # AndroidX & JVM settings
├── gradlew / gradlew.bat              # Gradle wrapper scripts
├── gradle/wrapper/                    # Gradle wrapper distribution
└── app/
    ├── build.gradle                   # Android Java app configuration (Room with annotationProcessor)
    ├── proguard-rules.pro             # Proguard rules
    └── src/main/
        ├── AndroidManifest.xml        # Freyr app manifest
        ├── res/
        │   ├── layout/                # XML Layouts
        │   │   ├── activity_main.xml
        │   │   ├── activity_login.xml
        │   │   ├── activity_register.xml
        │   │   ├── activity_add_debt.xml (Fixed focusable & editable fields)
        │   │   ├── activity_debt_detail.xml
        │   │   ├── activity_add_payment.xml
        │   │   ├── activity_create_family_group.xml
        │   │   ├── activity_family_group_detail.xml
        │   │   ├── activity_edit_profile.xml
        │   │   ├── fragment_dashboard.xml
        │   │   ├── fragment_personal_finances.xml
        │   │   ├── fragment_family_groups.xml
        │   │   ├── fragment_invitations.xml
        │   │   ├── item_debt.xml
        │   │   ├── item_payment.xml
        │   │   ├── item_family_group.xml
        │   │   └── item_invitation.xml
        │   ├── values/
        │   │   ├── strings.xml        # App name: "Freyr"
        │   │   ├── colors.xml         # #69042A (Primary) & #E1CCD4 (Secondary)
        │   │   └── themes.xml         # Theme.Freyr (Material3 Light)
        │   └── drawable/              # Custom shapes, buttons & badges
        └── java/com/freyr/app/
            ├── FreyrApplication.java  # Application singleton & repository setup
            ├── data/
            │   ├── model/
            │   │   ├── User.java
            │   │   ├── FamilyGroup.java
            │   │   ├── Debt.java
            │   │   ├── Payment.java
            │   │   ├── Invitation.java
            │   │   └── FinancialSummary.java
            │   ├── local/
            │   │   ├── Converters.java
            │   │   ├── UserDao.java
            │   │   ├── FamilyGroupDao.java
            │   │   ├── DebtDao.java
            │   │   ├── PaymentDao.java
            │   │   ├── InvitationDao.java
            │   │   └── FreyrDatabase.java # SQLite Room DB with demo seed data
            │   └── repository/
            │       └── FreyrRepository.java # Thread-safe data access layer
            └── ui/
                ├── MainActivity.java  # Bottom Navigation controller
                ├── auth/
                │   ├── LoginActivity.java
                │   └── RegisterActivity.java
                ├── dashboard/
                │   └── DashboardFragment.java # 4 Core actions & financial metrics
                ├── debt/
                │   ├── AddDebtActivity.java # CRITICAL BUG FIX implemented here
                │   ├── PersonalFinancesFragment.java
                │   ├── DebtDetailActivity.java
                │   └── AddPaymentActivity.java
                ├── group/
                │   ├── FamilyGroupsFragment.java
                │   ├── CreateFamilyGroupActivity.java
                │   ├── FamilyGroupDetailActivity.java
                │   └── InvitationsFragment.java
                ├── profile/
                │   └── EditProfileActivity.java
                └── adapter/
                    ├── DebtAdapter.java
                    ├── PaymentAdapter.java
                    ├── FamilyGroupAdapter.java
                    └── InvitationAdapter.java
```

---

## Critical Bug Fix — Add Debt Screen

In `AddDebtActivity.java` and `res/layout/activity_add_debt.xml`:
- Form inputs are implemented using native Android `EditText` components.
- Every text input field:
  - Is explicitly focusable (`android:focusable="true"` and `android:focusableInTouchMode="true"`).
  - Responds immediately to user touch and tap gestures (`android:clickable="true"`).
  - Displays a visible cursor (`android:cursorVisible="true"`).
  - Opens the Android soft keyboard seamlessly (`windowSoftInputMode="adjustResize"` in `AndroidManifest.xml`).
  - Supports standard keyboard input, backspace/deletion, text selection, and editing.
  - The **Total Amount** input utilizes `android:inputType="numberDecimal"`, providing the proper numeric keypad.
  - Form validation verifies positive amounts and non-empty titles before persisting to the database.

---

## Core Application Capabilities

1. **Authentication & Registration**:
   - Register with Full Name, Username, Email, and Password.
   - Login persistence via `SharedPreferences`.
   - Quick-switch demo profiles: **Juan Pérez**, **María González**, and **Carlos Rodríguez**.
2. **Main Dashboard**:
   - 4 Primary Action Cards:
     1. **Edit Profile**
     2. **Add Debt**
     3. **Create Family Group**
     4. **Accept Family Group Invitation**
   - Clean financial summary: Pending Debts, Paid Debts, Personal Payments, Family Debts, and Total Amount Pending.
3. **Personal Debts & Partial Payments**:
   - Independent personal finance ledger.
   - Partial installment logging with automatic recalculation of remaining amounts and status transitions to "Paid".
4. **Family Groups & Shared Debts**:
   - Create family groups and invite members.
   - Assign shared debts to individual members or the entire family.
   - Member contribution tracking and collective balances.
5. **Accept Family Group Invitations**:
   - Review pending invitations and join family groups.
