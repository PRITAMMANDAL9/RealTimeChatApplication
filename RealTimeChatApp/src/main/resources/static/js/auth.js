function login() {
  fetch("/api/auth/login", {
    method: "POST",
    body: JSON.stringify({ username, password })
  })
  .then(res => res.json())
  .then(data => localStorage.setItem("jwt", data.token));
}
