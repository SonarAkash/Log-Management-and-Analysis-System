document.addEventListener('DOMContentLoaded', () => {
    // --- STATE & CONFIG ---
    let stompClient = null;
    let jwtToken = localStorage.getItem('jwtToken');
    const logBuffer = [];
    let isRendering = false;
    let isAutoScrollEnabled = true;
    const MAX_LOG_LINES = 1000; // Prevents the browser from crashing with too many logs

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
    const autoscrollToggle = document.getElementById('autoscroll-toggle');
    const filterInput = document.getElementById('filter-input');
    const clearButton = document.getElementById('clear-button');

    // --- INITIALIZATION ---
    if (jwtToken) {
        showView('log-view');
        connectWebSocket();
    } else {
        showView('login-view');
    }

    // Start the rendering loop for smooth, throttled updates
    setInterval(renderLogBuffer, 250); // Render logs every 250ms

    // --- EVENT LISTENERS ---
    showSignupLink.addEventListener('click', (e) => { e.preventDefault(); showView('signup-view'); });
    showLoginLink.addEventListener('click', (e) => { e.preventDefault(); showView('login-view'); });

    loginForm.addEventListener('submit', async (e) => {
        e.preventDefault();
        const email = document.getElementById('login-email').value;
        const password = document.getElementById('login-password').value;
        try {
            // Using your corrected API path
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
            // Using your corrected API path
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
            stompClient.deactivate();
        }
        showView('login-view');
        logContainer.innerHTML = '';
        logBuffer.length = 0;
    });

    autoscrollToggle.addEventListener('change', () => {
        isAutoScrollEnabled = autoscrollToggle.checked;
        if (isAutoScrollEnabled) {
            scrollToBottom();
        }
    });

    logContainer.addEventListener('scroll', () => {
        if (logContainer.scrollTop + logContainer.clientHeight < logContainer.scrollHeight - 20) {
            if (isAutoScrollEnabled) {
                isAutoScrollEnabled = false;
                autoscrollToggle.checked = false;
            }
        }
    });

    clearButton.addEventListener('click', () => {
        logContainer.innerHTML = '';
    });

    filterInput.addEventListener('input', () => {
        const filterText = filterInput.value.toLowerCase();
        const logEntries = logContainer.children;
        for (const entry of logEntries) {
            const entryText = entry.textContent.toLowerCase();
            if (entryText.includes(filterText)) {
                entry.classList.remove('hidden');
            } else {
                entry.classList.add('hidden');
            }
        }
    });

    logContainer.addEventListener('click', (e) => {
        const summary = e.target.closest('.log-json-summary');
        if (summary) {
            const fullView = summary.nextElementSibling;
            if (fullView && fullView.classList.contains('log-json-full')) {
                fullView.style.display = fullView.style.display === 'none' ? 'block' : 'none';
            }
        }
    });

    // Add this new event listener to handle expanding JSON logs
        logContainer.addEventListener('click', (e) => {
            const targetLogEntry = e.target.closest('.log-entry'); // Find the closest log entry
            if (!targetLogEntry) return; // Not a log entry

            const jsonSummary = targetLogEntry.querySelector('.log-json-summary');
            const jsonFull = targetLogEntry.querySelector('.log-json-full');

            if (jsonSummary && jsonFull) {
                // This log entry contains expandable JSON
                jsonFull.style.display = jsonFull.style.display === 'none' ? 'block' : 'none';
            }
        });

    // --- RENDERING & CORE LOGIC ---
    function renderLogBuffer() {
        if (isRendering || logBuffer.length === 0) return;

        isRendering = true;
        const fragment = document.createDocumentFragment();
        const itemsToRender = logBuffer.splice(0, logBuffer.length);

        itemsToRender.forEach(data => {
            const logElement = createLogElement(data);
            fragment.appendChild(logElement);
        });

        const shouldScroll = isAutoScrollEnabled && (logContainer.scrollTop + logContainer.clientHeight === logContainer.scrollHeight);
        logContainer.appendChild(fragment);

        while (logContainer.children.length > MAX_LOG_LINES) {
            logContainer.removeChild(logContainer.firstChild);
        }

        if (isAutoScrollEnabled || shouldScroll) {
            scrollToBottom();
        }
        isRendering = false;
    }

    function onMessageReceived(message) {
        try {
            const data = JSON.parse(message.body);
            logBuffer.push(data); // Add to buffer instead of directly rendering
        } catch (e) {
            console.error("Failed to parse incoming log message:", message.body);
        }
    }

    function connectWebSocket() {
        if (!jwtToken || stompClient?.active) return;
        updateConnectionStatus('Connecting...', 'status-connecting');

        stompClient = new StompJs.Client({
            webSocketFactory: () => new SockJS('/websocket-connect'),
            connectHeaders: { Authorization: `Bearer ${jwtToken}` },
            heartbeatIncoming: 10000,
            heartbeatOutgoing: 10000,
            reconnectDelay: 5000,
            debug: (str) => { console.log('STOMP DEBUG:', str); },
        });

        stompClient.onConnect = (frame) => {
            updateConnectionStatus('Connected', 'status-connected');

            // Using your corrected API path
            fetch('subscribe-stream', {
                method: 'POST',
                headers: { 'Authorization': `Bearer ${jwtToken}` }
            })
            .then(response => {
                if (!response.ok) throw new Error('Failed to register for stream via API');
                return response.text();
            })
            .then(tenantId => {
                console.log("Successfully registered. Tenant ID:", tenantId);
                const destination = `/queue/stream/${tenantId}`;
                stompClient.subscribe(destination, onMessageReceived);
            })
            .catch(error => { console.error(error); alert(error.message); });
        };

        stompClient.onStompError = (frame) => {
            console.error('Broker reported error: ' + frame.headers['message']);
            updateConnectionStatus('Disconnected', 'status-disconnected');
        };

        stompClient.activate();
    }

    function createLogElement(data) {
        const logEntry = document.createElement('div');
        logEntry.className = 'log-entry';

        const levelSpan = document.createElement('span');
        let logLevel = 'INFO'; // Default

        // Try to parse log level from the content for color-coding
        const levelMatch = data.payload.match(/level=(\w+)/i) ||
                           (data.type === 'JSON' && data.payload.match(/"level":\s*"(\w+)"/i));
        if (levelMatch && levelMatch[1]) {
            logLevel = levelMatch[1].toUpperCase();
        }

        levelSpan.className = `log-level log-level-${logLevel}`;
        levelSpan.textContent = `[${logLevel}]`;

        const contentSpan = document.createElement('span');
        contentSpan.className = 'log-content';

        if (data.type === 'JSON') {
            try {
                const jsonObj = JSON.parse(data.payload);
                const summary = document.createElement('div');
                summary.className = 'log-json-summary';
                summary.textContent = jsonObj.message || 'JSON Log (click to expand)';

                const full = document.createElement('div');
                full.className = 'log-json-full';
                full.innerHTML = syntaxHighlightJson(jsonObj);

                contentSpan.appendChild(summary);
                contentSpan.appendChild(full);
            } catch (e) {
                contentSpan.textContent = data.payload;
            }
        } else {
            contentSpan.textContent = data.payload;
        }

        logEntry.appendChild(levelSpan);
        logEntry.appendChild(contentSpan);
        return logEntry;
    }

    // --- UTILITY FUNCTIONS ---
    function scrollToBottom() {
        logContainer.scrollTop = logContainer.scrollHeight;
    }

    function syntaxHighlightJson(json) {
        if (typeof json != 'string') {
            json = JSON.stringify(json, undefined, 2);
        }
        json = json.replace(/&/g, '&amp;').replace(/</g, '&lt;').replace(/>/g, '&gt;');
        return json.replace(/("(\\u[a-zA-Z0-9]{4}|\\[^u]|[^\\"])*"(\s*:)?|\b(true|false|null)\b|-?\d+(?:\.\d*)?(?:[eE][+\-]?\d+)?)/g, function (match) {
            let cls = 'json-number';
            if (/^"/.test(match)) {
                cls = /:$/.test(match) ? 'json-key' : 'json-string';
            } else if (/true|false/.test(match)) {
                cls = 'json-boolean';
            } else if (/null/.test(match)) {
                cls = 'json-null';
            }
            return '<span class="' + cls + '">' + match + '</span>';
        });
    }

    function showView(viewId) {
        document.querySelectorAll('.view').forEach(v => v.style.display = 'none');
        document.getElementById(viewId).style.display = 'flex';
        if (viewId === 'login-view' || viewId === 'signup-view') {
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