$(document).ready(function () {
    $('#formGuardarMesa').on('submit', function (e) {
        e.preventDefault(); // Prevenir el comportamiento por defecto del formulario

        // Crear un FormData para manejar el archivo y los otros datos
        var formData = new FormData(this);

        $.ajax({
            url: '/mesa/crear',
            type: 'POST',
            data: formData,
            headers: {
                "Authorization": 'Bearer ' + token
            },
            contentType: false, // No establecer ningún tipo de contenido
            processData: false, // No procesar los datos (especialmente necesario para archivos)
            success: function (response) {
                alert(response);
                // Aquí podrías recargar la lista de platos o cerrar el modal
                $('#formagre').modal('hide');
                location.reload(); // O recargar los platos en la tabla si no deseas recargar toda la página
            },
            error: function (response) {
                // Mostrar el mensaje de error detallado
                alert(response.responseText);

            }
        });
    });

    $('#updateForm').on('submit', function (e) {
        e.preventDefault(); // Prevenir el comportamiento por defecto del formulario

        var formData = new FormData(this);
        var idMesa = $('#idMesa').val(); // Obtener el ID del plato

        $.ajax({
            url: '/mesa/actualizar',
            type: 'POST',
            data: formData,
            headers: {
                "Authorization": 'Bearer ' + token
            },
            contentType: false, // No establecer ningún tipo de contenido
            processData: false, // No procesar los datos
            success: function (response) {
                alert('Plato actualizado exitosamente');
                $('#formactu').modal('hide');
                location.reload(); // Recargar la página para mostrar los cambios
            },
            error: function (response) {
                // Mostrar el mensaje de error detallado
                alert(response.responseText);

            }
        });
    });

    $(document).ready(function () {
        $('.eliminar').click(function () {
            var idmesa = $(this).attr('data-id'); // Obtiene el ID del plato del atributo data-id

            if (confirm('¿Estás seguro de que deseas eliminar esta mesa?')) {
                $.ajax({
                    url: '/mesa/eliminar', // Ruta de eliminación en el controlador
                    type: 'DELETE', // Método HTTP DELETE
                    data: { idmesa: idmesa }, // Datos que se envían al servidor
                    headers: {
                        "Authorization": 'Bearer ' + token // Token de autenticación si es necesario
                    },
                    success: function (response) {
                        alert('Mesa eliminada exitosamente');
                        location.reload(); // Recargar la página para actualizar la lista de platos
                    },
                    error: function (response) {
                        // Mostrar el mensaje de error detallado
                        alert(response.responseText);
        
                    },
                    
                });
            }
        });
    });


});
