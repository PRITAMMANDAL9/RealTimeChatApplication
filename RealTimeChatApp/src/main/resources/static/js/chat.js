// chat.js

let typingTimeout = null;

// -----------------------------
// GLOBAL CHAT STATE
// -----------------------------
window.chatMode = "PUBLIC";     // PUBLIC | PRIVATE
window.currentRoomId = null;

// -----------------------------
// SUBSCRIPTIONS
// -----------------------------
function subscribePublicChat() {
    if (!stompClient) return;

    stompClient.subscribe("/topic/message", msg => {
        handleChatMessage(JSON.parse(msg.body));
    });
}

// -----------------------------
// INCOMING MESSAGES
// -----------------------------
function handleChatMessage(message) {

    // ⌨️ Typing indicator
    if (message.type === "TYPING") {
        if (message.sender === currentUser) return;

        const typing = document.getElementById("typing");
        if (!typing) return;

        typing.innerText = `${message.sender} is typing...`;

        clearTimeout(typingTimeout);
        typingTimeout = setTimeout(() => {
            typing.innerText = "";
        }, 1200);
        return;
    }

    // 🔔 System join message
    if (message.type === "JOIN") {
        appendSystem(`${message.sender} joined the chat`);
        return;
    }

    // 💬 Chat message
    appendChat(
        message.sender,
        message.content,
        message.sender === currentUser
    );
}

// -----------------------------
// UI HELPERS
// -----------------------------
function appendChat(sender, content, self) {
    const chat = document.getElementById("chat");
    if (!chat) return;

    const div = document.createElement("div");
    div.className = `message ${self ? "self" : "other"}`;
    div.innerHTML = `<strong>${sender}</strong><br>${content}`;

    chat.appendChild(div);
    chat.scrollTop = chat.scrollHeight;
}

function appendSystem(text) {
    const chat = document.getElementById("chat");
    if (!chat) return;

    const div = document.createElement("div");
    div.className = "system";
    div.innerText = text;

    chat.appendChild(div);
}

// -----------------------------
// MODE SWITCHING
// -----------------------------
function openPublicChat() {
    window.chatMode = "PUBLIC";
    window.currentRoomId = null;

    document.getElementById("chatTitle").innerText = "Public Chat";
    document.getElementById("chat").innerHTML = "";
    document.getElementById("typing").innerText = "";

    document.querySelectorAll(".private-user")
        .forEach(el => el.classList.remove("active"));
}

// -----------------------------
// OUTGOING EVENTS
// -----------------------------
document.addEventListener("DOMContentLoaded", () => {

    const sendBtn = document.getElementById("sendBtn");
    const input = document.getElementById("messageInput");

    if (!sendBtn || !input) return;

    // SEND MESSAGE
    sendBtn.onclick = () => {
        const text = input.value.trim();
        if (!text || !stompClient?.connected) return;

        if (window.chatMode === "PUBLIC") {

            sendMessage({
                type: "CHAT",
                content: text
            });

        } else if (window.chatMode === "PRIVATE" && window.currentRoomId) {

            stompClient.publish({
                destination: `/app/chat/${window.currentRoomId}`,
                body: JSON.stringify({
                    type: "CHAT",
                    content: text
                })
            });

        }

        input.value = "";
    };

    // TYPING EVENT
    input.addEventListener("input", () => {
        if (!stompClient?.connected) return;

        if (window.chatMode === "PUBLIC") {

            sendMessage({ type: "TYPING" });

        } else if (window.chatMode === "PRIVATE" && window.currentRoomId) {

            stompClient.publish({
                destination: `/app/chat/${window.currentRoomId}`,
                body: JSON.stringify({ type: "TYPING" })
            });
        }
    });

});
