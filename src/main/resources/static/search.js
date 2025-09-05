document.addEventListener('DOMContentLoaded', () => {
    // --- STATE & CONFIG ---
    let jwtToken = localStorage.getItem('jwtToken');
    const PAGE_SIZE = 50;
    let currentSearchState = {
        query: '',
        start: null,
        end: null,
        page: 0
    };

    // --- DOM ELEMENTS ---
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

    // --- INITIALIZATION ---
    if (!jwtToken) {
        window.location.href = 'index.html'; // Redirect if not logged in
        return;
    }

    const datepicker = new Litepicker({
        element: daterangeInput,
        singleMode: false,
        format: 'YYYY-MM-DDTHH:mm:ss',
        timePicker: true,
        setup: (picker) => {
            picker.on('selected', (date1, date2) => {
                // This event fires when a date range is selected
            });
        }
    });

    // Check for query params in URL on page load
    const urlParams = new URLSearchParams(window.location.search);
    if (urlParams.has('q')) {
        lsqlInput.value = urlParams.get('q');
        // Add logic for start/end if you implement them in the URL
        performSearch(0);
    }

    // --- EVENT LISTENERS ---
    searchButton.addEventListener('click', () => performSearch(0));
    lsqlInput.addEventListener('keyup', (e) => {
        if (e.key === 'Enter') performSearch(0);
    });
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
            if (fullView && fullView.classList.contains('log-json-full')) {
                fullView.style.display = fullView.style.display === 'none' ? 'block' : 'none';
            }
        }
    });


    // --- CORE SEARCH FUNCTION ---
    async function performSearch(page) {
        if (!lsqlInput.value) {
            alert('Please enter a search query.');
            return;
        }

        // Show loading state
        showState('loading');

        // Store search state
        currentSearchState.query = lsqlInput.value;
        currentSearchState.page = page;
        const range = datepicker.getDateRange();
        currentSearchState.start = range ? range.from.toISOString() : '';
        currentSearchState.end = range ? range.to.toISOString() : '';

        // Build URL
        const params = new URLSearchParams({
            q: currentSearchState.query,
            page: currentSearchState.page,
            size: PAGE_SIZE,
            start: currentSearchState.start,
            end: currentSearchState.end
        });
        const url = `/api/v1/logs/search?${params.toString()}`;

        // Update browser history/URL
        const browserUrl = `?q=${encodeURIComponent(currentSearchState.query)}&start=${currentSearchState.start}&end=${currentSearchState.end}&page=${currentSearchState.page}`;
        history.pushState({page: currentSearchState.page}, '', browserUrl);

        try {
            const response = await fetch(url, {
                headers: { 'Authorization': `Bearer ${jwtToken}` }
            });
            if (!response.ok) throw new Error(`Search failed with status: ${response.status}`);
            const data = await response.json();
            renderResults(data);
        } catch (error) {
            console.error(error);
            alert(error.message);
            showState('initial');
        }
    }

    // --- RENDERING FUNCTIONS ---
    function renderResults(data) {
        resultsContainer.innerHTML = ''; // Clear previous results
        if (data.content.length === 0) {
            showState('empty');
            return;
        }

        const fragment = document.createDocumentFragment();
        const barewords = currentSearchState.query.match(/(?<=^|\s)(?!.*\S+:)\S+/g) || [];

        data.content.forEach(log => {
            const logElement = createLogElement(log, barewords);
            fragment.appendChild(logElement);
        });
        resultsContainer.appendChild(fragment);

        renderPagination(data);
        showState('results');
    }

    function renderPagination(data) {
        if(data.totalPages <= 0) return;

        pageInfo.textContent = `Page ${data.pageable.pageNumber + 1} of ${data.totalPages} (Total: ${data.totalElements} logs)`;
        prevButton.disabled = data.first;
        nextButton.disabled = data.last;
    }

    function createLogElement(log, barewordsToHighlight) {
        // This is very similar to the live tail version, but adds highlighting
        const logEntry = document.createElement('div');
        logEntry.className = 'log-entry';

        const levelSpan = document.createElement('span');
        const logLevel = log.level?.toUpperCase() || 'INFO';
        levelSpan.className = `log-level log-level-${logLevel}`;
        levelSpan.textContent = `[${logLevel}]`;

        const contentSpan = document.createElement('span');
        contentSpan.className = 'log-content';
        let message = log.message || '';

        // Highlight barewords
        if (barewordsToHighlight.length > 0) {
            const regex = new RegExp(barewordsToHighlight.join('|'), 'gi');
            message = message.replace(regex, match => `<mark>${match}</mark>`);
        }

        // Handle JSON or plain text
        if (log.attrs) {
            const summary = document.createElement('div');
            summary.className = 'log-json-summary';
            summary.innerHTML = message; // Use innerHTML to render the <mark> tags

            const full = document.createElement('div');
            full.className = 'log-json-full';
            full.innerHTML = syntaxHighlightJson(log.attrs);

            contentSpan.appendChild(summary);
            contentSpan.appendChild(full);
        } else {
            contentSpan.innerHTML = message;
        }

        logEntry.appendChild(levelSpan);
        logEntry.appendChild(contentSpan);
        return logEntry;
    }

    function showState(state) {
        initialState.classList.add('hidden');
        loadingSpinner.classList.add('hidden');
        emptyState.classList.add('hidden');
        paginationControls.classList.add('hidden');

        if (state === 'initial') initialState.classList.remove('hidden');
        if (state === 'loading') loadingSpinner.classList.remove('hidden');
        if (state === 'empty') emptyState.classList.remove('hidden');
        if (state === 'results') paginationControls.classList.remove('hidden');
    }

    // Keep the syntaxHighlightJson function from the previous script
    function syntaxHighlightJson(json) {
       // ... (same function as before)
    }
});