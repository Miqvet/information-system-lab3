document.addEventListener('DOMContentLoaded', function() {

    document.getElementById('lessForm').addEventListener('submit', function(e) {
        e.preventDefault();
        const formData = new FormData(this);

        fetch('/admin/admin-page/less-than', {
            method: 'POST',
            body: formData
        })
            .then(response => response.text())
            .then(html => {
                const parser = new DOMParser();
                const doc = parser.parseFromString(html, 'text/html');
                const newForm = doc.getElementById('lessForm');
                document.getElementById('lessForm').innerHTML = newForm.innerHTML;
            });
    });


    document.getElementById('greaterForm').addEventListener('submit', function(e) {
        e.preventDefault();
        const formData = new FormData(this);

        fetch('/admin/admin-page/greater-than', {
            method: 'POST',
            body: formData
        })
            .then(response => response.text())
            .then(html => {
                const parser = new DOMParser();
                const doc = parser.parseFromString(html, 'text/html');
                const newForm = doc.getElementById('greaterForm');
                document.getElementById('greaterForm').innerHTML = newForm.innerHTML;
            });
    });

    document.getElementById('adminsForm').addEventListener('submit', function(e) {
        e.preventDefault();

        fetch('/admin/admin-page/unique-admins')
            .then(response => response.text())
            .then(html => {
                const parser = new DOMParser();
                const doc = parser.parseFromString(html, 'text/html');
                const newForm = doc.getElementById('adminsForm');
                document.getElementById('adminsForm').innerHTML = newForm.innerHTML;
            });
    });


    document.getElementById('expelForm').addEventListener('submit', function(e) {
        e.preventDefault();
        const formData = new FormData(this);

        fetch('/admin/admin-page/expel-group', {
            method: 'POST',
            body: formData
        })
            .then(response => {
                if (response.status === 404) {
                    throw new Error('Ресурс не найден');
                }
                return response.text();
            })
            .then(html => {
                const parser = new DOMParser();
                const doc = parser.parseFromString(html, 'text/html');
                const newForm = doc.getElementById('expelForm');
                document.getElementById('expelForm').innerHTML = newForm.innerHTML;
            })
            .catch(error => {
                Toastify({
                    text: error.message || "Произошла ошибка при обработке запроса",
                    duration: 3000,
                    gravity: "top",
                    position: "right",
                    style: {
                        background: "#f44336"
                    }
                }).showToast();
            });
    });


    document.getElementById('transferForm').addEventListener('submit', function(e) {
        e.preventDefault();
        const formData = new FormData(this);

        fetch('/admin/admin-page/transfer-students', {
            method: 'POST',
            body: formData
        })
            .then(response => {
                if (response.status === 404) {
                    throw new Error('Ресурс не найден');
                }
                return response.text();
            })
            .then(html => {
                const parser = new DOMParser();
                const doc = parser.parseFromString(html, 'text/html');
                const newForm = doc.getElementById('transferForm');
                document.getElementById('transferForm').innerHTML = newForm.innerHTML;
            })
            .catch(error => {
                Toastify({
                    text: error.message || "Произошла ошибка при обработке запроса",
                    duration: 3000,
                    gravity: "top",
                    position: "right",
                    style: {
                        background: "#f44336"
                    }
                }).showToast();
            });
    });
});

function showError(formId, error) {
    const errorElement = document.createElement('div');
    errorElement.className = 'error-message';
    errorElement.textContent = error;
    document.getElementById(formId).appendChild(errorElement);
    setTimeout(() => errorElement.remove(), 3000);
}

function showSuccess(formId, message) {
    const successElement = document.createElement('div');
    successElement.className = 'success-message';
    successElement.textContent = message;
    document.getElementById(formId).appendChild(successElement);
    setTimeout(() => successElement.remove(), 3000);
}