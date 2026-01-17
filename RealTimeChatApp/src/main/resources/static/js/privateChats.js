// privateChats.js

document.addEventListener("DOMContentLoaded", () => {
    const privateBtn = document.getElementById("privateBtn");
    const publicBtn = document.getElementById("publicBtn");

    if (privateBtn) {
        privateBtn.onclick = () => {
            loadPrivateChats();
        };
    }

    if (publicBtn) {
        publicBtn.onclick = () => {
            openPublicChat();
        };
    }
});

// ---------------------------
// LOAD PRIVATE CHATS
// ---------------------------
function loadPrivateChats() {
    const token = localStorage.getItem("jwt");
    if (!token) return;

    fetch("/api/private-chats", {
        headers: {
            Authorization: "Bearer " + token
        }
    })
    .then(res => {
        if (!res.ok) throw new Error("Failed to load private chats");
        return res.json();
    })
    .then(renderPrivateChats)
    .catch(err => {
        console.error(err);
        showEmptyState();
    });
}

// ---------------------------
// RENDER PRIVATE CHAT LIST
// ---------------------------
function renderPrivateChats(chats) {
    const list = document.getElementById("privateChatList");
    if (!list) return;

    list.innerHTML = "";

    if (!Array.isArray(chats) || chats.length === 0) {
        showEmptyState();
        return;
    }

    chats.forEach(chat => {
        const div = document.createElement("div");
        div.className = "private-user";
        div.dataset.roomId = chat.roomId;

        div.innerHTML = `
            <span class="username">${chat.username}</span>
        `;

        div.onclick = () => openPrivateChat(chat.roomId, chat.username, div);
        list.appendChild(div);
    });
}

// ---------------------------
// OPEN PRIVATE CHAT
// ---------------------------
function openPrivateChat(roomId, username, element) {
    if (!roomId) return;

    window.chatMode = "PRIVATE";
    window.currentRoomId = roomId;

    document.getElementById("chatTitle").innerText = username;
    document.getElementById("chat").innerHTML = "";
    document.getElementById("typing").innerText = "";

    document.querySelectorAll(".private-user")
        .forEach(el => el.classList.remove("active"));

    if (element) {
        element.classList.add("active");
    }
}

// ---------------------------
// EMPTY STATE
// ---------------------------
function showEmptyState() {
    const list = document.getElementById("privateChatList");
    if (!list) return;

    list.innerHTML = `
        <div class="empty-state">
            Start messaging
        </div>
    `;
}
