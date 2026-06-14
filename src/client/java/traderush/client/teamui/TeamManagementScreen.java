package traderush.client.teamui;

import static traderush.client.teamui.TeamManagementScreenLayout.BUTTON_HEIGHT;
import static traderush.client.teamui.TeamManagementScreenLayout.TEAM_ROW_STRIDE;
import static traderush.client.teamui.TeamManagementScreenLayout.TEXT_LINE_HEIGHT;

import java.util.ArrayList;
import java.util.List;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.MenuAccess;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import traderush.game.team.TeamService;
import traderush.platform.teamui.TeamManagementActionPayload;
import traderush.platform.teamui.TeamManagementActionPayload.Action;
import traderush.platform.teamui.TeamManagementMenu;
import traderush.platform.teamui.TeamManagementSnapshot;
import traderush.platform.teamui.TeamManagementSnapshot.MemberEntry;
import traderush.platform.teamui.TeamManagementSnapshot.TeamRow;
import traderush.platform.teamui.TeamManagementStatePayload;

public final class TeamManagementScreen
    extends Screen
    implements MenuAccess<TeamManagementMenu>
{

    private static final int BACKGROUND_COLOR = 0xC0101010;
    private static final int PANEL_COLOR = 0x80202020;
    private static final int PANEL_BORDER_COLOR = 0xFF707070;
    private static final int TEXT_COLOR = 0xFFFFFFFF;
    private static final int MUTED_TEXT_COLOR = 0xFFB0B0B0;
    private static final int SUCCESS_TEXT_COLOR = 0xFF70FF90;
    private static final int ERROR_TEXT_COLOR = 0xFFFF7070;
    private static final int SELECTED_COLOR = 0xFF6FA8FF;
    private static final int CURRENT_TEAM_COLOR = 0xFF70FF90;
    private static final int SCROLL_TRACK_COLOR = 0x80303030;
    private static final int SCROLL_THUMB_COLOR = 0xFFB0B0B0;

    private static TeamManagementStatePayload pendingState;

    private final TeamManagementMenu menu;
    private final List<Button> teamButtons = new ArrayList<>();

    private TeamManagementSnapshot snapshot = TeamManagementSnapshot.EMPTY;
    private String selectedTeamId = "";
    private Component message = Component.empty();
    private boolean errorMessage;
    private int teamScrollOffset;
    private int memberScrollOffset;
    private int actionScrollOffset;
    private EditBox createNameInput;
    private EditBox renameNameInput;
    private PendingTextInputClear pendingTextInputClear;
    private Button createButton;
    private Button joinButton;
    private Button leaveButton;
    private Button deleteButton;
    private Button renameButton;

    public TeamManagementScreen(
        TeamManagementMenu menu,
        Inventory inventory,
        Component title
    ) {
        super(title);
        this.menu = menu;
    }

    public static void receiveState(
        Minecraft client,
        TeamManagementStatePayload payload
    ) {
        if (client.screen instanceof TeamManagementScreen screen) {
            screen.applyState(payload);
            return;
        }

        pendingState = payload;
    }

    @Override
    public TeamManagementMenu getMenu() {
        return menu;
    }

    @Override
    protected void init() {
        super.init();
        rebuildWidgets();

        if (pendingState != null) {
            TeamManagementStatePayload state = pendingState;
            pendingState = null;
            applyState(state);
        } else {
            sendRefresh();
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
        TeamManagementScreenLayout layout = layout();
        drawPanels(graphics, layout);

        super.extractRenderState(graphics, mouseX, mouseY, partialTick);

        drawLabelsAndLists(graphics, layout);
    }

    @Override
    public boolean mouseScrolled(
        double mouseX,
        double mouseY,
        double horizontalAmount,
        double verticalAmount
    ) {
        if (verticalAmount == 0.0D) {
            return false;
        }

        TeamManagementScreenLayout layout = layout();
        int direction = verticalAmount > 0.0D ? -1 : 1;

        if (
            contains(
                layout.teamListX(),
                layout.teamListY(),
                layout.teamListWidth(),
                layout.teamListHeight(),
                mouseX,
                mouseY
            )
        ) {
            int previousOffset = teamScrollOffset;
            teamScrollOffset = clamp(
                teamScrollOffset + direction,
                0,
                maxTeamScrollOffset(layout)
            );

            if (teamScrollOffset != previousOffset) {
                updateWidgetsFromSnapshot();
            }

            return true;
        }

        if (
            contains(
                layout.membersX(),
                layout.membersY(),
                layout.membersWidth(),
                layout.membersHeight(),
                mouseX,
                mouseY
            )
        ) {
            memberScrollOffset = clamp(
                memberScrollOffset + direction,
                0,
                maxMemberScrollOffset(layout)
            );
            return true;
        }

        if (
            contains(
                layout.actionsContentX(),
                layout.actionViewportY(),
                layout.actionsContentWidth(),
                layout.actionViewportHeight(),
                mouseX,
                mouseY
            )
        ) {
            int previousOffset = actionScrollOffset;
            actionScrollOffset = clamp(
                actionScrollOffset + direction * TEXT_LINE_HEIGHT,
                0,
                maxActionScrollOffset(layout)
            );

            if (actionScrollOffset != previousOffset) {
                rebuildWidgets();
            }

            return true;
        }

        return false;
    }

    @Override
    protected void rebuildWidgets() {
        String createValue =
            createNameInput == null ? "" : createNameInput.getValue();
        String renameValue =
            renameNameInput == null ? "" : renameNameInput.getValue();
        TeamManagementScreenLayout layout = layout();

        clearWidgets();
        teamButtons.clear();

        int visibleTeamRows = visibleTeamRows(layout);
        actionScrollOffset = clamp(
            actionScrollOffset,
            0,
            maxActionScrollOffset(layout)
        );

        for (int index = 0; index < visibleTeamRows; index++) {
            int visibleIndex = index;
            Button button = Button.builder(Component.literal("-"), clicked ->
                selectVisibleRow(visibleIndex)
            )
                .bounds(
                    layout.teamListX(),
                    layout.teamListY() + index * TEAM_ROW_STRIDE,
                    layout.teamListWidth(),
                    BUTTON_HEIGHT
                )
                .build();
            teamButtons.add(addRenderableWidget(button));
        }

        addRenderableWidget(
            Button.builder(
                Component.translatable(
                    "screen.trade-rush.team_management.refresh"
                ),
                button -> sendRefresh()
            )
                .bounds(
                    layout.footerX(),
                    layout.footerY(),
                    layout.refreshButtonWidth(),
                    BUTTON_HEIGHT
                )
                .build()
        );

        joinButton = addRenderableWidget(
            Button.builder(
                Component.translatable(
                    "screen.trade-rush.team_management.join"
                ),
                button -> sendAction(Action.JOIN, selectedTeamId, "")
            )
                .bounds(
                    layout.actionsContentX(),
                    layout.joinLeaveButtonY() - actionScrollOffset,
                    layout.actionsContentWidth(),
                    BUTTON_HEIGHT
                )
                .build()
        );

        leaveButton = addRenderableWidget(
            Button.builder(
                Component.translatable(
                    "screen.trade-rush.team_management.leave"
                ),
                button -> sendAction(Action.LEAVE, selectedTeamId, "")
            )
                .bounds(
                    layout.actionsContentX(),
                    layout.joinLeaveButtonY() - actionScrollOffset,
                    layout.actionsContentWidth(),
                    BUTTON_HEIGHT
                )
                .build()
        );

        renameNameInput = new EditBox(
            this.font,
            layout.actionsContentX(),
            layout.renameInputY() - actionScrollOffset,
            layout.actionsContentWidth(),
            BUTTON_HEIGHT,
            Component.translatable(
                "screen.trade-rush.team_management.rename_input"
            )
        );
        renameNameInput.setMaxLength(TeamService.MAX_TEAM_NAME_LENGTH);
        renameNameInput.setHint(
            Component.translatable(
                "screen.trade-rush.team_management.rename_hint"
            )
        );
        renameNameInput.setValue(renameValue);
        renameNameInput.setResponder(value -> updateActionButtons());
        addRenderableWidget(renameNameInput);

        int editButtonWidth = Math.max(
            30,
            (layout.actionsContentWidth() - 4) / 2
        );
        renameButton = addRenderableWidget(
            Button.builder(
                Component.translatable(
                    "screen.trade-rush.team_management.rename"
                ),
                button ->
                    sendAction(
                        Action.RENAME_EMPTY,
                        selectedTeamId,
                        renameNameInput.getValue()
                    )
            )
                .bounds(
                    layout.actionsContentX(),
                    layout.renameButtonsY() - actionScrollOffset,
                    editButtonWidth,
                    BUTTON_HEIGHT
                )
                .build()
        );

        deleteButton = addRenderableWidget(
            Button.builder(
                Component.translatable(
                    "screen.trade-rush.team_management.delete"
                ),
                button -> sendAction(Action.DELETE_EMPTY, selectedTeamId, "")
            )
                .bounds(
                    layout.actionsContentX() + editButtonWidth + 4,
                    layout.renameButtonsY() - actionScrollOffset,
                    layout.actionsContentWidth() - editButtonWidth - 4,
                    BUTTON_HEIGHT
                )
                .build()
        );

        createNameInput = new EditBox(
            this.font,
            layout.actionsContentX(),
            layout.createInputY() - actionScrollOffset,
            layout.actionsContentWidth(),
            BUTTON_HEIGHT,
            Component.translatable(
                "screen.trade-rush.team_management.name_input"
            )
        );
        createNameInput.setMaxLength(TeamService.MAX_TEAM_NAME_LENGTH);
        createNameInput.setHint(
            Component.translatable(
                "screen.trade-rush.team_management.name_hint"
            )
        );
        createNameInput.setValue(createValue);
        createNameInput.setResponder(value -> updateActionButtons());
        addRenderableWidget(createNameInput);

        createButton = addRenderableWidget(
            Button.builder(
                Component.translatable(
                    "screen.trade-rush.team_management.create"
                ),
                button ->
                    sendAction(Action.CREATE, "", createNameInput.getValue())
            )
                .bounds(
                    layout.actionsContentX(),
                    layout.createButtonY() - actionScrollOffset,
                    layout.actionsContentWidth(),
                    BUTTON_HEIGHT
                )
                .build()
        );

        updateWidgetsFromSnapshot();
    }

    private void applyState(TeamManagementStatePayload payload) {
        String previousSelectedTeamId = selectedTeamId;
        snapshot = payload.snapshot();
        selectedTeamId = resolvedSelectedTeamId();
        message = payload.message();
        errorMessage = payload.error();

        clearSubmittedTextInputAfterSuccess(payload);

        if (!selectedTeamId.equals(previousSelectedTeamId)) {
            memberScrollOffset = 0;
        }

        scrollSelectedTeamIntoView(layout());
        updateWidgetsFromSnapshot();
    }

    private void clearSubmittedTextInputAfterSuccess(
        TeamManagementStatePayload payload
    ) {
        PendingTextInputClear pendingClear = pendingTextInputClear;
        pendingTextInputClear = null;

        if (
            pendingClear == null ||
            payload.error() ||
            payload.message().getString().isBlank()
        ) {
            return;
        }

        if (
            pendingClear.action() == Action.CREATE &&
            createNameInput != null &&
            createNameInput.getValue().equals(pendingClear.value())
        ) {
            createNameInput.setValue("");
        } else if (
            pendingClear.action() == Action.RENAME_EMPTY &&
            renameNameInput != null &&
            renameNameInput.getValue().equals(pendingClear.value())
        ) {
            renameNameInput.setValue("");
        }
    }

    private void updateWidgetsFromSnapshot() {
        if (!containsTeam(selectedTeamId)) {
            selectedTeamId = resolvedSelectedTeamId();
        }

        TeamManagementScreenLayout layout = layout();
        teamScrollOffset = clamp(
            teamScrollOffset,
            0,
            maxTeamScrollOffset(layout)
        );
        memberScrollOffset = clamp(
            memberScrollOffset,
            0,
            maxMemberScrollOffset(layout)
        );

        for (int index = 0; index < teamButtons.size(); index++) {
            Button button = teamButtons.get(index);
            int teamIndex = teamScrollOffset + index;

            if (teamIndex >= snapshot.teams().size()) {
                button.visible = false;
                button.active = false;
                continue;
            }

            TeamRow team = snapshot.teams().get(teamIndex);
            button.visible = true;
            button.active = true;
            button.setMessage(
                Component.literal(
                    TeamManagementScreenText.teamButtonLabel(
                        team,
                        selectedTeamId,
                        snapshot.currentTeamId()
                    )
                )
            );
        }

        updateActionButtons();
    }

    private void updateActionButtons() {
        TeamRow selected = selectedTeam();
        boolean hasSelectedTeam = selected != null;
        boolean selectedTeamIsEmpty =
            hasSelectedTeam && selected.memberCount() == 0;
        boolean hasCreateName =
            createNameInput != null &&
            !createNameInput.getValue().trim().isEmpty();
        boolean hasRenameName =
            renameNameInput != null &&
            !renameNameInput.getValue().trim().isEmpty();
        boolean selectedIsCurrent =
            hasSelectedTeam && selected.id().equals(snapshot.currentTeamId());
        TeamManagementScreenLayout layout = layout();
        boolean joinLeaveVisible = isActionWidgetInViewport(
            layout.joinLeaveButtonY() - actionScrollOffset,
            BUTTON_HEIGHT,
            layout
        );
        boolean renameInputVisible = isActionWidgetInViewport(
            layout.renameInputY() - actionScrollOffset,
            BUTTON_HEIGHT,
            layout
        );
        boolean renameButtonsVisible = isActionWidgetInViewport(
            layout.renameButtonsY() - actionScrollOffset,
            BUTTON_HEIGHT,
            layout
        );
        boolean createInputVisible = isActionWidgetInViewport(
            layout.createInputY() - actionScrollOffset,
            BUTTON_HEIGHT,
            layout
        );
        boolean createButtonVisible = isActionWidgetInViewport(
            layout.createButtonY() - actionScrollOffset,
            BUTTON_HEIGHT,
            layout
        );

        if (createNameInput != null) {
            createNameInput.visible = createInputVisible;
            createNameInput.active = createInputVisible;
            createNameInput.setEditable(createInputVisible);
        }

        if (createButton != null) {
            createButton.visible = createButtonVisible;
            createButton.active = createButtonVisible && hasCreateName;
        }

        if (renameNameInput != null) {
            renameNameInput.visible = renameInputVisible;
            renameNameInput.active = renameInputVisible && selectedTeamIsEmpty;
            renameNameInput.setEditable(
                renameInputVisible && selectedTeamIsEmpty
            );
        }

        if (renameButton != null) {
            renameButton.visible = renameButtonsVisible;
            renameButton.active =
                renameButtonsVisible && hasRenameName && selectedTeamIsEmpty;
        }

        if (deleteButton != null) {
            deleteButton.visible = renameButtonsVisible;
            deleteButton.active = renameButtonsVisible && selectedTeamIsEmpty;
        }

        if (joinButton != null) {
            joinButton.visible =
                joinLeaveVisible && hasSelectedTeam && !selectedIsCurrent;
            joinButton.active = joinButton.visible;
        }

        if (leaveButton != null) {
            leaveButton.visible =
                joinLeaveVisible && hasSelectedTeam && selectedIsCurrent;
            leaveButton.active = leaveButton.visible;
        }
    }

    private void selectVisibleRow(int visibleIndex) {
        int teamIndex = teamScrollOffset + visibleIndex;

        if (teamIndex < 0 || teamIndex >= snapshot.teams().size()) {
            return;
        }

        String nextSelectedTeamId = snapshot.teams().get(teamIndex).id();

        if (!nextSelectedTeamId.equals(selectedTeamId)) {
            selectedTeamId = nextSelectedTeamId;
            memberScrollOffset = 0;
        }

        updateWidgetsFromSnapshot();
    }

    private void sendRefresh() {
        sendAction(Action.REFRESH, selectedTeamId, "");
    }

    private void sendAction(Action action, String teamId, String value) {
        if (!ClientPlayNetworking.canSend(TeamManagementActionPayload.TYPE)) {
            message = Component.translatable(
                "screen.trade-rush.team_management.network_unavailable"
            );
            errorMessage = true;
            return;
        }

        trackTextInputClearOnSuccess(action, value);

        ClientPlayNetworking.send(
            new TeamManagementActionPayload(action, teamId, value)
        );
    }

    private void trackTextInputClearOnSuccess(Action action, String value) {
        if (action == Action.CREATE || action == Action.RENAME_EMPTY) {
            pendingTextInputClear = new PendingTextInputClear(
                action,
                value == null ? "" : value
            );
        }
    }

    private void drawPanels(
        GuiGraphicsExtractor graphics,
        TeamManagementScreenLayout layout
    ) {
        graphics.fill(
            layout.left(),
            layout.top(),
            layout.right(),
            layout.bottom(),
            BACKGROUND_COLOR
        );
        graphics.outline(
            layout.left(),
            layout.top(),
            layout.width(),
            layout.height(),
            PANEL_BORDER_COLOR
        );

        drawColumnPanel(
            graphics,
            layout.teamsX(),
            layout.columnTop(),
            layout.columnWidth(),
            layout.columnHeight()
        );
        drawColumnPanel(
            graphics,
            layout.detailsX(),
            layout.columnTop(),
            layout.columnWidth(),
            layout.columnHeight()
        );
        drawColumnPanel(
            graphics,
            layout.actionsX(),
            layout.columnTop(),
            layout.actionsWidth(),
            layout.columnHeight()
        );
    }

    private void drawColumnPanel(
        GuiGraphicsExtractor graphics,
        int x,
        int y,
        int width,
        int height
    ) {
        graphics.fill(x, y, x + width, y + height, PANEL_COLOR);
        graphics.outline(x, y, width, height, 0x80707070);
    }

    private void drawLabelsAndLists(
        GuiGraphicsExtractor graphics,
        TeamManagementScreenLayout layout
    ) {
        graphics.centeredText(
            this.font,
            this.title,
            this.width / 2,
            layout.top() - 14,
            TEXT_COLOR
        );

        drawTeamsColumn(graphics, layout);
        drawDetailsColumn(graphics, layout);
        drawActionsColumn(graphics, layout);
        drawMessage(graphics, layout);
    }

    private void drawTeamsColumn(
        GuiGraphicsExtractor graphics,
        TeamManagementScreenLayout layout
    ) {
        graphics.text(
            this.font,
            Component.translatable("screen.trade-rush.team_management.teams"),
            layout.teamsContentX(),
            layout.headingY(),
            TEXT_COLOR
        );
        graphics.text(
            this.font,
            Component.translatable("screen.trade-rush.team_management.legend"),
            layout.teamsContentX(),
            layout.teamLegendY(),
            MUTED_TEXT_COLOR
        );

        drawTeamRowStateOverlays(graphics);
        drawScrollbar(
            graphics,
            layout.teamListX(),
            layout.teamListY(),
            layout.teamListWidth(),
            layout.teamListHeight(),
            teamScrollOffset,
            snapshot.teams().size(),
            visibleTeamRows(layout)
        );
    }

    private void drawDetailsColumn(
        GuiGraphicsExtractor graphics,
        TeamManagementScreenLayout layout
    ) {
        TeamRow selected = selectedTeam();

        graphics.text(
            this.font,
            Component.translatable("screen.trade-rush.team_management.details"),
            layout.detailsContentX(),
            layout.headingY(),
            TEXT_COLOR
        );
        graphics.text(
            this.font,
            TeamManagementScreenText.currentTeamText(snapshot),
            layout.detailsContentX(),
            layout.detailsCurrentTeamY(),
            MUTED_TEXT_COLOR
        );

        if (selected == null) {
            graphics.textWithWordWrap(
                this.font,
                Component.translatable(
                    "screen.trade-rush.team_management.no_team_selected"
                ),
                layout.detailsContentX(),
                layout.detailsSelectedTeamY(),
                layout.contentWidth(),
                MUTED_TEXT_COLOR
            );
            return;
        }

        graphics.text(
            this.font,
            TeamManagementScreenText.selectedTeamText(selected),
            layout.detailsContentX(),
            layout.detailsSelectedTeamY(),
            TEXT_COLOR
        );
        graphics.text(
            this.font,
            TeamManagementScreenText.selectedScoreText(selected),
            layout.detailsContentX(),
            layout.detailsScoreY(),
            MUTED_TEXT_COLOR
        );
        graphics.text(
            this.font,
            TeamManagementScreenText.selectedMembersHeader(),
            layout.detailsContentX(),
            layout.membersHeaderY(),
            MUTED_TEXT_COLOR
        );
        drawMembers(graphics, layout);
    }

    private void drawActionsColumn(
        GuiGraphicsExtractor graphics,
        TeamManagementScreenLayout layout
    ) {
        TeamRow selected = selectedTeam();

        graphics.text(
            this.font,
            Component.translatable("screen.trade-rush.team_management.actions"),
            layout.actionsContentX(),
            layout.headingY(),
            TEXT_COLOR
        );

        graphics.enableScissor(
            layout.actionsContentX(),
            layout.actionViewportY(),
            layout.actionsContentX() + layout.actionsContentWidth(),
            layout.actionViewportY() + layout.actionViewportHeight()
        );
        graphics.textWithWordWrap(
            this.font,
            TeamManagementScreenText.selectedActionText(selected),
            layout.actionsContentX(),
            layout.actionSelectedTeamY() - actionScrollOffset,
            layout.actionsContentWidth(),
            MUTED_TEXT_COLOR
        );
        graphics.text(
            this.font,
            Component.translatable(
                "screen.trade-rush.team_management.rename_input"
            ),
            layout.actionsContentX(),
            layout.renameLabelY() - actionScrollOffset,
            TEXT_COLOR
        );
        drawEmptyTeamExplanation(graphics, layout);
        graphics.text(
            this.font,
            Component.translatable(
                "screen.trade-rush.team_management.create_section"
            ),
            layout.actionsContentX(),
            layout.createSectionY() - actionScrollOffset,
            TEXT_COLOR
        );
        graphics.disableScissor();

        drawScrollbar(
            graphics,
            layout.actionsContentX(),
            layout.actionViewportY(),
            layout.actionsContentWidth(),
            layout.actionViewportHeight(),
            actionScrollOffset,
            layout.actionContentHeight(),
            layout.actionViewportHeight()
        );
    }

    private void drawTeamRowStateOverlays(GuiGraphicsExtractor graphics) {
        for (int index = 0; index < teamButtons.size(); index++) {
            int teamIndex = teamScrollOffset + index;

            if (teamIndex >= snapshot.teams().size()) {
                continue;
            }

            Button button = teamButtons.get(index);
            TeamRow team = snapshot.teams().get(teamIndex);
            boolean selected = team.id().equals(selectedTeamId);
            boolean current = team.id().equals(snapshot.currentTeamId());

            if (selected) {
                graphics.outline(
                    button.getX() - 1,
                    button.getY() - 1,
                    button.getWidth() + 2,
                    button.getHeight() + 2,
                    SELECTED_COLOR
                );
            }

            if (current) {
                graphics.fill(
                    button.getX() + 2,
                    button.getY() + 2,
                    button.getX() + 5,
                    button.getY() + button.getHeight() - 2,
                    CURRENT_TEAM_COLOR
                );
            }
        }
    }

    private void drawMembers(
        GuiGraphicsExtractor graphics,
        TeamManagementScreenLayout layout
    ) {
        TeamRow selected = selectedTeam();

        if (selected == null) {
            return;
        }

        if (selected.members().isEmpty()) {
            graphics.text(
                this.font,
                Component.translatable(
                    "screen.trade-rush.team_management.members_empty"
                ),
                layout.membersX(),
                layout.membersY(),
                MUTED_TEXT_COLOR
            );
            return;
        }

        int visibleRows = visibleMemberRows(layout);
        int first = clamp(memberScrollOffset, 0, maxMemberScrollOffset(layout));
        int last = Math.min(selected.members().size(), first + visibleRows);

        graphics.enableScissor(
            layout.membersX(),
            layout.membersY(),
            layout.membersX() + layout.membersWidth(),
            layout.membersY() + layout.membersHeight()
        );

        for (int index = first; index < last; index++) {
            MemberEntry member = selected.members().get(index);
            graphics.text(
                this.font,
                "- " + TeamManagementScreenText.memberLabel(member),
                layout.membersX(),
                layout.membersY() + (index - first) * TEXT_LINE_HEIGHT,
                MUTED_TEXT_COLOR
            );
        }

        graphics.disableScissor();
        drawScrollbar(
            graphics,
            layout.membersX(),
            layout.membersY(),
            layout.membersWidth(),
            layout.membersHeight(),
            first,
            selected.members().size(),
            visibleRows
        );
    }

    private void drawEmptyTeamExplanation(
        GuiGraphicsExtractor graphics,
        TeamManagementScreenLayout layout
    ) {
        TeamRow selected = selectedTeam();

        if (selected == null || selected.memberCount() == 0) {
            return;
        }

        graphics.textWithWordWrap(
            this.font,
            Component.translatable(
                "screen.trade-rush.team_management.empty_team_actions_only"
            ),
            layout.actionsContentX(),
            layout.emptyTeamExplanationY() - actionScrollOffset,
            layout.actionsContentWidth(),
            MUTED_TEXT_COLOR
        );
    }

    private void drawMessage(
        GuiGraphicsExtractor graphics,
        TeamManagementScreenLayout layout
    ) {
        if (message.getString().isBlank()) {
            return;
        }

        graphics.textWithWordWrap(
            this.font,
            message,
            layout.statusMessageX(),
            layout.statusMessageY(),
            layout.statusMessageWidth(),
            errorMessage ? ERROR_TEXT_COLOR : SUCCESS_TEXT_COLOR
        );
    }

    private void drawScrollbar(
        GuiGraphicsExtractor graphics,
        int x,
        int y,
        int width,
        int height,
        int offset,
        int totalRows,
        int visibleRows
    ) {
        int maxOffset = Math.max(0, totalRows - visibleRows);

        if (maxOffset <= 0 || height <= 0) {
            return;
        }

        int trackX = x + width - 4;
        int thumbHeight = Math.max(10, (height * visibleRows) / totalRows);
        int thumbTravel = Math.max(0, height - thumbHeight);
        int thumbY = y + (thumbTravel * offset) / maxOffset;

        graphics.fill(trackX, y, trackX + 2, y + height, SCROLL_TRACK_COLOR);
        graphics.fill(
            trackX,
            thumbY,
            trackX + 2,
            thumbY + thumbHeight,
            SCROLL_THUMB_COLOR
        );
    }

    private String resolvedSelectedTeamId() {
        if (containsTeam(selectedTeamId)) {
            return selectedTeamId;
        }

        if (containsTeam(snapshot.selectedTeamId())) {
            return snapshot.selectedTeamId();
        }

        return snapshot.teams().isEmpty()
            ? ""
            : snapshot.teams().getFirst().id();
    }

    private void scrollSelectedTeamIntoView(TeamManagementScreenLayout layout) {
        int selectedIndex = selectedTeamIndex();

        if (selectedIndex < 0) {
            teamScrollOffset = clamp(
                teamScrollOffset,
                0,
                maxTeamScrollOffset(layout)
            );
            return;
        }

        int visibleRows = visibleTeamRows(layout);

        if (selectedIndex < teamScrollOffset) {
            teamScrollOffset = selectedIndex;
        } else if (selectedIndex >= teamScrollOffset + visibleRows) {
            teamScrollOffset = selectedIndex - visibleRows + 1;
        }

        teamScrollOffset = clamp(
            teamScrollOffset,
            0,
            maxTeamScrollOffset(layout)
        );
    }

    private boolean containsTeam(String teamId) {
        if (teamId == null || teamId.isBlank()) {
            return false;
        }

        return snapshot
            .teams()
            .stream()
            .anyMatch(team -> team.id().equals(teamId));
    }

    private int selectedTeamIndex() {
        for (int index = 0; index < snapshot.teams().size(); index++) {
            if (snapshot.teams().get(index).id().equals(selectedTeamId)) {
                return index;
            }
        }

        return -1;
    }

    private TeamRow selectedTeam() {
        return snapshot
            .teams()
            .stream()
            .filter(team -> team.id().equals(selectedTeamId))
            .findFirst()
            .orElse(null);
    }

    private int visibleTeamRows(TeamManagementScreenLayout layout) {
        return Math.max(
            1,
            (layout.teamListHeight() - BUTTON_HEIGHT) / TEAM_ROW_STRIDE + 1
        );
    }

    private int visibleMemberRows(TeamManagementScreenLayout layout) {
        return Math.max(1, layout.membersHeight() / TEXT_LINE_HEIGHT);
    }

    private int maxTeamScrollOffset(TeamManagementScreenLayout layout) {
        return Math.max(0, snapshot.teams().size() - visibleTeamRows(layout));
    }

    private int maxMemberScrollOffset(TeamManagementScreenLayout layout) {
        TeamRow selected = selectedTeam();

        if (selected == null) {
            return 0;
        }

        return Math.max(
            0,
            selected.members().size() - visibleMemberRows(layout)
        );
    }

    private int maxActionScrollOffset(TeamManagementScreenLayout layout) {
        return Math.max(
            0,
            layout.actionContentHeight() - layout.actionViewportHeight()
        );
    }

    private boolean isActionWidgetInViewport(
        int widgetY,
        int widgetHeight,
        TeamManagementScreenLayout layout
    ) {
        return (
            widgetY >= layout.actionViewportY() &&
            widgetY + widgetHeight <=
                layout.actionViewportY() + layout.actionViewportHeight()
        );
    }

    private boolean contains(
        int x,
        int y,
        int width,
        int height,
        double mouseX,
        double mouseY
    ) {
        return (
            mouseX >= x &&
            mouseX < x + width &&
            mouseY >= y &&
            mouseY < y + height
        );
    }

    private int clamp(int value, int min, int max) {
        if (value < min) {
            return min;
        }

        return Math.min(value, max);
    }

    private TeamManagementScreenLayout layout() {
        return TeamManagementScreenLayout.create(this.width, this.height);
    }

    private record PendingTextInputClear(Action action, String value) {}
}
