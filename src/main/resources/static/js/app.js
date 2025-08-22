let archivoSeleccionado = null;
let graficaHistogramaUrl = null;
let graficaBarrasUrl = null;

document.getElementById('fileInput').addEventListener('change', function(e) {
    archivoSeleccionado = e.target.files[0];
    graficaHistogramaUrl = null;
    graficaBarrasUrl = null;

    if (archivoSeleccionado) {
        const fileInfo = document.getElementById('fileInfo');
        const fileName = document.getElementById('fileName');

        fileName.textContent = archivoSeleccionado.name;
        fileInfo.classList.remove('hidden');

        // Ocultar resultados anteriores
        document.getElementById('resultados').classList.add('hidden');
    }
});

async function analizarDatos() {
    if (!archivoSeleccionado) return;

    const loading = document.getElementById('loading');
    const resultados = document.getElementById('resultados');
    const error = document.getElementById('error');

    loading.classList.remove('hidden');
    resultados.classList.add('hidden');
    error.classList.add('hidden');

    try {
        const formData = new FormData();
        formData.append('archivo', archivoSeleccionado);

        // Analizar datos
        const response = await fetch('/api/estadistica/analizar', {
            method: 'POST',
            body: formData
        });

        if (!response.ok) throw new Error('Error en el análisis');

        const data = await response.json();
        mostrarResultados(data);

        // Cargar ambas gráficas
        await Promise.all([
            cargarGrafica('histograma'),
            cargarGrafica('barras')
        ]);

    } catch (err) {
        mostrarError(err.message);
    } finally {
        loading.classList.add('hidden');
    }
}

async function cargarGrafica(tipo) {
    const formData = new FormData();
    formData.append('archivo', archivoSeleccionado);

    const endpoint = tipo === 'histograma'
        ? '/api/estadistica/grafica/histograma'
        : '/api/estadistica/grafica/barras';

    try {
        const response = await fetch(endpoint, {
            method: 'POST',
            body: formData
        });

        if (response.ok) {
            const blob = await response.blob();
            const url = URL.createObjectURL(blob);

            if (tipo === 'histograma') {
                document.getElementById('graficaHistograma').src = url;
                graficaHistogramaUrl = url;
            } else {
                document.getElementById('graficaBarras').src = url;
                graficaBarrasUrl = url;
            }
        } else {
            console.error(`Error cargando gráfica ${tipo}:`, response.status);
        }
    } catch (error) {
        console.error(`Error cargando gráfica ${tipo}:`, error);
    }
}
function mostrarResultados(data) {
    const content = document.getElementById('resultadosContent');

    content.innerHTML = `
        <div class="stats-grid">
            <div class="stat-card">
                <h3>Media</h3>
                <p class="stat-value">${data.media.toFixed(4)}</p>
            </div>
            <div class="stat-card">
                <h3>Desviación Estándar</h3>
                <p class="stat-value">${data.desviacionEstandar.toFixed(4)}</p>
            </div>
            <div class="stat-card">
                <h3>Total de Datos</h3>
                <p class="stat-value">${data.totalDatos}</p>
            </div>
        </div>

        <div class="chebyshev-results">
            <h3>Teorema de Chebyshev</h3>
            ${data.resultadosChebyshev.map(resultado => `
                <div class="chebyshev-card ${resultado.cumpleCriterio ? 'success' : 'warning'}">
                    <h4>Para k = ${resultado.k}</h4>
                    <p>Intervalo: [${resultado.limiteInferior.toFixed(4)}, ${resultado.limiteSuperior.toFixed(4)}]</p>
                    <p>Mínimo teórico: ${resultado.minimoTeorico.toFixed(2)}%</p>
                    <p>Datos en intervalo: ${resultado.datosEnIntervalo} (${resultado.porcentajeReal.toFixed(2)}%)</p>
                    <p class="status">${resultado.cumpleCriterio ? '✓ CUMPLE' : '✗ NO CUMPLE'}</p>
                </div>
            `).join('')}
        </div>
    `;

    document.getElementById('resultados').classList.remove('hidden');
}

function mostrarError(mensaje) {
    const error = document.getElementById('error');
    const errorMessage = document.getElementById('errorMessage');

    errorMessage.textContent = mensaje;
    error.classList.remove('hidden');
}

function descargarGrafica(tipo) {
    const url = tipo === 'histograma' ? graficaHistogramaUrl : graficaBarrasUrl;
    const nombre = tipo === 'histograma' ? 'histograma_estadistica.png' : 'barras_chebyshev.png';

    if (url) {
        const link = document.createElement('a');
        link.href = url;
        link.download = nombre;
        link.click();
    } else {
        alert('La gráfica no está disponible para descargar');
    }
}