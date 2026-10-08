# Telegram Bot Coursework

Telegram-бот принимает напоминания в формате:

`01.01.2027 20:00 Сделать домашнюю работу`

Напоминания сохраняются в PostgreSQL. Таблица создаётся через Liquibase. Spring Scheduler раз в минуту выбирает задачи на текущую минуту и отправляет сообщения в Telegram.

Перед запуском необходимо:

1. Создать PostgreSQL базу `telegram_bot` и пользователя `telegram_bot_user`.
2. Установить пароль пользователя `telegram_bot_password`.
3. Вставить токен BotFather в `src/main/resources/application.properties` вместо `PASTE_TOKEN_FROM_BOTFATHER_HERE`.
