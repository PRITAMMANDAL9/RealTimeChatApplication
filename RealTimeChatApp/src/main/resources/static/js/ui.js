/* =========================================================
   UI CONTROLLER – SINGLE SOURCE OF TRUTH
   ========================================================= */

const UI = (() => {

    /* ================= ELEMENT CACHE ================= */

    const els = {};

    /* ================= STATE ================= */

    let activeView = "PUBLIC";

    /* ================= INIT ================= */

    function init() {
        cacheElements();
        bindUIActions();
        openPublicChat(); // default
    }

    function cacheElements() {
        els.navPublic = document.getElementById("nav-public");
        els.navPrivate = document.getElementById("nav-private");
        els.navRequests = document.getElementById("nav-requests");

        els.privatePanel = document.getElementById("privatePanel");
        els.chatTitle = document.getElementById("chatTitle");
        els.privateSearch = document.getElementById("privateSearch");
    }

    /* ================= BINDINGS ================= */

    function bindUIActions() {
        els.navPublic?.addEventListener("click", openPublicChat);
        els.navPrivate?.addEventListener("click", openPrivateChatPanel);
        els.navRequests?.addEventListener("click", openRequests);

        els.privateSearch?.addEventListener("input", filterPrivateUsers);
    }

    /* ================= NAV HELPERS ================= */

    function setActiveIcon(activeBtn) {
        document
            .querySelectorAll(".icon-btn")
            .forEach(btn => btn.classList.remove("active"));

        activeBtn?.classList.add("active");
    }

    /* ================= PUBLIC CHAT ================= */

    function openPublicChat() {
        activeView = "PUBLIC";
        window.chatMode = "PUBLIC";

        setActiveIcon(els.navPublic);
        closePrivatePanel();

        els.chatTitle.innerText = "Public Chat";

        if (!window.hasJoinedPublic && typeof subscribePublicChat === "function") {
            subscribePublicChat();
            window.hasJoinedPublic = true;
        }
    }

    /* ================= PRIVATE CHAT ================= */

    function openPrivateChatPanel() {
        activeView = "PRIVATE";
        window.chatMode = "PRIVATE";

        setActiveIcon(els.navPrivate);
        openPrivatePanel();

        els.chatTitle.innerText = "Private Chat";
    }

    function openPrivatePanel() {
        els.privatePanel?.classList.add("open");
        els.privateSearch?.focus();
    }

    function closePrivatePanel() {
        els.privatePanel?.classList.remove("open");
    }

    function openPrivateChat(username) {
        window.chatMode = "PRIVATE";
        els.chatTitle.innerText = username;
        closePrivatePanel();
    }

    /* ================= SEARCH ================= */

    function filterPrivateUsers(e) {
        const term = e.target.value.toLowerCase();

        document
            .querySelectorAll(".private-user")
            .forEach(user => {
                const name = user.dataset.username?.toLowerCase() || "";
                user.style.display = name.includes(term)
                    ? "flex"
                    : "none";
            });
    }

    /* ================= REQUESTS ================= */

    function openRequests() {
        setActiveIcon(els.navRequests);
        closePrivatePanel();
        window.location.href = "/requests";
    }

    /* ================= NOTIFICATIONS ================= */

    function showNotification(notification) {
        console.log("🔔", notification.message);
        incrementBadge("nav-requests");
    }

    function incrementBadge(id) {
        const btn = document.getElementById(id);
        btn?.classList.add("has-alert");
    }

    /* ================= PUBLIC API ================= */

    return {
        init,
        openPrivateChat,
        showNotification
    };

})();

/* ================= START ================= */

document.addEventListener("DOMContentLoaded", UI.init);
