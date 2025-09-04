document.addEventListener('DOMContentLoaded', () => {
    // --- STATE & CONFIG ---
    let stompClient = null;
    let jwtToken = localStorage.getItem('jwtToken');

    // --- DOM ELEMENTS ---
    const loginView = document.getElementById('login-view');
    const signupView = document.getElementById('signup-view');
    const logView = document.getElementById('log-view');

    const loginForm = document.getElementById('login-form');
    const signupForm = document.getElementById('signup-form');
    const logoutButton = document.getElementById('logout-button');
    const showSignupLink = document.getElementById('show-signup');
    const showLoginLink = document.getElementById('show-login');
    const connectionStatus = document.getElementById('connection-status');
    const logContainer = document.getElementById('log-container');

    // --- INITIALIZATION ---
    if (jwtToken) {
        showView('log-view');
        connectWebSocket();
    } else {
        showView('login-view');
    }

    // --- EVENT LISTENERS ---
    showSignupLink.addEventListener('click', (e) => { e.preventDefault(); showView('signup-view'); });
    showLoginLink.addEventListener('click', (e) => { e.preventDefault(); showView('login-view'); });

    loginForm.addEventListener('submit', async (e) => {
        e.preventDefault();
        const email = document.getElementById('login-email').value;
        const password = document.getElementById('login-password').value;
        try {
            const response = await fetch('auth/login', {
                method: 'POST',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify({ email, password })
            });
            if (!response.ok) throw new Error('Login failed');
            const data = await response.json();
            jwtToken = data.token;
            localStorage.setItem('jwtToken', jwtToken);
            showView('log-view');
            connectWebSocket();
        } catch (error) {
            alert(error.message);
        }
    });

    signupForm.addEventListener('submit', async (e) => {
        e.preventDefault();
        const email = document.getElementById('signup-email').value;
        const password = document.getElementById('signup-password').value;
        const companyName = document.getElementById('signup-company').value;
        try {
            const response = await fetch('auth/register', {
                method: 'POST',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify({ email, password, companyName })
            });
            if (!response.ok) throw new Error('Signup failed');
            alert('Signup successful! Please log in.');
            showView('login-view');
        } catch (error) {
            alert(error.message);
        }
    });

    logoutButton.addEventListener('click', () => {
        localStorage.removeItem('jwtToken');
        jwtToken = null;
        if (stompClient) {
            stompClient.disconnect();
        }
        showView('login-view');
    });

    // --- CORE LOGIC ---
    function connectWebSocket() {
        if (!jwtToken) return;

        updateConnectionStatus('Connecting...', 'status-connecting');

        const socket = new SockJS('/websocket-connect');
        stompClient = Stomp.over(socket);

        stompClient.connect({ Authorization: `Bearer ${jwtToken}` }, (frame) => {
            updateConnectionStatus('Connected', 'status-connected');

            // 1. Register for the stream via HTTP API
            fetch('subscribe-stream', {
                method: 'POST',
                headers: { 'Authorization': `Bearer ${jwtToken}` }
            }).then(response => {
                      if (!response.ok) {
                          throw new Error('Failed to register for stream via API');
                      }
                      return response.text(); // Get the tenantId from the response body
                  })
                  .then(tenantId => {
                      console.log("Successfully subscribed. Tenant ID:", tenantId);

                      // --- THIS IS THE KEY CHANGE ---
                      // Construct the unique destination path
                      const destination = `/queue/stream/${tenantId}`;
                      console.log("Subscribing to STOMP destination:", destination);

                      // Subscribe to the explicit, tenant-specific destination
                      stompClient.subscribe(destination, onMessageReceived);
                  })
                  .catch(error => {
                      console.error(error);
                      alert(error.message);
                  });

        }, (error) => {
            console.error('STOMP error:', error);
            updateConnectionStatus('Disconnected', 'status-disconnected');
        });
    }

    function onMessageReceived(message) {
        const data = JSON.parse(message.body);
        const logEntry = document.createElement('div');
        logEntry.className = 'log-entry';

        let formattedPayload = data.payload;

        // Per your requirement, check if the type is exactly "JSON"
        if (data.type === 'JSON') {
            try {
                const jsonObj = JSON.parse(data.payload);
                // Pretty-print the JSON
                formattedPayload = JSON.stringify(jsonObj, null, 2);
                logEntry.innerHTML = `<pre class="log-json">${formattedPayload}</pre>`;
            } catch (e) {
                // If parsing fails, just show the raw string
                logEntry.textContent = formattedPayload;
            }
        } else {
             // For LOGFMT or other types, display as plain text
            logEntry.textContent = formattedPayload;
        }

        logContainer.appendChild(logEntry);
        // Auto-scroll to the bottom
        logContainer.scrollTop = logContainer.scrollHeight;
    }

    // --- UTILITY FUNCTIONS ---
    function showView(viewId) {
        document.querySelectorAll('.view').forEach(v => v.style.display = 'none');
        document.getElementById(viewId).style.display = 'flex';
        // Adjust flex for form views to center them
        if(viewId === 'login-view' || viewId === 'signup-view') {
             document.getElementById(viewId).style.justifyContent = 'center';
             document.getElementById(viewId).style.alignItems = 'center';
        }
    }

    function updateConnectionStatus(text, className) {
        connectionStatus.textContent = text;
        connectionStatus.className = '';
        connectionStatus.classList.add(className);
    }
});