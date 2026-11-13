# fineEpal-MediSync
 <p align="center">
  <img src="assets/medisync-banner.jpg" alt="MediSync Banner" width="100%">
</p>

<h1 align="center">🩺 MediSync</h1>

<h3 align="center">Medication Monitoring and Management System</h3>

<p align="center">
  <i>Your medications, On track.</i>
</p>

<p align="center">
  A Java-based application designed to help individuals, parents, guardians, and family caregivers organize and monitor medication routines.
</p>

---

<h2>✦ Overview</h2>

**MediSync** is a Java-based medication monitoring and management application designed to provide users with a centralized and organized platform for managing their medication needs. It aims to simplify the process of keeping track of multiple medications by allowing users to record essential medication information, organize dosage schedules, monitor actual medication intake, maintain intake history, and track remaining medicine supplies.

The system addresses common challenges associated with medication management, such as forgetting scheduled doses, keeping track of different medication schedules, monitoring missed or skipped intake, and determining when medicine supplies need replenishment. Through its integrated features, MediSync enables users to manage medication routines more efficiently, receive timely reminders, monitor medication adherence, and keep important health-related information accessible in one place.

MediSync is designed not only for individuals managing their own medications but also for **parents, guardians, and family caregivers responsible for managing the medications of their dependents**. Users can choose to manage their personal medication records or oversee the medication routines of others, including children aged 10 years old and below and elderly individuals who require assistance.

By integrating medication management, scheduling, intake tracking, supply monitoring, and appointment reminders, MediSync promotes a more systematic approach to medication monitoring and encourages consistency in health-related routines.

---

<h2>✦ Key Features</h2>

- 💊 **Medication Management** – Allows users to record, organize, and manage essential medication information.
- 🕒 **Medication Scheduling** – Helps users organize dosage schedules and medication routines.
- 📋 **Medication Intake Tracking** – Enables users to record medication intake and monitor taken, missed, or skipped doses.
- 📖 **Medication History** – Maintains records of medication intake for easier monitoring and reference.
- 📦 **Medicine Supply Monitoring** – Helps users monitor remaining medicine quantities and identify when supplies need replenishment.
- 🔔 **Reminders and Notifications** – Supports medication and appointment reminders.
- 👨‍👩‍👧 **Guardian and Caregiver Support** – Allows parents, guardians, and family caregivers to manage medication records and routines on behalf of their dependents.

---

<h2>✦ Technologies Used</h2>

- **Java** – Primary programming language
- **Object-Oriented Programming (OOP)** – Application structure and implementation
- **Android Studio** – Integrated Development Environment (IDE)
- **XML** – User interface layout and design
---

<h2>✦ Project Structure</h2>

```text
MediSync/
│
├── assets/
│   ├── medisync-banner.png
│   ├── sharmilaroa.jpg
│   ├── amber.jpg
│   ├── sean.jpg
│   ├── medisync-dashboard.png
│   ├── medisync-medications.png
│   ├── medisync-schedule.png
│   └── sdg3.png
│
├── src/
│   └── [Java source files]
│
└── README.md
```

<h3>Core Classes</h3>

| Class | Responsibility |
| :--- | :--- |
| `User` | Represents the MediSync user and stores basic account information. |
| `Medication` | Stores and manages details of each medication. |
| `MedicationSchedule` | Manages the dates and times when a medication should be taken. |
| `IntakeRecord` | Records actual medication intake and maintains history. |
| `Appointment` | Stores and manages follow-up medical appointments. |
| `Reminder` | Handles reminders for schedules, low supply, and appointments. |

---

<h2>✦ Object-Oriented Programming Concepts</h2>

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

<h2>✦ Application Screenshots</h2>

<p align="center">
  <img src="assets/medisync-dashboard.png" alt="MediSync Dashboard" width="85%">
  <br>
  <i>Dashboard</i>
</p>

<p align="center">
  <img src="assets/medisync-medications.png" alt="Medication Management Interface" width="85%">
  <br>
  <i>Medication Management</i>
</p>

<p align="center">
  <img src="assets/medisync-schedule.png" alt="Medication Schedule Interface" width="85%">
  <br>
  <i>Medication Scheduling</i>
</p>

---

<h2>✦ Getting Started</h2>

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

<h2>✦ Project Objectives</h2>

MediSync aims to:

- Provide an organized platform for managing medication information.
- Help users maintain consistent medication schedules.
- Allow monitoring and documentation of actual medication intake.
- Support parents, guardians, and caregivers in managing dependents' medication routines.
- Help users monitor remaining medicine supplies.
- Encourage responsible medication management and health awareness.

---

<h2>✦ Project Limitations</h2>

MediSync is intended for medication organization and monitoring purposes only. It does not provide medical diagnoses, prescribe medications, recommend treatments, or replace professional medical advice.

The accuracy of the information and records depends on the details entered by users. Medication reminders also depend on the application's configuration and availability.

---

<h2>✦ Developed By</h2>

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

<h2>✦ Sustainable Development Goal</h2>

<p align="center">
  <img src="assets/sdg3.png" alt="United Nations SDG 3: Good Health and Well-Being" width="180">
</p>

<h3 align="center">SDG 3: Good Health and Well-Being</h3>

MediSync contributes to **Sustainable Development Goal 3** by promoting responsible medication management, supporting consistent health-related routines, and helping individuals and caregivers become more organized in monitoring medication intake.

---

<p align="center">
  <b>MEDISYNC</b>
  <br>
  <i>Your medications, On track.</i>
</p>
