I am developing an Android app using the Kotlin Android Template(https://github.com/cortinico/kotlin-android-template). This app should allow users to select and send files to a Telegram channel using the Telegram Bot API.

# Core Requirements:

User Interface: The app should have a simple UI with a single button. When clicked, it should open the native Android file selector.

File Selection: Users can select 1 to 10 files, each ≤500MB, of any type. The app should enforce this limit before allowing file selection.

File Sending: Once files are selected, the app should close and send them in the background, ensuring the user does not need to keep the app open.

File Integrity: Files should be sent with their original names preserved.

Background Execution: The app must run a reliable background service to handle file uploads, ensuring stability across Android 8+ devices.

Error Handling & Optimization: The app should be highly optimized, with robust error handling for file access, network failures, and API limits.

Permissions: Request all required permissions at app launch for seamless operation.


# Technical Considerations:

Add other modules incrementally as needed

Use the Telegram Bot API for file uploads (strictly no polling or webhooks).

Ensure compatibility with Android 8+ (Oreo and later).

Efficient parallel uploads to speed up file sending.

Analyze the current project structure and build.gradle configuration, then suggest an implementation plan before proceeding.

Include necessary dependencies for background execution, file handling, and network optimization.
