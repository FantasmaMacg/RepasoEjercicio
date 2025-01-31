function showMessage(message, isError = false) {
    const modalBody = document.getElementById('messageModalBody');
    modalBody.textContent = message;

    if (isError) {
        modalBody.style.color = 'red';
    } else {
        modalBody.style.color = 'black';
    }

    const messageModal = new bootstrap.Modal(document.getElementById('messageModal'));
    messageModal.show();
}

function confirmDelete(id) {
    const confirmModal = new bootstrap.Modal(document.getElementById('confirmModal'));
    confirmModal.show();

    const confirmDeleteButton = document.getElementById('confirmDeleteButton');
    confirmDeleteButton.onclick = () => {
        deletePartido(id); 
        confirmModal.hide(); 
    };
}

function getPartidos() {
    fetch('/api/partidos')
        .then(response => response.json())
        .then(data => {
            console.log('Partidos:', data);
            const listaPartidos = document.getElementById('listaPartidos');
            listaPartidos.innerHTML = '';

            data.forEach(partido => {
                const row = document.createElement('tr');
                let resultado = '';
                switch (partido.resultado) {
                    case 1:
                        resultado = "Ganado";
                        break;
                    case -1:
                        resultado = "Perdido";
                        break;
                    case 0:
                        resultado = "Esperando finalización";
                        break;
                }

                const deporteEmoji = {
                    futbol: "⚽",
                    tenis: "🎾",
                    baloncesto: "🏀",
                    voleibol: "🏐",
                    rugby: "🏉",
                    hockey: "🏒",
                    default: "🏅"
                };

                const emoji = deporteEmoji[partido.deporte.toLowerCase()] || deporteEmoji.default;

                let botonesAccion = `
                    <button class="btn btn-warning" onclick="abrirModalEditar(${partido.id})">Editar</button>
                    <button class="btn btn-danger" onclick="confirmDelete(${partido.id})">Eliminar</button>
                    <button class="btn btn-info" onclick="downloadFile(${partido.id})">Descargar</button>
                `;

                if (partido.resultado === 0) {
                    botonesAccion += `
                        <button class="btn btn-success" onclick="ganarPartido(${partido.id})">Ganar</button>
                        <button class="btn btn-danger" onclick="perderPartido(${partido.id})">Perder</button>
                    `;
                }

                row.innerHTML = `
                    <td>${partido.nombre}</td>
                    <td>${partido.descripcion}</td>
                    <td>${emoji} ${partido.deporte}</td>
                    <td>${resultado}</td>
                    <td>${partido.apuesta} €</td>
                    <td>${botonesAccion}</td>
                `;
                listaPartidos.appendChild(row);
            });
        })
        .catch(error => showMessage('Error al obtener los partidos: ' + error.message, true));
}

function abrirModalEditar(id) {
    fetch(`/api/partidos/${id}`)
        .then(response => response.json())
        .then(data => {
            document.getElementById('editarNombre').value = data.nombre;
            document.getElementById('editarDescripcion').value = data.descripcion;
            document.getElementById('editarDeporte').value = data.deporte; 
            document.getElementById('editarResultado').value = data.resultado;
            document.getElementById('editarApuesta').value = data.apuesta;

            const editarPartidoForm = document.getElementById('editarPartidoForm');
            editarPartidoForm.onsubmit = (event) => {
                event.preventDefault();
                const partidoActualizado = {
                    nombre: document.getElementById('editarNombre').value,
                    descripcion: document.getElementById('editarDescripcion').value,
                    deporte: document.getElementById('editarDeporte').value,
                    resultado: parseInt(document.getElementById('editarResultado').value, 10),
                    apuesta: parseFloat(document.getElementById('editarApuesta').value)
                };
                updatePartido(id, partidoActualizado);
            };

            const editarPartidoModal = new bootstrap.Modal(document.getElementById('editarPartidoModal'));
            editarPartidoModal.show();
        })
        .catch(error => showMessage('Error al obtener el partido: ' + error.message, true));
}

document.getElementById('partidoForm').addEventListener('submit', function(event) {
    event.preventDefault();

    const partido = {
        nombre: document.getElementById('nombre').value,
        descripcion: document.getElementById('descripcion').value,
        deporte: document.getElementById('deporte').value,
        resultado: parseInt(document.getElementById('resultado').value, 10),
        apuesta: parseFloat(document.getElementById('apuesta').value)
    };

    createPartido(partido);
});

function createPartido(partido) {
    fetch('/api/partidos', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify(partido)
    })
    .then(response => response.json())
    .then(data => {
        showMessage('Partido creado correctamente.');
        getPartidos(); 
        document.getElementById('partidoForm').reset();
        const crearPartidoModal = bootstrap.Modal.getInstance(document.getElementById('crearPartidoModal'));
        crearPartidoModal.hide(); 
    })
    .catch(error => showMessage('Error al crear el partido: ' + error.message, true));
}

function updatePartido(id, partido) {
    fetch(`/api/partidos/${id}`, {
        method: 'PUT',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify(partido)
    })
    .then(response => response.json())
    .then(data => {
        showMessage('Partido actualizado correctamente.');
        getPartidos(); 
        const editarPartidoModal = bootstrap.Modal.getInstance(document.getElementById('editarPartidoModal'));
        editarPartidoModal.hide();
    })
    .catch(error => showMessage('Error al actualizar el partido: ' + error.message, true));
}

function deletePartido(id) {
    fetch(`/api/partidos/${id}`, { method: 'DELETE' })
    .then(response => {
        if (response.ok) {
            showMessage('Partido eliminado correctamente.');
            getPartidos();
        } else {
            showMessage('Error al eliminar el partido.', true);
        }
    })
    .catch(error => showMessage('Error al eliminar el partido: ' + error.message, true));
}

function ganarPartido(id) {
    fetch(`/api/partidos/${id}/gana`, { method: 'POST' })
        .then(response => {
            if (!response.ok) {
                return response.text().then(errorMessage => {
                    throw new Error(errorMessage);
                });
            }
            return response.text();
        })
        .then(message => {
            showMessage(message);
            getPartidos(); 
        })
        .catch(error => showMessage('Error al ganar el partido: ' + error.message, true));
}

function perderPartido(id) {
    fetch(`/api/partidos/${id}/pierde`, { method: 'POST' })
        .then(response => {
            if (!response.ok) {
                return response.text().then(errorMessage => {
                    throw new Error(errorMessage);
                });
            }
            return response.text();
        })
        .then(message => {
            showMessage(message);
            getPartidos(); 
        })
        .catch(error => showMessage('Error al perder el partido: ' + error.message, true));
}

document.getElementById('uploadForm').addEventListener('submit', function(event) {
    event.preventDefault(); 

    const fileInput = document.getElementById('fileInput');
    const file = fileInput.files[0];

    if (!file) {
        showMessage("Por favor, selecciona un archivo.", true);
        return;
    }

    const formData = new FormData();
    formData.append('file', file);

    fetch('/api/partidos/upload', {
        method: 'POST',
        body: formData
    })
    .then(response => {
        if (!response.ok) {
            return response.text().then(errorMessage => {
                throw new Error(errorMessage);
            });
        }
        return response.text();
    })
    .then(message => {
        showMessage(message); 
        document.getElementById('uploadForm').reset();
        getPartidos();
    })
    .catch(error => {
        showMessage("Error al subir el archivo: " + error.message, true);
    });
});

function downloadFile(id) {
    fetch(`/api/partidos/download?id=${id}`)
        .then(response => {
            if (!response.ok) {
                return response.text().then(errorMessage => {
                    throw new Error(errorMessage);
                });
            }
            return response.blob();
        })
        .then(blob => {
            const link = document.createElement('a');
            link.href = URL.createObjectURL(blob);
            link.download = `archivo_partido_${id}.txt`;
            link.click();
        })
        .catch(error => showMessage("Error al descargar el archivo: " + error.message, true));
}

document.addEventListener('DOMContentLoaded', function() {
    getPartidos();
});