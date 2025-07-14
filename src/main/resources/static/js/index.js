

document.addEventListener("scroll", function () {
    // Obtén la posición actual del scroll
    var scrollPosition = window.scrollY;

    // Determina cuándo quieres que aparezcan los elementos del primer conjunto
    var triggerPosition1 = 300;

    // Determina cuándo quieres que aparezcan los elementos del segundo conjunto
    var triggerPosition2 = 600;


    if (scrollPosition > triggerPosition1) {

        document.getElementById("efecto2").classList.add('visible2');
    }
    if (scrollPosition > triggerPosition2) {

        setTimeout(function () {
            document.getElementById("efecto1").classList.add('visible2');
        }, 500);
    }
});
$('#logout-btn').on('click', function () {
    $.ajax({
        type: "POST",
        url: "http://localhost:3600/cerrar",
        headers: {
            'Authorization': 'Bearer ' + token
        },
        success: function () {
            // Redirigir a la página de inicio de sesión o a otra página después del logout
            window.location.href = "/login_admin";
        },
        error: function (xhr, status, error) {
            console.log("Error: " + error);
        }
    });
});