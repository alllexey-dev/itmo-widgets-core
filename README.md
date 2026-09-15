<h1 align="center">ITMO.Widgets Core</h1>

<p align="center">
  <strong>Компонент для уникальных функций приложения <a href="https://github.com/alllexey-dev/ITMO.Widgets">ITMO.Widgets</a> </strong>
</p>

**ITMO.Widgets Core** — это ключевой компонент экосистемы ITMO.Widgets, предоставляющий унифицированный доступ к API сервисов проекта.<br>
Проект использует <a href="https://github.com/alllexey-dev/my-itmo-api">my-itmo-api</a> как основу для аутентификации в сервисах.


<a href="https://github.com/users/alllexey-dev/projects/1"><strong>Roadmap & status </strong></a>

### 🌟 Возможности
* **Аутентификация:** прозрачное обновление access-token ITMO.ID через my-itmo-api.
* **API сервисов ITMO.Widgets:** типизированный Retrofit-контракт: профили, друзья, приватность, спорт, устройства, версия приложения.
* **FCM-модель:** типы данных, которые приложение получает через Google FCM.
* **Строгое декодирование:** обязательные поля и неизвестные значения перечислений не превращаются в null.

### 🛠️ Зависимости
* `my-itmo-api`
* `OkHttp`
* `Retrofit`
* `Kotlin Coroutines Core`

### 🚀 Использование

Добавьте в pom.xml:

```xml
<dependencies>
    <dependency>
        <groupId>dev.alllexey</groupId>
        <artifactId>itmo-widgets-core</artifactId>
        <version>1.1.9</version>
    </dependency>
</dependencies>
```

Текущая разработка идёт в версии `1.2.0-SNAPSHOT`, публикуемой только в локальный
Maven до релиза Android 2.1. Изменения — в [CHANGELOG.md](CHANGELOG.md),
соглашения контракта — в [docs/contract.md](docs/contract.md), правила для
агентов — в [AGENTS.md](AGENTS.md).

Сборка: `JAVA_HOME=$(/usr/libexec/java_home -v 17) ./gradlew build`.
