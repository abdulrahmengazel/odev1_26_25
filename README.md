# TCP Chat Application

A robust, multi-threaded TCP Chat Server and Client application built with Java and Swing. This project provides a modern, dark-themed graphical user interface (GUI) for real-time communication between multiple users.

## Features

* **Modern GUI**: A custom dark-themed interface built with Java Swing, featuring distinct chat bubbles for incoming, outgoing, private, and system messages.
* **Multi-user Support**: The server can handle multiple clients concurrently using multi-threading.
* **Public Messaging**: Send messages that are broadcasted to all connected users in the chat room.
* **Private Messaging**: Send private messages to specific users using the `@username` syntax.
* **Auto-complete Mentions**: Type `@` followed by a few characters to see a popup list of matching online users, allowing quick auto-completion.
* **File Sharing**: Share files with other users in the chat. Users are prompted with a download confirmation when a file is shared.
* **Online User List**: See a real-time list of all currently connected users.
* **Server Logging**: The server UI provides a detailed log of events, connections, disconnections, and errors.

## Architecture

The project follows a clean architecture by separating the User Interface (UI) from the Business Logic:

* **Config**: Contains shared constants like colors, fonts, and reusable UI component builders.
* **Logic**: Contains the networking logic (`ChatClientLogic` and `ChatServerLogic`), handling socket connections, input/output streams, and protocol parsing.
* **UI**: Contains the graphical components (`ChatClientUI` and `ChatServerUI`), rendering the chat windows, buttons, and handling user inputs.
* **Main**: Contains the entry points for launching the Server and Client applications.

## How to Run

1. **Compile the project**: Ensure you have a Java Development Kit (JDK) installed. Compile the `.java` files located in the `src` directory.
2. **Start the Server**: Run the `Main.ChatServer` class.
    * Enter a port number (default is 5555) and click "Start Server".
3. **Start the Client(s)**: Run the `Main.ChatClient` class. You can run multiple instances to simulate different users.
    * Enter the Server's IP address (use `localhost` if running on the same machine).
    * Enter the same Port number the server is listening on.
    * Choose a unique Username.
    * Click "Connect".

## Usage

* **Sending a Public Message**: Type your message in the input field and press Enter or click the "Send" button.
* **Sending a Private Message**: Double-click a user in the "Online Users" list, or type `@username` followed by a space and your message.
* **Sending a File**: Click the "File" (paperclip) button, select a file (up to 10MB), and it will be broadcasted to the chat.
* **Disconnecting**: Click the "Disconnect" button to leave the chat room.

## Requirements

* Java 8 or higher.