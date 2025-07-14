$(document).ready(function () {
    $('#formGuardarPlato').on('submit', function (e) {
        e.preventDefault(); // Prevenir el comportamiento por defecto del formulario

        // Crear un FormData para manejar el archivo y los otros datos
        var formData = new FormData(this);

        $.ajax({
            url: '/plato/guardar',
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
        var idPlato = $('#id_plato').val(); // Obtener el ID del plato

        $.ajax({
            url: '/plato/actualizar',
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
                location.reload(); // Regar la página para mostrar los cambios
            },
            error: function (response) {
                // Mostrar el mensaje de error detallado
                alert(response.responseText);

            }
        });
    });

    $(document).ready(function () {
        $('.eliminar').click(function () {
            var idPlato = $(this).attr('data-id'); // Obtiene el ID del plato del atributo data-id

            if (confirm('¿Estás seguro de que deseas eliminar este plato?')) {
                $.ajax({
                    url: '/plato/eliminar', // Ruta de eliminación en el controlador
                    type: 'DELETE', // Método HTTP DELETE
                    data: { id_plato: idPlato }, // Datos que se envían al servidor
                    headers: {
                        "Authorization": 'Bearer ' + token // Token de autenticación si es necesario
                    },
                    success: function (response) {
                        alert('Plato eliminado exitosamente');
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
