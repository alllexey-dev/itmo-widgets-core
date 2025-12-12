<h1 align="center">ITMO.Widgets Core</h1>

<p align="center">
  <strong>Компонент для уникальных функций приложения <a href="https://github.com/alllexey-dev/ITMO.Widgets">ITMO.Widgets</a> </strong>
</p>

**ITMO.Widgets Core** — это ключевой компонент экосистемы ITMO.Widgets, предоставляющий унифицированный доступ к API сервисов проекта.<br>
Проект использует <a href="https://github.com/alllexey-dev/my-itmo-api">my-itmo-api</a> как основу для аутентификации в сервисах.


<a href="https://github.com/users/alllexey-dev/projects/1"><strong>Roadmap & status </strong></a>

## 🌟 Текущие возможности
* **Управление аутентификацией:** Автоматическое и прозрачное управление access и refresh токенами, включая их обновление.
* **API для сервисов ITMO.Widgets:** Полная реализация модели и методов API.
* **FCM-модель для приложения ITMO.Widgets:** Полная реализация модели данных, получаемых приложением через Google FCM.

## 🛠️ Зависимости
* `my-itmo-api`
* `OkHttp`
* `Retrofit`
* `Kotlin Coroutines Core`

## 🚀 Использование

Добавьте в pom.xml:

```xml
<dependencies>
    <dependency>
        <groupId>dev.alllexey</groupId>
        <artifactId>itmo-widgets-core</artifactId>
        <version>1.1.5</version>
    </dependency>
</dependencies>
```