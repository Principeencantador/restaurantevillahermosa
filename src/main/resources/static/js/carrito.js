// carrito.js
const API_BASE = 'http://localhost:3600/pedido';
const token = document.querySelector('input[name="token"]').value;
const correo = document.querySelector('input[name="correo"]').value;

// Pago de carrito existente
document.getElementById('pagar-carrito').addEventListener('click', async () => {
     // Construcción del arreglo de detalles del carrito
     const carrito = [];
     document.querySelectorAll('#lista-carrito tbody tr').forEach(row => {
          carrito.push({
               id_plato: Number(row.cells[0].innerText),
               nombre: row.cells[2].innerText,
               precio: parseFloat(row.cells[3].innerText),
               cantidad: Number(row.cells[4].innerText)
          });
     });

     // Datos para crear el pedido
     const datosPedido = {
          correo: correo,
          id_platos: carrito.map(item => item.id_plato),
          estado: 'ESPERA',
          cantidades: carrito.map(item => item.cantidad)
     };

     if (token) {
          try {
               const response = await fetch(`${API_BASE}/guardar`, {
                    method: 'POST',
                    headers: {
                         'Content-Type': 'application/json',
                         'Authorization': 'Bearer ' + token
                    },
                    body: JSON.stringify(datosPedido)
               });

               const text = await response.text();
               alert(text);

               if (response.ok) {
                    // Recarga para mostrar cambios (o redirigir)
                    window.location.reload();
               }
          } catch (err) {
               console.error('Error al guardar pedido:', err);
               alert('Error al guardar pedido: ' + err.message);
          }
     } else {
          alert('No has iniciado sesión');
          window.location.href = '/login_admin';
     }
});

// Lógica de UI para agregar/eliminar items en el carrito (sin cambios)
const carritoElement = document.querySelector('#carrito');
const listaplato = document.querySelector('#lista-cursos');
const contenedorCarrito = document.querySelector('#lista-carrito tbody');
const vaciarCarritoBtn = document.querySelector('#vaciar-carrito');
let articulosCarrito = [];

cargarEventListeners();
function cargarEventListeners() {
     listaplato.addEventListener('click', agregarCurso);
     carritoElement.addEventListener('click', eliminarCurso);
     vaciarCarritoBtn.addEventListener('click', vaciarCarrito);
}

function agregarCurso(e) {
     e.preventDefault();
     if (e.target.classList.contains('agregar-carrito')) {
          leerDatosCurso(e.target.parentElement.parentElement);
     }
}

function leerDatosCurso(plato) {
     const infoplato = {
          imagen: plato.querySelector('img').src,
          titulo: plato.querySelector('h4').textContent,
          precio: plato.querySelector('.precio span').textContent,
          id: plato.querySelector('a').getAttribute('data-id'),
          cantidad: 1
     };
     if (articulosCarrito.some(p => p.id === infoplato.id)) {
          articulosCarrito = articulosCarrito.map(p => {
               if (p.id === infoplato.id) p.cantidad++;
               return p;
          });
     } else {
          articulosCarrito.push(infoplato);
     }
     carritoHTML();
}

function eliminarCurso(e) {
     e.preventDefault();
     if (e.target.classList.contains('borrar-curso')) {
          articulosCarrito = articulosCarrito.filter(p => p.id !== e.target.getAttribute('data-id'));
          carritoHTML();
     }
}

function carritoHTML() {
     vaciarCarrito();
     articulosCarrito.forEach(plato => {
          const row = document.createElement('tr');
          row.innerHTML = `
             <td>${plato.id}</td>
             <td><img src="${plato.imagen}" width="100"></td>
             <td>${plato.titulo}</td>
             <td>${plato.precio}</td>
             <td>${plato.cantidad}</td>
             <td><a href="#" class="borrar-curso" data-id="${plato.id}">X</a></td>
          `;
          contenedorCarrito.appendChild(row);
     });
}

function vaciarCarrito() {
     while (contenedorCarrito.firstChild) {
          contenedorCarrito.removeChild(contenedorCarrito.firstChild);
     }
}
