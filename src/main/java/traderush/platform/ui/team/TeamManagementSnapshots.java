package traderush.platform.ui.team;

import java.util.List;
import java.util.Optional;
import net.minecraft.server.level.ServerPlayer;
import traderush.game.player.PlayerId;
import traderush.game.team.Team;
import traderush.game.team.TeamService;
import traderush.platform.ui.team.TeamManagementSnapshot.MemberEntry;
import traderush.platform.ui.team.TeamManagementSnapshot.TeamRow;

public final class TeamManagementSnapshots {

    private TeamManagementSnapshots() {}

    public static TeamManagementSnapshot create(
        TeamService teamService,
        ServerPlayer viewer,
        String requestedSelectedTeamId
    ) {
        PlayerId viewerId = PlayerId.fromUuid(viewer.getUUID());
        Optional<Team> currentTeam = teamService.getTeamForPlayer(viewerId);
        List<TeamRow> rows = teamService
            .listTeams()
            .stream()
            .map(team -> toTeamRow(viewer, team))
            .toList();

        String currentTeamId = currentTeam
            .map(team -> team.getId().toString())
            .orElse("");
        String currentTeamName = currentTeam.map(Team::getName).orElse("");
        String selectedTeamId = selectTeamId(
            rows,
            requestedSelectedTeamId,
            currentTeamId
        );

        return new TeamManagementSnapshot(
            rows,
            selectedTeamId,
            currentTeamId,
            currentTeamName
        );
    }

    private static TeamRow toTeamRow(ServerPlayer viewer, Team team) {
        List<MemberEntry> members = team
            .getPlayers()
            .stream()
            .map(playerId ->
                new MemberEntry(
                    playerId.toString(),
                    resolvePlayerName(viewer, playerId)
                )
            )
            .toList();

        return new TeamRow(
            team.getId().toString(),
            team.getName(),
            team.getScore(),
            team.getPlayers().size(),
            members
        );
    }

    private static String resolvePlayerName(
        ServerPlayer viewer,
        PlayerId playerId
    ) {
        if (viewer.level().getServer() != null) {
            ServerPlayer onlinePlayer = viewer
                .level()
                .getServer()
                .getPlayerList()
                .getPlayer(playerId.value());

            if (onlinePlayer != null) {
                return onlinePlayer.getGameProfile().name();
            }
        }

        return playerId.toString();
    }

    private static String selectTeamId(
        List<TeamRow> rows,
        String requestedSelectedTeamId,
        String currentTeamId
    ) {
        if (containsTeam(rows, requestedSelectedTeamId)) {
            return requestedSelectedTeamId;
        }

        if (containsTeam(rows, currentTeamId)) {
            return currentTeamId;
        }

        return rows.isEmpty() ? "" : rows.getFirst().id();
    }

    private static boolean containsTeam(List<TeamRow> rows, String teamId) {
        if (teamId == null || teamId.isBlank()) {
            return false;
        }

        return rows.stream().anyMatch(team -> team.id().equals(teamId));
    }
}
