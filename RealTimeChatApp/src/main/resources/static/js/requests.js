/* ======================================================
   CHAT REQUESTS UI CONTROLLER (PRODUCTION READY)
   ====================================================== */

document.addEventListener("DOMContentLoaded", initRequests);

/* ================= STATE ================= */

let incomingEl;
let sentEl;
let usernameInput;

/* ================= INIT ================= */

function initRequests() {
    const token = getToken();
    if (!token) return redirectLogin();

    cacheElements();
    bindSendButton();
    loadIncoming();
    loadSent();
}

/* ================= CACHE ================= */

function cacheElements() {
    incomingEl = document.getElementById("incomingList");
    sentEl = document.getElementById("sentList");
    usernameInput = document.getElementById("requestUsername");
}

/* ================= TOKEN HELPERS ================= */

function getToken() {
    return localStorage.getItem("jwt");
}

function authHeaders() {
    return {
        Authorization: "Bearer " + getToken()
    };
}

/* ================= SEND REQUEST ================= */

function bindSendButton() {
    const btn = document.getElementById("sendRequestBtn");
    if (!btn) return;

    btn.addEventListener("click", () => {
        const username = usernameInput?.value.trim();
        if (!username) return alert("Username required");

        sendRequest(username);
    });
}

function sendRequest(username) {
    fetch(`/api/chat-requests/${username}`, {
        method: "POST",
        headers: authHeaders()
    })
        .then(handleResponse)
        .then(() => {
            usernameInput.value = "";
            loadSent();
        })
        .catch(err => alert(err.message));
}

/* ================= LOAD INCOMING ================= */

function loadIncoming() {
    fetch("/api/chat-requests/incoming", {
        headers: authHeaders()
    })
        .then(handleAuth)
        .then(data => renderIncoming(data))
        .catch(console.error);
}

function renderIncoming(data = []) {
    if (!incomingEl) return;

    incomingEl.innerHTML = "";

    if (!data.length) {
        incomingEl.innerHTML =
            `<div class="empty-state">No incoming requests</div>`;
        return;
    }

    data.forEach(req => {
        const item = createIncomingItem(req);
        incomingEl.appendChild(item);
    });
}

function createIncomingItem(req) {
    const item = document.createElement("div");
    item.className = "request-item";
    item.dataset.id = req.id;

    item.innerHTML = `
        <div class="request-left">
            <div class="request-avatar">
                ${createAvatarInitial(req.senderUsername)}
            </div>
            <span class="request-user">${req.senderUsername}</span>
        </div>

        <div class="request-actions">
            <button class="btn-accept">Accept</button>
            <button class="btn-decline">Decline</button>
        </div>
    `;

    item.querySelector(".btn-accept")
        .addEventListener("click", () =>
            acceptRequest(req.id, item)
        );

    item.querySelector(".btn-decline")
        .addEventListener("click", () =>
            rejectRequest(req.id, item)
        );

    return item;
}

/* ================= LOAD SENT ================= */

function loadSent() {
    fetch("/api/chat-requests/sent", {
        headers: authHeaders()
    })
        .then(handleAuth)
        .then(data => renderSent(data))
        .catch(console.error);
}

function renderSent(data = []) {
    if (!sentEl) return;

    sentEl.innerHTML = "";

    if (!data.length) {
        sentEl.innerHTML =
            `<div class="empty-state">No sent requests</div>`;
        return;
    }

    data.forEach(req => {
        const item = document.createElement("div");
        item.className = "request-item muted";

        item.innerHTML = `
            <div class="request-left">
                <div class="request-avatar">
                    ${createAvatarInitial(req.receiverUsername)}
                </div>
                <span class="request-user">
                    ${req.receiverUsername}
                </span>
            </div>

            <span class="pending-badge">
                ${req.status}
            </span>
        `;

        sentEl.appendChild(item);
    });
}

/* ================= ACCEPT / REJECT ================= */

function acceptRequest(id, element) {
    fetch(`/api/chat-requests/${id}/accept`, {
        method: "POST",
        headers: authHeaders()
    })
        .then(handleResponse)
        .then(() => {
            animateRemoval(element);
            loadSent();
            notifyPrivateChatRefresh();
        })
        .catch(err => alert(err.message));
}

function rejectRequest(id, element) {
    fetch(`/api/chat-requests/${id}/reject`, {
        method: "POST",
        headers: authHeaders()
    })
        .then(handleResponse)
        .then(() => animateRemoval(element))
        .catch(err => alert(err.message));
}

/* ================= ANIMATION ================= */

function animateRemoval(element) {
    element.classList.add("removing");

    setTimeout(() => {
        element.remove();
    }, 250);
}

/* ================= HELPERS ================= */

function createAvatarInitial(username) {
    return username?.charAt(0).toUpperCase() || "?";
}

function handleAuth(res) {
    if (res.status === 401) {
        logout();
        return [];
    }
    return res.json();
}

function handleResponse(res) {
    if (!res.ok) {
        if (res.status === 404) throw new Error("User not found");
        if (res.status === 409) throw new Error("Request already exists");
        throw new Error("Request failed");
    }
}

function notifyPrivateChatRefresh() {
    if (window.stompClient?.connected) {
        stompClient.publish({
            destination: "/app/private-refresh",
            body: "{}"
        });
    }
}

function logout() {
    localStorage.removeItem("jwt");
    redirectLogin();
}

function redirectLogin() {
    window.location.href = "/login";
}
