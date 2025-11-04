document.addEventListener('DOMContentLoaded', () => {
    const myModsBtn = document.getElementById('my-mods-btn');
    const publishedModsBtn = document.getElementById('published-mods-btn');
    const myModsView = document.getElementById('my-mods-view');
    const marketplaceView = document.getElementById('marketplace-view');

    myModsBtn.addEventListener('click', () => {
        myModsView.style.display = 'block';
        marketplaceView.style.display = 'none';
        myModsBtn.classList.add('active');
        publishedModsBtn.classList.remove('active');
        fetchMods();
    });

    publishedModsBtn.addEventListener('click', () => {
        myModsView.style.display = 'none';
        marketplaceView.style.display = 'block';
        myModsBtn.classList.remove('active');
        publishedModsBtn.classList.add('active');
        fetchMarketplaceMods();
    });

    fetchMods();
});

async function fetchMods() {
    try {
        const response = await fetch('/api/mods');
        const mods = await response.json();
        displayMods(mods);
    } catch (error) {
        console.error('Error fetching mods:', error);
        document.getElementById('mod-list').innerHTML = '<p>Error loading mods. Please check the server console.</p>';
    }
}

function displayMods(mods) {
    const modListDiv = document.getElementById('mod-list');
    modListDiv.innerHTML = '';
    if (mods.length === 0) {
        modListDiv.innerHTML = '<p>No mods loaded.</p>';
        return;
    }

    mods.forEach(mod => {
        const modCard = document.createElement('div');
        modCard.className = 'mod-card';
        modCard.innerHTML = `
            <h2>${mod.name} <span class="version">v${mod.version}</span></h2>
            <p><strong>ID:</strong> ${mod.id}</p>
            <p><strong>Author:</strong> ${mod.author}</p>
            <p><strong>Description:</strong> ${mod.description}</p>
            <p><strong>Status:</strong> <span class="status ${mod.state.toLowerCase()}">${mod.state}</span></p>
            <div class="actions">
                <button class="${mod.state.toLowerCase() === 'enabled' ? 'disable' : 'enable'}" data-mod-id="${mod.id}">
                    ${mod.state.toLowerCase() === 'enabled' ? 'Disable' : 'Enable'}
                </button>
                <button class="reload" data-mod-id="${mod.id}">Reload</button>
            </div>
        `;
        modListDiv.appendChild(modCard);
    });

    modListDiv.querySelectorAll('.actions button').forEach(button => {
        button.addEventListener('click', handleModAction);
    });
}

async function handleModAction(event) {
    const button = event.target;
    const modId = button.dataset.modId;
    const action = button.classList.contains('enable') ? 'enable' : (button.classList.contains('disable') ? 'disable' : 'hotreload');

    try {
        const response = await fetch(`/api/mod/${action}/${modId}`, {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json'
            }
        });
        const result = await response.json();
        if (result.status === 'success') {
            alert(result.message);
            fetchMods();
        } else {
            alert('Error: ' + result.message);
        }
    } catch (error) {
        console.error(`Error performing ${action} on mod ${modId}:`, error);
        alert(`Error performing ${action} on mod ${modId}. Check console for details.`);
    }
}

async function fetchMarketplaceMods() {
    try {
        const response = await fetch('/api/marketplace/mods');
        const mods = await response.json();
        displayMarketplaceMods(mods);
    } catch (error) {
        console.error('Error fetching marketplace mods:', error);
        document.getElementById('marketplace-list').innerHTML = '<p>Error loading marketplace mods. Please check the server console.</p>';
    }
}

function displayMarketplaceMods(mods) {
    const marketplaceListDiv = document.getElementById('marketplace-list');
    const paginationControls = document.getElementById('pagination-controls');
    marketplaceListDiv.innerHTML = '';
    paginationControls.innerHTML = '';

    if (mods.length === 0) {
        marketplaceListDiv.innerHTML = '<p>No mods in the marketplace.</p>';
        return;
    }

    let currentPage = 1;
    const modsPerPage = 10;

    function renderPage(page) {
        marketplaceListDiv.innerHTML = '';
        const start = (page - 1) * modsPerPage;
        const end = start + modsPerPage;
        const paginatedMods = mods.slice(start, end);

        paginatedMods.forEach(mod => {
            const modCard = document.createElement('div');
            modCard.className = 'mod-card';
            modCard.innerHTML = `
                <h2>${mod.name} <span class="version">v${mod.version}</span></h2>
                <p><strong>Author:</strong> ${mod.author}</p>
                <p><strong>Description:</strong> ${mod.description}</p>
                <div class="actions">
                    <a href="/download/mod/${mod.id}" class="button download-btn" download>Download</a>
                </div>
            `;
            marketplaceListDiv.appendChild(modCard);
        });
    }

    function setupPagination() {
        const pageCount = Math.ceil(mods.length / modsPerPage);
        if (pageCount <= 1) return;

        const backButton = document.createElement('button');
        backButton.innerText = 'Back';
        backButton.disabled = true;
        backButton.addEventListener('click', () => {
            if (currentPage > 1) {
                currentPage--;
                renderPage(currentPage);
                updatePaginationButtons();
            }
        });

        const forwardButton = document.createElement('button');
        forwardButton.innerText = 'Forward';
        forwardButton.addEventListener('click', () => {
            if (currentPage < pageCount) {
                currentPage++;
                renderPage(currentPage);
                updatePaginationButtons();
            }
        });

        paginationControls.appendChild(backButton);
        paginationControls.appendChild(forwardButton);

        function updatePaginationButtons() {
            backButton.disabled = currentPage === 1;
            forwardButton.disabled = currentPage === pageCount;
        }
        updatePaginationButtons();
    }

    renderPage(currentPage);
    setupPagination();
}
