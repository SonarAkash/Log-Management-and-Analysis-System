document.addEventListener('DOMContentLoaded', () => {
    const loginBtn = document.getElementById('login-btn');
    const getStartedBtn = document.getElementById('get-started-btn');

    const goToAuthPage = () => {
        window.location.href = 'auth.html';
    };

    loginBtn.addEventListener('click', goToAuthPage);
    getStartedBtn.addEventListener('click', goToAuthPage);
});