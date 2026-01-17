// websocket.js

window.stompClient = null;
window.currentUser = null;

function connectWebSocket() {
    const token = localStorage.getItem("jwt");

    if (!token) {
        window.location.href = "/login";
        return;
    }

    const socket = new SockJS("/chat");

    window.stompClient = new StompJs.Client({
        webSocketFactory: () => socket,
        connectHeaders: {
            Authorization: "Bearer " + token
        },
        reconnectDelay: 5000
    });

    stompClient.onConnect = frame => {
        // ✅ authenticated username from backend
        window.currentUser = frame.headers["user-name"];

        // ✅ subscribe first
        if (typeof subscribePublicChat === "function") {
            subscribePublicChat();
        }

        if (typeof subscribeNotifications === "function") {
            subscribeNotifications();
        }

        // ✅ now broadcast JOIN
        stompClient.publish({
            destination: "/app/sendMessage",
            body: JSON.stringify({ type: "JOIN" })
        });
    };

    stompClient.onStompError = () => {
        localStorage.removeItem("jwt");
        window.location.href = "/login";
    };

    stompClient.activate();
}

/* -----------------------------
   PUBLIC MESSAGE
----------------------------- */
function sendMessage(payload) {
    if (!window.stompClient || !window.stompClient.connected) return;

    stompClient.publish({
        destination: "/app/sendMessage",
        body: JSON.stringify(payload)
    });
}

/* -----------------------------
   PRIVATE MESSAGE
----------------------------- */
function sendPrivateMessage(roomId, text) {
    if (!window.stompClient || !window.stompClient.connected) return;
    if (!roomId || !text) return;

    stompClient.publish({
        destination: "/app/chat/" + roomId,
        body: JSON.stringify({
            type: "CHAT",
            content: text
        })
    });
}
