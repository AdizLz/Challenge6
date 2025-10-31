// Manejo del formulario de ofertas
document.addEventListener('DOMContentLoaded', function() {
    const offerBtn = document.querySelector('.offer-btn');
    const offerForm = document.getElementById('offer-form');

    if (offerBtn && offerForm) {
        // Mostrar/ocultar formulario
        offerBtn.addEventListener('click', function() {
            if (offerForm.style.display === 'none' || offerForm.style.display === '') {
                offerForm.style.display = 'block';
                offerBtn.textContent = '✕ Cerrar formulario';
            } else {
                offerForm.style.display = 'none';
                offerBtn.innerHTML = '<i class="bi bi-hand-thumbs-up"></i> Hacer una Oferta';
            }
        });

        // Enviar oferta con AJAX
        offerForm.addEventListener('submit', function(e) {
            e.preventDefault();

            const name = document.getElementById('name-input').value.trim();
            const email = document.getElementById('email-input').value.trim();
            const amountRaw = document.getElementById('amount-input').value;
            const amount = parseFloat(amountRaw);
            const itemId = document.getElementById('item-id').value;

            // Validación robusta: aceptar montos mayores a 0
            if (!name || !email || isNaN(amount) || amount <= 0) {
                alert('Por favor completa todos los campos correctamente y asegúrate de que el monto sea mayor que 0.');
                return;
            }

            const payload = {
                name: name,
                email: email,
                id: itemId,
                amount: amount
            };

            const submitBtn = offerForm.querySelector('button[type="submit"]');
            if (submitBtn) {
                submitBtn.disabled = true;
                submitBtn.textContent = 'Enviando...';
            }

            fetch('/api/offers', {
                method: 'POST',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify(payload)
            })
            .then(response => {
                // Intentar parsear cuerpo JSON siempre para obtener mensaje de error si lo hay
                return response.json().then(body => {
                    if (!response.ok) {
                        const msg = (body && body.message) ? body.message : ('Error HTTP ' + response.status);
                        throw new Error(msg);
                    }
                    return body;
                }).catch(err => {
                    // Si no se pudo parsear JSON, lanzar con status
                    if (!response.ok) throw new Error('Error HTTP ' + response.status);
                    // si parse ok pero body vacío, devolver objeto vacío
                    return {};
                });
            })
            .then(data => {
                alert('¡Tu oferta ha sido enviada exitosamente!');
                offerForm.reset();
                offerForm.style.display = 'none';
                window.location.href = '/offers';
            })
            .catch(error => {
                alert('Error al enviar la oferta: ' + error.message);
            })
            .finally(() => {
                if (submitBtn) {
                    submitBtn.disabled = false;
                    submitBtn.innerHTML = '<i class="bi bi-send-fill"></i> Enviar Oferta';
                }
            });
        });
    }
});