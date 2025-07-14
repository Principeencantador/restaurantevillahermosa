function verificar() {
    let password1 = document.getElementById('password-input2').value;
    let password2 = document.getElementById('password-input').value;
    let correo = document.getElementById("vercorreo").value;
    let regex = /^(([^<>()[\]\.,;:\s@\"]+(\.[^<>()[\]\.,;:\s@\"]+)*)|(\".+\"))@(([^<>()[\]\.,;:\s@\"]+\.)+[^<>()[\]\.,;:\s@\"]{2,})$/i
    var esvalido = regex.test(correo);

    if (esvalido == true) {

        if (password1 !== password2) {
            alert("Las contraseñas son diferentes");
            return false;

        }
        return true;

    } else {
        alert("No es un correo")
        return false;
    }

   

}
