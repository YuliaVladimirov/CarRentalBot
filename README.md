![status: active](https://img.shields.io/badge/status-active-blue)
![development: WIP](https://img.shields.io/badge/development-WIP-yellow)

> 🔧 __Status:__ This project is currently under active development (WIP).  
> Features, structure, and documentation may change.

# Car Rental Bot (Demo Project)
This demo project is a Telegram Bot for car rental services designed to let customers browse cars, make bookings, and manage reservations directly through Telegram. The application built using __Spring Boot__ and works in __webhook mode__ for efficient and secure communication with Telegram.

---

## 📑 Table of Contents
- [Description](#general-description)
- [Technology Stack](#-technology-stack)
- [Project Documentation](#-project-documentation)
- [Getting Started](#-getting-started)
   - [Prerequisites](#-prerequisites)
   - [Installation](#-installation)
   - [Webhook Setup](#-setting-the-webhook)
- [Usage](#-usage)

---
## 📖 General Description
The bot allows customers to browse available cars, make reservations, view and manage their bookings, and receive real-time updates via Telegram.<br>
For administrators, the bot provides tools to manage the car fleet, handle reservations, and track basic rental analytics (planned for future updates).

###  Key Features:
- __Browse Inventory:__  View the list of available cars with category, model, price, and availability information.
- __Booking System:__ Create and cancel reservations. View booking history
- __User Notifications:__ Send real-time booking confirmations and reminders via Telegram and per mail.
- __Admin Tools (Future):__ Interface for managing the car fleet and customer bookings.
- __Security:__ Uses Telegram webhook for secure message delivery.

---

## ⚙️ Technology Stack
This project is implemented as a Spring Boot Web Application using:
- __Java 17__
- __Spring Boot 3.5.5__
- __Spring Data JPA__ (Hibernate)
- __PostgeSQL__ (default DB, configurable)
- __Redis__ (as cache / session store)
- __Liquibase__ (for database schema and migrations)
- __Lombok__ (for boilerplate reduction)
- __Docker / Docker Compose:__ (to simplify local setup)

---

## 📝 Project Documentation
- [Database Structure](doc/DB.md)
- JavaDoc: Full documentation for classes/methods *(to be added)*

---

## 🚀 Getting Started
Use this section to set up the project locally in your IDE  _(IntelliJ IDEA, Eclipse, VS Code)_ if you want to run or modify the source code.

### 📋 Prerequisites
- A __Telegram Bot Token__ from [BotFather](https://t.me/BotFather)
- A __public webhook URL__ (HTTPS) to receive Telegram updates
- Docker installed _(optional but recommended)_

---


## 🛠️ Installation

### 1. Clone the repository:

```[bash]
git clone https://github.com/YuliaVladimirov/CarRentalBot.git
```
### 2. Open the project in your IDE.

### 3. Create configuration files
Two configuration files must be present in your project root:

`.env`<br>
Used for PostgreSQL & Redis when running via Docker Compose:

```[bash]
DB_USER=USERNAME
DB_PASSWORD=PASSWORD
DB_NAME=YOUR_DATABASE_NAME
DB_PORT=YOUR_DATABASE_PORT

REDIS_HOST=YOUR_HOST
REDIS_PORT=REDIS_PORT
REDIS_PASSWORD=REDIS_PASSWORD 
```

`secret.properties`<br>
Contains sensitive credentials (DB secrets, mailing config, Telegram token, etc.)

Use the provided [`secret.properties.example`](secret.properties.example) - rename it to `secret.properties` and fill in your own values.

> ⚠️ __Note__<br>
> Both `secret.properties` and `.env` must be excluded from Git!<br>

### 4. Start PostgreSQL and Redis
You can run them using Docker Compose (recommended):
    
```[bash]
docker-compose up
```
Or run only one service:

__Only PostgreSQL__

```[bash]
docker-compose up db
```

__Only Redis__

```[bash]
docker-compose up redis
```  

On first application launch, __Liquibase__ will automatically apply the schema and insert test data.

### 5. Run the Application
Using Maven:

```[bash]
./mvnw spring-boot:run
```

Or directly from your IDE (Spring Boot run configuration).

---

## 🔗 Setting the Webhook
Once your application is running on a public HTTPS endpoint (e.g. https://my-domain.com), set your webhook:

```[bash]
curl -F "url=https://my-domain.com/[YOUR_CUSTOM_WEBHOOK_PATH]" https://api.telegram.org/bot[YOUR_BOT_TOKEN]/setWebhook
```

Replace:

- `[YOUR_BOT_TOKEN]` – Telegram bot token
- `[YOUR_CUSTOM_WEBHOOK_PATH]` – the controller endpoint you configured

Check status:
```[bash]
https://api.telegram.org/bot[YOUR_BOT_TOKEN]/getWebhookInfo
```

---

## ⌨️ Usage
After the webhook is set, open Telegram and start interacting with your bot.

__User Commands:__

- `/start`  - Initializes the bot and displays a welcome message and the main menu.<br>
- `/main`	- Displays main menu.<br>
- `/help` - Displays the help message.<br>
- `/browse` - Displays available cars for rental.<br>
- `/bookings` - Lists your current active and past bookings.<br>

__Admin Commands (Planned)__

- Fleet management
- Booking administration
- Analytics/statistics