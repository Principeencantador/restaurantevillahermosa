document.querySelectorAll(".btn-edit").forEach((btn) => {
    btn.addEventListener("click", (e) => {
        e.preventDefault();
        var id = btn.getAttribute("data-id");
        var name = btn.getAttribute("data-nombre");
        let id_oculto = document.getElementById("id_empleado");
        let nombre = document.getElementById("actuNombre");


        var correo = document.getElementById("actucorreo");
        let email = btn.getAttribute("data-correo");


        nombre.value = name;
        id_oculto.value = id;

        correo.value = email;
        contrasena.value = password;
        //console.log(nombre.value);
        //console.log(id_oculto);
    });
})