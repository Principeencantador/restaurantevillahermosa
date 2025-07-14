
document.querySelectorAll(".btn-edit").forEach((btn) => {
    btn.addEventListener("click", (e) => {
        e.preventDefault();
        var id = btn.getAttribute("data-id");
        let id_oculto = document.getElementById("id_plato");
        let nombre = document.getElementById("actuNombre");
        var name = btn.getAttribute("data-nombre");
        let precio = document.getElementById("actuprecio");
        var price = btn.getAttribute("data-precio");
        let descripcion = document.getElementById("actuDescripcion");
        var description = btn.getAttribute("data-descripcion");



        precio.value = price;
        descripcion.value = description;
        nombre.value = name;


        id_oculto.value = id;


        // Obtener otros valores del botón y asignarlos a los campos del formulario

        // Para el campo de la imagen, podrías necesitar manejarlo de manera diferente dependiendo de tus necesidades
        console.log(id);
    });
})