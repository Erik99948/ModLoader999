document.addEventListener('DOMContentLoaded', () => {
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
