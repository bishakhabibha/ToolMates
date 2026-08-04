# ToolMates Codebase Summary

This document explains the project in plain language. It focuses first on `src/main/java` and `src/main/resources`, then explains Maven and the supporting project files.

## Big Picture

- Project type:
  Summary: ToolMates is a Spring Boot web application. The backend is Java, the frontend is mostly static HTML/CSS/JavaScript files served from `src/main/resources/static`, and the database is MySQL accessed with Spring JDBC.

- Main user journey:
  Summary: A CUET student signs up or logs in, lists tools they own, browses tools listed by others, sends rental requests, chats with other users, receives notifications, confirms pickup/payment/return steps, reviews users, and reports problems.

- Backend structure:
  Summary: Controllers receive HTTP requests, services contain business rules, repositories/DAOs run SQL queries, models represent database-backed data, and DTOs represent request bodies sent from the browser.

- Frontend structure:
  Summary: Each HTML file is a complete page. The JavaScript inside those pages calls backend endpoints such as `/api/auth/login`, `/api/tools/recent`, `/api/rent-requests/...`, `/api/messages/...`, and `/api/notifications/...`.

- Database structure:
  Summary: `schema.sql` creates the main tables: users, tools, tool images, rental requests, notifications, messages, reports, reviews, rental logs, and moderation history.

## Main Backend Flow

- Request flow:
  Summary: Browser JavaScript sends an HTTP request to a controller. The controller checks session data and basic input, then calls a service. The service applies project rules, then uses DAO classes to read or update MySQL.

- Authentication:
  Summary: Login creates an HTTP session containing `userId`, `studentId`, and `email`. Later protected actions read those session values to know who is acting.

- Tool listing:
  Summary: A logged-in user creates a tool. The backend sets the owner from the current session instead of trusting owner fields from the browser.

- Rental workflow:
  Summary: A borrower requests a tool, the owner accepts or rejects it, pickup details are submitted, both sides confirm pickup and advance payment, the rental becomes active, then both sides confirm return.

- Notifications and scheduler:
  Summary: `RentalDeadlineScheduler` runs repeatedly in the background. It sends reminders and moves rentals into pickup or return confirmation states when times are reached.

## `src/main/java`

### Root Application

- `src/main/java/com/javaproj/ToolMates/ToolMatesApplication.java`
  Summary: This is the Spring Boot entry point. `main` starts the application, `@SpringBootApplication` enables component scanning and auto-configuration, and `@EnableScheduling` turns on scheduled background jobs such as rental deadline checks.

### Configuration

- `src/main/java/com/javaproj/ToolMates/config/SecurityConfig.java`
  Summary: Configures Spring Security. It disables CSRF, enables CORS, creates a BCrypt password encoder, permits the public pages and selected API paths, and adds a custom session filter for protected API actions.

  Summary: The custom filter returns `401 Not logged in.` when a protected `/api/...` endpoint is called without a valid session. Public tool browsing and public profile viewing are allowed, but actions such as creating rentals, messaging, notifications, and editing tools require login.

### Controllers

- `src/main/java/com/javaproj/ToolMates/auth/controller/AuthController.java`
  Summary: Exposes authentication endpoints under `/api/auth`. It handles signup, login, logout, and `/me`, which returns the currently logged-in user's session/profile basics.

  Summary: It catches duplicate signup fields, invalid credentials, and validation errors, then returns user-friendly JSON responses.

- `src/main/java/com/javaproj/ToolMates/auth/controller/ToolController.java`
  Summary: Exposes tool APIs under `/api/tools`. It supports adding a tool, loading recent tools, searching tools, opening one tool by id, updating a tool, and unlisting a tool.

  Summary: Create, update, and unlist actions depend on the logged-in user's session. Read actions are public so visitors can browse listings.

- `src/main/java/com/javaproj/ToolMates/auth/controller/RentalRequestController.java`
  Summary: Exposes the rental workflow under `/api/rent-requests`. It creates rental requests, accepts/rejects them, submits pickup details, confirms pickup/payment/return, cancels rentals, and handles reports.

  Summary: This controller is thin: it mostly reads `userId` from the session and delegates the real workflow rules to `RentalRequestService`.

- `src/main/java/com/javaproj/ToolMates/auth/controller/RentalHistoryController.java`
  Summary: Exposes rental history under `/api/rental-history`. It returns borrowed rentals, lent rentals, rental detail pages, timeline entries, and progress data.

  Summary: It supports search, filter, sort, page, and size query parameters for history lists.

- `src/main/java/com/javaproj/ToolMates/auth/controller/MessageController.java`
  Summary: Exposes chat APIs under `/api/messages`. It sends messages, loads chat history for a rental, lists the user's conversations, and opens a conversation by user id or student id.

  Summary: It always checks that the current user is logged in. For rental-specific chat, the service checks that the sender and receiver belong to that rental.

- `src/main/java/com/javaproj/ToolMates/auth/controller/NotificationController.java`
  Summary: Exposes notification APIs under `/api/notifications`. It loads notifications, counts unread notifications, and marks the logged-in user's notifications as read.

  Summary: It supports both a `/me` style endpoint and a student-id endpoint that only allows users to view their own notifications.

- `src/main/java/com/javaproj/ToolMates/auth/controller/ProfileController.java`
  Summary: Exposes profile APIs under `/api/profile`. It loads a public profile by student id, loads the current user's profile, and lets the current user update their bio.

  Summary: Public profile viewing includes listed tools, ratings, reports received, and rental counts.

- `src/main/java/com/javaproj/ToolMates/auth/controller/ReviewController.java`
  Summary: Exposes review APIs under `/api/reviews`. It submits reviews, returns the leaderboard, and returns all reviews newest first.

  Summary: Submitting a review validates rating range, prevents self-review, resolves reviewed users by student id when needed, and requires the tool name to match an existing listed tool exactly.

- `src/main/java/com/javaproj/ToolMates/auth/controller/PageRouteController.java`
  Summary: Maps friendly routes such as `/`, `/dashboard`, `/login`, `/signup`, `/rental-history`, and `/rental-details` to static HTML files.

  Summary: It also returns an empty response for `/favicon.ico` so missing favicon requests do not create noisy errors.

### Services

- `src/main/java/com/javaproj/ToolMates/auth/service/AuthService.java`
  Summary: Contains signup, login, and logout logic. It validates CUET student emails, checks duplicate student IDs/emails, hashes passwords with BCrypt, and stores login data in the HTTP session.

  Summary: It allows login by student ID or email and supports "remember me" by extending the session lifetime.

- `src/main/java/com/javaproj/ToolMates/auth/service/ToolService.java`
  Summary: Contains business logic for tool listings. It sets owner details from the logged-in user, validates required tool fields, fetches recent/search results, updates tools, and unlists tools.

  Summary: It protects owner-only actions by comparing the logged-in student id with the tool's stored owner id.

- `src/main/java/com/javaproj/ToolMates/auth/service/RentalRequestService.java`
  Summary: This is the core rental workflow engine. It creates requests, validates dates and durations, calculates rent amounts, moves rentals through statuses, creates notifications, records confirmations, and logs status transitions.

  Summary: Important statuses include `PENDING`, `OWNER_ACCEPTED`, `PICKUP_DETAILS_SUBMITTED`, `PICKUP_SCHEDULED`, `PICKUP_CONFIRMATION`, `ADVANCE_PAYMENT_CONFIRMATION`, `ACTIVE`, `AWAITING_RETURN`, `RETURN_CONFIRMATION`, `COMPLETED`, `CANCELLED`, `REJECTED`, and dispute/report states.

  Summary: It uses transactions for critical rental state changes so related database updates happen together. This matters for actions like accepting rentals, confirming pickup/payment, and completing returns.

- `src/main/java/com/javaproj/ToolMates/auth/service/RentalHistoryService.java`
  Summary: Builds rental history data directly with `JdbcTemplate`. It produces borrowed/lent lists, detail maps, timelines from action/status logs, progress values, and grouped/paginated results.

  Summary: It translates database rows into frontend-friendly fields such as tool name, owner/borrower names, status group, money values, dates, and past rentals.

- `src/main/java/com/javaproj/ToolMates/auth/service/RentalDeadlineScheduler.java`
  Summary: Runs periodically using `@Scheduled`. It creates pickup reminders, starts pickup confirmation when pickup time passes, sends payment and pickup confirmation reminders, warns before rentals end, and starts return workflows.

  Summary: It reads timing values from application properties and has demo-mode timing options so rental lifecycles can be tested faster.

- `src/main/java/com/javaproj/ToolMates/auth/service/RentalSchemaInitializer.java`
  Summary: Runs after startup and ensures newer rental workflow columns/indexes exist in the database. It adds missing columns such as pickup instructions, confirmation fields, notification flags, and scheduler indexes.

  Summary: This protects older databases from failing when newer code expects additional columns.

- `src/main/java/com/javaproj/ToolMates/auth/service/MessageService.java`
  Summary: Handles messaging logic. It resolves sender/receiver IDs, validates message text, prevents messaging yourself, checks rental chat permissions, saves messages, marks read conversations, and creates message notifications.

  Summary: Messages can be general user-to-user conversations or tied to a rental request.

- `src/main/java/com/javaproj/ToolMates/auth/service/ProfileService.java`
  Summary: Builds profile pages. It loads user information, average rating, listed tools, report counts, successful lending/borrowing counts, and lets a logged-in user update their bio.

  Summary: It limits bios to 500 characters and returns the updated full profile after saving.

### Repositories / DAO Classes

- `src/main/java/com/javaproj/ToolMates/auth/repository/UserDao.java`
  Summary: Performs SQL operations for users. It checks existence by student id/email, finds users by id/student id/email, saves new users, updates passwords, updates bios, and maps SQL rows into `User` objects.

  Summary: This project uses manual JDBC queries instead of Spring Data repository interfaces for most data access.

- `src/main/java/com/javaproj/ToolMates/auth/repository/ToolDao.java`
  Summary: Performs SQL operations for tools. It inserts tools and their images, finds recent tools, searches active tools, finds a tool by id, finds tools by owner, updates tools, unlists tools, and checks exact tool-name existence.

  Summary: Tool images are stored separately in `tool_images`, so `ToolDao` loads image URLs with an additional query for each tool.

- `src/main/java/com/javaproj/ToolMates/auth/repository/RentalRequestDao.java`
  Summary: Performs SQL operations for rental requests. It inserts requests, loads requests with owner/borrower ids, locks rows for updates, changes statuses, records pickup/payment/return confirmations, starts rentals, closes/rejects rentals, and finds rentals due for scheduler actions.

  Summary: It also counts successful rentals for profiles and writes rental action/status logs.

- `src/main/java/com/javaproj/ToolMates/auth/repository/NotificationDao.java`
  Summary: Performs SQL operations for notifications. It creates notifications, avoids duplicate notifications where needed, replaces old notifications, checks whether notifications exist, loads detailed notification data, counts unread notifications, and marks notifications read.

  Summary: Detailed notification loading joins rental requests, tools, owners, and borrowers so the dashboard can display rich notification cards.

- `src/main/java/com/javaproj/ToolMates/auth/repository/MessageDao.java`
  Summary: Performs SQL operations for messages. It saves messages, loads rental chat history, loads direct conversations between two users, marks conversations read, and returns conversation summaries with last message and unread count.

- `src/main/java/com/javaproj/ToolMates/auth/repository/ReviewDao.java`
  Summary: Performs SQL operations for reviews. It saves reviews, calculates average ratings, returns a top-three leaderboard, and returns all reviews with reviewer/reviewed user names.

- `src/main/java/com/javaproj/ToolMates/auth/repository/ReportDao.java`
  Summary: Performs SQL operations for user reports. It checks whether a user already reported a rental, counts reports received by a user, and saves report records.

### Models

- `src/main/java/com/javaproj/ToolMates/auth/model/User.java`
  Summary: Represents a user account. It stores identity, CUET student details, contact details, hashed password, account metadata, avatar URL, bio, and activity counters.

- `src/main/java/com/javaproj/ToolMates/auth/model/Tool.java`
  Summary: Represents a listed tool. It stores owner data, name, category, condition, daily price, maximum rental period, pickup location, description, extra info, active/unlisted state, and image URLs.

- `src/main/java/com/javaproj/ToolMates/auth/model/RentalRequest.java`
  Summary: Represents a rental transaction. It stores tool id, owner/borrower ids, requested dates, status, pickup details, confirmation states, payment totals, active rental time, return confirmations, and scheduler notification flags.

- `src/main/java/com/javaproj/ToolMates/auth/model/Notification.java`
  Summary: Represents a notification sent to a user. It stores notification id, user id, optional rental id, message text, notification type, read state, and creation time.

- `src/main/java/com/javaproj/ToolMates/auth/model/Message.java`
  Summary: Represents a chat message. It stores message id, optional rental id, sender id, receiver id, message text, sent time, and read state.

### DTOs

- `src/main/java/com/javaproj/ToolMates/auth/dto/SignupRequest.java`
  Summary: Request body for signup. It validates names, student id format, CUET student email, phone number, and password length before registration.

- `src/main/java/com/javaproj/ToolMates/auth/dto/LoginRequest.java`
  Summary: Request body for login. It contains username, password, and remember-me flag. Username can be either student id or email.

- `src/main/java/com/javaproj/ToolMates/auth/dto/ToolCreationRequest.java`
  Summary: Older or alternative DTO for tool creation. It contains fields for owner, tool details, image URLs, description, and additional info. The current `ToolController` accepts the `Tool` model directly instead.

- `src/main/java/com/javaproj/ToolMates/auth/dto/RentalRequestCreateRequest.java`
  Summary: Request body for creating a rental request. It contains the tool id, renter name/student id, requested start date, rental duration, and optional message.

- `src/main/java/com/javaproj/ToolMates/auth/dto/PickupScheduleRequest.java`
  Summary: Request body for pickup scheduling. It contains pickup date, pickup time, pickup location, and pickup instructions.

- `src/main/java/com/javaproj/ToolMates/auth/dto/ConfirmationRequest.java`
  Summary: Small request body used by pickup, payment, and return confirmation endpoints. It contains one Boolean field named `confirmed`.

- `src/main/java/com/javaproj/ToolMates/auth/dto/MessageRequest.java`
  Summary: Request body for sending chat messages. It can identify sender/receiver by numeric user ids or student ids, can optionally include a rental request id, and carries the message text.

- `src/main/java/com/javaproj/ToolMates/auth/dto/ReviewRequest.java`
  Summary: Request body for user reviews. It contains reviewer/reviewed user identifiers, reviewed student id, exact tool name, star rating, and review text.

- `src/main/java/com/javaproj/ToolMates/auth/dto/ReportRequest.java`
  Summary: Request body for reporting another user. It contains reporter/reported ids, rental id, tool id, reason category, details, and optional evidence URL.

### Exceptions

- `src/main/java/com/javaproj/ToolMates/auth/exception/DuplicateFieldException.java`
  Summary: Custom runtime exception used during signup when a student id or email already exists. It carries both the field name and message so the frontend can show the right error.

- `src/main/java/com/javaproj/ToolMates/auth/exception/InvalidCredentialsException.java`
  Summary: Custom runtime exception used when login credentials are wrong. It returns a consistent invalid login message.

## `src/main/resources`

### Spring Configuration And SQL

- `src/main/resources/application.properties`
  Summary: Configures server port, forwarded headers, optional `.env` import, MySQL connection, SQL initialization, CORS origins, session cookies, scheduler timing, demo-mode timing, Jetty request size, and multipart upload limits.

  Summary: Important note: this file currently contains concrete database connection values. In a safer setup, secrets should come from environment variables or `.env`, not be committed in plain text.

- `src/main/resources/schema.sql`
  Summary: Creates the full database schema if tables do not exist. It defines users, tools, tool images, rental requests, notifications, messages, user reports, user reviews, rental action logs, rental status logs, and moderation history.

  Summary: This file is run by Spring SQL initialization because `spring.sql.init.mode` defaults to `always` unless overridden.

- `src/main/resources/db-migration-rental-daily-chat.sql`
  Summary: Migration script for older databases. It renames hourly price to daily price, adds user profile fields, creates notifications/messages/reports/reviews/log tables, and adds many rental workflow columns.

  Summary: This looks like a manual migration companion to `schema.sql`, useful when upgrading an already-existing database.

- `src/main/resources/db-migration-pickup-rental-activation.sql`
  Summary: MySQL migration script that creates temporary stored procedures to add missing rental workflow columns and indexes only if they do not already exist.

  Summary: It focuses on pickup instructions, pickup/payment confirmation fields, scheduler flags, and scheduler indexes.

### Static HTML Pages

- `src/main/resources/static/dashboard.html`
  Summary: Main landing/dashboard page. It shows navigation, hero carousel, categories, recent tools, authentication-aware UI, notifications, rental action modals, message drawer, search, and logout behavior.

  Summary: This page calls many APIs: auth `/me`, tools `/recent` and `/search`, notifications, rent request actions, and messages.

- `src/main/resources/static/login.html`
  Summary: Login page. It submits username/password/remember-me to `/api/auth/login`, stores basic user details in `localStorage`, and redirects to `/dashboard` on success.

- `src/main/resources/static/signup.html`
  Summary: Signup page. It validates signup fields client-side, shows password strength, posts to `/api/auth/signup`, and redirects to `/login` after successful account creation.

- `src/main/resources/static/Browse Tools.html`
  Summary: Browse/search page for tools. It loads recent tools from `/api/tools/recent`, supports local sorting/filtering/search UI, and opens `ToolInfo.html?id=...` when a card is clicked.

- `src/main/resources/static/ToolInfo.html`
  Summary: Tool detail page. It loads one tool from `/api/tools/{id}`, renders images and details, estimates rental cost, sends rental requests, lets owners edit/unlist their tool, and supports messaging the owner.

- `src/main/resources/static/ListATool.html`
  Summary: Tool listing form. It collects tool details and images, previews selected images, sends the listing to `/api/tools/add`, and returns to the dashboard after success.

- `src/main/resources/static/RentalHistory.html`
  Summary: Rental history list page. It switches between borrowed and lent rentals, supports search/filter/sort/pagination, fetches `/api/rental-history/borrowed` or `/lent`, and opens detail pages.

- `src/main/resources/static/RentalDetails.html`
  Summary: Rental detail page. It loads rental details and timeline, shows progress/actions, allows confirmation actions through rent-request endpoints, and links to pickup details, messaging, reports, and reviews.

- `src/main/resources/static/PickupDetails.html`
  Summary: Pickup details page. It loads scheduled pickup information for a rental, displays owner/borrower/payment/pickup details, and can open a message thread with the owner.

- `src/main/resources/static/ReportUser.html`
  Summary: Report form page. It loads rental/report context, renders selectable report reasons, validates "Other" details, and posts reports to `/api/rent-requests/{id}/report`.

- `src/main/resources/static/Profile.html`
  Summary: User profile page. It loads either `/api/profile/me` or `/api/profile/{studentId}`, shows profile stats and listed tools, and lets the owner edit their bio.

- `src/main/resources/static/UserReviews.html`
  Summary: Reviews page. It lets logged-in users submit reviews, loads the top leaderboard from `/api/reviews/leaderboard`, and lists all reviews from `/api/reviews`.

- `src/main/resources/static/WhyToolMates.html`
  Summary: Informational page explaining why ToolMates exists and why students might use the platform.

- `src/main/resources/static/CommunityGuide.html`
  Summary: Informational page describing expected community behavior and rules for using ToolMates.

- `src/main/resources/static/TermsOfService.html`
  Summary: Static terms page describing service rules and responsibilities.

- `src/main/resources/static/PrivacyPolicy.html`
  Summary: Static privacy page describing how user information is handled.

## Maven And Java Project Files

- `pom.xml`
  Summary: Maven build file. It declares the project coordinates, Java 21, Spring Boot parent version, dependencies, and the Spring Boot Maven plugin.

  Summary: Main dependencies are Spring Boot Web for controllers/static serving, Spring Boot Security for sessions/passwords/security filters, Spring Data JDBC/JdbcTemplate support for database access, MySQL Connector/J for MySQL, Validation for DTO annotations, spring-dotenv for `.env`, and test dependencies.

- `mvnw` and `mvnw.cmd`
  Summary: Maven wrapper scripts. They let the project run Maven commands without requiring a globally installed Maven version. Use `mvnw.cmd` on Windows and `./mvnw` on Unix-like shells.

- `.mvn/`
  Summary: Maven wrapper support directory. It usually contains wrapper configuration so `mvnw` knows which Maven distribution to use.

- `target/`
  Summary: Maven build output directory. Compiled classes, packaged artifacts, and generated build files appear here after building or running tests. It should generally not be edited by hand.

- `Dockerfile`
  Summary: Container build file for packaging/running the Spring Boot app in Docker. It is useful for deployment environments that build from Docker.

- `.dockerignore`
  Summary: Tells Docker which files to ignore when building an image, reducing image build context and avoiding unnecessary files.

- `.env`
  Summary: Local environment configuration file imported by Spring through `spring.config.import`. It can override application properties locally.

- `.env.example`
  Summary: Template showing which environment variables/config values a developer may need without necessarily exposing real local values.

- `.gitignore`
  Summary: Lists files Git should ignore, such as build output, IDE files, logs, or local-only files.

- `.gitattributes`
  Summary: Git metadata file that controls text handling and other repository attributes.

- `HELP.md`
  Summary: Spring-generated help/documentation file. It usually lists useful links and basic build/run guidance from the Spring initializer.

- `.idea/`
  Summary: IntelliJ IDEA project settings. These files help the IDE understand the project but are not part of runtime application logic.

- `.agents/` and `.codex/`
  Summary: Local tooling directories related to assistant/agent workflows. They are not part of the Spring Boot runtime.

## How Everything Holds Together

- Startup:
  Summary: `ToolMatesApplication` starts Spring Boot. Spring scans the package, creates beans for controllers/services/DAOs/configuration, loads `application.properties`, connects to MySQL, runs SQL initialization, and starts the embedded web server.

- Web serving:
  Summary: Spring serves files from `src/main/resources/static` automatically. `PageRouteController` adds friendlier route names that forward to those HTML files.

- API serving:
  Summary: `@RestController` classes expose JSON endpoints. The static pages use `fetch()` to call those endpoints and update the page dynamically.

- Security:
  Summary: Signup and login are public. After login, the session stores the user identity. Protected API actions check the session through `SecurityConfig` and then controllers/services use that identity for authorization.

- Data access:
  Summary: DAO classes use `JdbcTemplate` and handwritten SQL. Models are simple Java classes with fields and getters/setters. The database schema controls the actual table structure.

- Rental state:
  Summary: `RentalRequestService` is the main source of truth for allowed rental transitions. `RentalRequestDao` persists those transitions, while `NotificationDao` creates messages that drive the dashboard UI.

- Background work:
  Summary: `RentalDeadlineScheduler` keeps the rental lifecycle moving without a user click. It detects due pickups, pending confirmations, upcoming rental endings, and overdue returns.

- Frontend state:
  Summary: The browser keeps some login/user information in `localStorage` and temporary conversation-opening state in `sessionStorage`. The real permission source is still the backend session.

## Suggested Reading Order

- Start with:
  Summary: Read `ToolMatesApplication.java`, `SecurityConfig.java`, `AuthController.java`, `AuthService.java`, and `schema.sql` first. These explain startup, security, login, and data shape.

- Then read tool features:
  Summary: Read `ToolController.java`, `ToolService.java`, `ToolDao.java`, `Tool.java`, `dashboard.html`, `Browse Tools.html`, `ToolInfo.html`, and `ListATool.html`.

- Then read rental features:
  Summary: Read `RentalRequestController.java`, `RentalRequestService.java`, `RentalRequestDao.java`, `RentalDeadlineScheduler.java`, `RentalHistoryService.java`, `RentalHistory.html`, `RentalDetails.html`, and `PickupDetails.html`.

- Then read social/trust features:
  Summary: Read `MessageController.java`, `MessageService.java`, `NotificationController.java`, `NotificationDao.java`, `ReviewController.java`, `ReviewDao.java`, `ReportDao.java`, `UserReviews.html`, and `ReportUser.html`.

