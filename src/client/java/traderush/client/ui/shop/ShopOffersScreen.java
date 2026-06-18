package traderush.client.ui.shop;

import java.util.ArrayList;
import java.util.List;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.MenuAccess;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import traderush.platform.ui.shop.ShopOfferEntry;
import traderush.platform.ui.shop.ShopOfferEntry.RequirementEntry;
import traderush.platform.ui.shop.ShopOfferEntry.UnitEntry;
import traderush.platform.ui.shop.ShopOffersMenu;
import traderush.platform.ui.shop.ShopOffersPayload;
import traderush.platform.ui.shop.ShopTradeActionPayload;
import traderush.platform.ui.shop.ShopTradeResultPayload;

public final class ShopOffersScreen
        extends Screen
        implements MenuAccess<ShopOffersMenu> {

    // ── Command-block-style palette (same dark minimal look) ─────────────────
    // Semi-transparent black — the exact fill the command block uses for text
    // areas
    private static final int C_PANEL = 0xB0000000;
    // Medium-gray thin border around panels
    private static final int C_FRAME = 0xFF3F3F3F;
    // Per-row background — slightly lighter black overlay
    private static final int C_ROW_BG = 0x60000000;
    // Row hover tint — subtle white
    private static final int C_ROW_HOVER = 0x22FFFFFF;
    // Selected row — standard MC selection blue
    private static final int C_ROW_SEL = 0x773278C0;
    private static final int C_SEL_BORDER = 0xFF3278C0;
    // Row divider
    private static final int C_DIVIDER = 0xFF3F3F3F;
    // Primary text — white
    private static final int C_TEXT = 0xFFFFFFFF;
    // Same (all text is white on dark bg)
    private static final int C_TEXT_BRIGHT = 0xFFFFFFFF;
    // Muted text — 0xFFA0A0A0, exact value from AbstractCommandBlockEditScreen
    private static final int C_MUTED = 0xFFA0A0A0;
    // Reward — gold/amber (XP-bar colour, clearly readable on dark)
    private static final int C_REWARD = 0xFFFFAA00;
    // Feedback
    private static final int C_SUCCESS = 0xFF55FF55;
    private static final int C_ERROR = 0xFFFF5555;
    // Type badge colours
    private static final int C_TIMED = 0xFF55DDFF;
    private static final int C_LIMITED = 0xFFFF8855;
    // Item slot — near black with gray border
    private static final int C_SLOT_BG = 0xFF0D0D0D;
    private static final int C_SLOT_BORDER = 0xFF3F3F3F;
    // Scrollbar
    private static final int C_SCROLL_TRACK = 0xFF1A1A1A;
    private static final int C_SCROLL_THUMB = 0xFF555555;

    // ── Layout
    // ────────────────────────────────────────────────────────────────
    private static final int PAD = 8;
    /** Row height: 16px icon + 4 top pad + 4 bottom pad + 12 for name line. */
    private static final int ROW_H = 40;
    private static final int LIST_W = 200;
    private static final int FOOTER_H = 32;
    private static final int TITLE_H = 20;
    private static final int SCROLLBAR_W = 4;
    private static final int ICON_SLOT = 18;
    /** How long (ticks) the trade-result message is shown — 1 second. */
    private static final long MESSAGE_TICKS = 20L;
    private static final long TICKS_PER_SECOND = 20L;
    private static final long HOUR_TICKS = 60L * 60L * TICKS_PER_SECOND;

    private static ShopOffersPayload pendingPayload;

    private final ShopOffersMenu menu;
    private final List<Button> rowButtons = new ArrayList<>();

    private String shopId = "";
    private String shopName = "";
    private List<ShopOfferEntry> offers = List.of();
    private long serverTickAtSnapshot = 0L;
    private long clientTickAtSnapshot = 0L;
    private int selectedIndex = -1;
    private int scrollOffset = 0;
    private String resultMessage = "";
    private boolean resultSuccess = false;
    private long resultTick = Long.MIN_VALUE;

    private Button tradeButton;

    public ShopOffersScreen(
            ShopOffersMenu menu,
            Inventory inventory,
            Component title
    ) {
        super(title);
        this.menu = menu;
    }

    // ── static receivers
    // ──────────────────────────────────────────────────────

    public static void receiveOffers(
            Minecraft client,
            ShopOffersPayload payload
    ) {
        if (client.screen instanceof ShopOffersScreen screen) {
            screen.applyPayload(payload);
            return;
        }

        pendingPayload = payload;
    }

    public static void receiveTradeResult(
            Minecraft client,
            ShopTradeResultPayload payload
    ) {
        if (client.screen instanceof ShopOffersScreen screen) {
            screen.applyTradeResult(payload);
        }
    }

    // ── lifecycle
    // ─────────────────────────────────────────────────────────────

    @Override
    protected void init() {
        super.init();
        rebuildWidgets();

        if (pendingPayload != null) {
            applyPayload(pendingPayload);
            pendingPayload = null;
        }
    }

    @Override
    protected void rebuildWidgets() {
        clearWidgets();
        rowButtons.clear();

        int lx = listX();
        int ly = listY();
        int lh = listH();
        int maxRows = lh / ROW_H;

        for (int i = 0; i < maxRows; i++) {
            int rowIndex = i;
            Button btn = Button
                    .builder(Component.empty(), b -> selectRow(rowIndex))
                    .bounds(lx + 1, ly + i * ROW_H, LIST_W - 2, ROW_H)
                    .build();
            rowButtons.add(addRenderableWidget(btn));
        }

        int btnY = height - PAD - 20;
        int doneX = PAD + (LIST_W - 100) / 2;
        int tradeX = detailX() + PAD;

        addRenderableWidget(
                Button.builder(
                        Component.translatable("gui.done"),
                        b -> onClose()
                ).bounds(doneX, btnY, 100, 20).build()
        );

        tradeButton = addRenderableWidget(
                Button.builder(
                        Component.literal("Trade"),
                        b -> sendTrade()
                ).bounds(tradeX, btnY, 80, 20).build()
        );

        syncRowButtons();
        updateTradeButton();
    }

    private void applyPayload(ShopOffersPayload payload) {
        this.shopId = payload.shopId();
        this.shopName = payload.shopName();
        this.offers = payload.offers();
        this.serverTickAtSnapshot = payload.serverTick();
        this.clientTickAtSnapshot = gameTime();
        this.selectedIndex = offers.isEmpty() ? -1 : 0;
        this.scrollOffset = 0;
        syncRowButtons();
        updateTradeButton();
    }

    private void applyTradeResult(ShopTradeResultPayload payload) {
        this.resultSuccess = payload.success();
        this.resultMessage = payload.success()
                ? "+" + payload.pointsAwarded() + " pts for your team!"
                : payload.message();
        // Reset timer — replaces any previous message immediately
        this.resultTick = gameTime();
    }

    private void selectRow(int visibleIndex) {
        int idx = visibleIndex + scrollOffset;

        if (idx >= 0 && idx < offers.size()) {
            selectedIndex = idx;
            updateTradeButton();
        }
    }

    private void sendTrade() {
        if (selectedIndex < 0 || selectedIndex >= offers.size()) {
            return;
        }

        ShopOfferEntry entry = offers.get(selectedIndex);

        if (isExpiredTimedOffer(entry)) {
            return;
        }

        ClientPlayNetworking.send(
                new ShopTradeActionPayload(
                        shopId,
                        entry.id(),
                        entry.fixedReward()
                )
        );
    }

    /**
     * Hide/show row buttons to match the current offers list size. Called after
     * offers change or scroll offset changes.
     */
    private void syncRowButtons() {
        for (int i = 0; i < rowButtons.size(); i++) {
            int offerIdx = i + scrollOffset;
            boolean exists = offerIdx < offers.size();
            rowButtons.get(i).visible = exists;
            rowButtons.get(i).active = exists;
        }
    }

    private void updateTradeButton() {
        if (tradeButton == null) {
            return;
        }

        boolean hasOffer = selectedIndex >= 0 && selectedIndex < offers.size();

        if (!hasOffer) {
            tradeButton.active = false;
            return;
        }

        ShopOfferEntry entry = offers.get(selectedIndex);
        tradeButton.active = !isExpiredTimedOffer(entry)
                && playerHasItems(entry);
    }

    private boolean playerHasItems(ShopOfferEntry entry) {
        if (minecraft == null || minecraft.player == null) {
            return false;
        }

        for (UnitEntry unit : entry.units()) {
            for (RequirementEntry req : unit.requirements()) {
                Item item = resolveClientItem(req.itemId());

                if (item == null) {
                    return false;
                }

                if (countClientItems(item) < req.quantity()) {
                    return false;
                }
            }
        }

        return true;
    }

    private int countClientItems(Item item) {
        if (minecraft == null || minecraft.player == null) {
            return 0;
        }

        int total = 0;
        var inv = minecraft.player.getInventory();

        for (int i = 0; i < inv.getContainerSize(); i++) {
            ItemStack stack = inv.getItem(i);

            if (stack.getItem() == item) {
                total += stack.getCount();
            }
        }

        return total;
    }

    private static Item resolveClientItem(String itemId) {
        try {
            return BuiltInRegistries.ITEM.getValue(Identifier.parse(itemId));
        } catch (Exception ignored) {
            return null;
        }
    }

    // ── rendering
    // ─────────────────────────────────────────────────────────────

    @Override
    public void extractRenderState(
            GuiGraphicsExtractor graphics,
            int mouseX,
            int mouseY,
            float partialTick
    ) {
        // Vanilla dark tiled stone background — identical to every MC screen
        extractMenuBackground(graphics);
        drawPanels(graphics);
        updateTradeButton();

        super.extractRenderState(graphics, mouseX, mouseY, partialTick);

        drawTitle(graphics);
        drawListRows(graphics, mouseX, mouseY);
        drawDetailPanel(graphics, mouseX, mouseY);
        drawResultMessage(graphics);
    }

    // ── panels
    // ────────────────────────────────────────────────────────────────

    private void drawPanels(GuiGraphicsExtractor graphics) {
        int lx = listX();
        int ly = listY();
        int lh = listH();

        graphics.fill(lx, ly, lx + LIST_W, ly + lh, C_PANEL);
        graphics.outline(lx, ly, LIST_W, lh, C_FRAME);

        int dx = detailX();
        int dw = detailW();

        if (dw > 40) {
            graphics.fill(dx, ly, dx + dw, ly + lh, C_PANEL);
            graphics.outline(dx, ly, dw, lh, C_FRAME);
        }
    }

    private void drawTitle(GuiGraphicsExtractor graphics) {
        String label = shopName.isEmpty() ? "Shop" : shopName;
        graphics.centeredText(
                font,
                Component.literal(label),
                width / 2,
                PAD,
                0xFFFFFFFF
        );
    }

    // ── offer list
    // ────────────────────────────────────────────────────────────

    private void drawListRows(
            GuiGraphicsExtractor graphics,
            int mouseX,
            int mouseY
    ) {
        int lx = listX();
        int ly = listY();
        int lh = listH();
        int maxRows = lh / ROW_H;

        if (offers.isEmpty()) {
            graphics.centeredText(
                    font,
                    Component.literal("No offers"),
                    lx + LIST_W / 2,
                    ly + lh / 2,
                    C_MUTED
            );
            return;
        }

        for (int i = 0; i < maxRows; i++) {
            int idx = i + scrollOffset;

            if (idx >= offers.size()) {
                break;
            }

            ShopOfferEntry entry = offers.get(idx);
            int rowY = ly + i * ROW_H;
            boolean selected = idx == selectedIndex;
            boolean hovered = mouseX >= lx
                    && mouseX < lx + LIST_W
                    && mouseY >= rowY
                    && mouseY < rowY + ROW_H;

            // Per-row darker background (slot style)
            graphics.fill(
                    lx + 1,
                    rowY,
                    lx + LIST_W - 1,
                    rowY + ROW_H,
                    C_ROW_BG
            );

            if (selected) {
                graphics.fill(
                        lx + 1,
                        rowY,
                        lx + LIST_W - 1,
                        rowY + ROW_H,
                        C_ROW_SEL
                );
                graphics.outline(
                        lx + 1,
                        rowY,
                        LIST_W - 2,
                        ROW_H,
                        C_SEL_BORDER
                );
            } else if (hovered) {
                graphics.fill(
                        lx + 1,
                        rowY,
                        lx + LIST_W - 1,
                        rowY + ROW_H,
                        C_ROW_HOVER
                );
            }

            if (i > 0) {
                graphics.fill(
                        lx + 2,
                        rowY,
                        lx + LIST_W - 2,
                        rowY + 1,
                        C_DIVIDER
                );
            }

            drawRowContent(graphics, entry, lx, rowY, mouseX, mouseY);
        }

        drawScrollbar(graphics);
    }

    /**
     * Draws one offer row. Left: small type badge + offer name (top), then item
     * icons (bottom). Right: points value (vertically centred).
     */
    private void drawRowContent(
            GuiGraphicsExtractor graphics,
            ShopOfferEntry entry,
            int lx,
            int rowY,
            int mouseX,
            int mouseY
    ) {
        boolean timed = "TIMED".equals(entry.kind().name());
        int kindColor = timed ? C_TIMED : C_LIMITED;
        String kindLabel = timed ? "T" : "L";

        // ── type badge (8 × 8, top-left) ─────────────────────────────────────
        int bx = lx + 3;
        int by = rowY + 3;
        graphics.fill(bx, by, bx + 8, by + 8, kindColor);
        int labelWidth = font.width(kindLabel);
        graphics.text(
                font,
                Component.literal(kindLabel),
                bx + 4 - labelWidth / 2,
                by + 1,
                0xFF000000,
                false
        );

        // ── offer name (right of badge, top line)
        // ─────────────────────────────
        int nameX = lx + 3 + 8 + 4;
        graphics.text(
                font,
                Component.literal(entry.name()),
                nameX,
                rowY + 3,
                C_TEXT_BRIGHT
        );

        // ── item icons (bottom part of row) ──────────────────────────────────
        List<RequirementEntry> reqs = firstUnitRequirements(entry);
        int iconX = lx + 4;
        int iconY = rowY + ROW_H - ICON_SLOT - 3;
        int iconLimitX = lx + LIST_W - 56;

        for (RequirementEntry req : reqs) {
            Item item = resolveClientItem(req.itemId());

            if (item != null) {
                ItemStack stack = new ItemStack(item, req.quantity());
                drawItemIcon(graphics, stack, iconX, iconY, mouseX, mouseY);
            }

            iconX += ICON_SLOT + 2;

            // Don't draw past the points/countdown area
            if (iconX + ICON_SLOT > iconLimitX) {
                break;
            }
        }

        // ── points + timed countdown (right-aligned) ─────────────────────────
        boolean hasCountdown = isTimedEntry(entry);
        String pts = entry.fixedReward() + " pts";
        int ptsX = lx + LIST_W - PAD - font.width(pts);
        int ptsY = hasCountdown ? rowY + 3 : rowY + (ROW_H - 8) / 2;
        graphics.text(font, Component.literal(pts), ptsX, ptsY, C_REWARD);

        if (hasCountdown) {
            long remainingTicks = remainingTicks(entry);
            String countdown = formatCountdown(remainingTicks);
            int countdownX = lx + LIST_W - PAD - font.width(countdown);
            int countdownY = rowY + 15;
            int countdownColor = remainingTicks <= 0L ? C_ERROR : C_TIMED;
            graphics.text(
                    font,
                    Component.literal(countdown),
                    countdownX,
                    countdownY,
                    countdownColor
            );
        }
    }

    private static List<RequirementEntry> firstUnitRequirements(
            ShopOfferEntry entry
    ) {
        if (entry.units().isEmpty()) {
            return List.of();
        }

        return entry.units().get(0).requirements();
    }

    private static boolean isTimedEntry(ShopOfferEntry entry) {
        return "TIMED".equals(entry.kind().name())
                && entry.expiresAtTick() >= 0L;
    }

    private boolean isExpiredTimedOffer(ShopOfferEntry entry) {
        return isTimedEntry(entry) && remainingTicks(entry) <= 0L;
    }

    private long remainingTicks(ShopOfferEntry entry) {
        if (!isTimedEntry(entry)) {
            return 0L;
        }

        return Math.max(0L, entry.expiresAtTick() - estimatedServerTick());
    }

    private long estimatedServerTick() {
        long elapsedClientTicks = Math
                .max(0L, gameTime() - clientTickAtSnapshot);
        return serverTickAtSnapshot + elapsedClientTicks;
    }

    private static String formatCountdown(long ticks) {
        if (ticks <= 0L) {
            return "Expired";
        }

        long totalSeconds = (ticks + TICKS_PER_SECOND - 1L) / TICKS_PER_SECOND;

        if (ticks >= HOUR_TICKS) {
            long hours = totalSeconds / 3600L;
            long minutes = totalSeconds % 3600L / 60L;
            long seconds = totalSeconds % 60L;
            return hours + ":" + twoDigits(minutes) + ":" + twoDigits(seconds);
        }

        long minutes = totalSeconds / 60L;
        long seconds = totalSeconds % 60L;
        return minutes + ":" + twoDigits(seconds);
    }

    private static String twoDigits(long value) {
        return value < 10L ? "0" + value : Long.toString(value);
    }

    private void drawScrollbar(GuiGraphicsExtractor graphics) {
        int lx = listX();
        int ly = listY();
        int lh = listH();
        int visibleRows = lh / ROW_H;

        if (offers.size() <= visibleRows) {
            return;
        }

        int maxScroll = offers.size() - visibleRows;
        int tx = lx + LIST_W - SCROLLBAR_W - 1;
        graphics.fill(tx, ly, tx + SCROLLBAR_W, ly + lh, C_SCROLL_TRACK);

        int thumbH = Math.max(12, lh * visibleRows / offers.size());
        int thumbY = ly + (lh - thumbH) * scrollOffset / maxScroll;
        graphics.fill(
                tx,
                thumbY,
                tx + SCROLLBAR_W,
                thumbY + thumbH,
                C_SCROLL_THUMB
        );
    }

    // ── detail panel
    // ──────────────────────────────────────────────────────────

    private void drawDetailPanel(
            GuiGraphicsExtractor graphics,
            int mouseX,
            int mouseY
    ) {
        int dx = detailX();
        int dw = detailW();

        if (dw < 60 || selectedIndex < 0 || selectedIndex >= offers.size()) {
            if (dw >= 60) {
                graphics.centeredText(
                        font,
                        Component.literal("Select an offer"),
                        dx + dw / 2,
                        listY() + listH() / 2,
                        C_MUTED
                );
            }

            return;
        }

        ShopOfferEntry entry = offers.get(selectedIndex);
        int cx = dx + PAD;
        int y = listY() + PAD;

        boolean timed = "TIMED".equals(entry.kind().name());
        int kindColor = timed ? C_TIMED : C_LIMITED;
        String kindLabel = timed ? "TIMED OFFER" : "LIMITED OFFER";

        graphics.text(font, Component.literal(kindLabel), cx, y, kindColor);
        y += 13;

        graphics.text(font, Component.literal(entry.name()), cx, y, C_TEXT);
        y += 13;

        graphics.textWithWordWrap(
                font,
                Component.literal(entry.description()),
                cx,
                y,
                dw - PAD * 2,
                C_MUTED
        );
        y += 22;

        graphics.fill(cx, y, dx + dw - PAD, y + 1, C_DIVIDER);
        y += 8;

        graphics.text(font, Component.literal("REWARD"), cx, y, C_MUTED);
        y += 11;

        graphics.text(
                font,
                Component.literal(entry.fixedReward() + " pts"),
                cx,
                y,
                C_REWARD
        );
        y += 17;

        if (isTimedEntry(entry)) {
            graphics.text(font, Component.literal("TIME LEFT"), cx, y, C_MUTED);
            y += 11;

            long remainingTicks = remainingTicks(entry);
            int countdownColor = remainingTicks <= 0L ? C_ERROR : C_TIMED;
            graphics.text(
                    font,
                    Component.literal(formatCountdown(remainingTicks)),
                    cx,
                    y,
                    countdownColor
            );
            y += 17;
        }

        graphics.fill(cx, y, dx + dw - PAD, y + 1, C_DIVIDER);
        y += 8;

        graphics.text(
                font,
                Component.literal("REQUIRES PER UNIT"),
                cx,
                y,
                C_MUTED
        );
        y += 13;

        drawItemIcons(graphics, entry, cx, y, dw - PAD * 2, mouseX, mouseY);
    }

    private void drawItemIcons(
            GuiGraphicsExtractor graphics,
            ShopOfferEntry entry,
            int startX,
            int startY,
            int maxW,
            int mouseX,
            int mouseY
    ) {
        int x = startX;
        int y = startY;
        int gap = 2;

        for (UnitEntry unit : entry.units()) {
            for (RequirementEntry req : unit.requirements()) {
                if (x + ICON_SLOT > startX + maxW && x > startX) {
                    x = startX;
                    y += ICON_SLOT + gap;
                }

                graphics.fill(
                        x - 1,
                        y - 1,
                        x + ICON_SLOT + 1,
                        y + ICON_SLOT + 1,
                        C_SLOT_BORDER
                );
                graphics.fill(x, y, x + ICON_SLOT, y + ICON_SLOT, C_SLOT_BG);

                Item item = resolveClientItem(req.itemId());

                if (item != null) {
                    ItemStack stack = new ItemStack(item, req.quantity());
                    drawItemIcon(graphics, stack, x + 1, y + 1, mouseX, mouseY);
                }

                x += ICON_SLOT + gap;
            }
        }
    }

    private void drawItemIcon(
            GuiGraphicsExtractor graphics,
            ItemStack stack,
            int x,
            int y,
            int mouseX,
            int mouseY
    ) {
        graphics.item(stack, x, y);
        graphics.itemDecorations(font, stack, x, y);

        if (mouseX >= x && mouseX < x + 16 && mouseY >= y && mouseY < y + 16) {
            graphics.setTooltipForNextFrame(font, stack, mouseX, mouseY);
        }
    }

    // ── result message
    // ────────────────────────────────────────────────────────

    private void drawResultMessage(GuiGraphicsExtractor graphics) {
        if (resultMessage.isEmpty()) {
            return;
        }

        long age = gameTime() - resultTick;

        if (age > MESSAGE_TICKS) {
            resultMessage = "";
            return;
        }

        int color = resultSuccess ? C_SUCCESS : C_ERROR;
        int dx = detailX();
        int dw = detailW();
        graphics.centeredText(
                font,
                Component.literal(resultMessage),
                dx + dw / 2,
                height - FOOTER_H - 14,
                color
        );
    }

    // ── scroll
    // ────────────────────────────────────────────────────────────────

    @Override
    public boolean mouseScrolled(
            double mouseX,
            double mouseY,
            double horizontalAmount,
            double verticalAmount
    ) {
        if (verticalAmount == 0.0) {
            return false;
        }

        int lx = listX();
        int ly = listY();

        if (mouseX >= lx
                && mouseX < lx + LIST_W
                && mouseY >= ly
                && mouseY < ly + listH()) {
            int visibleRows = listH() / ROW_H;
            int maxScroll = Math.max(0, offers.size() - visibleRows);
            int prev = scrollOffset;
            scrollOffset = clamp(
                    scrollOffset + (verticalAmount > 0.0 ? -1 : 1),
                    0,
                    maxScroll
            );

            if (scrollOffset != prev) {
                syncRowButtons();
            }

            return true;
        }

        return super.mouseScrolled(
                mouseX,
                mouseY,
                horizontalAmount,
                verticalAmount
        );
    }

    // ── helpers
    // ───────────────────────────────────────────────────────────────

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    @Override
    public ShopOffersMenu getMenu() {
        return menu;
    }

    private int listX() {
        return PAD;
    }

    private int listY() {
        return TITLE_H + PAD;
    }

    private int listH() {
        return height - listY() - FOOTER_H - PAD;
    }

    private int detailX() {
        return PAD + LIST_W + PAD;
    }

    private int detailW() {
        return width - detailX() - PAD;
    }

    private long gameTime() {
        if (minecraft == null || minecraft.level == null) {
            return 0L;
        }

        return minecraft.level.getGameTime();
    }

    private static int clamp(int v, int min, int max) {
        return Math.max(min, Math.min(max, v));
    }
}
