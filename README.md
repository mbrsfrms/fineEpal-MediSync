# fineEpal-MediSync
 <p align="center">
  <img src="assets/medisync-banner.jpg" alt="MediSync Banner" width="100%">
</p>

<h1 align="center">🩺 MediSync</h1>

<h3 align="center">Medication Monitoring and Management System</h3>

<p align="center">
 <img src="https://readme-typing-svg.herokuapp.com?font=Fira+Code&weight=600&size=20&duration=3000&pause=1000&color=3DDC84&center=true&vCenter=true&width=450&lines=Your+medications%2C+On+track." alt="Typing SVG" />
</p>

<p align="center">
  A Java-based application designed to help individuals, parents, guardians, and family caregivers organize and monitor medication routines.
</p>

---

<h2>℞☤ Overview </h2>

**MediSync** is a Java-based medication monitoring and management application designed to provide users with a centralized and organized platform for managing their medication needs. It aims to simplify keeping track of multiple medications by allowing users to:
- 📝 **Record** essential medication information
- ⏰ **Organize** dosage schedules and receive timely reminders
- ✅ **Monitor** actual medication intake and maintain intake history
- 💊 **Track** remaining medicine supplies for timely replenishment

### 👥 Who Can Use MediSync?

<table>
  <tr>
    <td align="center" width="25%">
      <img src="https://img.icons8.com/color/96/user-male-circle--v1.png" width="65" alt="Individuals" /><br>
      <b>Individuals</b><br>
      <sub>Self-managing personal medication schedules, intake history, and medicine supplies.</sub>
    </td>
    <td align="center" width="25%">
      <img src="https://img.icons8.com/color/96/family--v1.png" width="65" alt="Parents and Guardians" /><br>
      <b>Parents & Guardians</b><br>
      <sub>Family caregivers organizing and overseeing medication routines for their dependents.</sub>
    </td>
    <td align="center" width="25%">
      <img src="https://img.icons8.com/color/96/children.png" width="65" alt="Children" /><br>
      <b>Children (10 & Below)</b><br>
      <sub>Young dependents whose dosages and schedules are closely monitored by guardians.</sub>
    </td>
    <td align="center" width="25%">
      <img src="https://img.icons8.com/color/96/elderly-person.png" width="65" alt="Elderly" /><br>
      <b>Elderly Individuals</b><br>
      <sub>Senior dependents whose medication accounts are handled and monitored by guardians.</sub>
    </td>
  </tr>
</table>

---

<h2>℞☤ Key Features</h2>

- 💊 **Medication Management** – Allows users to record, organize, and manage essential medication information.
- 🕒 **Medication Scheduling** – Helps users organize dosage schedules and medication routines.
- 📋 **Medication Intake Tracking** – Enables users to record medication intake and monitor taken, missed, or skipped doses.
- 📖 **Medication History** – Maintains records of medication intake for easier monitoring and reference.
- 📦 **Medicine Supply Monitoring** – Helps users monitor remaining medicine quantities and identify when supplies need replenishment.
- 🔔 **Reminders and Notifications** – Supports medication and appointment reminders.
- 👨‍👩‍👧 **Guardian and Caregiver Support** – Allows parents, guardians, and family caregivers to manage medication records and routines on behalf of their dependents.

---

<h2>℞☤ Technologies Used</h2>

- [![Java][Java.js]][Java-url]
- [![OOP][OOP.js]][OOP-url]
- [![Android Studio][AndroidStudio.js]][AndroidStudio-url]
- [![XML][XML.js]][XML-url]
---

## ℞☤ Project Structure

```text
MediSync/
│
├── app/
│   ├── src/
│   │   ├── main/
│   │   │   ├── java/
│   │   │   │   └── com/
│   │   │   │       └── medisync/
│   │   │   │           └── app/
│   │   │   │               ├── auth/
│   │   │   │               │   ├── EmailVerifier.java
│   │   │   │               │   ├── GoogleSignIn.java
│   │   │   │               │   ├── GoogleSignInBase.java
│   │   │   │               │   └── GoogleSignInFlow.java
│   │   │   │               │
│   │   │   │               ├── database/
│   │   │   │               │   ├── DbAccounts.java
│   │   │   │               │   ├── DbAppointments.java
│   │   │   │               │   ├── DbCore.java
│   │   │   │               │   ├── DbDoses.java
│   │   │   │               │   ├── DbMeds.java
│   │   │   │               │   ├── DbPatients.java
│   │   │   │               │   └── MediSyncDatabase.java
│   │   │   │               │
│   │   │   │               ├── manager/
│   │   │   │               │   ├── AccountStore.java
│   │   │   │               │   ├── AlarmScheduler.java
│   │   │   │               │   ├── AuthManager.java
│   │   │   │               │   ├── CalendarBuilder.java
│   │   │   │               │   ├── HealthAlertService.java
│   │   │   │               │   ├── NotificationHelper.java
│   │   │   │               │   ├── PasswordHasher.java
│   │   │   │               │   ├── ReminderService.java
│   │   │   │               │   ├── StreakCalculator.java
│   │   │   │               │   └── Validator.java
│   │   │   │               │
│   │   │   │               ├── model/
│   │   │   │               │   ├── Appointment.java
│   │   │   │               │   ├── AppointmentReminder.java
│   │   │   │               │   ├── CalendarEvent.java
│   │   │   │               │   ├── HealthAlert.java
│   │   │   │               │   ├── IntakeRecord.java
│   │   │   │               │   ├── IntakeStatus.java
│   │   │   │               │   ├── Medication.java
│   │   │   │               │   ├── MedicationReminder.java
│   │   │   │               │   ├── MedicationSchedule.java
│   │   │   │               │   ├── Patient.java
│   │   │   │               │   ├── Person.java
│   │   │   │               │   ├── Reminder.java
│   │   │   │               │   ├── RestockReminder.java
│   │   │   │               │   └── User.java
│   │   │   │               │
│   │   │   │               ├── receiver/
│   │   │   │               │   ├── BootReceiver.java
│   │   │   │               │   └── ReminderReceiver.java
│   │   │   │               │
│   │   │   │               └── util/
│   │   │   │                   ├── AccountScreens.java
│   │   │   │                   ├── AnimalArt.java
│   │   │   │                   ├── AppHooks.java
│   │   │   │                   ├── AppState.java
│   │   │   │                   ├── AppointmentDetail.java
│   │   │   │                   ├── AppointmentScreen.java
│   │   │   │                   ├── AvatarPicker.java
│   │   │   │                   ├── AvatarView.java
│   │   │   │                   ├── BarView.java
│   │   │   │                   ├── CalendarCells.java
│   │   │   │                   ├── CalendarScreen.java
│   │   │   │                   ├── DependentScreens.java
│   │   │   │                   ├── DoseLogic.java
│   │   │   │                   ├── FlameView.java
│   │   │   │                   ├── FormHelpers.java
│   │   │   │                   ├── FormatHelpers.java
│   │   │   │                   ├── GoogleAuthFlow.java
│   │   │   │                   ├── GoogleAuthUi.java
│   │   │   │                   ├── HelpScreens.java
│   │   │   │                   ├── HistoryScreen.java
│   │   │   │                   ├── HomeAlerts.java
│   │   │   │                   ├── HomeScreen.java
│   │   │   │                   ├── HomeStreak.java
│   │   │   │                   ├── IconView.java
│   │   │   │                   ├── LoginScreens.java
│   │   │   │                   ├── LogoView.java
│   │   │   │                   ├── MainActivity.java
│   │   │   │                   ├── MedFormFields.java
│   │   │   │                   ├── MedFormSave.java
│   │   │   │                   ├── MedFormState.java
│   │   │   │                   ├── MedListScreen.java
│   │   │   │                   ├── MoreScreen.java
│   │   │   │                   ├── Navigation.java
│   │   │   │                   ├── OnboardingScreens.java
│   │   │   │                   ├── PatientScreen.java
│   │   │   │                   ├── PersonArt.java
│   │   │   │                   ├── PhotoStorage.java
│   │   │   │                   ├── ReminderSettings.java
│   │   │   │                   ├── SessionRestore.java
│   │   │   │                   ├── SplashScreen.java
│   │   │   │                   ├── StreakAboutScreens.java
│   │   │   │                   ├── UiBasics.java
│   │   │   │                   ├── UiDialogs.java
│   │   │   │                   ├── UiInputs.java
│   │   │   │                   └── VerifyScreens.java
│   │   │
│   │   │   ├── res/
│   │   │   │   ├── drawable/
│   │   │   │   │   ├── ic_google_logo.xml
│   │   │   │   │   └── ic_launcher_foreground.xml
│   │   │   │   │
│   │   │   │   ├── layout/
│   │   │   │   │   └── activity_main.xml
│   │   │   │   │
│   │   │   │   ├── mipmap-anydpi-v26/
│   │   │   │   │   ├── ic_launcher.xml
│   │   │   │   │   └── ic_launcher_round.xml
│   │   │   │   │
│   │   │   │   ├── mipmap-mdpi/
│   │   │   │   │   ├── ic_launcher.xml
│   │   │   │   │   └── ic_launcher_round.xml
│   │   │   │   │
│   │   │   │   └── values/
│   │   │   │       ├── ic_launcher_background.xml
│   │   │   │       ├── strings.xml
│   │   │   │       └── styles.xml
│   │   │   │
│   │   │   └── AndroidManifest.xml
│   │   │
│   │   └── test/
│   │       └── java/
│   │           └── com/
│   │               └── medisync/
│   │                   └── app/
│   │                       └── DomainTests.java
│   │
│   ├── build.gradle
│   └── google-services.json
│
├── gradle/
│   └── wrapper/
│       ├── gradle-wrapper.jar
│       ├── gradle-wrapper.properties
│       └── gradle-daemon-jvm.properties
│
├── assets/
│   ├── amber.jpg
│   ├── medisync-banner.jpg
│   ├── seanjerseyalbo.png
│   └── sharmilaroa.jpg
│
├── .gitignore
├── build.gradle
├── gradle.properties
├── gradlew
├── gradlew.bat
├── settings.gradle
└── README.md

```

<h3>℞☤ Core Classes</h3>

| Class | Responsibility |
| :--- | :--- |
| `User` | Represents the MediSync user and stores basic account information. |
| `Medication` | Stores and manages details of each medication. |
| `MedicationSchedule` | Manages the dates and times when a medication should be taken. |
| `IntakeRecord` | Records actual medication intake and maintains history. |
| `Appointment` | Stores and manages follow-up medical appointments. |
| `Reminder` | Handles reminders for schedules, low supply, and appointments. |

---

<h2>℞☤ Object-Oriented Programming Concepts</h2>

MediSync applies Object-Oriented Programming principles and Java features to organize its components, ensure a modular application structure, and handle data efficiently.

- **Classes and Objects:** Classes will represent users, medications, schedules, intake records, appointments, and reminders, while objects will represent their actual runtime instances used by the system.
- **Methods:** Methods will be used for system actions such as adding medications, managing schedules, recording intake, updating supplies, and processing appointments.
- **Encapsulation:** Important data such as user information, medication dosages, quantities, and transaction details will be kept private and accessed safely through designated methods (getters and setters).
- **Inheritance:** A general base class can be used to establish structural relationships between related components and support code reuse where appropriate.
- **Polymorphism:** Polymorphism will be applied by allowing objects from related classes to be treated as instances of a common parent class or interface while performing their own specific implementations. For example, different types of reminders (such as medication reminders, restock/supply reminders, and appointment reminders) may implement a common `Reminder` behavior while providing different messages, conditions, and actions.
- **Abstraction:** Abstract classes or interfaces will be used to define common behaviors for related system components—such as notification or reminder functions—while hiding internal implementation details.
- **Exception Handling:** Exception handling will be used to manage invalid inputs, incorrect data formats, empty fields, invalid dosage or quantity values, and other runtime errors without causing the application to terminate unexpectedly.
- **Arrays/Data Handling:** Arrays or appropriate data structures will be used to store, organize, and manage collections of medications, schedules, intake records, and other related system information.


---

<h2>℞☤ Getting Started</h2>

<h3>Prerequisites</h3>

Before running the application, make sure you have:

- Java Development Kit (JDK) installed
- A Java-compatible IDE or code editor
- The MediSync project files

<h3>Installation</h3>

1. Clone the repository:

```bash
git clone https://github.com/fineEpal/MediSync.git
```

2. Open the downloaded project folder in your preferred Java IDE or code editor.

3. Locate the main Java class of the application.

4. Compile and run the application using your development environment.

---

<h2>℞☤ Project Objectives</h2>

MediSync aims to:

- Provide an organized platform for managing medication information.
- Help users maintain consistent medication schedules.
- Allow monitoring and documentation of actual medication intake.
- Support parents, guardians, and caregivers in managing dependents' medication routines.
- Help users monitor remaining medicine supplies.
- Encourage responsible medication management and health awareness.

---

<h2>℞☤ Project Limitations</h2>

MediSync is intended for medication organization and monitoring purposes only. It does not provide medical diagnoses, prescribe medications, recommend treatments, or replace professional medical advice.

The accuracy of the information and records depends on the details entered by users. Medication reminders also depend on the application's configuration and availability.

---

<h2>℞☤ Developed By</h2>

<p align="center">
  <img src="assets/seanjerseyalbo.png" alt="Sean Jersey A. Albo" width="125" height="150">
  <br>
  <b>Sean Jersey A. Albo</b>
</p>

<p align="center">
  <img src="assets/amber.jpg" alt="Amber Sofia C. Ramos" width="125" height="150">
  <br>
  <b>Amber Sofia C. Ramos</b>
</p>

<p align="center">
  <img src="assets/sharmilaroa.jpg" alt="Sharmila C. Roa" width="125" height="150">
  <br>
  <b>Sharmila C. Roa</b>
</p>

<p align="center">
  <i>BS Computer Science Students</i>
  <br>
  Batangas State University – The National Engineering University
  <br>
  College of Informatics and Computing Sciences
  <br>
  Alangilan Campus
</p>

---
<h2>℞☤ Sustainable Development Goal</h2>

<p align="center">
  <a href="https://sdgs.un.org/goals/goal3">
    <img src="https://img.shields.io/badge/SDG_3-Good_Health-4C9F38?style=for-the-badge" alt="SDG 3">
  </a>
</p>

**MediSync** contributes to **Sustainable Development Goal 3 (Good Health and Well-Being)** by:

- 💊 Promoting responsible medication management
- ⏰ Supporting consistent health-related routines
- 📋 Helping individuals and caregivers monitor medication intake

---

<h3 align="center">MEDISYNC</h3>

<p align="center">
  <img src="https://readme-typing-svg.herokuapp.com?font=Fira+Code&weight=600&size=20&duration=3000&pause=1000&color=3DDC84&center=true&vCenter=true&width=450&lines=Your+medications%2C+On+track." alt="Typing SVG" />
</p>


[Java.js]: https://img.shields.io/badge/Java-ED8B00?style=for-the-badge&logo=data:image/svg+xml;base64,PHN2ZyB4bWxucz0iaHR0cDovL3d3dy53My5vcmcvMjAwMC9zdmciIHZpZXdCb3g9IjAgMCAyNCAyNCIgZmlsbD0id2hpdGUiPjxwYXRoIGQ9Ik04Ljg1MSAxOC41NjlzLS45MTcuNTM0LjY1My43MWMxLjkwMi4yMTYgMi44NzcuMTg1IDQuOTc1LS4yMTIgMCAwIC41NTIuMzQ2IDEuMzIxLjY0NC00LjcgMi4wMTQtMTAuNjM2LS4xMTctNi45NDktLjY0MnpNOC4yNzYgMTUuOTNzLTEuMDI5Ljc2Mi41NDMuOTI0YzIuMDMyLjIxIDMuNjM2LjIyNyA2LjQxMi0uMzA4IDAgMCAuMzg0LjM4OS45ODcuNjAxLTUuNjg0IDEuNjYxLTEyLjAxNi4xMy03Ljk0Mi0uNzE3ek0xMy4xMTYgMTEuNDc1YzEuMTU4IDEuMzMzLS4zMDQgMi41MzMtLjMwNCAyLjUzM3MyLjkzOS0xLjUxOCAxLjU4OS0zLjQxOGMtMS4yNTktMS43Ny0yLjIyNC0yLjY1IDMuMDAyLTUuNjgxIDAgMC04LjIwMSAyLjA0OC00LjI4NyA2LjU2NnpNMTkuMzMxIDIwLjUzMXMwIC42NzktLjc4NyAxLjE5N2MtMS40OTYuNTctNi4yMjggMS4yODEtNy41NDMuMDM1LS4yNzQtLjI1OS41NTYtLjYxMi41NTYtLjYxMi0zLjM2Ny43MjktNy4wNjkuOTIxLTguMjc0LjI2NS0uNDM1LS4yMzYuNDMtLjU2My43MTktLjYyOC0uMjcyLjA1OC0uNTkxLjEzMS0uNTkxLjEzMS0yLjEzNS4zMzYtMS4zNDMgMS4zOTQuMzgxIDEuNjI3IDMuNjM4LjQyNSA5LjMzNC4xOSAxMi40ODktLjUyMyAwIDAgLjM2NC4yMzEuODc2LjM1NnptLTkuOTQtNy42NThjLTIuOTY4LS42NzIgMS41NTktMi44NDMgMS41NTktMi44NDMtMi42MjcuMTItNC43NDQgMS41MTYtMy43MjYgMi43MTcgMS4wMjcgMS4yMSAxLjgzNCAxLjE1NSAxLjgzNCAxLjE1NXMtLjkyNC0uMzU3LjMzMy0xLjAyOXptNi4xNjMgNC4zMDhjMi4xMTYtMS4zNjcgMS4xMzctMi42OTkuMjgyLTMuMzAzLS4yMDMtLjE0MS0uNDktLjI3NC0uNDktLjI3NHMuMjA1LjMyMi4xMDIuNjg3Yy0uMjg2Ljk5OC0xLjQ3MSAxLjM1OS0xLjQ3MSAxLjM1OXMuNzYuMzY3IDEuNTc3LjUzMXpNMTUuMDQxIDIuMHMxLjU1NSAxLjU1Ni0xLjQ3NSAzLjk0OWMtMi40MjkgMS45MTktLjU1NCAzLjAxNC0uMDAxIDQuMjY0LTEuNDE5LTEuMjgxLTIuNDYxLTIuNDA4LTEuNzYyLTMuNDU3IDEuMDI3LTEuNTQgMy44NzMtMi4yODUgMy4yMzgtNC43NTZ6Ii8+PC9zdmc+&logoColor=white
[Java-url]: https://www.java.com/

[OOP.js]: https://img.shields.io/badge/OOP-6F42C1?style=for-the-badge&logo=codeforces&logoColor=white
[OOP-url]: https://en.wikipedia.org/wiki/Object-oriented_programming

[AndroidStudio.js]: https://img.shields.io/badge/Android_Studio-3DDC84?style=for-the-badge&logo=android-studio&logoColor=white
[AndroidStudio-url]: https://developer.android.com/studio

[XML.js]: https://img.shields.io/badge/XML-005FAD?style=for-the-badge&logo=xml&logoColor=white
[XML-url]: https://www.w3.org/XML/

