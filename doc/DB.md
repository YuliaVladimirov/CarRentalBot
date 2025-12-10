#### This is the Entity Relationship diagram for the application's database schema:

```mermaid
erDiagram
%%{init: {
  "theme": "default",
  "themeCSS": [
    ".er.relationshipLabel { fill: black; }",
    ".er.relationshipLabelBox { fill: white; }",
    ".er.entityBox { fill: lightgray}",
    "[id^=entity-customers] .er.entityBox { fill: lightgreen;} ",
    "[id^=entity-cars] .er.entityBox { fill: powderblue;} ",
    "[id^=entity-bookings] .er.entityBox { fill: powderblue;} ",
    "[id^=entity-reminders] .er.entityBox { fill: lightgreen;} "
    ]
}}%%

    customers {
        UUID id PK
        BIGINT telegram_user_id  "UNIQUE NOT NULL"
        BIGINT chat_id "UNIQUE NOT NULL"
        VARCHAR(100) username
        VARCHAR(100) first_name "NOT NULL"
        VARCHAR(100) last_name "NOT NULL"
        TIMESTAMP created_at  "NOT NULL DEFAULT CURRENT_TIMESTAMP"
    }
    
    cars {
        UUID id PK
        VARCHAR(100) brand "NOT NULL"
        VARCHAR(100) model "NOT NULL"
        VARCHAR(50) category "NOT NULL (SEDAN, SUV, HATCHBACK, CONVERTIBLE, VAN)"
        VARCHAR(500) description
        VARCHAR(200) image_file_id
        DECIMAL daily_rate "precision:10 scale:2, NOT NULL"
        BOOLEAN available "DEFAULT TRUE"
    }

    bookings {
        UUID id PK
        UUID customer_id FK "NOT NULL"
        UUID car_id FK "NOT NULL"
        DATE start_date "NOT NULL"
        DATE end_date "NOT NULL"
        INTEGER total_days "NOT NULL"
        DECIMAL total_cost "precision:10 scale:2, NOT NULL"
        VARCHAR(20) phone
        VARCHAR(100) email
        VARCHAR(20) status "NOT NULL (CONFIRMED, CANCELLED)"
        TIMESTAMP created_at "NOT NULL DEFAULT CURRENT_TIMESTAMP"
        TIMESTAMP updated_at "NOT NULL"
    }

    reminders {
        UUID id PK
        UUID booking_id FK "NOT NULL"
        TIMESTAMP sent_at "DEFAULT CURRENT_TIMESTAMP"
        VARCHAR(30) reminder_type "NOT NULL (START_DAY_BEFORE, START_DAY_OF, END_DAY_BEFORE, END_DAY_OF)"
        TIMESTAMP due_at "NOT NULL"
        VARCHAR(20) reminder_status "NOT NULL (PENDING, SENT, FAILED, PERMANENTLY_FAILED, CANCELLED)"
        INT retry_count "NOT NULL"
        TIMESTAMP created_at "NOT NULL DEFAULT CURRENT_TIMESTAMP"
    }

    customers ||--o{ bookings : "1 to many"
    cars ||--o{ bookings : "1 to many"
    bookings ||--o{ reminders : "1 to many"
```
