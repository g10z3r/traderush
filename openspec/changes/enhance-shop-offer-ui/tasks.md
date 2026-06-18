## 1. Серверный snapshot и DTO

- [x] 1.1 Добавить getter для `endsAtTick` в `TimedActiveOffer`.
- [x] 1.2 Расширить `ShopOfferEntry` полем `expiresAtTick` для timed-заказов и sentinel-значением для заказов без таймера.
- [x] 1.3 Обновить `ShopOfferEntry.from(...)`, чтобы он принимал `ActiveOffer` и заполнял `fixedReward`/`expiresAtTick` из активного заказа.
- [x] 1.4 Обновить `ShopOfferEntry.write(...)` и `ShopOfferEntry.read(...)` синхронно с новым полем.
- [x] 1.5 Расширить `ShopOffersPayload` полем `serverTick` и обновить его codec read/write.
- [x] 1.6 Обновить `ShopNetworking.sendOffers(...)` и `buildOfferEntries(...)`, чтобы payload содержал `serverTick`, а entries создавались из `Offer` + `ActiveOffer`.

## 2. Countdown в UI магазина

- [x] 2.1 В `ShopOffersScreen.applyPayload(...)` сохранить `serverTickAtSnapshot` и локальный `clientTickAtSnapshot`.
- [x] 2.2 Добавить helper-методы для определения timed-entry с валидным `expiresAtTick`, расчёта remaining ticks, форматирования countdown и проверки истечения.
- [x] 2.3 Отрисовать countdown timed-заказов в строках левой колонки, не показывая таймер для limited-заказов.
- [x] 2.4 Отрисовать синхронизированный countdown выбранного timed-заказа в правой детальной панели.
- [x] 2.5 Обновить `updateTradeButton()`, чтобы expired timed-заказы не могли быть отправлены на trade из stale UI.
- [x] 2.6 Уточнить и реализовать формат countdown: меньше часа — `M:SS` без часов, час и больше — `H:MM:SS`.

## 3. Tooltip требуемых предметов

- [x] 3.1 Передать координаты мыши в методы отрисовки item icons в списке и детальной панели.
- [x] 3.2 Добавить общий helper для отрисовки `ItemStack` icon + decorations + hover hit-test.
- [x] 3.3 При hover по иконке вызывать `graphics.setTooltipForNextFrame(font, stack, mouseX, mouseY)` для vanilla tooltip.
- [ ] 3.4 Проверить, что tooltip работает для иконок в левой колонке и в правой панели.

## 4. Безопасность trade-flow

- [x] 4.1 Переставить проверку active offer в `ShopNetworking.handleTrade(...)` до удаления предметов из инвентаря.
- [x] 4.2 Убедиться, что при отсутствии active offer сервер отправляет ошибку и не меняет инвентарь/очки команды.
- [x] 4.3 Проверить, что успешный trade по активному заказу сохраняет прежнее поведение.

## 5. Проверка качества

- [x] 5.1 Запустить `./gradlew compileJava compileClientJava` или эквивалентную сборочную проверку проекта.
- [x] 5.2 Запустить релевантные тесты, если они доступны для затронутых классов.
- [x] 5.3 Если появятся ошибки форматирования, запустить `mise run fmt` согласно правилам проекта.
- [ ] 5.4 Вручную проверить UI в игре: таймер в списке, таймер в детальной панели, hover tooltip, disabled trade после истечения timed-заказа.
