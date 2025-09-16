document.addEventListener('DOMContentLoaded', () => {
    const jwtToken = localStorage.getItem('jwtToken');
    const userEmailDisplay = document.getElementById('user-email-display');
    const logoutButton = document.getElementById('logout-button');

    if (!jwtToken) {
        window.location.href = '/auth.html';
        return; // Stop script execution
    }

    try {
        const payload = JSON.parse(atob(jwtToken.split('.')[1]));
        userEmailDisplay.textContent = payload.sub;
    } catch (e) {
        console.error("Failed to decode JWT:", e);
        localStorage.removeItem('jwtToken');
        window.location.href = '/auth.html';
    }

    logoutButton.addEventListener('click', () => {
        localStorage.removeItem('jwtToken');
        window.location.href = '/index.html';
    });
});