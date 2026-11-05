# DelFilm TV (Android TV)

Минимальное Android TV-приложение на Java с нативной поисковой строкой и встроенной WebView для сайта https://delfilm.pro/.

## Сборка APK

1. Установите Android Studio (с Android SDK Platform 35 и Build-Tools).
2. Откройте папку `DelFilmTV` как проект Android Studio.
3. Дождитесь Gradle Sync. Потребуется интернет, чтобы скачать Android Gradle Plugin 8.7.3.
4. В меню **Build → Build Bundle(s) / APK(s) → Build APK(s)**.
5. Установочный debug APK появится в `app/build/outputs/apk/debug/app-debug.apk`.
6. Передайте APK на Android TV и установите (при необходимости разрешив установку из неизвестных источников).

Альтернативно: `gradle assembleDebug` (если Gradle установлен отдельно).

## Особенности и ограничения

- Приложение показывается в меню Android TV благодаря LEANBACK_LAUNCHER и баннеру 320×180.
- Поиск набирается в нативном поле, затем вводится в поисковое поле сайта через JavaScript. Для выбора результатов используется WebView и пульт телевизора.
- Видеоплеер остаётся исходным плеером сайта; полноэкранный режим обрабатывается через WebChromeClient.
- Страница сайта или его поисковая разметка могут измениться; в таком случае потребуется поправить функцию `injectSearch`.
- Если сторонний iframe/плеер блокирует WebView, видео может не воспроизводиться.
- Не тестировалось на физическом Android TV, APK в этой среде не собран из-за отсутствия Android SDK и доступа к репозиториям.
- Проект не связан с владельцами сайта. Используйте только контент, на который у вас есть право доступа.

## Онлайн-сборка через GitHub Actions (без Android Studio)

1. Создайте новый GitHub-репозиторий.
2. В браузере откройте Codespaces для репозитория (Code → Codespaces → Create codespace on main).
3. Загрузите архив проекта в Codespaces через файловый проводник VS Code, затем откройте терминал и выполните `unzip DelFilmTV-OnlineBuild.zip -d tmp-project && cp -a tmp-project/DelFilmTV/. . && rm -rf tmp-project DelFilmTV-OnlineBuild.zip`.
4. Выполните `git add . && git commit -m "Add Android TV app" && git push`.
5. В репозитории перейдите в Actions → Build Android TV APK → Run workflow, если сборка ещё не запустилась после push.
6. Откройте завершённый запуск и скачайте Artifacts → DelFilmTV-debug-APK; внутри будет `app-debug.apk`.

APK — отладочная версия, но её можно установить на телевизор вне Google Play. GitHub Codespaces/Actions могут иметь лимиты в зависимости от аккаунта. Не публикуйте секретные ключи в репозитории.
