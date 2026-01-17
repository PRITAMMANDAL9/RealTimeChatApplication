document.addEventListener("DOMContentLoaded", () => {
    const token = localStorage.getItem("jwt");
    if (!token) {
        window.location.href = "/login";
        return;
    }

    loadIncoming();
    loadSent();
});

/* ---------------------------
   SEND REQUEST
---------------------------- */
function sendRequest() {
    const username = document.getElementById("usernameInput").value.trim();
    if (!username) return alert("Username required");

    fetch(`/api/chat-requests/${username}`, {
        method: "POST",
        headers: {
            Authorization: "Bearer " + localStorage.getItem("jwt")
        }
    })
    .then(res => {
        if (res.status === 401) {
            logout();
            return;
        }
        if (!res.ok) throw new Error("Failed to send request");
        return res.text();
    })
    .then(() => loadSent())
    .catch(err => alert(err.message));
}

/* ---------------------------
   LOAD INCOMING
---------------------------- */
function loadIncoming() {
    fetch("/api/chat-requests/incoming", {
        headers: {
            Authorization: "Bearer " + localStorage.getItem("jwt")
        }
    })
    .then(res => {
        if (res.status === 401) {
            logout();
            return [];
        }
        return res.json();
    })
    .then(data => {
        const div = document.getElementById("incomingRequests");
        if (!div) return;

        div.innerHTML = "";

        if (!data.length) {
            div.innerHTML = "<p class='text-muted'>No incoming requests</p>";
            return;
        }

        data.forEach(req => {
            const el = document.createElement("div");
            el.className = "request-item";
            el.innerHTML = `
                <strong>${req.senderUsername}</strong>
                <button onclick="accept(${req.id})">Accept</button>
                <button onclick="reject(${req.id})">Reject</button>
            `;
            div.appendChild(el);
        });
    });
}

/* ---------------------------
   LOAD SENT
---------------------------- */
function loadSent() {
    fetch("/api/chat-requests/sent", {
        headers: {
            Authorization: "Bearer " + localStorage.getItem("jwt")
        }
    })
    .then(res => {
        if (res.status === 401) {
            logout();
            return [];
        }
        return res.json();
    })
    .then(data => {
        const div = document.getElementById("sentRequests");
        if (!div) return;

        div.innerHTML = "";

        if (!data.length) {
            div.innerHTML = "<p class='text-muted'>No sent requests</p>";
            return;
        }

        data.forEach(req => {
            const el = document.createElement("div");
            el.innerText = `${req.receiverUsername} (${req.status})`;
            div.appendChild(el);
        });
    });
}

/* ---------------------------
   ACCEPT / REJECT
---------------------------- */
function accept(id) {
    fetch(`/api/chat-requests/${id}/accept`, {
        method: "POST",
        headers: {
            Authorization: "Bearer " + localStorage.getItem("jwt")
        }
    }).then(() => {
        loadIncoming();
        notifyPrivateChatRefresh();
    });
}

function reject(id) {
    fetch(`/api/chat-requests/${id}/reject`, {
        method: "POST",
        headers: {
            Authorization: "Bearer " + localStorage.getItem("jwt")
        }
    }).then(loadIncoming);
}

/* ---------------------------
   LOGOUT HANDLER
---------------------------- */
function logout() {
    localStorage.removeItem("jwt");
    window.location.href = "/login";
}

/* ---------------------------
   REALTIME REFRESH HOOK
---------------------------- */
function notifyPrivateChatRefresh() {
    if (window.stompClient?.connected) {
        stompClient.publish({
            destination: "/app/private-refresh",
            body: "{}"
        });
    }
}
