window.privateSubscription = null;

/* -------- INIT -------- */
document.addEventListener("DOMContentLoaded", loadPrivateChats);

/* -------- LOAD ROOMS -------- */
function loadPrivateChats() {
    fetch("/api/private-chats", {
        headers: { Authorization: "Bearer " + localStorage.getItem("jwt") }
    })
    .then(r => r.json())
    .then(renderPrivateChats)
    .catch(showEmptyState);
}

function renderPrivateChats(chats) {
    const list = document.getElementById("privateChatList");
    list.innerHTML = "";

    chats.forEach(chat => {
        const div = document.createElement("div");
        div.className = "private-user";
        div.textContent = chat.username;
        div.onclick = () => openPrivateChat(chat.roomId, chat.username, div);
        list.appendChild(div);
    });
}

/* -------- OPEN CHAT -------- */
function openPrivateChat(roomId, username, el) {
    window.chatMode = "PRIVATE";
    window.currentRoomId = roomId;
    window.lastPrivateRoomId = roomId;

    document.getElementById("chatTitle").innerText = username;
    document.getElementById("chat").innerHTML = "";
    document.getElementById("typing").innerText = "";

    document.querySelectorAll(".private-user").forEach(e => e.classList.remove("active"));
    el.classList.add("active");

    loadPrivateHistory(roomId);

    if (window.privateSubscription) {
        window.privateSubscription.unsubscribe();
    }

    window.privateSubscription = stompClient.subscribe(
        `/topic/chat/${roomId}`,
        msg => showPrivateMessage(JSON.parse(msg.body))
    );
}

/* -------- HISTORY -------- */
function loadPrivateHistory(roomId) {
    fetch(`/api/messages/private/${roomId}`, {
        headers: { Authorization: "Bearer " + localStorage.getItem("jwt") }
    })
    .then(r => r.json())
    .then(messages => {
        messages.forEach(showPrivateMessage);
        markRead(roomId);
    });
}

/* -------- INCOMING -------- */
function showPrivateMessage(msg) {
    if (!msg) return;

    if (msg.type === "TYPING") {
        if (msg.sender !== window.currentUser) showTyping(msg.sender);
        return;
    }

    if (msg.type === "CHAT") {
        appendChat(
            msg.sender,
            msg.content,
            msg.sender === window.currentUser,
            msg.status,
            msg.id
        );

        if (msg.sender !== window.currentUser) {
            stompClient.publish({
                destination: `/app/chat/${window.currentRoomId}/read`,
                body: JSON.stringify(msg.id)
            });
        }
    }

    if (msg.type === "READ") {
        const el = document.querySelector(`[data-id="${msg.id}"]`);
        if (el) el.classList.add("read");
    }
}

/* -------- READ -------- */
function markRead(roomId) {
    document.querySelectorAll(".message.other").forEach(el => {
        const id = el.dataset.id;
        if (!id) return;

        stompClient.publish({
            destination: `/app/chat/${roomId}/read`,
            body: JSON.stringify(id)
        });
    });
}

function showEmptyState() {
    document.getElementById("privateChatList").innerHTML = "<p>No private chats</p>";
}
