document.addEventListener('DOMContentLoaded', function() {
    attachValidators();
});

function attachValidators() {
    const studyGroupValidationRules = {
        'name': {
            pattern: /^[a-zA-Zа-яА-Я0-9]+(?:\s[a-zA-Zа-яА-Я0-9]+)*$/,
            message: 'Название группы должно содержать осмысленные символы'
        },
        'coordinates.x': {
            min: -407,
            max: 500,
            required: true,
            message: 'X должна быть между -407 и 500'
        },
        'coordinates.y': {
            required: true,
            message: 'Y не может быть пустым'
        },
        'studentsCount': {
            min: 0,
            required: true,
            message: 'Число студентов должно быть больше 0'
        },
        'expelledStudents': {
            min: 0,
            required: true,
            message: 'Число исключённых студентов должно быть больше 0'
        },
        'transferredStudents': {
            min: 0,
            required: true,
            message: 'Число переведённых студентов должно быть больше 0'
        },
        'formOfEducation': {
            required: true,
            message: 'Форма обучения не может быть пустой'
        },
        'shouldBeExpelled': {
            min: 0,
            required: true,
            message: 'Количество студентов должно быть больше 0'
        },
        'averageMark': {
            min: 1,
            required: true,
            message: 'Средняя оценка должна быть больше 0'
        },
        'semesterEnum': {
            required: true,
            message: 'Семестр должен быть выбран'
        },
        'groupAdmin': {
            min: 1,
            required: true,
            message: 'Администратор должен быть указан и должен быть больше 0'
        }
    };

    const personValidationRules = {
        'passportID': {
            pattern: /^[1234567890a-zA-Zа-яА-Я]+$/,
            minLength: 1,
            maxLength: 42,
            message: 'Паспорт должен содержать от 1 до 42 символов и состоять из букв и цифр'
        },
        'name': {
            pattern: /^[a-zA-Zа-яА-Я]+(?:\s[a-zA-Zа-яА-Я]+)*$/,
            required: true,
            message: 'Имя должно содержать только буквы и пробелы'
        },
        'hairColor': {
            required: true,
            message: 'Цвет волос должен быть выбран'
        },
        'nationality': {
            required: true,
            message: 'Национальность должна быть выбрана'
        },
        'location.x': {
            required: true,
            message: 'X координата обязательна'
        },
        'location.y': {
            required: true,
            message: 'Y координата обязательна'
        },
        'location.name': {
            required: true,
            message: 'Название локации обязательно'
        }
    };

    const forms = document.querySelectorAll('.main-form');
    forms.forEach(form => {
        const isPersonForm = form.action.includes('/people/');
        const rules = isPersonForm ? personValidationRules : studyGroupValidationRules;

        Object.keys(rules).forEach(fieldName => {
            const field = form.querySelector(`[name="${fieldName}"]`);
            if (field) {
                let touched = false;

                field.addEventListener('blur', function() {
                    touched = true;
                    validateField(field, rules[fieldName]);
                });

                field.addEventListener('input', function() {
                    if (touched) {
                        validateField(field, rules[fieldName]);
                    }
                });
            }
        });

        form.addEventListener('submit', function(e) {
            let isValid = true;
            Object.keys(rules).forEach(fieldName => {
                const field = form.querySelector(`[name="${fieldName}"]`);
                if (field) {
                    if (!validateField(field, rules[fieldName])) {
                        isValid = false;
                    }
                }
            });

            if (!isValid) {
                e.preventDefault();
            }
        });
    });
}

function validateField(field, rules) {
    const allErrors = field.parentElement.querySelectorAll('.form-errors');
    allErrors.forEach(error => error.remove());
    
    let isValid = true;
    let errorMessage = '';

    if (rules.required && !field.value.trim()) {
        isValid = false;
        errorMessage = rules.message;
    }

    if (rules.pattern && field.value.trim() && !rules.pattern.test(field.value)) {
        isValid = false;
        errorMessage = rules.message;
    }

    if (rules.minLength && field.value.length < rules.minLength) {
        isValid = false;
        errorMessage = rules.message;
    }

    if (rules.maxLength && field.value.length > rules.maxLength) {
        isValid = false;
        errorMessage = rules.message;
    }

    if (rules.min !== undefined && field.value !== '' && Number(field.value) < rules.min) {
        isValid = false;
        errorMessage = rules.message;
    }

    if (rules.max !== undefined && field.value !== '' && Number(field.value) > rules.max) {
        isValid = false;
        errorMessage = rules.message;
    }

    if (!isValid) {
        const errorDiv = document.createElement('div');
        errorDiv.className = 'validation-error form-errors';
        errorDiv.textContent = errorMessage;
        field.parentElement.appendChild(errorDiv);
    }

    return isValid;
} 