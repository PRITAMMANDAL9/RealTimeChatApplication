// websocket.js

window.stompClient = null;
window.currentUser = null;
window.privateSubscription = null;
window.lastPrivateRoomId = null;
window.hasJoinedPublic = false;
window.joinedPrivateRooms = new Set();


function connectWebSocket() {
    const token = localStorage.getItem("jwt");

    if (!token) {
        window.location.href = "/login";
        return;
    }

    const socket = new SockJS("/ws");

    window.stompClient = new StompJs.Client({
        webSocketFactory: () => socket,
        connectHeaders: {
            Authorization: "Bearer " + token
        },
        reconnectDelay: 5000
    });

	stompClient.onConnect = frame => {
	    console.log("✅ WS connected");

	    window.currentUser = frame.headers["user-name"];

	    if (typeof subscribePublicChat === "function") {
	        subscribePublicChat();
	    }

	    // ✅ JOIN only once per browser session
	    if (!window.hasJoinedPublic) {
	        stompClient.publish({
	            destination: "/app/sendMessage",
	            body: JSON.stringify({ type: "JOIN" })
	        });
	        window.hasJoinedPublic = true;
	    }

	    // 🔁 restore private room after reconnect
	    if (window.lastPrivateRoomId) {
	        restorePrivateChat(window.lastPrivateRoomId);
	    }
	};


    stompClient.onStompError = () => {
        console.error("❌ WS auth failed");
        localStorage.removeItem("jwt");
        window.location.href = "/login";
    };

    stompClient.activate();
}

/* -----------------------------
   PUBLIC MESSAGE
----------------------------- */
function sendMessage(payload) {
    if (!stompClient?.connected) return;

    stompClient.publish({
        destination: "/app/sendMessage",
        body: JSON.stringify(payload)
    });
}

/* -----------------------------
   PRIVATE MESSAGE
----------------------------- */
function sendPrivateMessage(roomId, text) {
    if (!stompClient?.connected) return;

    stompClient.publish({
        destination: `/app/chat/${roomId}`,
        body: JSON.stringify({
            type: "CHAT",
            content: text
        })
    });
}

/* -----------------------------
   RESTORE PRIVATE CHAT
----------------------------- */
function restorePrivateChat(roomId) {
    console.log("🔁 Restoring private room:", roomId);

    if (window.privateSubscription) {
        window.privateSubscription.unsubscribe();
        window.privateSubscription = null;
    }

    if (typeof loadPrivateHistory === "function") {
        loadPrivateHistory(roomId);
    }

    window.privateSubscription = stompClient.subscribe(
        `/topic/chat/${roomId}`,
        msg => showMessage(JSON.parse(msg.body))
    );

    // ✅ JOIN only once per room
    if (!window.joinedPrivateRooms.has(roomId)) {
        stompClient.publish({
            destination: `/app/chat/${roomId}`,
            body: JSON.stringify({ type: "JOIN" })
        });
        window.joinedPrivateRooms.add(roomId);
    }
}
