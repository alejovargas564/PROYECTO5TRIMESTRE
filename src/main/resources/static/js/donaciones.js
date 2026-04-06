function iniciarPago() {
    const montoInput = document.getElementById('monto').value;
    const email = document.getElementById('email').value;

    if (!montoInput || !email) {
        alert("Por favor, ingresa el monto y tu correo.");
        return;
    }

    // 1. Llamamos a tu microservicio de Python
    fetch('http://localhost:5000/api/payments/create-checkout', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({
            amount: parseInt(montoInput),
            email: email,
            reference: 'SAPIB-' + Date.now()
        })
    })
    .then(response => {
        if (!response.ok) throw new Error("Error en el servidor de Python");
        return response.json();
    })
    .then(data => {
        console.log("DATOS RECIBIDOS:", data);

        if (!data.public_key) {
            alert("Error: Llave pública no recibida.");
            return;
        }

        // 2. Configuración del Widget con limpieza de datos
        var checkout = new WidgetCheckout({
            currency: 'COP',
            amountInCents: data.amount_in_cents,
            publicKey: data.public_key.trim(), // Quitamos espacios accidentales
            reference: data.reference.toString(),
            customerEmail: data.customer_email
        });

        // 3. Abrir y manejar el resultado
        checkout.open(function ( result ) {
            var transaction = result.transaction;
            console.log("Estado de la transacción:", transaction.status);
            
            if(transaction.status === 'APPROVED'){
                alert('¡Donación Exitosa! Muchas gracias por tu apoyo.');
                window.location.href = '/voluntario/fundaciones?exito';
            } else {
                alert('La transacción no fue aprobada. Estado: ' + transaction.status);
            }
        });
    })
    .catch(error => {
        console.error('Error completo:', error);
        alert("No se pudo conectar con el servicio de pagos. Verifica que Python esté corriendo.");
    });
}