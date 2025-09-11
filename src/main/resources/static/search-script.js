document.addEventListener('DOMContentLoaded', () => {
    const jwtToken = localStorage.getItem('jwtToken');
    const PAGE_SIZE = 50;
    let currentSearchState = { query: '', start: null, end: null, page: 0 };

    const lsqlInput = document.getElementById('lsql-input');
    const searchButton = document.getElementById('search-button');
    const daterangeInput = document.getElementById('daterange-input');
    const resultsContainer = document.getElementById('results-container');
    const initialState = document.getElementById('initial-state');
    const loadingSpinner = document.getElementById('loading-spinner');
    const emptyState = document.getElementById('empty-state');
    const paginationControls = document.getElementById('pagination-controls');
    const pageInfo = document.getElementById('page-info');
    const prevButton = document.getElementById('prev-button');
    const nextButton = document.getElementById('next-button');
    const logoutButton = document.getElementById('logout-button');
    const userEmailDisplay = document.getElementById('user-email-display');
    const notification = document.getElementById('notification');

    if (!jwtToken) { window.location.href = 'auth.html'; return; }
    try {
        const payload = JSON.parse(atob(jwtToken.split('.')[1]));
        userEmailDisplay.textContent = payload.sub;
    } catch (e) { window.location.href = 'auth.html'; }

     const datepicker = new Litepicker({
            element: daterangeInput,
            singleMode: false,
            timePicker: true,
            format: 'YYYY-MM-DD hh:mm A' // Example: 2025-09-24 10:30 AM
        });

    const urlParams = new URLSearchParams(window.location.search);
    if (urlParams.has('q')) {
        lsqlInput.value = urlParams.get('q');
        const page = parseInt(urlParams.get('page')) || 0;
        performSearch(page);
    }

    searchButton.addEventListener('click', () => performSearch(0));
    lsqlInput.addEventListener('keyup', (e) => { if (e.key === 'Enter') performSearch(0); });
    prevButton.addEventListener('click', () => performSearch(currentSearchState.page - 1));
    nextButton.addEventListener('click', () => performSearch(currentSearchState.page + 1));
    logoutButton.addEventListener('click', () => {
        localStorage.removeItem('jwtToken');
        window.location.href = 'index.html';
    });
    resultsContainer.addEventListener('click', (e) => {
        const summary = e.target.closest('.log-json-summary');
        if (summary) {
            const fullView = summary.nextElementSibling;
            if (fullView?.classList.contains('log-json-full')) {
                fullView.style.display = fullView.style.display === 'none' ? 'block' : 'none';
            }
        }
    });

    async function performSearch(page) {
        if (!lsqlInput.value.trim()) {
            showNotification('Please enter a search query.');
            return;
        }
        showState('loading');

        const startDate = datepicker.getStartDate();
        const endDate = datepicker.getEndDate();

        currentSearchState = {
            query: lsqlInput.value.trim(),
            page: page,
            start: startDate ? startDate.toJSDate().toISOString() : null,
            end: endDate ? endDate.toJSDate().toISOString() : null
        };

        const url = `logs/search?page=${currentSearchState.page}&size=${PAGE_SIZE}`;

        const body = {
            q: currentSearchState.query,
            start: currentSearchState.start,
            end: currentSearchState.end
        };

        const browserParams = new URLSearchParams({
            q: currentSearchState.query,
            page: currentSearchState.page
        });
        if (currentSearchState.start) browserParams.append('start', currentSearchState.start);
        if (currentSearchState.end) browserParams.append('end', currentSearchState.end);
        history.pushState({ page }, '', `search.html?${browserParams.toString()}`);

        try {
            // Debug: Log the search request
            console.log('Sending search request:', {
                url,
                body,
                page: currentSearchState.page
            });

            const response = await fetch(url, {
                method: 'POST',
                headers: {
                    'Authorization': `Bearer ${jwtToken}`,
                    'Content-Type': 'application/json',
                    'Accept': 'application/json'
                },
                body: JSON.stringify(body)
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
                console.error('Search failed:', {
                    status: response.status,
                    statusText: response.statusText,
                    data: data
                });

                let errorMsg;
                if (data && data.error) {
                    errorMsg = data.error;  // Use the exact error message from the response
                } else if (data && typeof data === 'object') {
                    errorMsg = data.message || data.errorMessage || 'Search failed';
                } else if (typeof data === 'string') {
                    errorMsg = data;
                } else {
                    errorMsg = `Search failed with status: ${response.status}`;
                }
                
                throw new Error(errorMsg);
            }

            renderResults(data);
        } catch (error) {
            console.error('Search error:', error);
            showNotification(error.message);
            showState('initial');
        }
    }

    function renderResults(data) {
        resultsContainer.innerHTML = '';
        if (data.content.length === 0) {
            showState('empty');
            return;
        }

        const fragment = document.createDocumentFragment();
        const barewords = currentSearchState.query.match(/(?:[^\s"]+|"[^"]*")+/g)
            ?.filter(term => !term.includes(':') && !term.includes('='))
            .map(term => term.replace(/"/g, '')) || [];

        data.content.forEach(log => fragment.appendChild(createLogElement(log, barewords)));
        resultsContainer.appendChild(fragment);

        renderPagination(data);
        showState('results');
    }

    function renderPagination(data) {
        if (data.totalPages <= 0) return;
        const startItem = data.pageable.offset + 1;
        const endItem = data.pageable.offset + data.numberOfElements;
        pageInfo.textContent = `Showing ${startItem}-${endItem} of ${data.totalElements} logs`;
        prevButton.disabled = data.first;
        nextButton.disabled = data.last;
    }

    function createLogElement(log, barewordsToHighlight) {
            const logEntry = document.createElement('div');
            logEntry.className = 'log-entry';

            const levelSpan = document.createElement('span');
            const logLevel = log.level?.toUpperCase() || 'INFO';
            levelSpan.className = `log-level log-level-${logLevel}`;
            levelSpan.textContent = `[${logLevel}]`;

            const contentSpan = document.createElement('span');
            contentSpan.className = 'log-content';

            let messageText = String(log.message || '');

            if (barewordsToHighlight.length > 0 && messageText) {
                const regex = new RegExp(barewordsToHighlight.map(b => b.replace(/[.*+?^${}()|[\]\\]/g, '\\$&')).join('|'), 'gi');
                messageText = messageText.replace(regex, match => `<mark>${match}</mark>`);
            }

            if (log.attrs && typeof log.attrs === 'object' && Object.keys(log.attrs).length > 0) {
                const summary = document.createElement('div');
                summary.className = 'log-json-summary';
                summary.innerHTML = messageText || 'JSON Log (click to expand)';

                const full = document.createElement('div');
                full.className = 'log-json-full';
                full.innerHTML = syntaxHighlightJson(log.attrs);

                contentSpan.append(summary, full);
            } else {
                contentSpan.innerHTML = messageText;
            }

            logEntry.append(levelSpan, contentSpan);
            return logEntry;
        }

    function showState(state) {
        [initialState, loadingSpinner, emptyState].forEach(el => el.classList.add('hidden'));
        paginationControls.classList.add('hidden');
        if (state === 'initial') initialState.classList.remove('hidden');
        if (state === 'loading') loadingSpinner.classList.remove('hidden');
        if (state === 'empty') emptyState.classList.remove('hidden');
        if (state === 'results') paginationControls.classList.remove('hidden');
    }

    function showNotification(message, type = 'error') {
        notification.textContent = message;
        notification.className = `notification ${type} show`;
        setTimeout(() => notification.classList.remove('show'), 3000);
    }

    function syntaxHighlightJson(json) {
        if (typeof json !== 'string') json = JSON.stringify(json, undefined, 2);
        json = json.replace(/&/g, '&amp;').replace(/</g, '&lt;').replace(/>/g, '&gt;');
        return json.replace(/("(\\u[a-zA-Z0-9]{4}|\\[^u]|[^\\"])*"(\s*:)?|\b(true|false|null)\b|-?\d+(?:\.\d*)?(?:[eE][+\-]?\d+)?)/g, (match) => {
            let cls = 'json-number';
            if (/^"/.test(match)) { cls = /:$/.test(match) ? 'json-key' : 'json-string';
            } else if (/true|false/.test(match)) { cls = 'json-boolean';
            } else if (/null/.test(match)) { cls = 'json-null'; }
            return `<span class="${cls}">${match}</span>`;
        });
    }
});