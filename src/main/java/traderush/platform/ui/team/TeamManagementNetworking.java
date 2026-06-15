package traderush.platform.ui.team;

import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import traderush.TradeRush;
import traderush.game.player.PlayerId;
import traderush.game.team.Team;
import traderush.game.team.TeamError;
import traderush.game.team.TeamId;
import traderush.game.team.TeamOperationResult;
import traderush.game.team.TeamService;
import traderush.platform.TeamMessages;
import traderush.platform.ui.team.TeamManagementActionPayload.Action;

public final class TeamManagementNetworking {

    private static boolean payloadTypesRegistered;
    private static boolean serverHandlersRegistered;

    private TeamManagementNetworking() {}

    public static void registerPayloadTypes() {
        if (payloadTypesRegistered) {
            return;
        }

        PayloadTypeRegistry.serverboundPlay().register(
            TeamManagementActionPayload.TYPE,
            TeamManagementActionPayload.CODEC
        );
        PayloadTypeRegistry.clientboundPlay().register(
            TeamManagementStatePayload.TYPE,
            TeamManagementStatePayload.CODEC
        );
        payloadTypesRegistered = true;
    }

    public static void registerServerHandlers() {
        if (serverHandlersRegistered) {
            return;
        }

        ServerPlayNetworking.registerGlobalReceiver(
            TeamManagementActionPayload.TYPE,
            (payload, context) ->
                context
                    .server()
                    .execute(() -> handleAction(payload, context.player()))
        );
        serverHandlersRegistered = true;
    }

    public static void sendInitialSnapshot(ServerPlayer player) {
        sendState(player, "", Component.empty(), false);
    }

    private static void handleAction(
        TeamManagementActionPayload payload,
        ServerPlayer player
    ) {
        if (!hasValidTeamManagementMenu(player)) {
            return;
        }

        TeamService teamService;

        try {
            teamService = TradeRush.runtime().teamService();
        } catch (IllegalStateException exception) {
            sendRawState(
                player,
                TeamManagementSnapshot.EMPTY,
                Component.translatable(
                    "message.trade-rush.team.error.runtime_not_ready"
                ),
                true
            );
            return;
        }

        if (payload.action() == Action.REFRESH) {
            sendState(player, payload.teamId(), Component.empty(), false);
            return;
        }

        switch (payload.action()) {
            case CREATE -> handleCreate(teamService, player, payload);
            case JOIN -> handleJoin(teamService, player, payload);
            case LEAVE -> handleLeave(teamService, player, payload);
            case DELETE_EMPTY -> handleDelete(teamService, player, payload);
            case RENAME_EMPTY -> handleRename(teamService, player, payload);
            case REFRESH -> sendState(
                player,
                payload.teamId(),
                Component.empty(),
                false
            );
        }
    }

    private static boolean hasValidTeamManagementMenu(ServerPlayer player) {
        return (
            player.containerMenu instanceof TeamManagementMenu menu &&
            menu.stillValid(player)
        );
    }

    private static void handleCreate(
        TeamService teamService,
        ServerPlayer player,
        TeamManagementActionPayload payload
    ) {
        TeamOperationResult<Team> result = teamService.createTeam(
            payload.value()
        );
        sendResult(
            player,
            result,
            result.isSuccess()
                ? result.value().getId().toString()
                : payload.teamId(),
            TeamMessages::created
        );
    }

    private static void handleJoin(
        TeamService teamService,
        ServerPlayer player,
        TeamManagementActionPayload payload
    ) {
        TeamId teamId = parseTeamId(payload.teamId());

        if (teamId == null) {
            sendTeamError(player, payload.teamId(), TeamError.TEAM_NOT_FOUND);
            return;
        }

        PlayerId playerId = PlayerId.fromUuid(player.getUUID());
        TeamOperationResult<Team> result = teamService.joinTeam(
            playerId,
            teamId
        );
        sendResult(player, result, payload.teamId(), TeamMessages::joined);
    }

    private static void handleLeave(
        TeamService teamService,
        ServerPlayer player,
        TeamManagementActionPayload payload
    ) {
        PlayerId playerId = PlayerId.fromUuid(player.getUUID());
        TeamOperationResult<Team> result = teamService.leaveTeam(playerId);
        sendResult(player, result, payload.teamId(), TeamMessages::left);
    }

    private static void handleDelete(
        TeamService teamService,
        ServerPlayer player,
        TeamManagementActionPayload payload
    ) {
        TeamId teamId = parseTeamId(payload.teamId());

        if (teamId == null) {
            sendTeamError(player, payload.teamId(), TeamError.TEAM_NOT_FOUND);
            return;
        }

        TeamOperationResult<Team> result = teamService.deleteTeam(teamId);
        sendResult(player, result, "", TeamMessages::deleted);
    }

    private static void handleRename(
        TeamService teamService,
        ServerPlayer player,
        TeamManagementActionPayload payload
    ) {
        TeamId teamId = parseTeamId(payload.teamId());

        if (teamId == null) {
            sendTeamError(player, payload.teamId(), TeamError.TEAM_NOT_FOUND);
            return;
        }

        TeamOperationResult<Team> result = teamService.renameTeam(
            teamId,
            payload.value()
        );
        sendResult(player, result, payload.teamId(), TeamMessages::renamed);
    }

    private static void sendResult(
        ServerPlayer player,
        TeamOperationResult<Team> result,
        String selectedTeamId,
        java.util.function.Function<Team, Component> successMessage
    ) {
        if (result.isSuccess()) {
            sendState(
                player,
                selectedTeamId,
                successMessage.apply(result.value()),
                false
            );
            return;
        }

        sendTeamError(player, selectedTeamId, result.error());
    }

    private static void sendTeamError(
        ServerPlayer player,
        String selectedTeamId,
        TeamError error
    ) {
        sendError(player, selectedTeamId, TeamMessages.componentFor(error));
    }

    private static void sendError(
        ServerPlayer player,
        String selectedTeamId,
        Component message
    ) {
        sendState(player, selectedTeamId, message, true);
    }

    private static void sendState(
        ServerPlayer player,
        String selectedTeamId,
        Component message,
        boolean error
    ) {
        if (
            !ServerPlayNetworking.canSend(
                player,
                TeamManagementStatePayload.TYPE
            )
        ) {
            return;
        }

        TeamManagementSnapshot snapshot = TeamManagementSnapshots.create(
            TradeRush.runtime().teamService(),
            player,
            selectedTeamId
        );
        sendRawState(player, snapshot, message, error);
    }

    private static void sendRawState(
        ServerPlayer player,
        TeamManagementSnapshot snapshot,
        Component message,
        boolean error
    ) {
        if (
            !ServerPlayNetworking.canSend(
                player,
                TeamManagementStatePayload.TYPE
            )
        ) {
            return;
        }

        ServerPlayNetworking.send(
            player,
            new TeamManagementStatePayload(snapshot, message, error)
        );
    }

    private static TeamId parseTeamId(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }

        try {
            return TeamId.fromString(value);
        } catch (IllegalArgumentException exception) {
            return null;
        }
    }
}
