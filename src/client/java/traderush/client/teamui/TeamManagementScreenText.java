package traderush.client.teamui;

import net.minecraft.network.chat.Component;
import traderush.platform.teamui.TeamManagementSnapshot;
import traderush.platform.teamui.TeamManagementSnapshot.MemberEntry;
import traderush.platform.teamui.TeamManagementSnapshot.TeamRow;

final class TeamManagementScreenText {

    private TeamManagementScreenText() {}

    static String teamButtonLabel(
        TeamRow team,
        String selectedTeamId,
        String currentTeamId
    ) {
        String selectedMarker = team.id().equals(selectedTeamId) ? "▶ " : "  ";
        String currentMarker = team.id().equals(currentTeamId) ? "★ " : "";

        return (
            selectedMarker +
            currentMarker +
            team.name() +
            " (" +
            team.memberCount() +
            ")"
        );
    }

    static Component currentTeamText(TeamManagementSnapshot snapshot) {
        if (snapshot.currentTeamName().isBlank()) {
            return Component.translatable(
                "screen.trade-rush.team_management.current_none"
            );
        }

        return Component.translatable(
            "screen.trade-rush.team_management.current_team",
            snapshot.currentTeamName()
        );
    }

    static Component selectedTeamText(TeamRow selected) {
        if (selected == null) {
            return Component.translatable(
                "screen.trade-rush.team_management.selected_none"
            );
        }

        return Component.translatable(
            "screen.trade-rush.team_management.selected_team",
            selected.name()
        );
    }

    static Component selectedActionText(TeamRow selected) {
        if (selected == null) {
            return Component.translatable(
                "screen.trade-rush.team_management.select_team_actions"
            );
        }

        return Component.translatable(
            "screen.trade-rush.team_management.selected_team",
            selected.name()
        );
    }

    static Component selectedScoreText(TeamRow selected) {
        if (selected == null) {
            return Component.literal("");
        }

        return Component.translatable(
            "screen.trade-rush.team_management.score_members",
            selected.score(),
            selected.memberCount()
        );
    }

    static Component selectedMembersHeader() {
        return Component.translatable(
            "screen.trade-rush.team_management.members"
        );
    }

    static String memberLabel(MemberEntry member) {
        return member.displayName().isBlank()
            ? member.id()
            : member.displayName();
    }
}
