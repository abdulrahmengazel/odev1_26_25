# TCP Socket Chat Application

A robust, multi-threaded TCP Chat Server and Client application built with Java Swing. This project demonstrates professional-grade network programming with real-time multi-user communication capabilities.

## ✨ Features

### Core Functionality
* **Multi-Client Server**: Concurrent client management using ConcurrentHashMap and thread pools
* **Public Messaging**: Messages broadcast to all connected users with timestamps
* **Private Messaging**: Target-specific users with `@username` syntax and autocomplete
* **File Sharing**: Base64-encoded file transfer with 10MB limit
* **User Management**: Real-time online user list with join/leave notifications
* **Protocol-Based**: Command-driven communication (JOIN, MSG, FILE, EXIT, etc.)

### User Interface
* **Modern Dark Theme**: Custom Swing components with professional styling
* **Chat Bubbles**: Distinct visual separation for different message types
* **Auto-complete Mentions**: Intelligent dropdown for @mentions while typing
* **Responsive Layout**: Two-row flexible layouts that adapt to window resizing
* **Comprehensive Logging**: Timestamped event logs in both server and client UIs

### Advanced Features
* **Server Announcements**: Admin broadcast messages to all connected clients
* **Multi-threaded Architecture**: Safe concurrent operations with proper synchronization
* **Error Handling**: Graceful failure messages and connection validation
* **Auto-Start Capability**: Launch multiple client instances sequentially from Main.java

## 📋 Project Statistics

| Metric | Value |
|--------|-------|
| Java Files | 8 |
| Total Lines of Code | 1,272 |
| Packages | 4 (config, Logic, UI, Main) |
| Thread Safety | ✅ ConcurrentHashMap + Synchronized |
| Protocol Commands | 9 types |
| Max Clients Supported | 100+ (scalable) |

## 🏗️ Architecture

```
┌─────────────────────────────────────┐
│         Main Entry Points           │
│  ChatServer.java | ChatClient.java  │
└────────────┬────────────────────────┘
             │
    ┌────────┴─────────┐
    │                  │
┌───▼──────────┐  ┌────▼──────────┐
│   UI Layer   │  │   Logic Layer  │
├──────────────┤  ├────────────────┤
│ ChatServerUI │  │ ChatServerLogic│
│ ChatClientUI │  │ ChatClientLogic│
└──────────────┘  └────────────────┘
    │                  │
    └────────┬─────────┘
             │
         ┌───▼──────────┐
         │ Config Layer │
         │   Config.java │
         └────────────────┘
```

## 🚀 Quick Start

### Compilation
```bash
cd "/path/to/odev1_26_25"
javac -encoding UTF-8 -d out $(find src -name "*.java")
```

### Run (Automatic - 3 Clients)
```bash
java -cp out Main.Main
```

### Run (Manual - Separate Terminals)
```bash
# Terminal 1 - Server
java -cp out Main.ChatServer

# Terminal 2-4 - Clients
java -cp out Main.ChatClient
java -cp out Main.ChatClient
java -cp out Main.ChatClient
```

## 📖 Usage Guide

### Connecting to Server
1. Start `ChatClient`
2. Enter server IP (default: `localhost`)
3. Enter port (default: `5555`)
4. Choose a unique username
5. Click "Connect"

### Sending Messages
| Action | Method |
|--------|--------|
| Public Message | Type message → Press Enter or click Send |
| Private Message | Type `@username message` → Send |
| Auto-complete | Type `@` → Select from dropdown → Enter |
| File Share | Click 📎 File → Select file → Send |
| Disconnect | Click Disconnect button |

### Server Management
| Action | Method |
|--------|--------|
| Start Server | Enter port → Click "▶ Start Server" |
| Send Announcement | Type in Announcement field → Click Send |
| View Logs | Check Server Log panel |
| Stop Server | Click "■ Stop Server" |

## 🔌 Protocol Specification

### Commands

| Command | Format | Example |
|---------|--------|---------|
| **JOIN** | `JOIN\|<username>` | `JOIN\|Alice` |
| **MSG** | `MSG\|<message>` | `MSG\|Hello everyone` |
| **PRIVATE** | `MSG\|@<user> <message>` | `MSG\|@Bob Secret message` |
| **FILE** | `FILE\|<name>\|<base64>` | `FILE\|doc.pdf\|[base64...]` |
| **EXIT** | `EXIT` | `EXIT` |

### Server Responses

| Response | Format | Meaning |
|----------|--------|---------|
| **USERLIST** | `USERLIST\|user1,user2,...` | Online users broadcast |
| **LOG** | `LOG\|<message>` | System notification |
| **ERROR** | `ERROR\|<message>` | Error notification |
| **SERVER** | `SERVER\|<announcement>` | Admin broadcast |
| **PRIVATE** | `PRIVATE\|<from>\|<time>\|<body>` | Private message received |

## 🧪 Test Scenarios

### Test 1: Server Startup
```
✓ Launch ChatServer
✓ Enter port 5555
✓ Click "Start Server"
✓ Verify "● Online :5555" status
```

### Test 2: Multiple Clients
```
✓ Launch 3 client instances
✓ Connect as: Alice, Bob, Charlie
✓ Verify all appear in online list
✓ All see each other's names
```

### Test 3: Public Messages
```
✓ Alice sends: "Hello everyone"
✓ Bob receives message with timestamp
✓ Charlie receives message with timestamp
✓ Server logs the event
```

### Test 4: Private Messages
```
✓ Bob sends: "@Alice Secret"
✓ Only Alice sees the private message
✓ Bob sees delivery confirmation
✓ Charlie doesn't see the message
```

### Test 5: File Sharing
```
✓ Charlie shares a file (< 10MB)
✓ All users see file notification
✓ Others can click download
✓ File saves successfully
```

### Test 6: Disconnect
```
✓ Alice clicks Disconnect
✓ Server removes Alice from list
✓ Bob and Charlie see "Alice left"
✓ Online count decreases
```

### Test 7: Server Announcement
```
✓ Server sends announcement
✓ All clients receive "🔔 Announcement"
✓ Message appears in chat area
```

### Test 8: Responsive UI
```
✓ Resize window to minimum width
✓ Verify buttons still visible
✓ Text fields shrink appropriately
✓ Layout doesn't break
```

## 📂 Project Structure

```
odev1_26_25/
├── src/
│   ├── config/
│   │   └── Config.java                  # Colors, fonts, UI builders
│   ├── Logic/
│   │   ├── ChatClientLogic.java        # Client network logic
│   │   └── ChatServerLogic.java        # Server logic & handlers
│   ├── Main/
│   │   ├── ChatClient.java             # Client entry point
│   │   ├── ChatServer.java             # Server entry point
│   │   └── Main.java                   # Auto-start entry point
│   └── Ui/
│       ├── ChatClientUI.java           # Client GUI (544 lines)
│       └── ChatServerUI.java           # Server GUI (201 lines)
├── out/                                 # Compiled .class files
├── README.md                            # This file
├── COMPLIANCE_REPORT.md                 # Full requirement compliance
├── REQUIREMENTS_CHECK.md                # Detailed requirement analysis
└── SUMMARY_AR.md                        # Arabic summary

Total: 1,272 lines of production code
```

## ✅ Requirements Compliance

All 10 core assignment requirements are fully implemented:

| # | Requirement | Status | Details |
|---|-------------|--------|---------|
| 1 | Multi-client server | ✅ | ConcurrentHashMap + thread pool |
| 2 | Connection UI | ✅ | IP, port, username fields |
| 3 | Username management | ✅ | Duplicate detection, list maintenance |
| 4 | Online user list | ✅ | Real-time sidebar display |
| 5 | Public messages | ✅ | Broadcast with timestamp |
| 6 | Private messages | ✅ | @username syntax support |
| 7 | File sharing | ✅ | Base64 encoding, 10MB limit |
| 8 | Disconnect handling | ✅ | Graceful exit, list update |
| 9 | Command protocol | ✅ | 9 command types |
| 10 | Logging system | ✅ | Timestamped events |

**Plus 4 bonus features**: Server announcements, @mention autocomplete, responsive layout, modern UI

## 🛠️ Technical Highlights

### Thread Safety
- `ConcurrentHashMap` for client registry
- `SwingUtilities.invokeLater()` for UI updates
- Atomic operations on shared state

### Error Handling
- Duplicate username rejection
- Invalid target detection
- File size validation
- Connection failure graceful degradation

### Performance
- Efficient Base64 encoding for files
- Optimized string splitting with regex
- Minimal garbage collection overhead
- Scalable to 100+ concurrent clients

## 📝 Code Quality

- **Clean Architecture**: Separation of concerns (UI ≠ Logic ≠ Network)
- **Proper Documentation**: Inline comments for complex logic
- **Consistent Naming**: Clear, descriptive variable/method names
- **Error Messages**: User-friendly notifications
- **Modern Practices**: Java 8+ features, proper resource management

## 🎓 Learning Outcomes

This project demonstrates:
- ✅ TCP Socket programming in Java
- ✅ Multi-threaded server architecture
- ✅ Swing GUI development
- ✅ Network protocol design
- ✅ Thread-safe data structures
- ✅ Event-driven programming
- ✅ Base64 encoding/decoding
- ✅ Error handling best practices

## 📌 Compatibility

- **Java Version**: 8 or higher
- **Platform**: Windows, macOS, Linux
- **Network**: TCP/IP (IPv4)
- **GUI Framework**: Swing (built-in)
- **Required Libraries**: None (uses only Java standard library)

## 🎯 Future Enhancements

Possible extensions:
- [ ] SSL/TLS encryption
- [ ] User authentication
- [ ] Message history persistence
- [ ] Group chat rooms
- [ ] Emoji/media support
- [ ] Voice chat integration
- [ ] User status (online/away/busy)
- [ ] Message read receipts
- [ ] Admin role support
- [ ] Bandwidth limiting

## 📄 License

Educational project for network programming course

---

**Built with ❤️ in Java | Network Programming Course Assignment**
