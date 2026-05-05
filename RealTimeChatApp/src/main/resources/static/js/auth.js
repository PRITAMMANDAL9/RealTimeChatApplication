// auth.js
function login(username, password) {
    fetch("/api/auth/login", {
        method: "POST",
        headers: {
            "Content-Type": "application/json"
        },
        body: JSON.stringify({ username, password })
    })
    .then(res => {
        if (!res.ok) throw new Error("Login failed");
        return res.json();
    })
    .then(data => {
        localStorage.setItem("jwt", data.token);
        window.location.href = "/chat";
    })
    .catch(err => alert(err.message));
}
