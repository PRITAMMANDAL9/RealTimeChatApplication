// notifications.js

function subscribeNotifications() {
    stompClient.subscribe("/user/queue/notifications", msg => {
        const notification = JSON.parse(msg.body);
        alert(notification.message); // later replace with toast
    });
}
