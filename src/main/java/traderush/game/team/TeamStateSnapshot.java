package traderush.game.team;

import java.util.List;

public record TeamStateSnapshot(int version, List<TeamSnapshot> teams) {
    public static final int CURRENT_VERSION = 1;

    public TeamStateSnapshot {
        teams = teams == null ? List.of() : List.copyOf(teams);
    }

    public static TeamStateSnapshot empty() {
        return new TeamStateSnapshot(CURRENT_VERSION, List.of());
    }

    public record TeamSnapshot(String id, String name, long score, List<String> members) {
        public TeamSnapshot {
            members = members == null ? List.of() : List.copyOf(members);
        }
    }
}
