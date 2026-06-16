## Context

В Trade Rush уже есть доменный слой команд (`TeamService`, `Team`, `TeamRepository`) и книга рейтинга, реализованная в рамках `add-team-rating-book`. Книга рейтинга уже использует `TeamService.listTeams()` как единый источник порядка команд: по очкам от большего к меньшему, затем по имени без учёта регистра. Для книги также добавлены snapshot/payload/networking паттерны и runtime-level обработчик изменения командного состояния.

Картина рейтинга отличается от книги тем, что это не screen/menu конкретного игрока, а world-space объект, видимый всем игрокам рядом с ним. Поэтому обновления картины не должны зависеть от открытого menu. При этом количество размещённых картин не должно умножать сетевую нагрузку: 20 картин в разных местах мира должны использовать один общий top-rating snapshot на клиенте, а не получать 20 независимых обновлений.

Текущий стек: Fabric, Minecraft `26.1.2`, split client/main sources. В Minecraft `26.1.2` есть vanilla `Painting`, `HangingEntity`, `Display.TextDisplay`, `EntityType.TEXT_DISPLAY`, а Fabric предоставляет `EntityRendererRegistry`. Однако `TextDisplay` не выглядит удобным как основа feature: setter-ы текста и части display state приватные, а companion entities сложнее надёжно очищать. Поэтому картина рейтинга должна быть собственным контролируемым world object с собственным renderer-ом.

## Goals / Non-Goals

**Goals:**

- Добавить предмет `trade-rush:team_rating_painting`, который можно получить через creative/admin workflow.
- Размещать картину рейтинга на вертикальной стене как большой настенный объект размером 4×4 блока.
- Отображать на картине read-only top-рейтинг: место, название команды, очки.
- Переиспользовать `TeamService.listTeams()` как единый источник сортировки и очков.
- Ограничить видимый список первыми строками, которые помещаются на картине; в первой версии зафиксировать `MAX_VISIBLE_ROWS = 8` как layout capacity.
- Автоматически обновлять все видимые картины при изменении командного состояния без действий игрока.
- Масштабировать server/network update cost по числу online clients и частоте изменений рейтинга, а не по числу размещённых картин.
- Сохранить текущую семантику книги рейтинга: book UI обновляется только у игроков с открытым `RatingBookMenu`.
- Добавить focused tests для snapshot builder-а и broadcaster-а там, где это возможно без запуска клиента.

**Non-Goals:**

- Не реализовывать выдачу картины через бесконечный сундук на спавне.
- Не добавлять страницы, scroll, кнопки, поля ввода или управление командами на картину.
- Не хранить snapshot рейтинга отдельно в каждой размещённой картине.
- Не менять правила начисления очков, сортировку `TeamService.listTeams()` или формат сохранения команд.
- Не добавлять внешние зависимости.
- Не пытаться сделать vanilla `Painting` динамической через fragile renderer mixin в первой версии.

## Decisions

### Решение: использовать custom hanging entity, а не vanilla `Painting`

Картина рейтинга должна вести себя как настенная картина: размещаться на вертикальной поверхности, занимать большую область, исчезать при потере поддержки и дропать свой предмет. Для этого лучше добавить custom entity вроде `TeamRatingPaintingEntity`, основанную на `HangingEntity`/настенной entity-механике Minecraft, и отдельный предмет `TeamRatingPaintingItem`, который вручную размещает эту entity через `useOn`.

Entity должна хранить только world placement state: позицию, направление, размеры/поддержку и обычную entity-сериализацию. Она не должна хранить строки рейтинга, потому что рейтинг глобален для сервера и может изменяться независимо от chunk/entity state.

Рассмотренные альтернативы:

- Vanilla `Painting` + custom painting variant + mixin в `PaintingRenderer`: ближе к vanilla, но хрупко. Нужно отличать специальную картину от обычных, менять renderer, drop/pick result и placement behavior.
- Custom 4×4 block/multiblock + block entity renderer: проще хранить state, но это уже скорее табло/блок, а не картина; появятся задачи мультиблочного placement/breaking.
- `TextDisplay` companion entities поверх картины: может работать как datapack-like подход, но в Java API `TextDisplay` в `26.1.2` неудобен для прямой установки текста, а cleanup связанных entities при break/unload/disconnect усложняет feature.
- Map/item frame: не соответствует требованию “картина”, хуже читаемость, сложнее поддерживать и очищать.

### Решение: renderer картины использует глобальный client-side snapshot cache

Клиент должен хранить один актуальный `TeamRatingPaintingSnapshot` в singleton/service вроде `RatingPaintingClientState`. Любая загруженная и видимая `TeamRatingPaintingEntity` при рендеринге берёт строки из этого cache. Если snapshot ещё не получен, renderer показывает loading/unavailable state; если команд нет, empty state.

Такой подход означает, что 1, 5 или 20 размещённых картин не требуют 1, 5 или 20 разных payload-ов. Все картины на клиенте используют одинаковый snapshot и автоматически становятся согласованными после получения нового payload.

Рассмотренные альтернативы:

- Хранить snapshot в entity data каждой картины: проще думать локально, но плохо масштабируется и дублирует глобальное состояние.
- Запрашивать snapshot при рендере/nearby: создаёт polling и лишние serverbound/clientbound interactions.
- Отправлять snapshot только игрокам, tracking конкретные painting entities: оптимальнее по сети, но сложнее для первой версии и может ошибаться при edge cases chunk tracking. Глобальная рассылка online modded clients проще и достаточно дешева для compact top-10 snapshot.

### Решение: добавить отдельный compact payload для картины рейтинга

Книга рейтинга использует full snapshot, потому что должна показывать все команды постранично. Картина показывает только верхнюю часть рейтинга, поэтому ей нужен compact payload вроде `TeamRatingPaintingStatePayload` с `TeamRatingPaintingSnapshot`, содержащий top `MAX_VISIBLE_ROWS` строк и `runtimeReady`.

Snapshot builder должен переиспользовать общий путь построения строк рейтинга поверх `TeamService.listTeams()`. Если текущие book-specific классы (`TeamRatingBookSnapshot`, `TeamRatingBookSnapshots`) начнут дублировать логику, лучше выделить общий helper/model для строк рейтинга, а book/painting snapshots оставить отдельными payload-level моделями.

Рассмотренные альтернативы:

- Переиспользовать `TeamRatingBookStatePayload`: технически возможно, но он отправляет полный список и семантически привязан к книге.
- Отправлять только текстовые строки: быстрее для первой реализации, но хуже тестируемость, локализация/layout и будущий reuse.

### Решение: рассылать painting snapshot на join и при team state changes

При подключении игрока сервер должен отправить актуальный painting snapshot, если клиент поддерживает payload. При изменении командного состояния `TradeRushRuntime.onTeamsChanged()` должен после сохранения и UI broadcaster-а также отправить новый painting snapshot online clients.

В первой версии допустимо отправлять snapshot сразу на каждое изменение командного состояния. Архитектура должна держать отправку в отдельном `RatingPaintingNetworking`/broadcaster слое, чтобы при необходимости позже добавить debounce/dirty flag на server tick без изменения item/entity/renderer API.

Критически важно: server MUST NOT искать все размещённые картины и отправлять отдельный update на каждую. Один team-change event строит один top snapshot и отправляет его каждому подходящему online client один раз.

Рассмотренные альтернативы:

- Подписки на конкретные картины: дороже и сложнее, нужны отписки при unload/chunk tracking.
- Обновлять только при размещении картины: не отражает изменения очков в реальном времени.
- Client-side polling: сервер уже знает момент изменения и может делать push.

### Решение: фиксировать layout capacity как 8 строк в первой версии

Картина занимает 4×4 блока, но читаемость зависит от renderer scale, GUI/font rendering и расстояния до игрока. После ручной проверки читаемости первая версия фиксирует deterministic product behavior: показывать top 8 команд, либо меньше, если команд меньше. Значение должно оставаться фиксированным и тестируемым.

Рассмотренные альтернативы:

- Вычислять capacity динамически от расстояния/GUI scale: world-space rendering не должен менять состав рейтинга в зависимости от клиента.
- Уменьшать шрифт под все команды: ухудшает читаемость и противоречит отсутствию страниц/scroll.
- Показывать `+N more`: можно добавить позднее, но первая версия может просто показывать top 8 без дополнительной строки.

### Решение: начать implementation с rendering spike

Minecraft `26.1.2` использует новый renderer pipeline (`extractRenderState`/`submit`) для entity/block renderers. Перед тем как завершать всю feature, implementation должна сначала доказать минимальный путь: custom hanging entity размещается на стене и renderer может вывести статический фон и тестовый текст в world-space. Это снизит риск того, что networking/domain часть будет готова раньше, чем станет ясно, как правильно рисовать читаемый текст.

## Risks / Trade-offs

- [Новый renderer pipeline Minecraft `26.1.2` может оказаться сложнее ожидаемого] → Начать с rendering spike и только после него подключать live data.
- [World-space текст может быть плохо читаемым на расстоянии] → Зафиксировать простой layout, ограничить top 8, использовать контрастный фон и вручную проверить читаемость в dev world.
- [Частые начисления очков могут вызвать много payload-ов] → Держать painting broadcast в отдельном слое; при необходимости добавить debounce/dirty flag. Top-8 snapshot маленький и отправляется один раз на online client, не на картину.
- [Много картин рядом может увеличить client render cost] → Renderer должен быть простым, без анимаций и без тяжёлых пересчётов каждый frame; unloaded/out-of-view entities не рендерятся.
- [Custom hanging placement может отличаться от vanilla paintings] → Реализовать survival/support checks и manual QA: placement, break, support removal, creative/survival item consumption.
- [Snapshot может прийти до загрузки мира или до появления картины] → Клиентский cache должен принимать payload независимо от наличия видимых картин.
- [Runtime может быть недоступен при edge cases] → Сервер отправляет `runtimeReady=false`, renderer показывает unavailable state без краша.

## Migration Plan

1. Добавить новые registry entries, item/entity/payload/renderer без изменения существующего save format команд.
2. Existing worlds продолжат загружаться без миграции; новых rating painting entities в старых мирах нет.
3. Rollback безопасен до размещения картин: удаление feature не затрагивает команды и очки.
4. Если rollback после размещения картин, мир может содержать unknown custom entities/items; это стандартный риск удаления modded entities и должен решаться обычной очисткой мира/backup.

## Open Questions

Нет открытых продуктовых вопросов для старта реализации. Приняты рабочие решения: ID `trade-rush:team_rating_painting`, custom 4×4 hanging entity, top 8 строк, отдельный compact painting snapshot, глобальный client cache и рассылка online clients без умножения на число размещённых картин.
