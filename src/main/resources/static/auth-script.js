document.addEventListener('DOMContentLoaded', () => {
    const loginView = document.getElementById('login-view');
    const signupView = document.getElementById('signup-view');
    const loginForm = document.getElementById('login-form');
    const signupForm = document.getElementById('signup-form');
    const showSignupLink = document.getElementById('show-signup');
    const showLoginLink = document.getElementById('show-login');
    const notification = document.getElementById('notification');
    const apiKeyDisplay = document.getElementById('api-key-display');


    const copyKeyBtn = document.getElementById('copy-key-btn');
    const copyUrlBtn = document.getElementById('copy-url-btn');
    const goToLoginBtn = document.getElementById('go-to-login-btn');

    if (copyKeyBtn) {
        copyKeyBtn.addEventListener('click', () => {
            const apiKey = document.getElementById('api-key-value').textContent;
            if (apiKey) copyToClipboard(apiKey, 'API Key');
        });
    }

    if (copyUrlBtn) {
        copyUrlBtn.addEventListener('click', () => {
            const ingestUrl = document.getElementById('ingest-url-value').textContent;
            if (ingestUrl) copyToClipboard(ingestUrl, 'Ingest URL');
        });
    }

    if (goToLoginBtn) {
        goToLoginBtn.addEventListener('click', () => {
            apiKeyDisplay.style.display = 'none';
            showView('login-view');
        });
    }

    showSignupLink.addEventListener('click', (e) => { e.preventDefault(); showView('signup-view'); });
    showLoginLink.addEventListener('click', (e) => { e.preventDefault(); showView('login-view'); });

    loginForm.addEventListener('submit', async (e) => {
        e.preventDefault();
        const email = document.getElementById('login-email').value;
        const password = document.getElementById('login-password').value;
        try {
            // console.log('Attempting login for:', email);

            const response = await fetch('/auth/login', {
                method: 'POST',
                headers: { 
                    'Content-Type': 'application/json',
                    'Accept': 'application/json'
                },
                body: JSON.stringify({
                    email: email,
                    password: password
                })
            });

            // console.log('Response status:', response.status);
            // console.log('Response ok:', response.ok);
            
            let data;
            const responseText = await response.text();
            // console.log('Raw response text:', responseText);
            
            try {
                data = JSON.parse(responseText);
                // console.log('Parsed response data:', data);
            } catch (parseError) {
                // console.error('Error parsing response:', parseError);
                data = { error: responseText };
            }

            if (!response.ok || !data.token) {
                // console.error('Login failed:', {
                //     status: response.status,
                //     statusText: response.statusText,
                //     data: data
                // });

                let errorMsg;
                if (data && data.error) {
                    errorMsg = data.error; 
                } else if (data && typeof data === 'object') {
                    errorMsg = data.message || data.errorMessage || 'Login failed';
                } else if (typeof data === 'string') {
                    errorMsg = data;
                } else {
                    errorMsg = 'Login failed';
                }
                
                throw new Error(errorMsg);
            }

            localStorage.setItem('jwtToken', data.token);
            window.location.href = '/dashboard.html';
        } catch (error) {
            showNotification(error.message);
        }
    });

    const signupInitiateForm = document.getElementById('signup-initiate-form');
    const signupCompleteForm = document.getElementById('signup-complete-form');
    const resendOtpBtn = document.getElementById('resend-otp-btn');
    let registrationEmail = '';

    function isValidEmail(email) {
        // RFC 5322 compliant email regex
        const emailRegex = /^[a-zA-Z0-9.!#$%&'*+/=?^_`{|}~-]+@[a-zA-Z0-9](?:[a-zA-Z0-9-]{0,61}[a-zA-Z0-9])?(?:\.[a-zA-Z0-9](?:[a-zA-Z0-9-]{0,61}[a-zA-Z0-9])?)*$/;
        return emailRegex.test(email) && !email.includes('.@') && !email.endsWith('.');
    }

    signupInitiateForm.addEventListener('submit', async (e) => {
        e.preventDefault();
        const submitBtn = signupInitiateForm.querySelector('button[type="submit"]');
        submitBtn.disabled = true;
        submitBtn.innerHTML = 'Sending OTP...';

        registrationEmail = document.getElementById('signup-email').value.trim();
        const password = document.getElementById('signup-password').value;
        const companyName = document.getElementById('signup-company').value;

        if (!isValidEmail(registrationEmail)) {
            showNotification('Please enter a valid email address');
            return;
        }
        
        sessionStorage.setItem('tempPassword', password);
        sessionStorage.setItem('tempCompanyName', companyName);

        try {
            const response = await fetch('/api/auth/register/initiate?email=' + encodeURIComponent(registrationEmail), {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json'
                }
            });

            const data = await response.json();
            
            if (!response.ok) {
                throw new Error(data.error || 'Failed to initiate registration');
            }

            signupInitiateForm.style.display = 'none';
            signupCompleteForm.style.display = 'block';
            showNotification('OTP sent to your email!', 'success');
        } catch (error) {
            showNotification(error.message);
        } finally {
            const submitBtn = signupInitiateForm.querySelector('button[type="submit"]');
            submitBtn.disabled = false;
            submitBtn.innerHTML = 'Get OTP';
        }
    });

    signupCompleteForm.addEventListener('submit', async (e) => {
        e.preventDefault();
        const otp = document.getElementById('signup-otp').value;
        const password = sessionStorage.getItem('tempPassword');
        const companyName = sessionStorage.getItem('tempCompanyName');
        try {
            // console.log('Sending registration completion request for:', registrationEmail);

            const response = await fetch(`/api/auth/register/complete?email=${encodeURIComponent(registrationEmail)}&otp=${encodeURIComponent(otp)}`, {
                method: 'POST',
                headers: { 
                    'Content-Type': 'application/json',
                    'Accept': 'application/json'
                },
                body: JSON.stringify({ 
                    email: registrationEmail,
                    password: password,
                    companyName: companyName
                })
            });

            // console.log('Response status:', response.status);
            // console.log('Response ok:', response.ok);
            
            let data;
            const responseText = await response.text();
            // console.log('Raw response text:', responseText);
            
            try {
                data = JSON.parse(responseText);
                // console.log('Parsed response data:', data);
            } catch (parseError) {
                // console.error('Error parsing response:', parseError);
                data = { error: responseText };
            }

            if (!response.ok) {
                // console.error('Registration failed:', {
                //     status: response.status,
                //     statusText: response.statusText,
                //     data: data
                // });

                let errorMsg = 'Signup failed';
                if (data && typeof data === 'object') {
                    errorMsg = data.error || data.message || data.errorMessage || errorMsg;
                } else if (typeof data === 'string') {
                    errorMsg = data;
                }
                
                throw new Error(errorMsg);
            }

            sessionStorage.removeItem('tempPassword');
            sessionStorage.removeItem('tempCompanyName');

            signupInitiateForm.style.display = 'none';
            signupCompleteForm.style.display = 'none';

            if (data.apiKey && data.ingestUrl) {
                // Scenario 1: First user of a new tenant
                document.getElementById('api-key-value').textContent = data.apiKey;
                document.getElementById('ingest-url-value').textContent = data.ingestUrl;
                apiKeyDisplay.style.display = 'block';
                showNotification('Registration successful! Please save your API key.', 'success');
            } else {
                // Scenario 2: Subsequent user of an existing tenant
                showNotification('Registration successful! Please log in.', 'success');
                setTimeout(() => showView('login-view'), 2000);
            }

        } catch (error) {
            showNotification(error.message);
        }
    });

    function showView(viewId) {
        document.querySelectorAll('.view').forEach(v => v.style.display = 'none');
        
        if(viewId === 'login-view') {
            signupInitiateForm.style.display = 'block';
            signupCompleteForm.style.display = 'none';
            apiKeyDisplay.style.display = 'none';
            
            document.getElementById('signup-email').value = '';
            document.getElementById('signup-password').value = '';
            document.getElementById('signup-company').value = '';
            document.getElementById('signup-otp').value = '';
            
            sessionStorage.removeItem('tempPassword');
            sessionStorage.removeItem('tempCompanyName');
            registrationEmail = '';
        }
        
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
            // console.error('Async: Could not copy text: ', err);
        });
    }

    resendOtpBtn.addEventListener('click', async () => {
        resendOtpBtn.disabled = true;
        resendOtpBtn.innerHTML = 'Sending...';
        try {
            const response = await fetch('/api/auth/register/resend-otp?email=' + encodeURIComponent(registrationEmail), {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json'
                }
            });

            const data = await response.json();
            
            if (!response.ok) {
                throw new Error(data.error || 'Failed to resend OTP');
            }

            showNotification('New OTP sent to your email!', 'success');
        } catch (error) {
            showNotification(error.message);
        } finally {
            resendOtpBtn.disabled = false;
            resendOtpBtn.innerHTML = 'Resend OTP';
        }
    });
});