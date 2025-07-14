
document.querySelectorAll(".btn-edit").forEach((btn) => {
    btn.addEventListener("click", (e) => {
        e.preventDefault();
        var id = btn.getAttribute("data-id");
        let id_oculto = document.getElementById("idmesa");
        let capacidad= document.getElementById("actucapacidad");
        var capacity = btn.getAttribute("data-capacidad");
        let numero = document.getElementById("actunumero");
        var number = btn.getAttribute("data-nro");
      



        numero.value = number;
        capacidad.value = capacity;


        id_oculto.value = id;


        // Obtener otros valores del botón y asignarlos a los campos del formulario

        // Para el campo de la imagen, podrías necesitar manejarlo de manera diferente dependiendo de tus necesidades
        console.log(id);
    });
})