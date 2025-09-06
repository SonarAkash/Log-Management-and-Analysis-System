document.addEventListener('DOMContentLoaded', () => {
    let stompClient = null;
    const jwtToken = localStorage.getItem('jwtToken');
    const logBuffer = [];
    let isRendering = false;
    let isAutoScrollEnabled = true;
    const MAX_LOG_LINES = 1000;

    const connectionStatus = document.getElementById('connection-status');
    const logContainer = document.getElementById('log-container');
    const autoscrollToggle = document.getElementById('autoscroll-toggle');
    const filterInput = document.getElementById('filter-input');
    const clearButton = document.getElementById('clear-button');
    const logoutButton = document.getElementById('logout-button');
    const userEmailDisplay = document.getElementById('user-email-display');
    const notification = document.getElementById('notification');

    if (!jwtToken) {
        window.location.href = 'auth.html';
        return;
    }

    try {
        const payload = JSON.parse(atob(jwtToken.split('.')[1]));
        userEmailDisplay.textContent = payload.sub;
    } catch (e) {
        console.error("Invalid JWT:", e);
        window.location.href = 'auth.html';
    }

    connectWebSocket();
    setInterval(renderLogBuffer, 250);

    logoutButton.addEventListener('click', () => {
        localStorage.removeItem('jwtToken');
        if (stompClient) stompClient.deactivate();
        window.location.href = 'index.html';
    });

    clearButton.addEventListener('click', () => { logContainer.innerHTML = ''; });

    autoscrollToggle.addEventListener('change', () => {
        isAutoScrollEnabled = autoscrollToggle.checked;
        if (isAutoScrollEnabled) scrollToBottom();
    });

    logContainer.addEventListener('scroll', () => {
        const isScrolledToBottom = logContainer.scrollTop + logContainer.clientHeight >= logContainer.scrollHeight - 20;
        if (!isScrolledToBottom && isAutoScrollEnabled) {
            isAutoScrollEnabled = false;
            autoscrollToggle.checked = false;
        }
    });

    filterInput.addEventListener('input', () => {
        const filterText = filterInput.value.toLowerCase();
        Array.from(logContainer.children).forEach(entry => {
            entry.classList.toggle('hidden', !entry.textContent.toLowerCase().includes(filterText));
        });
    });

    logContainer.addEventListener('click', (e) => {
        const summary = e.target.closest('.log-json-summary');
        if (summary) {
            const fullView = summary.nextElementSibling;
            if (fullView?.classList.contains('log-json-full')) {
                fullView.style.display = fullView.style.display === 'none' ? 'block' : 'none';
            }
        }
    });

    function renderLogBuffer() {
        if (isRendering || logBuffer.length === 0) return;

        isRendering = true;
        const fragment = document.createDocumentFragment();
        const itemsToRender = logBuffer.splice(0, 100);

        itemsToRender.forEach(data => fragment.appendChild(createLogElement(data)));

        const shouldScroll = isAutoScrollEnabled || (logContainer.scrollTop + logContainer.clientHeight === logContainer.scrollHeight);
        logContainer.appendChild(fragment);

        while (logContainer.children.length > MAX_LOG_LINES) {
            logContainer.removeChild(logContainer.firstChild);
        }

        if (shouldScroll) scrollToBottom();
        isRendering = false;
    }

    function onMessageReceived(message) {
        try {
            logBuffer.push(JSON.parse(message.body));
        } catch (e) {
            console.error("Failed to parse incoming log message:", message.body);
        }
    }

    function connectWebSocket() {
        updateConnectionStatus('Connecting...', 'status-connecting');
        stompClient = new StompJs.Client({
            webSocketFactory: () => new SockJS('/websocket-connect'),
            connectHeaders: { Authorization: `Bearer ${jwtToken}` },
            heartbeatIncoming: 10000, heartbeatOutgoing: 10000,
            reconnectDelay: 5000,
        });

        stompClient.onConnect = (frame) => {
            updateConnectionStatus('Connected', 'status-connected');
            fetch('subscribe-stream', {
                method: 'POST',
                headers: { 'Authorization': `Bearer ${jwtToken}` }
            })
            .then(res => { if (!res.ok) throw new Error('Stream registration failed'); return res.text(); })
            .then(tenantId => {
                const destination = `/queue/stream/${tenantId}`;
                stompClient.subscribe(destination, onMessageReceived);
            })
            .catch(err => { console.error(err); showNotification(err.message); });
        };

        stompClient.onStompError = (frame) => {
            console.error('Broker error:', frame.headers['message'], frame.body);
            updateConnectionStatus('Disconnected', 'status-disconnected');
        };

        stompClient.activate();
    }

    function createLogElement(data) {
        const logEntry = document.createElement('div');
        logEntry.className = 'log-entry';

        const levelSpan = document.createElement('span');
        let logLevel = 'INFO';
        const levelMatch = data.payload.match(/level=(\w+)/i) || (data.type === 'JSON' && data.payload.match(/"level":\s*"(\w+)"/i));
        if (levelMatch?.[1]) logLevel = levelMatch[1].toUpperCase();

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
                contentSpan.append(summary, full);
            } catch (e) { contentSpan.textContent = data.payload; }
        } else {
            contentSpan.textContent = data.payload;
        }

        logEntry.append(levelSpan, contentSpan);
        return logEntry;
    }

    function scrollToBottom() { logContainer.scrollTop = logContainer.scrollHeight; }

    function syntaxHighlightJson(json) {
        if (typeof json !== 'string') json = JSON.stringify(json, undefined, 2);
        json = json.replace(/&/g, '&amp;').replace(/</g, '&lt;').replace(/>/g, '&gt;');
        return json.replace(/("(\\u[a-zA-Z0-9]{4}|\\[^u]|[^\\"])*"(\s*:)?|\b(true|false|null)\b|-?\d+(?:\.\d*)?(?:[eE][+\-]?\d+)?)/g, (match) => {
            let cls = 'json-number';
            if (/^"/.test(match)) {
                cls = /:$/.test(match) ? 'json-key' : 'json-string';
            } else if (/true|false/.test(match)) {
                cls = 'json-boolean';
            } else if (/null/.test(match)) {
                cls = 'json-null';
            }
            return `<span class="${cls}">${match}</span>`;
        });
    }

    function updateConnectionStatus(text, className) {
        connectionStatus.textContent = text;
        connectionStatus.className = '';
        connectionStatus.classList.add(className);
    }

    function showNotification(message, type = 'error') {
        notification.textContent = message;
        notification.className = `notification ${type} show`;
        setTimeout(() => notification.classList.remove('show'), 3000);
    }
});