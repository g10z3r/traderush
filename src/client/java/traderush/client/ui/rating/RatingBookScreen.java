package traderush.client.ui.rating;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.MenuAccess;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import traderush.platform.ui.rating.RatingBookMenu;
import traderush.platform.ui.rating.TeamRatingBookSnapshot;
import traderush.platform.ui.rating.TeamRatingBookStatePayload;
import traderush.platform.ui.rating.TeamRatingRow;

public final class RatingBookScreen
        extends Screen
        implements MenuAccess<RatingBookMenu> {

    private static final int BOOK_COLOR = 0xFFFFF4CC;
    private static final int BOOK_EDGE_COLOR = 0xFF5F4A24;
    private static final int BOOK_SPINE_COLOR = 0x505F4A24;
    private static final int TEXT_COLOR = 0xFF000000;
    private static final int MUTED_TEXT_COLOR = 0xFF302817;
    private static final int ERROR_TEXT_COLOR = 0xFFB00020;
    private static final int HEADER_LINE_COLOR = 0xFF8B6F3E;
    private static final int ROW_STRIDE = 18;
    private static final int BUTTON_SIZE = 22;

    private static TeamRatingBookStatePayload pendingState;

    private final RatingBookMenu menu;
    private TeamRatingBookSnapshot snapshot = TeamRatingBookSnapshot.EMPTY;
    private int currentPage;
    private Button previousPageButton;
    private Button nextPageButton;

    public RatingBookScreen(
            RatingBookMenu menu,
            Inventory inventory,
            Component title
    ) {
        super(title);
        this.menu = menu;
    }

    public static void receiveState(
            Minecraft client,
            TeamRatingBookStatePayload payload
    ) {
        if (client.screen instanceof RatingBookScreen screen) {
            screen.applyState(payload);
            return;
        }

        pendingState = payload;
    }

    @Override
    public RatingBookMenu getMenu() {
        return menu;
    }

    @Override
    protected void init() {
        super.init();
        rebuildWidgets();

        if (pendingState != null) {
            TeamRatingBookStatePayload state = pendingState;
            pendingState = null;
            applyState(state);
        } else {
            clampCurrentPage(layout());
            updatePageButtons();
        }
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    @Override
    public void extractRenderState(
            GuiGraphicsExtractor graphics,
            int mouseX,
            int mouseY,
            float partialTick
    ) {
        RatingBookLayout layout = layout();
        clampCurrentPage(layout);
        drawBook(graphics, layout);

        super.extractRenderState(graphics, mouseX, mouseY, partialTick);

        drawContent(graphics, layout);
    }

    @Override
    protected void rebuildWidgets() {
        RatingBookLayout layout = layout();
        clearWidgets();

        previousPageButton = addRenderableWidget(
                Button.builder(Component.literal("<"), button -> previousPage())
                        .bounds(
                                layout.previousButtonX(),
                                layout.pageButtonY(),
                                BUTTON_SIZE,
                                BUTTON_SIZE
                        )
                        .build()
        );
        nextPageButton = addRenderableWidget(
                Button.builder(Component.literal(">"), button -> nextPage())
                        .bounds(
                                layout.nextButtonX(),
                                layout.pageButtonY(),
                                BUTTON_SIZE,
                                BUTTON_SIZE
                        )
                        .build()
        );

        updatePageButtons();
    }

    private void applyState(TeamRatingBookStatePayload payload) {
        snapshot = payload.snapshot();
        clampCurrentPage(layout());
        updatePageButtons();
    }

    private void previousPage() {
        RatingBookLayout layout = layout();
        currentPage = clamp(currentPage - 1, 0, pageCount(layout) - 1);
        updatePageButtons();
    }

    private void nextPage() {
        RatingBookLayout layout = layout();
        currentPage = clamp(currentPage + 1, 0, pageCount(layout) - 1);
        updatePageButtons();
    }

    private void updatePageButtons() {
        if (previousPageButton == null || nextPageButton == null) {
            return;
        }

        RatingBookLayout layout = layout();
        int pageCount = pageCount(layout);
        boolean hasRows = snapshot.runtimeReady() && !snapshot.rows().isEmpty();
        previousPageButton.visible = hasRows && pageCount > 1;
        nextPageButton.visible = hasRows && pageCount > 1;
        previousPageButton.active = currentPage > 0;
        nextPageButton.active = currentPage < pageCount - 1;
    }

    private void drawBook(
            GuiGraphicsExtractor graphics,
            RatingBookLayout layout
    ) {
        graphics.fill(
                layout.left(),
                layout.top(),
                layout.right(),
                layout.bottom(),
                BOOK_COLOR
        );
        graphics.outline(
                layout.left(),
                layout.top(),
                layout.width(),
                layout.height(),
                BOOK_EDGE_COLOR
        );
        graphics.fill(
                layout.left() + 6,
                layout.top() + 5,
                layout.left() + 8,
                layout.bottom() - 5,
                BOOK_SPINE_COLOR
        );
    }

    private void drawContent(
            GuiGraphicsExtractor graphics,
            RatingBookLayout layout
    ) {
        readableCenteredText(
                graphics,
                this.title,
                this.width / 2,
                layout.titleY(),
                TEXT_COLOR
        );

        if (!snapshot.runtimeReady()) {
            graphics.textWithWordWrap(
                    this.font,
                    Component.translatable(
                            "screen.trade-rush.team_rating_book.runtime_not_ready"
                    ),
                    layout.contentX(),
                    layout.emptyStateY(),
                    layout.contentWidth(),
                    ERROR_TEXT_COLOR,
                    false
            );
            return;
        }

        if (snapshot.rows().isEmpty()) {
            graphics.textWithWordWrap(
                    this.font,
                    Component.translatable(
                            "screen.trade-rush.team_rating_book.empty"
                    ),
                    layout.contentX(),
                    layout.emptyStateY(),
                    layout.contentWidth(),
                    MUTED_TEXT_COLOR,
                    false
            );
            return;
        }

        drawTable(graphics, layout);
        drawPageIndicator(graphics, layout);
    }

    private void drawTable(
            GuiGraphicsExtractor graphics,
            RatingBookLayout layout
    ) {
        drawHeader(graphics, layout);

        int rowsPerPage = rowsPerPage(layout);
        int first = currentPage * rowsPerPage;
        int last = Math.min(snapshot.rows().size(), first + rowsPerPage);

        graphics.enableScissor(
                layout.contentX(),
                layout.rowsY(),
                layout.contentX() + layout.contentWidth(),
                layout.rowsY() + layout.rowsHeight()
        );

        for (int index = first; index < last; index++) {
            TeamRatingRow row = snapshot.rows().get(index);
            int y = layout.rowsY() + (index - first) * ROW_STRIDE;
            drawRow(graphics, layout, row, y);
        }

        graphics.disableScissor();
    }

    private void drawHeader(
            GuiGraphicsExtractor graphics,
            RatingBookLayout layout
    ) {
        readableText(
                graphics,
                Component.translatable(
                        "screen.trade-rush.team_rating_book.place"
                ),
                layout.placeX(),
                layout.headerY(),
                MUTED_TEXT_COLOR
        );
        readableText(
                graphics,
                Component.translatable(
                        "screen.trade-rush.team_rating_book.team"
                ),
                layout.teamX(),
                layout.headerY(),
                MUTED_TEXT_COLOR
        );
        Component scoreHeader = Component.translatable(
                "screen.trade-rush.team_rating_book.score"
        );
        readableText(
                graphics,
                scoreHeader,
                layout.scoreRightX() - this.font.width(scoreHeader),
                layout.headerY(),
                MUTED_TEXT_COLOR
        );
        graphics.fill(
                layout.contentX(),
                layout.headerLineY(),
                layout.contentX() + layout.contentWidth(),
                layout.headerLineY() + 1,
                HEADER_LINE_COLOR
        );
    }

    private void drawRow(
            GuiGraphicsExtractor graphics,
            RatingBookLayout layout,
            TeamRatingRow row,
            int y
    ) {
        String place = Integer.toString(row.place());
        String score = Long.toString(row.score());
        String teamName = trimmedTeamName(row.teamName(), layout.teamWidth());

        readableText(graphics, place, layout.placeX(), y, TEXT_COLOR);
        readableText(graphics, teamName, layout.teamX(), y, TEXT_COLOR);
        readableText(
                graphics,
                score,
                layout.scoreRightX() - this.font.width(score),
                y,
                TEXT_COLOR
        );
    }

    private void drawPageIndicator(
            GuiGraphicsExtractor graphics,
            RatingBookLayout layout
    ) {
        Component indicator = Component.translatable(
                "screen.trade-rush.team_rating_book.page",
                currentPage + 1,
                pageCount(layout)
        );
        readableCenteredText(
                graphics,
                indicator,
                this.width / 2,
                layout.pageIndicatorY(),
                MUTED_TEXT_COLOR
        );
    }

    private void readableText(
            GuiGraphicsExtractor graphics,
            Component text,
            int x,
            int y,
            int color
    ) {
        graphics.text(this.font, text, x, y, color, false);
    }

    private void readableText(
            GuiGraphicsExtractor graphics,
            String text,
            int x,
            int y,
            int color
    ) {
        graphics.text(this.font, text, x, y, color, false);
    }

    private void readableCenteredText(
            GuiGraphicsExtractor graphics,
            Component text,
            int centerX,
            int y,
            int color
    ) {
        readableText(
                graphics,
                text,
                centerX - this.font.width(text) / 2,
                y,
                color
        );
    }

    private String trimmedTeamName(String teamName, int maxWidth) {
        if (this.font.width(teamName) <= maxWidth) {
            return teamName;
        }

        String suffix = "...";
        return this.font.plainSubstrByWidth(
                teamName,
                Math.max(1, maxWidth - this.font.width(suffix))
        ) + suffix;
    }

    private void clampCurrentPage(RatingBookLayout layout) {
        currentPage = clamp(currentPage, 0, pageCount(layout) - 1);
    }

    private int pageCount(RatingBookLayout layout) {
        if (snapshot.rows().isEmpty()) {
            return 1;
        }

        int rowsPerPage = rowsPerPage(layout);
        return Math.max(
                1,
                (snapshot.rows().size() + rowsPerPage - 1) / rowsPerPage
        );
    }

    private int rowsPerPage(RatingBookLayout layout) {
        return Math.max(1, layout.rowsHeight() / ROW_STRIDE);
    }

    private int clamp(int value, int min, int max) {
        if (value < min) {
            return min;
        }

        return Math.min(value, max);
    }

    private RatingBookLayout layout() {
        return RatingBookLayout.create(this.width, this.height);
    }

    private record RatingBookLayout(
            int left,
            int top,
            int width,
            int height
    ) {
        private static final int MAX_WIDTH = 460;
        private static final int MAX_HEIGHT = 300;
        private static final int MIN_WIDTH = 320;
        private static final int MIN_HEIGHT = 220;
        private static final int MARGIN = 12;
        private static final int CONTENT_PADDING_X = 30;
        private static final int CONTENT_PADDING_TOP = 30;
        private static final int CONTENT_PADDING_BOTTOM = 48;
        private static final int PLACE_WIDTH = 44;
        private static final int SCORE_WIDTH = 72;
        private static final int COLUMN_GAP = 12;

        private static RatingBookLayout create(
                int screenWidth,
                int screenHeight
        ) {
            int availableWidth = Math.max(MIN_WIDTH, screenWidth - MARGIN * 2);
            int availableHeight = Math
                    .max(MIN_HEIGHT, screenHeight - MARGIN * 2);
            int width = Math.min(MAX_WIDTH, availableWidth);
            int height = Math.min(MAX_HEIGHT, availableHeight);
            int left = Math.max(MARGIN, (screenWidth - width) / 2);
            int top = Math.max(MARGIN, (screenHeight - height) / 2);

            return new RatingBookLayout(left, top, width, height);
        }

        private int right() {
            return left + width;
        }

        private int bottom() {
            return top + height;
        }

        private int contentX() {
            return left + CONTENT_PADDING_X;
        }

        private int contentWidth() {
            return Math.max(1, width - CONTENT_PADDING_X * 2);
        }

        private int titleY() {
            return top + 16;
        }

        private int headerY() {
            return top + CONTENT_PADDING_TOP + 18;
        }

        private int headerLineY() {
            return headerY() + 11;
        }

        private int rowsY() {
            return headerLineY() + 6;
        }

        private int rowsHeight() {
            return Math.max(
                    ROW_STRIDE,
                    pageIndicatorY() - rowsY() - 12
            );
        }

        private int placeX() {
            return contentX();
        }

        private int teamX() {
            return placeX() + PLACE_WIDTH + COLUMN_GAP;
        }

        private int teamWidth() {
            return Math.max(
                    1,
                    scoreRightX() - SCORE_WIDTH - COLUMN_GAP - teamX()
            );
        }

        private int scoreRightX() {
            return contentX() + contentWidth();
        }

        private int emptyStateY() {
            return top + height / 2 - 12;
        }

        private int pageButtonY() {
            return bottom() - CONTENT_PADDING_BOTTOM + 10;
        }

        private int previousButtonX() {
            return contentX();
        }

        private int nextButtonX() {
            return contentX() + contentWidth() - BUTTON_SIZE;
        }

        private int pageIndicatorY() {
            return pageButtonY() + 7;
        }
    }
}
