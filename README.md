<div align="center">

<img src="https://skomua.online/assets/img/logo.png" alt="SkomUA" width="240">

# SkomUA для Cloudstream

Українські переозвучки, фільми, серіали та стендапи у Cloudstream.

[![Build](https://github.com/girilovsky/cloudstream-skomua/actions/workflows/build.yml/badge.svg)](https://github.com/girilovsky/cloudstream-skomua/actions/workflows/build.yml)
[![Language](https://img.shields.io/badge/мова-українська-0057B7)](https://skomua.online/)
[![Cloudstream](https://img.shields.io/badge/Cloudstream-extension-2196F3)](https://github.com/recloudstream/cloudstream)

[Встановлення](#встановлення) · [Підтримка](#підтримка) · [Для розробників](#для-розробників)

</div>

## Про розширення

Неофіційний провайдер [SkomUA](https://skomua.online/) для [Cloudstream](https://github.com/recloudstream/cloudstream). Каталог і посилання на відео отримуються із сайту; доступність контенту залежить від SkomUA та його відеохостингів.

- **Чотири розділи:** переозвучки, фільми, серіали та стендапи.
- **Пошук** за назвою.
- **Картки контенту** з описами й постерами.
- **Серіали** з вибором сезону та серії через плейлисти Ashdi.

## Встановлення

Потрібен установлений Cloudstream із підтримкою розширень.

1. Відкрийте **Налаштування → Розширення → Додати репозиторій**.
2. Вкажіть назву **SkomUA** та вставте адресу:

   ```text
   https://raw.githubusercontent.com/girilovsky/cloudstream-skomua/builds/repo.json
   ```

3. Відкрийте репозиторій **SkomUA** й установіть однойменне розширення.
4. Виберіть **SkomUA** як джерело на головному екрані.

> Якщо розширення відсутнє у списку, перевірте українську мову у фільтрах та показ бета-розширень. Поточна версія позначена як Beta.

## Оновлення

Оновлюйте розширення через розділ **Розширення** у Cloudstream. Повторно додавати репозиторій для кожного оновлення не потрібно.

## Підтримка

| Проблема | Що перевірити |
| --- | --- |
| Репозиторій не додається | Скопіюйте повну адресу вище. Вона має закінчуватися на `/builds/repo.json`. |
| Помилка `404: Not Found` | Перевірте доступність [маніфесту репозиторію](https://raw.githubusercontent.com/girilovsky/cloudstream-skomua/builds/repo.json). Після публікації оновлення повторіть спробу пізніше. |
| SkomUA відсутній у списку | Увімкніть українську мову та показ бета-розширень у фільтрах. |
| Відео не запускається | Перевірте відтворення тієї ж сторінки на сайті SkomUA та відкрийте картку заново у Cloudstream. |
| Немає списку серій | Серіали потребують доступного плейлиста Ashdi. Якщо на сайті серії є, створіть повідомлення про помилку. |

Знайшли помилку? Створіть [Issue](https://github.com/girilovsky/cloudstream-skomua/issues) і вкажіть:

- версію Cloudstream та розширення;
- посилання на сторінку SkomUA;
- сезон і серію, якщо проблема із серіалом;
- кроки відтворення та текст помилки або знімок екрана.

### Сумісність

Основний відеохостинг — **Ashdi**. Підтримка інших плеєрів залежить від екстракторів Cloudstream. Зміни на сайті або відеохостингу можуть потребувати оновлення провайдера.

Збірку та публікацію розширення перевірено через GitHub Actions. Повний цикл відтворення на пристрої Cloudstream ще не підтверджено.

## Для розробників

### Локальна збірка

Потрібні **JDK 17**, **Android SDK** та доступ до репозиторіїв залежностей Gradle, Google Maven, Maven Central і JitPack.

```bash
git clone https://github.com/girilovsky/cloudstream-skomua.git
cd cloudstream-skomua
chmod +x gradlew
./gradlew :SkomUA:make makePluginsJson
```

На Windows використовуйте `gradlew.bat` замість `./gradlew`.

Результати збірки:

| Файл | Призначення |
| --- | --- |
| `SkomUA/build/*.cs3` | Пакет розширення |
| `build/plugins.json` | Список розширень для репозиторію |

### Публікація

[GitHub Actions](https://github.com/girilovsky/cloudstream-skomua/actions/workflows/build.yml) запускається після push у `main` / `master` або вручну через **Run workflow**.

Workflow збирає розширення, формує маніфести та публікує `SkomUA.cs3`, `plugins.json` і `repo.json` у гілку `builds`. Для публікації використовується дозвіл `contents: write`. Перед випуском нової версії збільшіть `version` у `SkomUA/build.gradle.kts`.

## Подяки

- [SkomUA](https://skomua.online/) — джерело каталогу та українського озвучення.
- [Cloudstream](https://github.com/recloudstream/cloudstream) — застосунок і платформа розширень.
- [CakesTwix/cloudstream-extensions-uk](https://github.com/CakesTwix/cloudstream-extensions-uk) — основа конфігурації Gradle.

Це незалежне розширення, не пов’язане офіційно із SkomUA або Cloudstream. Воно не зберігає відеофайли та не обходить авторизацію, платний доступ чи DRM.
