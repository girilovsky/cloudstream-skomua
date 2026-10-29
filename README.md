# SkomUA для Cloudstream

[![Build SkomUA](https://github.com/girilovsky/cloudstream-skomua/actions/workflows/build.yml/badge.svg)](https://github.com/girilovsky/cloudstream-skomua/actions/workflows/build.yml)

Неофіційне розширення [Cloudstream](https://github.com/recloudstream/cloudstream) для перегляду контенту [SkomUA](https://skomua.online/) українською.

## Можливості

- Категорії: переозвучки, фільми, серіали та стендапи.
- Пошук за назвою.
- Картки з описами й постерами.
- Сезони та серії з плейлистів Ashdi.
- Відео HLS та MP4. Додаткові плеєри залежать від екстракторів Cloudstream.

## Встановлення

**Спочатку переконайтеся, що хоча б одна [збірка](https://github.com/girilovsky/cloudstream-skomua/actions) завершилася успішно.** Файли репозиторію з’являються лише після публікації в гілку `builds`.

1. Відкрийте Cloudstream → Налаштування → Розширення → Додати репозиторій.
2. Назва: **SkomUA**. URL:

   ```text
   https://raw.githubusercontent.com/girilovsky/cloudstream-skomua/builds/repo.json
   ```

3. Відкрийте доданий репозиторій та встановіть **SkomUA**. Якщо провайдер не показується, перевірте мовні фільтри та показ бета-розширень.
4. Виберіть SkomUA як джерело на головному екрані.

## Помилка 404 / «Недійсна URL-адреса»

Якщо помилка містить `404: Not Found`, Cloudstream не знайшов `repo.json` або `plugins.json`.

- Перевірте [Actions](https://github.com/girilovsky/cloudstream-skomua/actions/workflows/build.yml): збірка має завершитися успішно, включно з кроком **Publish builds branch**.
- Дочекайтеся завершення першої збірки, якщо вона ще виконується.
- Якщо збірка впала, відкрийте журнал невдалого кроку. Повторне додавання URL не виправить помилку компіляції.
- Перевірте адресу: потрібна гілка `builds`, а не `main`, і файл `repo.json`.
- Після успішної публікації повторіть додавання репозиторію. GitHub Raw може оновлюватися із затримкою.

## Збірка

GitHub Actions запускається після push у `main` / `master` або вручну: **Actions → Build SkomUA → Run workflow**.

Workflow збирає `.cs3`, генерує `plugins.json` і `repo.json`, завантажує артефакт та публікує файли у гілку `builds`. Для цього workflow використовує `contents: write`.

Локально потрібні JDK 17, Android SDK і мережевий доступ до Gradle, Google Maven, Maven Central та JitPack:

```bash
chmod +x gradlew
./gradlew :SkomUA:make makePluginsJson
```

Результат: `SkomUA/build/*.cs3` та `build/plugins.json`. На Windows використовуйте `gradlew.bat`.

## Стан і обмеження

Розширення має статус **alpha / beta**. Перевірено структуру сторінок SkomUA, пряме посилання HLS фільму та плейлист Ashdi для двох сезонів / 16 серій «Безхатні виродки». Відтворення на пристрої Cloudstream ще не підтверджене.

- Серіали потребують доступного плейлиста Ashdi.
- Підтримка резервних плеєрів залежить від версії Cloudstream.
- Сайт і відеохостинги можуть змінити структуру або адреси. Якщо старе посилання перестало працювати, відкрийте картку заново.
- Розширення не обходить DRM, авторизацію чи платний доступ.

Про помилки повідомляйте через [Issues](https://github.com/girilovsky/cloudstream-skomua/issues): додайте версію Cloudstream, назву фільму або серію, посилання на сторінку та текст помилки. Не публікуйте паролі чи токени.

## Подяки

Інфраструктуру Gradle адаптовано з [CakesTwix/cloudstream-extensions-uk](https://github.com/CakesTwix/cloudstream-extensions-uk). Провайдер SkomUA та GitHub Actions додано в цьому репозиторії. Проєкт не є офіційним продуктом SkomUA чи Cloudstream.
