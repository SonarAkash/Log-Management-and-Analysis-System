document.addEventListener('DOMContentLoaded', () => {


    function showNotification(message, type = 'info') {
        const notification = document.createElement('div');
        notification.className = `notification ${type}`;
        notification.textContent = message;
        document.body.appendChild(notification);


        notification.offsetHeight;

        notification.classList.add('show');
        setTimeout(() => {
            notification.classList.remove('show');

            setTimeout(() => notification.remove(), 300);
        }, 3000);
    }



    const userEmailDisplay = document.getElementById('user-email-display');
    const logoutButton = document.getElementById('logout-button');
    const generateKeyBtn = document.getElementById('generate-key-btn');
    const toggleViewBtn = document.getElementById('toggle-view-btn');
    const copyKeyBtn = document.getElementById('copy-key-btn');
    const apiKeyDisplay = document.getElementById('api-key-display');
    const currentKeySection = document.getElementById('current-key-section');
    const regenerateWarning = document.getElementById('regenerate-warning');
    const noKeyPrompt = document.getElementById('no-key-prompt');
    const jwtToken = localStorage.getItem('jwtToken');



    let isConfirmingGeneration = false;
    let confirmationTimeout;



    async function fetchCurrentApiKey() {
        try {
            const response = await fetch('/api-key', {
                method: 'GET',
                headers: { 'Authorization': `Bearer ${jwtToken}` }
            });
            if (response.status === 404) return null; // No key exists, not an error
            if (!response.ok) throw new Error(`HTTP error! status: ${response.status}`);
            const data = await response.json();
            return data.apiKey;
        } catch (error) {
            console.error('Error fetching API key:', error);
            showNotification('Failed to fetch API key. Please try again.', 'error');
            return null;
        }
    }

    async function generateApiKey() {
        try {
            const response = await fetch('/new/api-key', {
                method: 'POST',
                headers: { 'Authorization': `Bearer ${jwtToken}` }
            });
            if (!response.ok) {
                const errorData = await response.json();
                throw new Error(errorData.message || `HTTP error! status: ${response.status}`);
            }
            const data = await response.json();
            updateApiKeyUI(data.apiKey);
            showNotification('New API key generated successfully!', 'success');
        } catch (error) {
            console.error('Error generating API key:', error);
            showNotification(`Failed to generate API key: ${error.message}`, 'error');
        }
    }



    function updateApiKeyUI(apiKey) {
        if (apiKey) {
            apiKeyDisplay.setAttribute('data-key', apiKey);
            apiKeyDisplay.textContent = '•'.repeat(40); // Always start hidden
            toggleViewBtn.textContent = 'View';
            currentKeySection.style.display = 'block';
            noKeyPrompt.style.display = 'none';
            generateKeyBtn.textContent = 'Generate New API Key';
        } else {
            currentKeySection.style.display = 'none';
            noKeyPrompt.style.display = 'block';
            generateKeyBtn.textContent = 'Generate Your First API Key';
        }
    }

    function toggleApiKeyVisibility() {
        const isHidden = apiKeyDisplay.textContent.includes('•');
        if (isHidden) {
            apiKeyDisplay.textContent = apiKeyDisplay.getAttribute('data-key');
            toggleViewBtn.textContent = 'Hide';
        } else {
            apiKeyDisplay.textContent = '•'.repeat(40);
            toggleViewBtn.textContent = 'View';
        }
    }

    function copyApiKey() {
        const apiKey = apiKeyDisplay.getAttribute('data-key');
        navigator.clipboard.writeText(apiKey).then(() => {
            showNotification('API key copied to clipboard!', 'success');
            copyKeyBtn.textContent = 'Copied!';
            setTimeout(() => { copyKeyBtn.textContent = 'Copy'; }, 2000);
        }).catch(err => {
            console.error('Failed to copy text:', err);
            showNotification('Failed to copy API key', 'error');
        });
    }

    function resetGenerationButton() {
        isConfirmingGeneration = false;
        regenerateWarning.style.display = 'none';
        generateKeyBtn.textContent = 'Generate New API Key';
        generateKeyBtn.classList.remove('warning-state');
        clearTimeout(confirmationTimeout);
    }



    logoutButton.addEventListener('click', () => {
        localStorage.removeItem('jwtToken');
        window.location.href = '/index.html';
    });

    toggleViewBtn.addEventListener('click', toggleApiKeyVisibility);
    copyKeyBtn.addEventListener('click', copyApiKey);

    generateKeyBtn.addEventListener('click', () => {
        const keyExists = !!apiKeyDisplay.getAttribute('data-key');

        if (keyExists && !isConfirmingGeneration) {
            isConfirmingGeneration = true;
            regenerateWarning.style.display = 'block';
            generateKeyBtn.textContent = 'Confirm Generation';
            generateKeyBtn.classList.add('warning-state');
            confirmationTimeout = setTimeout(resetGenerationButton, 5000);
        } else {
            generateApiKey().finally(resetGenerationButton);
        }
    });



    function initializeDashboard() {
        if (!jwtToken) {
            window.location.href = '/auth.html';
            return;
        }

        try {
            const payload = JSON.parse(atob(jwtToken.split('.')[1]));
            userEmailDisplay.textContent = payload.sub;
        } catch (e) {
            console.error("Failed to decode JWT:", e);
            localStorage.removeItem('jwtToken');
            window.location.href = '/auth.html';
            return;
        }

        fetchCurrentApiKey().then(apiKey => {
            updateApiKeyUI(apiKey);
        });
    }

    initializeDashboard();
});