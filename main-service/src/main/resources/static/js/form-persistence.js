document.addEventListener('DOMContentLoaded', function() {
    const forms = document.querySelectorAll('.main-form');

    forms.forEach((form) => {
        const isEditForm = form.action.includes('/edit/');
        const formType = form.action.includes('/people/') ? 'person' : 'group';
        const formKey = `form_${formType}_${isEditForm ? 'edit' : 'create'}`;

        restoreFormValues(form, formKey);

        form.addEventListener('input', function(e) {
            saveFormValues(form, formKey);
        });

        form.addEventListener('submit', async function(e) {
            e.preventDefault();

            try {
                const formData = new FormData(form);
                const response = await fetch(form.action, {
                    method: 'POST',
                    headers: {
                        'Content-Type': 'application/x-www-form-urlencoded',
                        'X-Requested-With': 'XMLHttpRequest'
                    },
                    body: new URLSearchParams(formData),
                    credentials: 'same-origin'
                });

                if (response.redirected) {
                    window.location.href = response.url;
                    return;
                }

                const contentType = response.headers.get('content-type');
                if (contentType && contentType.includes('text/html')) {
                    const html = await response.text();
                    const tempDiv = document.createElement('div');
                    tempDiv.innerHTML = html;

                    const newForms = tempDiv.querySelectorAll('.main-form');

                    const matchingNewForm = Array.from(newForms).find(newForm =>
                        newForm.action === form.action
                    );

                    if (matchingNewForm) {
                        form.parentNode.replaceChild(matchingNewForm, form);

                        attachFormHandlers(matchingNewForm, formKey);
                    }
                } else {
                    sessionStorage.removeItem(formKey);
                    window.location.reload();
                }
            } catch (error) {
                console.error('Ошибка:', error);
                alert('Произошла ошибка при отправке формы');
            }
        });
    });
});

function attachFormHandlers(form, formKey) {
    form.addEventListener('input', function(e) {
        saveFormValues(form, formKey);
    });

    attachValidators();

    form.addEventListener('submit', async function(e) {
        e.preventDefault();
        try {
            const formData = new FormData(form);
            const response = await fetch(form.action, {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/x-www-form-urlencoded',
                    'X-Requested-With': 'XMLHttpRequest'
                },
                body: new URLSearchParams(formData),
                credentials: 'same-origin'
            });

            if (response.redirected) {
                window.location.href = response.url;
                return;
            }

            const contentType = response.headers.get('content-type');
            if (contentType && contentType.includes('text/html')) {
                const html = await response.text();
                const tempDiv = document.createElement('div');
                tempDiv.innerHTML = html;

                const newForms = tempDiv.querySelectorAll('.main-form');
                const matchingNewForm = Array.from(newForms).find(newForm =>
                    newForm.action === form.action
                );

                if (matchingNewForm) {
                    form.parentNode.replaceChild(matchingNewForm, form);
                    attachFormHandlers(matchingNewForm, formKey);
                }
            } else {
                sessionStorage.removeItem(formKey);
                window.location.reload();
            }
        } catch (error) {
            console.error('Ошибка:', error);
            alert('Произошла ошибка при отправке формы');
        }
    });
}

function saveFormValues(form, formKey) {
    const formData = {};

    form.querySelectorAll('input, select').forEach(input => {
        const fieldId = input.name || input.id;
        if (fieldId) {
            if (input.type === 'checkbox') {
                formData[fieldId] = input.checked;
            } else {
                formData[fieldId] = input.value;
            }
        }
    });

    sessionStorage.setItem(formKey, JSON.stringify(formData));
}

function restoreFormValues(form, formKey) {
    const savedData = sessionStorage.getItem(formKey);

    if (savedData) {
        const formData = JSON.parse(savedData);
        const isEditForm = form.action.includes('/edit/');
        
        form.querySelectorAll('input, select').forEach(input => {
            const fieldId = input.name || input.id;
            if (fieldId && formData.hasOwnProperty(fieldId) && 
                (!isEditForm || !input.value)) {
                if (input.type === 'checkbox') {
                    input.checked = formData[fieldId];
                } else {
                    input.value = formData[fieldId];
                }
            }
        });
    }
}