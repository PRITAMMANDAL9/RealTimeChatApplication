/* =========================================================
   PRIVATE CHAT MODULE (FINAL - PRODUCTION)
========================================================= */

window.privateSubscription = null;
window.oldestTimestamp = null;
window.isLoadingOld = false;

/* =========================================================
   INIT
========================================================= */

document.addEventListener("DOMContentLoaded", () => {
    loadPrivateChats();
});

/* =========================================================
   LOAD PRIVATE ROOMS
========================================================= */

function loadPrivateChats() {

    fetch("/api/private-chats", {
        headers: {
            Authorization: "Bearer " + localStorage.getItem("jwt")
        }
    })
    .then(res => res.ok ? res.json() : [])
    .then(data => renderPrivateChats(data))
    .catch(() => renderPrivateChats([]));
}

function renderPrivateChats(chats) {

    const list = document.getElementById("privateChatList");
    if (!list) return;

    list.innerHTML = "";

    chats.forEach(chat => {

        const div = document.createElement("div");
        div.className = "private-user";
        div.dataset.username = chat.username.toLowerCase();

        div.innerHTML = `<span class="name">${chat.username}</span>`;

        div.onclick = () =>
            openPrivateChat(chat.roomId, chat.username, div);

        list.appendChild(div);
    });
}

/* =========================================================
   OPEN PRIVATE CHAT
========================================================= */

function openPrivateChat(roomId, username, el) {

    // 🔥 STOP PUBLIC SUBSCRIPTION
    if (window.publicSubscription) {
        window.publicSubscription.unsubscribe();
        window.publicSubscription = null;
    }

    // 🔥 RESET STATE
    window.chatMode = "PRIVATE";
    window.currentRoomId = roomId;
    window.currentChatUser = username;
    window.oldestTimestamp = null;

    const chat = document.getElementById("chat");
    chat.replaceChildren();

    document.getElementById("chatTitle").innerText = username;

    document.querySelectorAll(".private-user")
        .forEach(e => e.classList.remove("active"));
    el.classList.add("active");

    document.getElementById("chatMenuBtn").style.display = "block";

    // 🔥 LOAD INITIAL HISTORY
    loadPrivateHistory(roomId);

    // 🔥 HANDLE SUBSCRIPTION
    if (window.privateSubscription) {
        window.privateSubscription.unsubscribe();
        window.privateSubscription = null;
    }

    window.privateSubscription = stompClient.subscribe(
        `/topic/chat/${roomId}`,
        msg => {

            if (window.chatMode !== "PRIVATE") return;

            const message = JSON.parse(msg.body);
            if (!message) return;

            if (message.roomId !== window.currentRoomId) return;

			if (
			    message.type === "TYPING" &&
			    message.sender !== window.currentUser
			) {
			    showTyping(message.sender);
			    return;
			}

			if (message.type === "CHAT") {
			    appendPrivateMessage(message);
			}

			if (message.type === "READ") {
			    const bubble = document.querySelector(
			        `.bubble[data-id="${message.id}"]`
			    );

			    if (bubble) {
			        bubble.querySelector(".meta").innerText = "✓✓";
			    }
			}
        }
    );

    checkBlockStatus(username);

    attachScrollPagination();
}

/* =========================================================
   LOAD INITIAL HISTORY
========================================================= */

function loadPrivateHistory(roomId) {

    fetch(`/api/messages/private/${roomId}`, {
        headers: {
            Authorization: "Bearer " + localStorage.getItem("jwt")
        }
    })
    .then(res => res.ok ? res.json() : [])
    .then(messages => {

        if (!Array.isArray(messages)) return;

        // 🔥 IMPORTANT: reverse (DESC → ASC for UI)
        messages.reverse().forEach(appendPrivateMessage);

        if (messages.length) {
            window.oldestTimestamp = messages[0].timestamp;
        }

        scrollToBottom();
    });
}

/* =========================================================
   PAGINATION (SCROLL UP)
========================================================= */

function attachScrollPagination() {

    const chat = document.getElementById("chat");

    chat.onscroll = () => {

        if (chat.scrollTop === 0 &&
            !window.isLoadingOld &&
            window.oldestTimestamp) {

            loadOlderMessages();
        }
    };
}

function loadOlderMessages() {

    if (!window.oldestTimestamp) return;

    window.isLoadingOld = true;

    fetch(`/api/messages/private/${window.currentRoomId}/before?before=${window.oldestTimestamp}`, {
        headers: {
            Authorization: "Bearer " + localStorage.getItem("jwt")
        }
    })
    .then(res => res.ok ? res.json() : [])
    .then(messages => {

        if (!messages.length) return;

        const chat = document.getElementById("chat");
        const prevHeight = chat.scrollHeight;

        messages.reverse().forEach(prependPrivateMessage);

        chat.scrollTop = chat.scrollHeight - prevHeight;

        window.oldestTimestamp = messages[0].timestamp;
    })
    .finally(() => {
        window.isLoadingOld = false;
    });
}

/* =========================================================
   RENDER NEW MESSAGE (BOTTOM)
========================================================= */

function appendPrivateMessage(message) {

    const chat = document.getElementById("chat");
    if (!chat) return;

    if (message.id &&
        document.querySelector(`.bubble[data-id="${message.id}"]`)) return;

    const self = message.sender === window.currentUser;

    const group = createMessageGroup(message, self);

    chat.appendChild(group);
    scrollToBottom();
}

/* =========================================================
   PREPEND OLD MESSAGE (TOP)
========================================================= */

function prependPrivateMessage(message) {

    const chat = document.getElementById("chat");

    if (message.id &&
        document.querySelector(`.bubble[data-id="${message.id}"]`)) return;

    const self = message.sender === window.currentUser;

    const group = createMessageGroup(message, self);

    chat.prepend(group);
}

/* =========================================================
   MESSAGE GROUP BUILDER
========================================================= */

function createMessageGroup(message, self) {

    const group = document.createElement("div");
    group.className = `message-group ${self ? "sent" : "received"}`;

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

    return group;
}

/* =========================================================
   HELPERS
========================================================= */

function scrollToBottom() {
    const chat = document.getElementById("chat");
    chat.scrollTop = chat.scrollHeight;
}