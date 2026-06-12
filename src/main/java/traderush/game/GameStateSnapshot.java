package traderush.game;

import java.util.ArrayList;
import java.util.List;

public final class GameStateSnapshot {
    public static final class TeamSnapshot {
        public String id;
        public String name;
        public List<String> members = new ArrayList<>();
        public long score;
    }

    public int varsion = 1;
    public List<TeamSnapshot> teams = new ArrayList<>();
}
