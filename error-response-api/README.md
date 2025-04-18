# 🛡 Модуль автоматического построения сообщений об ошибках на основе объектов ConstraintViolation

Этот модуль представляет собой расширяемую библиотеку для автоматического построения структурированных сообщений об
ошибках на основе ограничений валидации `jakarta.validation.ConstraintViolation.`

**🎯 Основная цель — сделать ошибки валидные, читаемые и машиночитаемые, а также предоставить возможность настраивать
формат этих ошибок без дублирования логики в разных частях кода.**

## ✨ Функциональные возможности

### 📚 1. Автоматическое формирование сообщений об ошибках

Модуль автоматически формирует сообщения об ошибках для всех основных аннотаций, представленных
в `Jakarta Validation API`, таких, как `@NotNull`, `@NotBlank`, `@Size`, `@Max`, `@Min` и так далее.

<details>
    <summary>☰ Полный список поддерживаемых аннотаций</summary>    

#### В таблице представлен полный список аннотаций, которые поддерживаются модулем на текущий момент времени

| Аннотация         | Поддержка |
|-------------------|-----------|
| `@NotNull`        | ✅         |
| `@NotBlank`       | ✅         |    
| `@Size`           | ✅         |    
| `@Email`          | ✅         |    
| `@Min`            | ✅         |    
| `@Max`            | ✅         |    
| `@Positive`       | ✅         |    
| `@PositiveOrZero` | ✅         |    
| `@Negative`       | ✅         |    
| `@NegativeOrZero` | ✅         |    
| `@Pattern`        | ✅         |    

</details>

#### 🧩 Как это работает?

Автоматическое формирование сообщений об ошибках происходит за счет так называемых `провайдеров`

ℹ️ Провайдер - это специальный класс, который реализует интерфейс `ConstraintViolationErrorResponseProvider` и
возвращает сформированное сообщение об ошибке в определенном формате

#### </> Интерфейс `ConstraintViolationErrorResponseProvider` имеет следующий вид

```java
public interface ConstraintAnnotationErrorResponseProvider {
    boolean supports(Class<? extends Annotation> annotationClass);

    ConstraintViolationProblemDetails buildErrorResponseDetails(
            ConstraintViolation<?> constraintViolation,
            PropertyPathExtractor propertyPathExtractor
    );

    default Annotation extractIfSupportsOrThrow(ConstraintViolation<?> constraintViolation) throws UnsupportedConstraintAnnotationType {
        Annotation constraintAnnotation = constraintViolation.getConstraintDescriptor().getAnnotation();

        if (!this.supports(constraintAnnotation.annotationType()))
            throw new UnsupportedConstraintAnnotationType(
                    "Annotation with type"
                            + constraintAnnotation.annotationType().getName() +
                            " is not supported, because supports() returned false"
            );

        return constraintAnnotation;
    }

    @SuppressWarnings("unchecked")
    default <T extends Annotation> T extractAsIfSupportsOrThrow(ConstraintViolation<?> constraintViolation, Class<T> annotationClass) {
        return (T) this.extractIfSupportsOrThrow(constraintViolation);
    }
}
```

#### Назначение методов интерфейса `ConstraintViolationErrorResponseProvider`

```java
boolean supports(Class<? extends Annotation> annotationClass);
```

Метод определяет, поддерживает ли класс определенный тип аннотации. Это должно проверяться, если из аннотации нужно
извлечь какие-то специфичные только для нее поля, для чего необходимо привести аннотацию к соответствующему типу

```java
ConstraintViolationProblemDetails buildErrorResponseDetails(ConstraintViolation<?> constraintViolation,
                                                            PropertyPathExtractor propertyPathExtractor);
```

Метод отвечает за непосредственно формирование сообщения об ошибке на основе аннотации. Аннотация должна быть извлечена
из объекта `ConstraintViolation<?>` и, если необходимо, приведена к определенному типу.

<details>
    <summary>❓ Что такое PropertyPathExtractor</summary>

Интерфейс `PropertyPathExtractor` отвечает за формирование пути к полю, в значении которого была найдена ошибка

#### </> Интерфейс `PropertyPathExtractor` имеет следующий вид

```java
public interface PropertyPathExtractor {
    String extractFrom(ConstraintViolation<?> constraintViolation);
}
```

Стандартная реализация этого интерфейса - `DottedPropertyPathExtractor`, который извлекает путь поля в
формате `path.to.some.field`, т.е. разделенных точками

```java
public class DottedPropertyPathExtractor implements PropertyPathExtractor {
    @Override
    public String extractFrom(ConstraintViolation<?> constraintViolation) {
        Path pathObject = constraintViolation.getPropertyPath();

        StringBuilder propertyPath = new StringBuilder();

        for (Path.Node node : pathObject) {
            if (node.getKind().equals(ElementKind.PROPERTY)) {
                if (propertyPath.isEmpty())
                    propertyPath.append(node);
                else
                    propertyPath.append(".").append(node);
            }
        }

        return propertyPath.toString();
    }
}
```

</details>

---

### ⚙️ 2. Регистрация провайдеров и получение провайдера для конкретного типа аннотации

Модуль использует паттерн `Registry` для возможности регистрации кастомных провайдеров и получения конкретного
класса-провайдера для определенной аннотации. В стандартной реализации для всех аннотаций
пакета `Jakarta Validation API` созданы и зарегистрированы провайдеры, но благодаря паттерну их можно легко
перерегистрировать, заменив на свои собственные, без необходимости менять код

#### 🧩 Как это работает?

В проекте существует специальный интерфейс `ConstraintAnnotationErrorResponseProvidersRegistry`, который содержит методы
для регистрации провайдеров и получения провайдеров для определенной аннотации

```java
public interface ConstraintAnnotationErrorResponseProvidersRegistry {
    void registerProvider(Class<? extends Annotation> constraintAnnotationClass, ConstraintAnnotationErrorResponseProvider provider);

    ConstraintAnnotationErrorResponseProvider getResponseProviderFor(ConstraintViolation<?> constraintViolation);

    default UnsupportedAnnotationPassedAction onUnsupportedAnnotationType() {
        return UnsupportedAnnotationPassedAction.CALL_DEFAULT_PROVIDER;
    }

    default ConstraintAnnotationErrorResponseProvider getDefaultProvider() {
        return DefaultErrorResponseProvider.withUnknownConstraintViolationType();
    }

    enum UnsupportedAnnotationPassedAction {
        THROW_EXCEPTION,
        CALL_DEFAULT_PROVIDER
    }
}
```

Стандартная реализация уже содержит регистрацию всех существующих провайдеров для стандартных аннотаций

```java
Map.ofEntries(
        Map.entry(NotNull.class, new DefaultErrorResponseProvider(ConstraintViolationType.NOT_NULL)),
        Map.entry(Size .class,    new SizeConstraintErrorResponseProvider()),
        ...
)
```

Если провайдер для переданной аннотации не был найден, можно выбрать 1 из 2 стратегий действий:

| Стратегия               | Описание                                                                                                                   |
|-------------------------|----------------------------------------------------------------------------------------------------------------------------|
| `THROW_EXCEPTION`       | Если провайдер для этой аннотации не найден, выбросить исключение                                                          |
| `CALL_DEFAULT_PROVIDER` | Если провайдер для этой аннотации не найден, вызвать провайдер по-умолчанию, который формирует простое сообщение об ошибке |  

---

### 🔧 3. Создание и регистрация кастомных аннотаций

Модуль предоставляет возможность для добавления своих собственных, кастомных аннотаций. Чтобы модуль начал работать с вашей кастомной аннотацией, достаточно выполнить следующие шаги
- Создать саму аннотацию
- Создать провайдер, формирующий для этой аннотации сообщение об ошибке
- Зарегистрировать новый провайдер в `реестре провайдеров`

🏆 После этого модуль сможет понимать и работать с вашей кастомной аннотацией

#### Пример: создание кастомной аннотации для валидации пароля пользователя

📝 Задача: создать аннотацию которая бы проверяла введенный пользователем пароль на соответствие определенным требованиям, например, по длине, количеству символов и т.д.

1. Определим саму аннотацию

```java
@Target({ElementType.FIELD, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = PasswordValidator.class)
public @interface Password {
    String message() default "";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}
```

2. Определим валидатор, который будет проверять значение пароля

```java
public class PasswordValidator implements ConstraintValidator<Password, String> {
    @Override
    public boolean isValid(String password, ConstraintValidatorContext constraintValidatorContext) {
        if (password == null) return true; //Должно быть обработано другой аннотацией
        
        //Определенная логика проверки пароля
        
        if(!passwordValid) {
            constraintValidatorContext.disableDefaultConstraintViolation();
            constraintValidatorContext
                    .buildConstraintViolationWithTemplate(errorMessage.toString())
                    .addConstraintViolation();

            return false;
        }

        return true;
    }
}
```

3. Создадим кастомное сообщение об ошибке, которое будет в удобной машиночитаемой форме сообщать, какие есть требования к паролю

```java
public class PasswordConstraintViolationDetails extends ConstraintViolationDetails {
    public static final String CONSTRAINT_VIOLATION_TYPE = "PASSWORD";

    @JsonProperty(value = "min_length")
    private final int minLength;

    @JsonProperty(value = "min_letters_count")
    private final int minLettersCount;

    @JsonProperty(value = "min_upper_letters_count")
    private final int minUpperLettersCount;

    public PasswordConstraintViolationDetails(PasswordPolicyProperties passwordPolicyProperties) {
        super(CONSTRAINT_VIOLATION_TYPE);
        this.minLength = passwordPolicyProperties.getMinLength();
        this.minLettersCount = passwordPolicyProperties.getMinLettersCount();
        this.minUpperLettersCount = passwordPolicyProperties.getMinUpperLettersCount();
    }
}
```

4. Создадим кастомный провайдер, который будет строить сообщения об ошибке для нашей аннотации `@Password`

```java
@RequiredArgsConstructor
public class PasswordConstraintErrorResponseProvider implements ConstraintAnnotationErrorResponseProvider {
    private final PasswordPolicyProperties passwordPolicyProperties;

    @Override
    public boolean supports(Class<? extends Annotation> annotationClass) {
        return Password.class.isAssignableFrom(annotationClass);
    }

    @Override
    public ConstraintViolationProblemDetails buildErrorResponseDetails(ConstraintViolation<?> constraintViolation,
                                                                       PropertyPathExtractor propertyPathExtractor) {
        return new ConstraintViolationProblemDetails(
                propertyPathExtractor.extractFrom(constraintViolation),
                constraintViolation.getMessage(),
                new PasswordConstraintViolationDetails(passwordPolicyProperties)
        );
    }
}
```

5. Зарегистрируем наш провайдер в реестре провайдеров

```java
registry.registerProvider(Password.class, new PasswordConstraintErrorResponseProvider(passwordPolicyProperties));
```

#### 🎯 После этих манипуляций модуль начнет понимать новую аннотацию и работать с ней так же, как и со стандартными. Таким образом, ничего больше нигде добавлять не нужно, все автоматически будет сделано за нас

---

## 🔗 Использование в Spring

Для того чтобы использовать этот модуль в Spring, необходимо создать бины классов `PropertyPathExtractor`, `ConstraintAnnotationErrorResponseProvidersRegistry` и `AbstractConstraintViolationsErrorResponseBuilder`

❓ Для чего нужны эти классы

| Класс                                                | Описание                                                                                                                                                                                         |
|------------------------------------------------------|--------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| `PropertyPathExtractor`                              | Используется для извлечения пути свойства, в значении которого обнаружена ошибка. Путь позволяет четко указать, в каком поле произошла ошибка. Пример: `path.to.some.property`                   |
| `ConstraintAnnotationErrorResponseProvidersRegistry` | Реестр провайдеров, которые формируют сообщения об ошибках. Здесь регистрируются стандартные и кастомные провайдеры, а также присутствует метод, для получения провайдера для конкретной аннотации |
| `AbstractConstraintViolationsErrorResponseBuilder`   | Класс, который непосредственно занимается построением сообщений об ошибках                                                                                                                       |  

#### </> Полный код регистрации бинов в Spring приложении может выглядеть следующим образом

```java
@Configuration
@RequiredArgsConstructor
public class ResponseApiBeans {
    private final PasswordPolicyProperties passwordPolicyProperties;

    @Bean
    public PropertyPathExtractor propertyPathExtractor() {
        return new DottedPropertyPathExtractor();
    }

    @Bean
    public ConstraintAnnotationErrorResponseProvidersRegistry errorResponseProvidersRegistry() {
        ConstraintAnnotationErrorResponseProvidersRegistry registry = new DefaultConstraintAnnotationErrorResponseProvidersRegistry();

        registry.registerProvider(Password.class, new PasswordConstraintErrorResponseProvider(passwordPolicyProperties));

        return registry;
    }

    @Bean
    public AbstractConstraintViolationsErrorResponseBuilder errorResponseBuilder() {
        return new DefaultConstraintViolationsErrorResponseBuilder(this.errorResponseProvidersRegistry(), this.propertyPathExtractor());
    }

}
```

После того как соответствующие бины зарегистрированы, можно создать `@ExceptionHandler`, в котором наконец их можно будет использовать

```java
@RestControllerAdvice
@RequiredArgsConstructor
public class ValidationExceptionsHandler {
    private final MessageSource messageSource;
    private final AbstractConstraintViolationsErrorResponseBuilder errorResponseBuilder;

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<RequestBodyValidationProblem> handleConstraintViolationException(ConstraintViolationException e) {
        return ResponseEntity
                .unprocessableEntity()
                .header(Headers.X_JWT_API_ERROR_KIND, ProblemKind.VALIDATION_ERROR.getKind())
                .body(
                        new RequestBodyValidationProblem(
                                ApiErrorCode.INVALID_INPUT_CONSTRAINT_VIOLATIONS_FOUND.getCode(),
                                messageSource.getMessage(
                                        "messages.validation.errors-found",
                                        e.getConstraintViolations().size()
                                ),
                                errorResponseBuilder.buildErrorResponseDetailsFromConstraintViolations(e.getConstraintViolations())
                        )
                );
    }
}
```

---

## 👉 Примеры сообщений об ошибках, формируемых модулем

В этом разделе представлены примеры запросов, которые не проходят валидацию и сообщения, автоматически формируемые модулем 

### 💡 Пример 1. Валидация запроса на вход

Допустим, существует следующий DTO-класс:

```java
public record LoginRequest(
        @NotBlank(message = "{messages.user.validation.fields.email.blank}")
        @Email(message = "{messages.user.validation.fields.email.incorrect-pattern}")
        String email,


        @NotBlank(message = "{messages.user.validation.fields.password.blank}")
        String password
) {}
```

#### Запрос №1. Пустое тело / Пустые значения полей (Нарушение`@NotBlank`)

```http request
POST ./my-awesome-api/v1/login

{

}
```

Ответ от сервера

```json
{
    "type": "invalid-input.constraint-violations-found",
    "details": [
        {
            "message": "Электронная почта должна быть указана",
            "field": "email",
            "constraints": {
                "constraint_violation_type": "not_blank"
            }
        },
        {
            "message": "Пароль должен быть указан",
            "field": "password",
            "constraints": {
                "constraint_violation_type": "not_blank"
            }
        }
    ],
    "localized_message": "Обнаружено ошибок валидации в запросе: 2"
}
```

#### Запрос №2. Неверный формат электронной почты (Нарушение `@Email`)

```http request
POST ./my-awesome-api/v1/login

{
    "email": "dfgdfgdfg",
    "password": "123"
}
```

Ответ от сервера

```json
{
    "type": "invalid-input.constraint-violations-found",
    "details": [
        {
            "message": "Электронная почта имеет неверный формат",
            "field": "email",
            "constraints": {
                "pattern": ".*",
                "constraint_violation_type": "pattern_mismatch"
            }
        }
    ],
    "localized_message": "Обнаружено ошибок валидации в запросе: 1"
}
```

---

### 💡 Пример 2. Валидация запроса на пагинацию

Допустим, существует следующий DTO-класс, представляющий собой запрос на пагинацию сущностей

```java
public record EntityPaginationRequest(
        
        @NotNull(message = "{messages.entity-pagination-request.validation.page.null}")
        @Min(value = 0, message = "{messages.entity-pagination-request.validation.page.negative}")
        Integer page,
        
        @NotNull(message = "{messages.entity-pagination-request.validation.size.null}")
        @Min(value = 0, message = "{messages.entity-pagination-request.validation.size.negative}")
        @Max(value = 100, message = "{messages.entity-pagination-request.validation.size.too-big}")
        Integer size,
        
        String sortBy,
        
        Sort.Direction direction

) {
        public Pageable buildPageableFromRequest() {
                if (this.sortBy == null || this.direction == null)
                        return PageRequest.of(this.page, this.size);

                return PageRequest.of(this.page, this.size, this.direction, sortBy);
        }
}
```

#### Запрос №1. Пустое тело / Пустые значения полей (Нарушение`@NotBlank`)

```http request
GET ./my-awesome-api/v1/users
```

Ответ от сервера:

```json
{
    "type": "invalid-input.constraint-violations-found",
    "details": [
        {
            "message": "Укажите количество записей, которое будет выводиться на странице",
            "field": "size",
            "constraints": {
                "constraint_violation_type": "not_null"
            }
        },
        {
            "message": "Укажите номер страницы",
            "field": "page",
            "constraints": {
                "constraint_violation_type": "not_null"
            }
        }
    ],
    "localized_message": "Обнаружено ошибок валидации в запросе: 2"
}
```

#### Запрос №2. Слишком маленькие значения полей (Нарушение`@Min`)

```http request
GET ./my-awesome-api/v1/users?page=-1&size=-1
```

Ответ от сервера:

```json
{
    "type": "invalid-input.constraint-violations-found",
    "details": [
        {
            "message": "Значения номера страницы не может быть меньше 0",
            "field": "page",
            "constraints": {
                "min": 0,
                "max": null,
                "constraint_violation_type": "min_max"
            }
        },
        {
            "message": "Количество получаемых записей не может быть меньше 0",
            "field": "size",
            "constraints": {
                "min": 0,
                "max": null,
                "constraint_violation_type": "min_max"
            }
        }
    ],
    "localized_message": "Обнаружено ошибок валидации в запросе: 2"
}
```

#### Запрос №3. Слишком большое значение (Нарушение`@Max`)

```http request
GET ./my-awesome-api/v1/users?page=0&size=101
```

Ответ от сервера:

```json
{
    "type": "invalid-input.constraint-violations-found",
    "details": [
        {
            "message": "Количество получаемых записей не может быть больше 100",
            "field": "size",
            "constraints": {
                "min": null,
                "max": 100,
                "constraint_violation_type": "min_max"
            }
        }
    ],
    "localized_message": "Обнаружено ошибок валидации в запросе: 1"
}
```

---

### 💡 Пример 3. Валидация запроса на создание пользователя

Допустим, существует следующий DTO-класс, представляющий собой запрос на создание пользователя. Класс содержит кастомную аннотацию `@Password`, созданную и зарегистрированную в предыдущих разделах

```java
public record UserCreationRequest(
        @NotBlank(message = "{messages.user.validation.fields.email.blank}")
        @Email(
                message = "{messages.user.validation.fields.email.incorrect-pattern}",
                regexp = "^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}$"
        )
        @Size(
                max = User.Constraints.EMAIL_FIELD_MAX_LENGTH,
                message = "{messages.user.validation.fields.email.too-long}"
        )
        String email,

        @NotBlank(message = "{messages.user.validation.fields.password.blank}")
        @Password
        String password
) {}
```

#### Запрос №1. Неверный формат полей (Нарушение `@Email` и `@Password`)

```http request
POST ./my-awesome-api/v1/users

{
    "email": "test",
    "password": "123"
}

```

Ответ от сервера:

```json
{
    "type": "invalid-input.constraint-violations-found",
    "details": [
        {
            "message": "Пароль должен быть как минимум 6 символов в длину Пароль должен содержать как минимум 1 строчных букв Пароль должен содержать как минимум 1 заглавных букв ",
            "field": "password",
            "constraints": {
                "constraint_violation_type": "PASSWORD",
                "min_length": 6,
                "min_letters_count": 1,
                "min_upper_letters_count": 1
            }
        },
        {
            "message": "Электронная почта имеет неверный формат",
            "field": "email",
            "constraints": {
                "pattern": "^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}$",
                "constraint_violation_type": "pattern_mismatch"
            }
        }
    ],
    "localized_message": "Обнаружено ошибок валидации в запросе: 2"
}
```