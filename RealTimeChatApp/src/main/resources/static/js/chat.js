/* =========================================================
   GLOBAL CHAT STATE
========================================================= */

window.chatMode = "PUBLIC";
window.currentRoomId = null;
window.currentChatUser = null;
window.publicSubscription = null;
let typingTimeout = null;
let lastTypingSent = 0;

/* =========================================================
   PUBLIC CHAT SUBSCRIBE
========================================================= */

function subscribePublicChat() {

    if (!window.stompClient?.connected) return;

    if (window.publicSubscription) {
        window.publicSubscription.unsubscribe();
        window.publicSubscription = null;
    }

    window.publicSubscription = stompClient.subscribe(
        "/topic/message",
        msg => {

            if (window.chatMode !== "PUBLIC") return;

            const message = JSON.parse(msg.body);
            if (!message) return;

            if (message.type === "TYPING" &&
                message.sender !== window.currentUser) {
                showTyping(message.sender);
                return;
            }

            if (message.type === "CHAT") {
                renderPublicMessage(message);
            }
        }
    );
}

/* =========================================================
   RENDER PUBLIC MESSAGE
========================================================= */

function renderPublicMessage(message) {

    if (window.chatMode !== "PUBLIC") return;

    const chat = document.getElementById("chat");
    if (!chat) return;

    if (message.id &&
        document.querySelector(`.bubble[data-id="${message.id}"]`)) return;

    const self = message.sender === window.currentUser;

    const group = document.createElement("div");
    group.className = `message-group ${self ? "sent" : "received"}`;

    if (!self) {
        const senderEl = document.createElement("div");
        senderEl.className = "sender";
        senderEl.innerText = message.sender;
        group.appendChild(senderEl);
    }

    group.appendChild(createBubble(
        message.content,
        self,
        message.status,
        message.id
    ));

    chat.appendChild(group);
    chat.scrollTop = chat.scrollHeight;
}

/* =========================================================
   CREATE BUBBLE
========================================================= */

function createBubble(content, self, status, id) {

    const bubble = document.createElement("div");
    bubble.className = "bubble";
    if (id) bubble.dataset.id = id;

    const text = document.createElement("div");
    text.className = "text";
    text.innerText = content;

    const meta = document.createElement("div");
    meta.className = "meta";
    if (self) meta.innerText = status === "READ" ? "✓✓" : "✓";

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
            <span></span><span></span><span></span>
        </span>
    `;

    clearTimeout(typingTimeout);
    typingTimeout = setTimeout(() => typing.innerHTML = "", 1500);
}

/* =========================================================
   SEND MESSAGE
========================================================= */

document.addEventListener("DOMContentLoaded", () => {

    const sendBtn = document.getElementById("sendBtn");
    const input = document.getElementById("messageInput");

    if (!sendBtn || !input) return;

    sendBtn.onclick = () => {

        const text = input.value.trim();
        if (!text || !stompClient?.connected) return;

        if (window.chatMode === "PUBLIC") {
            stompClient.publish({
                destination: "/app/sendMessage",
                body: JSON.stringify({ type: "CHAT", content: text })
            });
        } else if (window.currentRoomId) {
            stompClient.publish({
                destination: `/app/chat/${window.currentRoomId}`,
                body: JSON.stringify({ type: "CHAT", content: text })
            });
        }

        input.value = "";
    };
});

/* =========================================================
   BLOCK MENU SYSTEM
========================================================= */

document.addEventListener("DOMContentLoaded", () => {

    const menuBtn = document.getElementById("chatMenuBtn");
    const menu = document.getElementById("chatMenu");
    const blockBtn = document.getElementById("blockToggleBtn");

    if (!menuBtn || !menu || !blockBtn) return;

    menuBtn.onclick = async () => {

        if (window.chatMode !== "PRIVATE" ||
            !window.currentChatUser) return;

        menu.classList.toggle("open");

        try {
            const res = await fetch(
                `/api/block/status/${window.currentChatUser}`,
                {
                    headers: {
                        Authorization:
                            "Bearer " + localStorage.getItem("jwt")
                    }
                }
            );

            const blocked = await res.json();
            blockBtn.innerText =
                blocked ? "Unblock User" : "Block User";

        } catch (err) {
            console.error("Block status error:", err);
        }
    };

    blockBtn.onclick = async () => {

        if (!window.currentChatUser) return;

        const isUnblock =
            blockBtn.innerText.includes("Unblock");

        const method = isUnblock ? "DELETE" : "POST";

        await fetch(
            `/api/block/${window.currentChatUser}`,
            {
                method,
                headers: {
                    Authorization:
                        "Bearer " + localStorage.getItem("jwt")
                }
            }
        );

        menu.classList.remove("open");
        checkBlockStatus(window.currentChatUser);
    };

    document.addEventListener("click", e => {
        if (!menu.contains(e.target) &&
            !menuBtn.contains(e.target)) {
            menu.classList.remove("open");
        }
    });
});

/* =========================================================
   CHECK BLOCK STATUS
========================================================= */

async function checkBlockStatus(username) {

    const input = document.getElementById("messageInput");
    if (!input) return;

    try {
        const res = await fetch(
            `/api/block/status/${username}`,
            {
                headers: {
                    Authorization:
                        "Bearer " + localStorage.getItem("jwt")
                }
            }
        );

        const blocked = await res.json();

        if (blocked) {
            input.disabled = true;
            input.placeholder = "You blocked this user";
        } else {
            input.disabled = false;
            input.placeholder = "Type a message…";
        }

    } catch (err) {
        console.error("Block check failed:", err);
    }
}

/* =========================================================
   SWITCH TO PUBLIC
========================================================= */

async function openPublicChat() {

    if (window.privateSubscription) {
        window.privateSubscription.unsubscribe();
        window.privateSubscription = null;
    }

    if (window.publicSubscription) {
        window.publicSubscription.unsubscribe();
        window.publicSubscription = null;
    }

    window.chatMode = "PUBLIC";
    window.currentRoomId = null;
    window.currentChatUser = null;

    document.getElementById("chatTitle").innerText = "Public Chat";
    document.getElementById("chat").replaceChildren();

    document.getElementById("chatMenuBtn").style.display = "none";
    document.getElementById("headerOnlineDot").style.visibility = "hidden";

    const input = document.getElementById("messageInput");
    input.disabled = false;
    input.placeholder = "Type a message…";

    // 🔥 LOAD 1-DAY HISTORY
    await loadPublicHistory();

    // 🔥 THEN SUBSCRIBE LIVE
    subscribePublicChat();
}
/* =========================================================
   LoadPublicHistory
========================================================= */
async function loadPublicHistory() {

    try {
        const res = await fetch("/api/messages/public", {
            headers: {
                Authorization: "Bearer " + localStorage.getItem("jwt")
            }
        });

        if (!res.ok) return;

        const messages = await res.json();

        if (!Array.isArray(messages)) return;

        const chat = document.getElementById("chat");
        chat.innerHTML = "";

        messages.forEach(message => {
            renderPublicMessage(message);
        });

    } catch (err) {
        console.error("Public history load error:", err);
    }
}