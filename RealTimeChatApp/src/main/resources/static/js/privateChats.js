/* =========================================================
   PRIVATE CHAT MODULE
========================================================= */

window.privateSubscription = null;

document.addEventListener("DOMContentLoaded", loadPrivateChats);

/* =========================================================
   LOAD PRIVATE ROOMS
========================================================= */

function loadPrivateChats() {

    fetch("/api/private-chats", {
        headers: {
            Authorization:
                "Bearer " + localStorage.getItem("jwt")
        }
    })
    .then(res => res.ok ? res.json() : [])
    .then(data => renderPrivateChats(data))
    .catch(() => renderPrivateChats([]));
}

function renderPrivateChats(chats) {

    const list =
        document.getElementById("privateChatList");
    if (!list) return;

    list.innerHTML = "";

    chats.forEach(chat => {

        const div = document.createElement("div");
        div.className = "private-user";
        div.dataset.username =
            chat.username.toLowerCase();

        div.innerHTML = `
            <span class="name">${chat.username}</span>
        `;

        div.onclick = () =>
            openPrivateChat(
                chat.roomId,
                chat.username,
                div
            );

        list.appendChild(div);
    });
}

/* =========================================================
   OPEN PRIVATE CHAT
========================================================= */

function openPrivateChat(roomId, username, el) {
	
	if (window.publicSubscription) {
	      window.publicSubscription.unsubscribe();
	      window.publicSubscription = null;
	  }

    window.chatMode = "PRIVATE";
    window.currentRoomId = roomId;
    window.currentChatUser = username;

    document.getElementById("chatTitle").innerText =
        username;

    document.getElementById("chat").replaceChildren();

    document.querySelectorAll(".private-user")
        .forEach(e => e.classList.remove("active"));

    el.classList.add("active");

    document.getElementById("chatMenuBtn")
        .style.display = "block";

    loadPrivateHistory(roomId);

    if (window.privateSubscription) {
        window.privateSubscription.unsubscribe();
        window.privateSubscription = null;
    }

    window.privateSubscription =
        stompClient.subscribe(
            `/topic/chat/${roomId}`,
            msg => {

                if (window.chatMode !== "PRIVATE")
                    return;

                const message =
                    JSON.parse(msg.body);
                if (!message) return;

                if (message.roomId !==
                    window.currentRoomId)
                    return;

                if (message.type === "CHAT") {
                    renderPrivateMessage(message);
                }

                if (message.type === "READ") {
                    const bubble =
                        document.querySelector(
                            `.bubble[data-id="${message.id}"]`
                        );
                    if (bubble)
                        bubble.querySelector(".meta")
                              .innerText = "✓✓";
                }
            }
        );

    checkBlockStatus(username);
}

/* =========================================================
   LOAD HISTORY
========================================================= */

function loadPrivateHistory(roomId) {

    fetch(`/api/messages/private/${roomId}`, {
        headers: {
            Authorization:
                "Bearer " + localStorage.getItem("jwt")
        }
    })
    .then(res => res.ok ? res.json() : [])
    .then(messages => {
        if (!Array.isArray(messages)) return;
        messages.forEach(renderPrivateMessage);
    });
}

/* =========================================================
   RENDER PRIVATE MESSAGE
========================================================= */

function renderPrivateMessage(message) {

    const chat =
        document.getElementById("chat");
    if (!chat) return;

    if (message.id &&
        document.querySelector(
            `.bubble[data-id="${message.id}"]`
        )) return;

    const self =
        message.sender === window.currentUser;

    const group =
        document.createElement("div");
    group.className =
        `message-group ${self ? "sent" : "received"}`;

    if (!self) {
        const senderEl =
            document.createElement("div");
        senderEl.className = "sender";
        senderEl.innerText =
            message.sender;
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

    chat.appendChild(group);
    chat.scrollTop = chat.scrollHeight;
}
