# Сервис авторизации (JWT)

Простая реализация сервиса авторизации, работающего на основе JWT токенов. Сервис управляет пользователями, их ролями,
правами доступа, занимается выдачей Access/Refresh токенов. Сервис также предоставляет публичный ключ в виде JWKS, чтобы
другие сервисы могли верифицировать подписи выданных токенов самостоятельно

## ✨ Функциональные возможности

Проект содержит гибко расширяемую систему авторизации и управления доступом, в том числе автоматическую инициализацию
ролей, учетных записей и прав. Ниже описаны основные особенности.

---

### 🧩 1. Автоматическая инициализация данных

При запуске приложения происходит автоматическая инициализация:

- **ролей**
- **учетной записи администратора**
- **прав доступа**

🔄 **Поддерживаются два режима инициализации:**

| Режим            | Описание                                                                   |
|------------------|----------------------------------------------------------------------------|
| `ON_TABLE_EMPTY` | Инициализация происходит только если соответствующая таблица пуста         |
| `ON_RELOAD`      | При каждом запуске проверяется наличие записи; если не найдена — создается |

⚙️ **Значения по умолчанию:**

- **Роли и админ-аккаунт:** `ON_TABLE_EMPTY`
- **Права доступа:** `ON_RELOAD`

🛠️ Управлять режимами инициализации можно с помощью переменных окружения (см. [Переменные окружения](#-переменные-окружения))

---

### 🔐 2. Роли

Пользователь может обладать несколькими ролями одновременно. Каждая роль связана с набором **прав доступа**, которые
определяют поведение системы авторизации.

📌 **Основные операции с ролями:**

| Метод    | URL                                             | Описание                            |
|----------|-------------------------------------------------|-------------------------------------|
| `GET`    | `/api/v1/jwt-auth/roles`                        | Получение всех ролей (с пагинацией) |
| `GET`    | `/api/v1/jwt-auth/roles/{roleName}`             | Получение роли по имени             |
| `POST`   | `/api/v1/jwt-auth/roles`                        | Создание новой роли                 |
| `PATCH`  | `/api/v1/jwt-auth/roles/{roleName}/permissions` | Обновление прав доступа у роли      |
| `DELETE` | `/api/v1/jwt-auth/roles/{roleName}`             | Удаление роли                       |

---

### 👥 3. Пользователи

Система поддерживает базовые операции с учетными записями, включая мягкое удаление и смену пароля.

📌 **Основные операции с пользователями:**

| Метод    | URL                                      | Описание                                    |
|----------|------------------------------------------|---------------------------------------------|
| `GET`    | `/api/v1/jwt-auth/users`                 | Получение всех пользователей (с пагинацией) |
| `GET`    | `/api/v1/jwt-auth/users/{uuid}`          | Получение пользователя по UUID              |
| `POST`   | `/api/v1/jwt-auth/users`                 | Создание нового пользователя                |
| `DELETE` | `/api/v1/jwt-auth/users/{uuid}`          | Мягкое удаление пользователя                |
| `PATCH`  | `/api/v1/jwt-auth/users/{uuid}/password` | Смена пароля у пользователя                 |

---

### 🛡️ 4. Права доступа

Проект включает **автоматическое сканирование прав доступа** по аналогии со Spring `@ComponentScan`.

🔍 Для этого используется аннотация `@PermissionScan`, в которую передаются пакеты, где необходимо искать права.

🧩 **Как это работает:**

- Аннотация `@PermissionScan` добавляется над главным Main-классом приложения

```java
@SpringBootApplication
@PermissionScan(packages = {"me.stinper.jwtauth.controller"})
public class JwtAuthApplication {

    public static void main(String[] args) {
        SpringApplication.run(JwtAuthApplication.class, args);
    }

}
```

- Сканер реализован в виде **интерфейса**, с 2 методами: первый отвечает за определение пакетов, в которых необходимо искать права доступа, а второй - за непосредственное сканирование прав

```java
public interface PermissionScanner {
    List<String> resolveCandidatePackages();

    Set<Permission> scanPermissionsFromPackage(@NonNull String packageName)
            throws Exception;
}
```

- Права доступа аннотируются специальными метками, которые позволяют указать как единичное право доступа, так и группу

Пример группы прав доступа

```java
    @GetMapping
    @Permissions(permissions = {
            @OperationPermission(
                    permission = "user.read.find-all-users",
                    description = "Пользователь с этим правом может просматривать список всех не деактивированных пользователей"
            ),
            @OperationPermission(
                    permission = "user.read.read-deactivated-users",
                    description = "Пользователь с этим правом может просматривать тех пользователей, чья учетная запись была деактивирована"
            )
    })
    @PreAuthorize("@userSecurityService.isAllowedToFindAllUsers(principal)")
    public ResponseEntity<Page<UserDto>> findAll(@ModelAttribute @ParameterObject EntityPaginationRequest entityPaginationRequest,
                                                 @AuthenticationPrincipal JwtAuthUserDetails user) {
        //Логика метода
    }
```

Пример единичного права доступа

```java
    @GetMapping("/{uuid}")
    @OperationPermission(
            permission = "user.read.find-by-uuid",
            description = "Пользователь с этим правом может получить пользователя по его идентификатору"
    )
    @PreAuthorize("@userSecurityService.isAllowedToFindUserByUUID(#uuid, principal)")
    public ResponseEntity<UserDto> findById(@PathVariable UUID uuid) {
        //Логика метода
    }
```

Все это позволяет динамически собирать права доступа из любого указанного пакета и использовать их, к примеру, для инициализации

💡 Это решение легко масштабируется и позволяет гибко добавлять новые права без необходимости вручную их регистрировать.

📌 **Основные операции с правами доступа:**

| Метод    | URL                                             | Описание                                   |
|----------|-------------------------------------------------|--------------------------------------------|
| `GET`    | `/api/v1/jwt-auth/permissions`                  | Получение всех прав доступа (с пагинацией) |
| `GET`    | `/api/v1/jwt-auth/permissions/{id}`             | Получение права доступа по ID              |
| `POST`   | `/api/v1/jwt-auth/permissions`                  | Создание нового права доступа              |
| `DELETE` | `/api/v1/jwt-auth/permissions/{id}`             | Удаление права доступа                     |
| `PATCH`  | `/api/v1/jwt-auth/permissions/{id}/description` | Смена описания права доступа               |

---

### 🔑 5. JWKS (JSON Web Key Set)

Приложение предоставляет публичный ключ для проверки подписи JWT в формате **JWKS**.

📍 **Публичный URL:**

```html
GET .../.well-known/jwks.json
```

📥 **Пример ответа:**

```json
{
  "keys": [
    {
      "kty": "RSA",
      "alg": "RS256",
      "n": "…",
      "e": "…"
    }
  ]
}
```

---

<details>
  <summary>Пример восстановления публичного ключа из JWKS</summary>

Для восстановления публичного ключа в сервисе (на Java), используйте следующие шаги

### 🔧 Шаг 1: Создание RestClient

Используйте любой HTTP клиент для отправки запроса на сервер, к примеру, RestClient

```java

@Bean
public RestClient restClient(RestClient.Builder restClientBuilder,
                             @Value("${app.jwt-auth.server.base-url}") String jwtAuthServerBaseUrl) {
    return restClientBuilder
            .baseUrl(jwtAuthServerBaseUrl)
            .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
            .build();
}
```

### 🔧 Шаг 2: Получите JWKS с сервера

#### 🧩 Структура DTO

Формат ответа представлен следующим классом:

```java
public record JwksDto(
        List<KeyBody> keys
) {
    @Data
    @Builder
    public static class KeyBody {
        private final String kty;
        private final String alg;
        private final String n;
        private final String e;

        public static KeyBodyBuilder RSA_KEY = KeyBody.builder()
                .alg("RS256")
                .kty("RSA");
    }
}
```

Далее, сделайте соответствующий запрос на сервер

```java
private JwksDto fetchJwksFromAuthServer() {
    return restClient.get()
            .uri("/.well-known/jwks.json")
            .retrieve()
            .body(JwksDto.class);
}
```

### 🔧 Шаг 3: Восстановите публичный ключ из JWKS

Для восстановления `RSAPublicKey` из полей `n` (модуль) и `e` (экспонента) используется следующий алгоритм:

```java
private PublicKey restorePublicKey(String base64Modulus, String base64Exponent) throws Exception {
    byte[] modulusBytes = Base64.getUrlDecoder().decode(base64Modulus);
    byte[] exponentBytes = Base64.getUrlDecoder().decode(base64Exponent);

    BigInteger modulus = new BigInteger(1, modulusBytes);
    BigInteger exponent = new BigInteger(1, exponentBytes);

    RSAPublicKeySpec spec = new RSAPublicKeySpec(modulus, exponent);
    KeyFactory factory = KeyFactory.getInstance("RSA");

    return factory.generatePublic(spec);
}
```

### 🔧 Шаг 4: Верифицируйте подпись токена

#### Примечание: В примере используется библиотеке `io.jsonwebtoken`

```java
    public void verifyTokenSignature(String token, PublicKey publicKey) throws JwtException {
    Jwts.parser()
            .verifyWith(publicKey)
            .build()
            .parseSignedClaims(token);
}
```

</details>

### ⚙️ 6. Модуль автоматического построения сообщений об ошибках

🧩 В проекте присутствует модуль, который **автоматически формирует сообщения об ошибках валидации**, основанной на аннотациях из пакета `Jakarta Validation API`, таких как `@NotNull`, `@Size`, `@Min` и других.

С подробной информацией об этом модуле можно ознакомиться [в отдельной документации](https://github.com/Stinper/spring-jwt-auth/tree/main/error-response-api) 📄


## Документация к API

Документация к эндпоинтам сервиса описана с помощью Swagger UI

#### URL адрес документации

```http
  GET .../swagger-ui.html
```

## ⚙️ Переменные окружения

В проекте присутствует множество переменных окружения, которые позволяют гибко влиять на некоторое поведение. В таблице
ниже описаны все переменные окружения, их назначение и значения по умолчанию (если присутствуют)

| Переменная окружения              | Значение по умолчанию | Назначение                                                                |
|-----------------------------------|-----------------------|---------------------------------------------------------------------------|
| `JWTAUTH_ADMIN_EMAIL`             | _Нет_                 | **Обязательно**. Электронная почта учетной записи администратора          |
| `JWTAUTH_ADMIN_PASSWORD`          | _Нет_                 | **Обязательно**. Пароль учетной записи администратора                     |
| `JWTAUTH_ADMIN_ROLE_NAME`         | `ROLE_ADMIN`          | Название роли администратора, используемое в системе                      |
| `JWTAUTH_ADMIN_ROLE_PREFIX`       | `Admin`               | Префикс роли администратора (например, используется в имени роли)         |
| `JWTAUTH_DB_HOST`                 | `localhost`           | Хост базы данных PostgreSQL                                               |
| `JWTAUTH_DB_PORT`                 | `5432`                | Порт базы данных PostgreSQL                                               |
| `JWTAUTH_DB_NAME`                 | `jwt_auth`            | Имя базы данных PostgreSQL                                                |
| `JWTAUTH_DB_USER`                 | `postgres`            | Имя пользователя базы данных                                              |
| `JWTAUTH_DB_PASSWORD`             | `123`                 | Пароль пользователя базы данных                                           |
| `JWTAUTH_ADMIN_ROLE_INIT_MODE`    | `ON_TABLE_EMPTY`      | Режим инициализации роли администратора                                   |
| `JWTAUTH_ADMIN_ACCOUNT_INIT_MODE` | `ON_TABLE_EMPTY`      | Режим инициализации учетной записи администратора                         |
| `JWTAUTH_PERMISSIONS_INIT_MODE`   | `ON_RELOAD`           | Режим инициализации прав доступа на выполнение различных операций сервиса |

## 🚀 Запуск и установка

Рекомендуется запускать проект с использованием **Docker Compose**. Убедитесь, что у вас установлены следующие
инструменты:

- 🐳 [Docker](https://www.docker.com/products/docker-desktop)
- 🧩 [Docker Compose](https://docs.docker.com/compose/)

---

### 🔧 Шаг 1: Клонирование репозитория

```bash
git clone https://github.com/Stinper/spring-jwt-auth.git
cd spring-jwt-auth
```

### 🔧 Шаг 2: Установите необходимые переменные окружения

Перед запуском проекта вам необходимо установить обязательные переменные окружения в .env файле в корне проекта

| Переменная окружения     | Назначение                                      |
|--------------------------|-------------------------------------------------|
| `JWTAUTH_ADMIN_EMAIL`    | Электронная почта учетной записи администратора |
| `JWTAUTH_ADMIN_PASSWORD` | Пароль учетной записи администратора            |                  

### 🔧 Шаг 3: Для сборки и запуска всех контейнеров выполните команду

```bash
docker-compose up --build
```

После запуска приложение будет доступно по адресу

```http
  http://localhost:8080/
```

💡 **Совет**: при необходимости вы можете изменить порты, настройки базы данных и другие параметры в docker-compose.yml или
.env файле.

## 🧪 Запуск тестов

Проект использует **Gradle** для сборки и запуска тестов. Убедитесь, что у вас
установлен [Gradle](https://gradle.org/install/) или используйте обертку `./gradlew`, поставляемую с проектом.

❗**ВАЖНО**: Проект использует библиотеку **TestContainers** ([официальный сайт](https://testcontainers.com/)) для запуска тестов репозиториев. Перед запуском тестов убедитесь, что у вас запущен **Docker**, иначе тесты, использующие TestContainers, не смогут выполниться.

---

### ▶️ Запуск всех тестов

Для запуска всех тестов в проекте используйте команду:

```bash
./gradlew test
```

### 🚀 Дополнительные команды

#### Запустить только тесты определенного класса

```bash
./gradlew test --tests com.example.MyTestClass
```

#### Запустить тест с определенным именем

```bash
./gradlew test --tests "myTestMethod"
```

### 📂 Отчёты о тестировании

После завершения тестов отчет в формате HTML будет доступен по следующему пути:

```bash
.../build/reports/tests/test/index.html
```

