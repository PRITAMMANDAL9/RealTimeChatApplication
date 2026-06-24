# 🚀 ChatX — Real-Time Messaging Platform

ChatX is a production-ready **real-time messaging platform** built with **Java, Spring Boot, Spring Security, WebSocket, JWT, MySQL, HTML, CSS and JavaScript**.  
It supports **public chat, private chat, secure authentication, online user presence, typing indicators, message delivery/read status, and persistent message storage**.

🔗 **Live Demo:** https://chatx-bp6v.onrender.com/login  
---

## 📌 Overview

ChatX is a full-stack chat application designed to simulate the experience of a modern messaging platform.  
The project focuses on **real-time communication**, **secure user authentication**, **private messaging workflows**, and **backend architecture using Spring Boot + WebSocket**.

The application allows users to:

- Register and log in securely using JWT authentication
- Participate in a **public global chat room**
- Send **private one-to-one messages**
- View **online/offline user presence**
- See **typing indicators**
- Track **message delivery / read status**
- Store chat history persistently in **MySQL**
- Access a responsive, modern chat interface

This project was built to demonstrate strong backend fundamentals in:

- **Java Backend Development**
- **Spring Boot application design**
- **REST API development**
- **Authentication & Authorization**
- **WebSocket-based real-time systems**
- **Database design and message persistence**
- **Frontend integration with backend messaging workflows**

---

# ✨ Features

## 🔐 Authentication & Security
- User registration and login
- **JWT-based authentication**
- **Spring Security** integration
- Protected routes and secured chat access
- Session-safe authenticated messaging flow

## 💬 Real-Time Messaging
- **Public chat room** for all connected users
- **Private chat** between individual users
- WebSocket-based real-time message delivery
- STOMP messaging architecture for live communication

## 👤 User Presence & Interaction
- Online/offline user presence tracking
- Typing indicators in chat
- Real-time UI updates without page refresh
- Dynamic conversation switching

## 📩 Message Management
- Persistent message storage in MySQL
- Chat history loading
- Private chat room management
- Message status handling:
  - Sent
  - Delivered
  - Read

## 🎨 UI / UX
- Responsive chat layout
- Sidebar-based chat navigation
- Public vs Private chat switching
- Clean dark-themed chat interface
- Smooth interaction between chat panels and message area

---

# 🛠️ Tech Stack

## Backend
- **Java**
- **Spring Boot**
- **Spring Security**
- **Spring WebSocket**
- **STOMP**
- **JWT Authentication**
- **Hibernate / JPA**
- **MySQL**

## Frontend
- **HTML5**
- **CSS3**
- **JavaScript**
- **Bootstrap**

## Deployment / Tools
- **Render** (deployment)
- **Maven**
- **Git & GitHub**

---

# 🏗️ Architecture Summary

ChatX combines **REST APIs** for authentication and user management with **WebSocket messaging** for real-time chat communication.

## Core architecture includes:
- **REST Layer**
  - User authentication
  - Login / registration
  - User and chat-related endpoints

- **WebSocket Layer**
  - Public chat subscription
  - Private chat messaging
  - Typing events
  - Message delivery and live updates

- **Persistence Layer**
  - Users
  - Chat messages
  - Private chat rooms
  - Message status tracking

---

# 📂 Major Functional Modules

## 1. User Authentication Module
Handles:
- User signup
- User login
- Password security
- JWT generation and validation
- Securing chat access

## 2. Public Chat Module
Handles:
- Global public chat room
- Broadcasting messages to all connected users
- Real-time updates through WebSocket subscriptions

## 3. Private Chat Module
Handles:
- One-to-one conversations
- Private room creation / lookup
- Private message persistence
- Conversation history retrieval

## 4. Presence & Typing Module
Handles:
- Online user indication
- Typing event propagation
- Live interaction status updates

## 5. Message Status Module
Handles:
- Sent / delivered / read state transitions
- Real-time UI reflection of message state

---

# 🗃️ Database Design

The application uses **MySQL** for persistent storage.

### Main entities used in the project:
- **User**
- **ChatMessage**
- **PrivateChatRoom**
- **MessageStatus / message metadata**

### Stored information includes:
- User credentials and profile information
- Public and private chat messages
- Sender / receiver mapping
- Chat room relationships
- Message timestamps and status

---

# ⚙️ How It Works

## Authentication Flow
1. User registers or logs in
2. Server validates credentials
3. JWT token is generated
4. User accesses protected chat routes
5. Authenticated user can use public and private chat features

## Public Chat Flow
1. User joins the application
2. Frontend subscribes to public WebSocket topic
3. User sends a message
4. Server broadcasts message to all subscribed users
5. Message appears in real time for everyone

## Private Chat Flow
1. User selects another user
2. Private room is created or fetched
3. Message is sent via private channel
4. Message is saved in database
5. Receiver gets the message instantly
6. Read / delivered state can be updated

---

# 📸 Key Functional Highlights

- Production-style **real-time chat platform**
- Strong use of **Spring Security + JWT**
- Hands-on implementation of **WebSocket + STOMP**
- Backend handling for **private chat rooms**
- **Persistent chat storage** with MySQL
- Good showcase of **full-stack Java project skills**
- Live deployed project that recruiters can test directly

---

# 🚀 Live Demo

### ChatX Live:
**https://chatx-bp6v.onrender.com/login**

> Note: Since the project is hosted on Render free tier, the first request may take a few seconds if the server is waking up.

---

# 🖥️ Local Setup Instructions

## 1) Clone the repository
```bash
git clone https://github.com/PRITAMMANDAL9/RealTimeChatApplication.git
cd RealTimeChatApplication
