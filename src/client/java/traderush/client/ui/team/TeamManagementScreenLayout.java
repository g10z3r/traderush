package traderush.client.ui.team;

record TeamManagementScreenLayout(
    int left,
    int top,
    int width,
    int height,
    int columnWidth,
    int actionsWidth
) {
    static final int BUTTON_HEIGHT = 20;
    static final int TEAM_ROW_STRIDE = 22;
    static final int TEXT_LINE_HEIGHT = 12;

    private static final int MAX_PANEL_WIDTH = 760;
    private static final int MAX_PANEL_HEIGHT = 320;
    private static final int MIN_PANEL_WIDTH = 300;
    private static final int MIN_PANEL_HEIGHT = 200;
    private static final int SCREEN_MARGIN = 10;
    private static final int PANEL_VERTICAL_MARGIN = 72;
    private static final int COLUMN_GAP = 8;
    private static final int COLUMN_PADDING = 6;
    private static final int CREATE_FORM_HEIGHT = 60;
    private static final int CREATE_FORM_BOTTOM_GAP = 12;

    static TeamManagementScreenLayout create(
        int screenWidth,
        int screenHeight
    ) {
        int availableWidth = Math.max(
            MIN_PANEL_WIDTH,
            screenWidth - SCREEN_MARGIN * 2
        );
        int panelWidth = Math.min(MAX_PANEL_WIDTH, availableWidth);
        int availableHeight = Math.max(
            MIN_PANEL_HEIGHT,
            screenHeight - PANEL_VERTICAL_MARGIN
        );
        int panelHeight = Math.min(MAX_PANEL_HEIGHT, availableHeight);
        int left = Math.max(SCREEN_MARGIN, (screenWidth - panelWidth) / 2);
        int top = Math.max(24, (screenHeight - panelHeight) / 2 - 4);
        int columnWidth = Math.max(
            70,
            (panelWidth - COLUMN_GAP * 2 - SCREEN_MARGIN * 2) / 3
        );
        int actionsWidth = Math.max(
            70,
            panelWidth - SCREEN_MARGIN * 2 - COLUMN_GAP * 2 - columnWidth * 2
        );

        return new TeamManagementScreenLayout(
            left,
            top,
            panelWidth,
            panelHeight,
            columnWidth,
            actionsWidth
        );
    }

    int right() {
        return left + width;
    }

    int bottom() {
        return top + height;
    }

    int columnTop() {
        return top + 22;
    }

    int columnHeight() {
        return Math.max(1, height - 36);
    }

    int headingY() {
        return top + 30;
    }

    int teamsX() {
        return left + SCREEN_MARGIN;
    }

    int detailsX() {
        return teamsX() + columnWidth + COLUMN_GAP;
    }

    int actionsX() {
        return detailsX() + columnWidth + COLUMN_GAP;
    }

    int teamsContentX() {
        return teamsX() + COLUMN_PADDING;
    }

    int detailsContentX() {
        return detailsX() + COLUMN_PADDING;
    }

    int actionsContentX() {
        return actionsX() + COLUMN_PADDING;
    }

    int contentWidth() {
        return Math.max(1, columnWidth - COLUMN_PADDING * 2);
    }

    int actionsContentWidth() {
        return Math.max(1, actionsWidth - COLUMN_PADDING * 2);
    }

    int teamLegendY() {
        return top + 43;
    }

    int teamListX() {
        return teamsContentX();
    }

    int teamListY() {
        return top + 60;
    }

    int teamListWidth() {
        return contentWidth();
    }

    int teamListHeight() {
        return Math.max(
            BUTTON_HEIGHT,
            columnTop() + columnHeight() - COLUMN_PADDING - teamListY()
        );
    }

    int detailsCurrentTeamY() {
        return top + 44;
    }

    int detailsSelectedTeamY() {
        return top + 62;
    }

    int detailsScoreY() {
        return top + 76;
    }

    int membersHeaderY() {
        return top + 94;
    }

    int membersX() {
        return detailsContentX();
    }

    int membersY() {
        return top + 108;
    }

    int membersWidth() {
        return contentWidth();
    }

    int membersHeight() {
        return Math.max(12, bottom() - 34 - membersY());
    }

    int actionViewportY() {
        return headingY() + TEXT_LINE_HEIGHT + 4;
    }

    int actionViewportHeight() {
        return Math.max(
            BUTTON_HEIGHT,
            columnTop() + columnHeight() - COLUMN_PADDING - actionViewportY()
        );
    }

    int actionSelectedTeamY() {
        return actionViewportY();
    }

    int joinLeaveButtonY() {
        return actionSelectedTeamY() + 22;
    }

    int renameLabelY() {
        return joinLeaveButtonY() + BUTTON_HEIGHT + 8;
    }

    int renameInputY() {
        return renameLabelY() + TEXT_LINE_HEIGHT;
    }

    int renameButtonsY() {
        return renameInputY() + BUTTON_HEIGHT + 4;
    }

    int emptyTeamExplanationY() {
        return renameButtonsY() + BUTTON_HEIGHT + 8;
    }

    int createSectionY() {
        return Math.max(
            emptyTeamExplanationY() + 42,
            actionViewportY() +
                actionViewportHeight() -
                CREATE_FORM_HEIGHT -
                CREATE_FORM_BOTTOM_GAP
        );
    }

    int createInputY() {
        return createSectionY() + 16;
    }

    int createButtonY() {
        return createInputY() + BUTTON_HEIGHT + 4;
    }

    int actionContentHeight() {
        return Math.max(
            actionViewportHeight(),
            createButtonY() +
                BUTTON_HEIGHT +
                CREATE_FORM_BOTTOM_GAP -
                actionViewportY()
        );
    }

    int footerX() {
        return left + SCREEN_MARGIN;
    }

    int footerY() {
        return bottom() + 5;
    }

    int refreshButtonWidth() {
        return 82;
    }

    int statusMessageX() {
        return footerX() + refreshButtonWidth() + 8;
    }

    int statusMessageY() {
        return footerY() + 5;
    }

    int statusMessageWidth() {
        return Math.max(1, right() - statusMessageX());
    }
}
