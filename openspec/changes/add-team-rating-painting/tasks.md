## 1. Shared rating snapshot и payload модели

- [x] 1.1 Проверить текущие `TeamRatingBookSnapshot`/`TeamRatingBookSnapshots` и выделить общий helper/model для строк рейтинга, если это уменьшит дублирование без изменения поведения книги
- [x] 1.2 Добавить snapshot-модель картины рейтинга с top `MAX_VISIBLE_ROWS = 8`, строками `place`, `teamName`, `score` и флагом `runtimeReady`
- [x] 1.3 Добавить snapshot builder картины рейтинга, который использует `TeamService.listTeams()` и не применяет отдельную сортировку
- [x] 1.4 Добавить clientbound payload картины рейтинга и codec/stream codec для передачи compact top-rating snapshot
- [x] 1.5 Добавить focused tests для snapshot builder-а: empty list, меньше лимита, больше лимита, score descending, равные очки с последовательными местами

## 2. Registry, предмет и размещённая entity

- [x] 2.1 Добавить `TradeRushEntities` или эквивалентный registry слой для entity type картины рейтинга и зарегистрировать его из `TradeRush.onInitialize()`
- [x] 2.2 Добавить `TeamRatingPaintingEntity` как настенный world object размером 4×4 блока, хранящий placement/direction state без строк рейтинга
- [x] 2.3 Реализовать survival/support checks для размещённой картины: стена/поддержка, свободная область 4×4 и удаление при потере поддержки
- [x] 2.4 Реализовать drop/pick-result поведение размещённой картины, чтобы она возвращала `trade-rush:team_rating_painting` при обычном разрушении
- [x] 2.5 Добавить `TeamRatingPaintingItem` с ID `trade-rush:team_rating_painting`, размещающий entity по вертикальной поверхности через server-side `useOn`
- [x] 2.6 Реализовать расход предмета только при успешном размещении и не расходовать предмет в creative-like режиме
- [x] 2.7 Зарегистрировать предмет картины рейтинга в `TradeRushItems` и добавить его в подходящий creative/admin workflow tab

## 3. Client renderer и world-space отображение

- [x] 3.1 Зарегистрировать client entity renderer для `TeamRatingPaintingEntity` через Fabric client rendering registry
- [x] 3.2 Выполнить rendering spike: добиться размещения 4×4 картины на стене и вывода статического фона/тестового текста в world-space
- [x] 3.3 Добавить client-side cache `RatingPaintingClientState` для последнего полученного snapshot картины рейтинга
- [x] 3.4 Добавить client networking receiver, который принимает payload картины рейтинга и обновляет `RatingPaintingClientState` независимо от наличия видимых картин
- [x] 3.5 Реализовать renderer layout: заголовок, колонки/строки места, названия команды и очков, top 8 строк
- [x] 3.6 Реализовать empty state для случая, когда команды ещё не созданы
- [x] 3.7 Реализовать runtime-not-ready/loading state без краша клиента
- [x] 3.8 Реализовать безопасное сокращение/клиппинг длинных названий команд, чтобы строки не выходили за область картины
- [x] 3.9 Убедиться, что renderer не содержит страниц, scroll, кнопок, полей ввода или действий управления командами

## 4. Server networking и live updates

- [x] 4.1 Зарегистрировать payload type картины рейтинга при инициализации мода рядом с существующими payload registrations
- [x] 4.2 Добавить server-side networking/broadcaster для отправки одного compact snapshot картины рейтинга подходящему `ServerPlayer`, если `ServerPlayNetworking.canSend(...)`
- [x] 4.3 Отправлять актуальный snapshot картины рейтинга игроку при подключении к серверу
- [x] 4.4 Подключить broadcast картины рейтинга к `TradeRushRuntime.onTeamsChanged()` после сохранения команд и рядом с существующими UI broadcasts
- [x] 4.5 Гарантировать, что изменение рейтинга строит общий top-rating snapshot и не отправляет отдельный payload на каждую размещённую картину
- [x] 4.6 Сохранить поведение книги/management UI: открытые `RatingBookMenu` и `TeamManagementMenu` продолжают получать свои snapshots только когда соответствующий menu открыт
- [x] 4.7 Добавить focused test или fake-target проверку broadcaster-а: painting update отправляется один раз на target и не зависит от количества размещённых картин

## 5. Assets, локализация и документация

- [x] 5.1 Добавить item asset/model для `team_rating_painting`
- [x] 5.2 Добавить texture/background asset для world-space картины рейтинга или переиспользуемый простой фон renderer-а
- [x] 5.3 Добавить localization strings для имени предмета, заголовка картины, empty/loading/runtime-not-ready states и подписей колонок, если renderer их использует
- [x] 5.4 Обновить `README.md`: описать `Team Rating Painting`, ID `trade-rush:team_rating_painting`, размещение на стене, top-рейтинг и automatic live updates
- [x] 5.5 Убедиться, что эта change не добавляет механику бесконечного сундука на спавне

## 6. Проверка

- [x] 6.1 Запустить focused domain/snapshot tests, включая существующий `domainTest`, если он остаётся основным тестовым entrypoint
- [x] 6.2 Запустить `./gradlew build`
- [x] 6.3 Запустить `openspec validate --changes "add-team-rating-painting" --strict`
- [x] 6.4 Вручную проверить в dev world, что предмет можно получить через creative/admin workflow
- [x] 6.5 Вручную проверить успешное размещение картины на стене 4×4 и отказ размещения без места/поддержки
- [x] 6.6 Вручную проверить survival/creative расход предмета, разрушение картины, drop item и удаление при потере поддержки
- [x] 6.7 Вручную проверить отображение без команд, с несколькими командами и с равными очками
- [x] 6.8 Вручную проверить список команд больше 8: картина показывает только top 8 и не отображает страницы/scroll/controls
- [x] 6.9 Вручную проверить live update после начисления очков, создания, переименования и удаления команды
- [x] 6.10 Вручную проверить два клиента: один изменяет рейтинг, другой видит обновление картины без открытия UI
- [x] 6.11 Вручную проверить несколько размещённых картин, включая две видимые одновременно: они показывают одинаковый актуальный top-рейтинг
