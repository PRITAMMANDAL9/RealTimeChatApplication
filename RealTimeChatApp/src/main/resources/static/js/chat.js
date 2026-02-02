let typingTimeout = null;

window.chatMode = "PUBLIC";
window.currentRoomId = null;

/* ---------------- PUBLIC SUB ---------------- */
function subscribePublicChat() {
    stompClient.subscribe("/topic/message", msg => {
        handleChatMessage(JSON.parse(msg.body));
    });
}

/* ---------------- INCOMING ---------------- */
function handleChatMessage(message) {
    if (!message) return;

    if (message.type === "TYPING") {
        if (message.sender === window.currentUser) return;
        showTyping(message.sender);
        return;
    }

    if (message.type === "CHAT") {
        appendChat(
            message.sender,
            message.content,
            message.sender === window.currentUser,
            message.status,
            message.id
        );
    }
}

/* ---------------- SEND ---------------- */
document.addEventListener("DOMContentLoaded", () => {
    const sendBtn = document.getElementById("sendBtn");
    const input = document.getElementById("messageInput");

    sendBtn.onclick = () => {
        const text = input.value.trim();
        if (!text || !stompClient?.connected) return;

        if (window.chatMode === "PUBLIC") {
            sendMessage({ type: "CHAT", content: text });
        }

        if (window.chatMode === "PRIVATE" && window.currentRoomId) {
            stompClient.publish({
                destination: `/app/chat/${window.currentRoomId}`,
                body: JSON.stringify({ type: "CHAT", content: text })
            });
        }

        input.value = "";
    };

    input.addEventListener("input", () => {
        if (!stompClient?.connected) return;
        if (window.chatMode === "PUBLIC") {
            sendMessage({ type: "TYPING" });
        }
    });
});

/* ---------------- UI ---------------- */
function appendChat(sender, content, self, status = "SENT", id = null) {
    if (id && document.querySelector(`[data-id="${id}"]`)) return;

    const chat = document.getElementById("chat");
    const div = document.createElement("div");

    div.className = `message ${self ? "self" : "other"}`;
    if (id) div.dataset.id = id;

    let ticks = "", cls = "";
    if (self) {
        if (status === "SENT") ticks = "✓";
        if (status === "DELIVERED") ticks = "✓✓";
        if (status === "READ") { ticks = "✓✓"; cls = "read"; }
    }

    div.innerHTML = `
        <div class="content">${content}</div>
        ${self ? `<div class="meta ${cls}">${ticks}</div>` : ""}
    `;

    chat.appendChild(div);
    chat.scrollTop = chat.scrollHeight;
}

function showTyping(user) {
    const typing = document.getElementById("typing");
    typing.innerText = `${user} is typing...`;
    clearTimeout(typingTimeout);
    typingTimeout = setTimeout(() => typing.innerText = "", 1200);
}
