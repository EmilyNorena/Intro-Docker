document.getElementById("getForm").addEventListener("submit", function(e) {
  e.preventDefault();
  const nombre = this.getInput.value;
  
  fetch(`/api/hello?name=${encodeURIComponent(nombre)}`)
    .then(response => {
      if (!response.ok) {
        throw new Error('Error en la respuesta del servidor');
      }
      return response.json();
    })
    .then(data => {
      alert(`Respuesta del servidor (GET): ${data.message}`);
      // Opcional: Mostrar en la página en lugar de alert
      // document.getElementById("getResponse").innerText = data.message;
    })
    .catch(error => {
      console.error('Error:', error);
      alert('Error al enviar la solicitud GET');
    });
});

document.getElementById("postForm").addEventListener("submit", function(e) {
  e.preventDefault();
  const nombre = this.postInput.value;
  
  fetch('/api/hellopost', {
    method: 'POST',
    headers: {
      'Content-Type': 'application/x-www-form-urlencoded',
    },
    body: `name=${encodeURIComponent(nombre)}`
  })
    .then(response => {
      if (!response.ok) {
        throw new Error('Error en la respuesta del servidor');
      }
      return response.json();
    })
    .then(data => {
      alert(`Respuesta del servidor (POST): ${data.message}`);
      // Opcional: Mostrar en la página en lugar de alert
      // document.getElementById("postResponse").innerText = data.message;
    })
    .catch(error => {
      console.error('Error:', error);
      alert('Error al enviar la solicitud POST');
    });
});