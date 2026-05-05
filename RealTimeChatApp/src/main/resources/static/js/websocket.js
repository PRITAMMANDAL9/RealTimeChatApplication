/* ================= GLOBAL STATE ================= */

window.stompClient = null;
window.currentUser = null;
window.hasJoinedPublic = false;
window.onlineUsers = new Set();
window.presenceSubscription = null;

/* ================= SAFE SUBSCRIBE ================= */

function safeSubscribe(destination, callback) {
    if (!stompClient || !stompClient.connected) {
        setTimeout(() => safeSubscribe(destination, callback), 200);
        return;
    }
    stompClient.subscribe(destination, callback);
}

/* ================= INIT ================= */


document.addEventListener("DOMContentLoaded", connectWebSocket);
function connectWebSocket() {
    console.log("connectWebSocket() called, token =", localStorage.getItem("jwt"));

    const token = localStorage.getItem("jwt");
    if (!token) {
        location.href = "/login";
        return;
    }

    const socket = new SockJS("/ws");

    window.stompClient = new StompJs.Client({
        webSocketFactory: () => socket,
        connectHeaders: {
            Authorization: "Bearer " + token
        },
        reconnectDelay: 5000,
        debug: str => console.log("[STOMP]", str)
    });

	stompClient.onConnect = frame => {
	    console.log("✅ WS connected");

	    window.currentUser = frame.headers["user-name"];

	    subscribePresence();

	    // 🔥 MODE-AWARE SUBSCRIPTION
	    if (window.chatMode === "PUBLIC") {
	        subscribePublicChat();
	    }

	    if (window.chatMode === "PRIVATE" && window.currentRoomId) {
	        if (window.privateSubscription) {
	            window.privateSubscription.unsubscribe();
	            window.privateSubscription = null;
	        }

	        window.privateSubscription = stompClient.subscribe(
	            `/topic/chat/${window.currentRoomId}`,
	            msg => {
	                const message = JSON.parse(msg.body);
	                if (!message) return;

	                if (message.roomId !== window.currentRoomId) return;

	                if (message.type === "CHAT") {
	                    renderPrivateMessage(message);
	                }
	            }
	        );
	    }
	};

    stompClient.onStompError = frame => {
        console.error("❌ Broker error:", frame.headers["message"]);
        console.error("Details:", frame.body);
    };

    stompClient.onWebSocketError = err => {
        console.error("❌ WebSocket error", err);
    };

    stompClient.activate();
}


/* ================= PRESENCE ================= */

function subscribePresence() {

    if (window.presenceSubscription) {
        window.presenceSubscription.unsubscribe();
        window.presenceSubscription = null;
    }

    window.presenceSubscription = stompClient.subscribe(
        "/topic/presence",
        msg => {
            const data = JSON.parse(msg.body);
            updateUserPresence(data.user, data.status);
        }
    );

    fetch("/api/presence/online", {
        headers: { Authorization: "Bearer " + localStorage.getItem("jwt") }
    })
    .then(r => r.json())
    .then(users => {
        document.querySelectorAll(".private-user")
            .forEach(u => u.classList.remove("online"));

        window.onlineUsers.clear();

        users.forEach(username => {
            updateUserPresence(username, "ONLINE");
        });
    });
}

function updateUserPresence(username, status) {
    if (!username) return;

    const normalized = username.toLowerCase();

    const el = document.querySelector(
        `.private-user[data-username="${normalized}"]`
    );

    if (!el) return;

    if (status === "ONLINE") {
        window.onlineUsers.add(normalized);
        el.classList.add("online");
    } else {
        window.onlineUsers.delete(normalized);
        el.classList.remove("online");
    }
}

