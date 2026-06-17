/* =========================================================
   GLOBAL STATE
========================================================= */

window.chatMode = "PUBLIC";
window.currentRoomId = null;
window.currentChatUser = null;

window.publicSubscription = null;

window.publicOldestTimestamp = null;
window.loadingPublicHistory = false;
window.hasMorePublicMessages = true;

let typingTimeout = null;

/* =========================================================
   OPEN PUBLIC CHAT
========================================================= */

async function openPublicChat() {

    console.log("OPEN PUBLIC CHAT");

    /* ================= RESET MODE ================= */

    window.chatMode = "PUBLIC";
    window.currentRoomId = null;
    window.currentChatUser = null;

    /* ================= RESET PAGINATION ================= */

    window.publicOldestTimestamp = null;
    window.loadingPublicHistory = false;
    window.hasMorePublicMessages = true;

    /* ================= UI RESET ================= */

    document.getElementById("chatTitle").innerText = "Public Chat";

    const chat = document.getElementById("chat");

    if (!chat) {
        console.error("❌ #chat element missing");
        return;
    }

    chat.innerHTML = "";

    const input = document.getElementById("messageInput");

    input.disabled = false;
    input.placeholder = "Type a message...";

    /* ================= LOAD HISTORY ================= */

    await loadPublicHistory();

    /* ================= LIVE SUBSCRIBE ================= */

    subscribePublicChat();
}

/* =========================================================
   PUBLIC CHAT SUBSCRIBE
========================================================= */

function subscribePublicChat() {

    if (!window.stompClient?.connected) {
        console.error("❌ STOMP not connected");
        return;
    }

    if (window.publicSubscription) {
        window.publicSubscription.unsubscribe();
    }

    window.publicSubscription = stompClient.subscribe(
        "/topic/message",
        msg => {

            if (window.chatMode !== "PUBLIC") return;

            const message = JSON.parse(msg.body);

            if (!message) return;

            if (
                message.type === "TYPING" &&
                message.sender !== window.currentUser
            ) {
                showTyping(message.sender);
                return;
            }

            if (message.type === "CHAT") {
                renderPublicMessage(message);
            }
        }
    );

    console.log("✅ PUBLIC SUBSCRIBED");
}

/* =========================================================
   LOAD PUBLIC HISTORY
========================================================= */

async function loadPublicHistory() {

    try {

        console.log("LOADING PUBLIC HISTORY");

        const res = await fetch("/api/messages/public", {
            headers: {
                Authorization: "Bearer " + localStorage.getItem("jwt")
            }
        });

        console.log("PUBLIC API STATUS:", res.status);

        if (!res.ok) {
            throw new Error("Public API failed");
        }

        let messages = await res.json();

        console.log("PUBLIC MESSAGES:", messages);

        if (!Array.isArray(messages)) {
            console.error("❌ Not array");
            return;
        }

        const chat = document.getElementById("chat");

        chat.innerHTML = "";

        /* ================= BACKEND RETURNS DESC ================= */

        messages.reverse();

        messages.forEach(msg => {
            renderPublicMessage(msg);
        });

        if (messages.length > 0) {
            window.publicOldestTimestamp =
                messages[0].timestamp;
        }

        requestAnimationFrame(() => {
            chat.scrollTop = chat.scrollHeight;
        });

    } catch (err) {

        console.error("❌ Public history error:", err);
    }
}

/* =========================================================
   PAGINATION
========================================================= */

async function loadOlderPublicMessages() {

    if (window.loadingPublicHistory) return;

    if (!window.publicOldestTimestamp) return;

    if (!window.hasMorePublicMessages) return;

    window.loadingPublicHistory = true;

    try {

        const chat = document.getElementById("chat");

        const prevHeight = chat.scrollHeight;

        const res = await fetch(
            `/api/messages/public/before?before=${encodeURIComponent(window.publicOldestTimestamp)}`,
            {
                headers: {
                    Authorization:
                        "Bearer " + localStorage.getItem("jwt")
                }
            }
        );

        if (!res.ok) {
            window.loadingPublicHistory = false;
            return;
        }

        let messages = await res.json();

        if (!Array.isArray(messages) || messages.length === 0) {

            window.hasMorePublicMessages = false;
            window.loadingPublicHistory = false;

            return;
        }

        messages.reverse();

        messages.forEach(msg => {
            renderPublicMessage(msg, true);
        });

        window.publicOldestTimestamp =
            messages[0].timestamp;

        requestAnimationFrame(() => {
            chat.scrollTop =
                chat.scrollHeight - prevHeight;
        });

    } catch (err) {

        console.error("❌ Pagination error:", err);

    } finally {

        window.loadingPublicHistory = false;
    }
}

/* =========================================================
   RENDER MESSAGE
========================================================= */

function renderPublicMessage(message, prepend = false) {

    if (window.chatMode !== "PUBLIC") return;

    const chat = document.getElementById("chat");

    if (!chat) return;

    /* ================= DUPLICATE BLOCK ================= */

    if (
        message.id &&
        document.querySelector(`.bubble[data-id="${message.id}"]`)
    ) {
        return;
    }

    const self = message.sender === window.currentUser;

    const group = document.createElement("div");

    group.className =
        `message-group ${self ? "sent" : "received"}`;

    if (!self) {

        const senderEl = document.createElement("div");

        senderEl.className = "sender";
        senderEl.innerText = message.sender;

        group.appendChild(senderEl);
    }

    group.appendChild(
        createBubble(
            message.content,
            self,
            message.status,
            message.id
        )
    );

    if (prepend) {

        chat.prepend(group);

    } else {

        chat.appendChild(group);
    }
}

/* =========================================================
   CREATE BUBBLE
========================================================= */

function createBubble(content, self, status, id) {

    const bubble = document.createElement("div");

    bubble.className = "bubble";

    if (id) {
        bubble.dataset.id = id;
    }

    const text = document.createElement("div");

    text.className = "text";
    text.innerText = content;

    const meta = document.createElement("div");

    meta.className = "meta";

    if (self) {
        meta.innerText =
            status === "READ" ? "✓✓" : "✓";
    }

    bubble.append(text, meta);

    return bubble;
}

/* =========================================================
   TYPING
========================================================= */

function showTyping(user) {

    const typing = document.getElementById("typing");

    if (!typing) return;

    typing.innerHTML = `
        <span>${user} is typing</span>
        <span class="typing-dots">
            <span></span>
            <span></span>
            <span></span>
        </span>
    `;

    clearTimeout(typingTimeout);

	typingTimeout = setTimeout(() => {
	    typing.innerHTML = "";
	}, 3000);
}

/* =========================================================
   SEND MESSAGE
========================================================= */

function sendMessage() {

    const input = document.getElementById("messageInput");

    const text = input.value.trim();

    if (!text) return;

    if (!window.stompClient?.connected) return;

    if (window.chatMode === "PUBLIC") {

        stompClient.publish({
            destination: "/app/sendMessage",
            body: JSON.stringify({
                type: "CHAT",
                content: text
            })
        });

    } else if (window.currentRoomId) {

        stompClient.publish({
            destination: `/app/chat/${window.currentRoomId}`,
            body: JSON.stringify({
                type: "CHAT",
                content: text
            })
        });
    }

    input.value = "";
}

/* =========================================================
   DOM READY
========================================================= */

document.addEventListener("DOMContentLoaded", () => {

    const sendBtn = document.getElementById("sendBtn");

    const input = document.getElementById("messageInput");

	/* ================= TYPING ================= */

	/* ================= TYPING ================= */

	let lastTypingSent = 0;

	input.addEventListener("input", () => {

	    if (!window.stompClient?.connected) return;

	    const now = Date.now();

	    if (now - lastTypingSent < 1000) {
	        return;
	    }

	    lastTypingSent = now;

	    if (window.chatMode === "PUBLIC") {

	        stompClient.publish({
	            destination: "/app/sendMessage",
	            body: JSON.stringify({
	                type: "TYPING"
	            })
	        });

	    } else if (window.currentRoomId) {

	        stompClient.publish({
	            destination: `/app/chat/${window.currentRoomId}`,
	            body: JSON.stringify({
	                type: "TYPING"
	            })
	        });
	    }
	});
    /* ================= SEND ================= */

    if (sendBtn) {
        sendBtn.onclick = sendMessage;
    }

    /* ================= ENTER SEND ================= */

    if (input) {

        input.addEventListener("keypress", e => {

            if (e.key === "Enter") {
                sendMessage();
            }
        });
    }

    /* ================= PAGINATION ================= */

	const chat = document.getElementById("chat");

	if (chat) {

	    chat.addEventListener("scroll", () => {

	        if (window.chatMode !== "PUBLIC") return;

	        if (chat.scrollTop <= 10) {
	            loadOlderPublicMessages();
	        }
	    });
	}

    /* ================= AUTO LOAD ================= */

    setTimeout(() => {

        openPublicChat();

    }, 1000);
});