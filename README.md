# Project Bunker - Minecraft Mod

## Описание

**Project Bunker** - это мод для Minecraft 1.21.1 на NeoForge, который добавляет постапокалиптическую атмосферу в игру.

### Основные компоненты

- 🧟 **Враждебные мобы**: Мутировавшие зомби, Радиационные привидения
- 🛡️ **Защита**: Hazmat-костюм для защиты от радиации
- 🏚️ **Новые блоки**: Блоки бункера, радиоактивная почва, апокалиптический камень
- 🌍 **Новое измерение**: "Мёртвая Земля" с радиоактивной пустошью
- 🔴 **Система радиации**: Урон от радиации для небережно игроков
- 💾 **Кастомная загрузка**: Атмосферные русские текст при загрузке мира

## 🎮 Кастомная загрузка

Мод содержит кастомные сообщения загрузки на русском языке:

```
Заражение распространяется...
Пробуждение тёмных сил...
Инициализация заражённого мира...
Загрузка...
```

Каждое сообщение автоматически переключается каждые 2 секунды.

Подробнее в [LOADING_SCREEN_GUIDE.md](LOADING_SCREEN_GUIDE.md) и [CUSTOM_LOADING_IMPLEMENTATION.md](CUSTOM_LOADING_IMPLEMENTATION.md)

## 📋 Установка и разработка

### Требования
- Java 21
- Gradle 9.2.1 (встроен в проект)

### Сборка

```bash
./gradlew.bat build
```

### Запуск клиента разработчика

```bash
./gradlew.bat runClient
```

### Запуск сервера разработчика

```bash
./gradlew.bat runServer
```

## 📁 Структура проекта

```
ProjectBunkers/
├── src/main/java/dg/projectbunker/
│   ├── ProjectBunker.java          # Главный класс мода
│   └── client/
│       └── LoadingMessages.java    # Система кастомной загрузки
├── src/main/resources/
│   ├── assets/project_bunker/
│   │   ├── lang/                   # Локализация (RU, EN)
│   │   ├── textures/               # Текстуры
│   │   ├── models/                 # 3D модели
│   │   └── blockstates/            # Состояния блоков
│   └── project_bunker.mixins.json  # Конфигурация Mixins
└── build.gradle                     # Конфигурация сборки
```

## 🔧 Технические детали

- **Язык**: Java 21
- **Версия Minecraft**: 1.21.1
- **Forge**: NeoForge 21.1.233
- **Версия мода**: 1.0.0

## 📚 Документация

- [ARCHITECTURE.md](ARCHITECTURE.md) - Архитектура мода
- [ARCHITECTURE_PROJECT_BUNKER.md](ARCHITECTURE_PROJECT_BUNKER.md) - Детальная архитектура
- [LOADING_SCREEN_GUIDE.md](LOADING_SCREEN_GUIDE.md) - Руководство по кастомной загрузке
- [CUSTOM_LOADING_IMPLEMENTATION.md](CUSTOM_LOADING_IMPLEMENTATION.md) - Техническая реализация
- [QUICK_REFERENCE.md](QUICK_REFERENCE.md) - Быстрая справка

## 🔗 Ресурсы

**NeoForge Documentation**: https://docs.neoforged.net/  
**NeoForge Discord**: https://discord.neoforged.net/  
**Minecraft Wiki**: https://minecraft.fandom.com/

## 📝 Лицензия

All Rights Reserved

---

**Последнее обновление**: 12 июня 2026
