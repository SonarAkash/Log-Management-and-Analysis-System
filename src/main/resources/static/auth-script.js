document.addEventListener('DOMContentLoaded', () => {
    const loginView = document.getElementById('login-view');
    const signupView = document.getElementById('signup-view');
    const loginForm = document.getElementById('login-form');
    const signupForm = document.getElementById('signup-form');
    const showSignupLink = document.getElementById('show-signup');
    const showLoginLink = document.getElementById('show-login');
    const notification = document.getElementById('notification');
    const apiKeyDisplay = document.getElementById('api-key-display');

    showSignupLink.addEventListener('click', (e) => { e.preventDefault(); showView('signup-view'); });
    showLoginLink.addEventListener('click', (e) => { e.preventDefault(); showView('login-view'); });

    loginForm.addEventListener('submit', async (e) => {
        e.preventDefault();
        const email = document.getElementById('login-email').value;
        const password = document.getElementById('login-password').value;
        try {
            // Debug: Log the login attempt
            console.log('Attempting login for:', email);

            const response = await fetch('auth/login', {
                method: 'POST',
                headers: { 
                    'Content-Type': 'application/json',
                    'Accept': 'application/json'
                },
                body: JSON.stringify({ email, password })
            });

            // Debug: Log the raw response
            console.log('Response status:', response.status);
            console.log('Response ok:', response.ok);
            
            let data;
            const responseText = await response.text();
            console.log('Raw response text:', responseText);
            
            try {
                data = JSON.parse(responseText);
                console.log('Parsed response data:', data);
            } catch (parseError) {
                console.error('Error parsing response:', parseError);
                data = { error: responseText };
            }

            if (!response.ok || !data.token) {  // Check both response.ok and if token exists
                // Debug: Log error details
                console.error('Login failed:', {
                    status: response.status,
                    statusText: response.statusText,
                    data: data
                });

                // Specifically check for the error field in the response
                let errorMsg;
                if (data && data.error) {
                    errorMsg = data.error;  // Use the exact error message from the response
                } else if (data && typeof data === 'object') {
                    errorMsg = data.message || data.errorMessage || 'Login failed';
                } else if (typeof data === 'string') {
                    errorMsg = data;
                } else {
                    errorMsg = 'Login failed';
                }
                
                throw new Error(errorMsg);
            }

            // If login successful, store token and redirect
            localStorage.setItem('jwtToken', data.token);
            window.location.href = 'dashboard.html';
        } catch (error) {
            showNotification(error.message);
        }
    });

    signupForm.addEventListener('submit', async (e) => {
        e.preventDefault();
        const email = document.getElementById('signup-email').value;
        const password = document.getElementById('signup-password').value;
        const companyName = document.getElementById('signup-company').value;
        try {
            // Debug: Log the request
            console.log('Sending registration request for:', email);

            const response = await fetch('auth/register', {
                method: 'POST',
                headers: { 
                    'Content-Type': 'application/json',
                    'Accept': 'application/json'
                },
                body: JSON.stringify({ email, password, companyName })
            });

            // Debug: Log the raw response
            console.log('Response status:', response.status);
            console.log('Response ok:', response.ok);
            
            let data;
            const responseText = await response.text();
            console.log('Raw response text:', responseText);
            
            try {
                data = JSON.parse(responseText);
                console.log('Parsed response data:', data);
            } catch (parseError) {
                console.error('Error parsing response:', parseError);
                data = { error: responseText };
            }

            if (!response.ok) {
                // Debug: Log error details
                console.error('Registration failed:', {
                    status: response.status,
                    statusText: response.statusText,
                    data: data
                });

                let errorMsg = 'Signup failed';
                if (data && typeof data === 'object') {
                    errorMsg = data.error || data.message || data.errorMessage || errorMsg;
                } else if (typeof data === 'string') {
                    errorMsg = data;
                }
                
                throw new Error(errorMsg);
            }

            if (data.apiKey && data.ingestUrl) {
                // Scenario 1: First user of a new tenant
                document.getElementById('signup-form').style.display = 'none';
                document.getElementById('api-key-value').textContent = data.apiKey;
                document.getElementById('ingest-url-value').textContent = data.ingestUrl;
                apiKeyDisplay.style.display = 'block';

                document.getElementById('copy-key-btn').addEventListener('click', () => copyToClipboard(data.apiKey, 'API Key'));
                document.getElementById('copy-url-btn').addEventListener('click', () => copyToClipboard(data.ingestUrl, 'Ingest URL'));
                document.getElementById('go-to-login-btn').addEventListener('click', () => showView('login-view'));

            } else {
                // Scenario 2: Subsequent user of an existing tenant
                showNotification('Signup successful! Please log in.', 'success');
                showView('login-view');
            }

        } catch (error) {
            showNotification(error.message);
        }
    });

    function showView(viewId) {
        if(viewId === 'login-view') {
            document.getElementById('signup-form').style.display = 'block';
            apiKeyDisplay.style.display = 'none';
        }
        document.querySelectorAll('.view').forEach(v => v.style.display = 'none');
        document.getElementById(viewId).style.display = 'flex';
    }

    function showNotification(message, type = 'error') {
        notification.textContent = message;
        notification.className = `notification ${type} show`;
        setTimeout(() => {
            notification.classList.remove('show');
        }, 4000);
    }

    function copyToClipboard(text, type) {
        navigator.clipboard.writeText(text).then(() => {
            showNotification(`${type} copied to clipboard!`, 'success');
        }, (err) => {
            showNotification(`Failed to copy ${type}.`);
            console.error('Async: Could not copy text: ', err);
        });
    }
});