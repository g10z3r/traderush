package traderush.game.team;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import java.util.List;

public record TeamStateSnapshot(int version, List<TeamSnapshot> teams) {
    public static final int CURRENT_VERSION = 1;

    public static final Codec<TeamStateSnapshot> CODEC = RecordCodecBuilder
            .create(instance -> instance
                    .group(Codec.INT.fieldOf("version").forGetter(TeamStateSnapshot::version),
                            TeamSnapshot.CODEC.listOf()
                                    .fieldOf("teams")
                                    .forGetter(TeamStateSnapshot::teams))
                    .apply(instance, TeamStateSnapshot::new));

    public TeamStateSnapshot {
        teams = teams == null ? List.of() : List.copyOf(teams);
    }

    public static TeamStateSnapshot empty() {
        return new TeamStateSnapshot(CURRENT_VERSION, List.of());
    }

    public record TeamSnapshot(String id, String name, long score, List<String> members) {
        public static final Codec<TeamSnapshot> CODEC = RecordCodecBuilder
                .create(instance -> instance
                        .group(Codec.STRING.fieldOf("id").forGetter(TeamSnapshot::id),
                                Codec.STRING.fieldOf("name").forGetter(TeamSnapshot::name),
                                Codec.LONG.fieldOf("score").forGetter(TeamSnapshot::score),
                                Codec.STRING.listOf()
                                        .fieldOf("members")
                                        .forGetter(TeamSnapshot::members))
                        .apply(instance, TeamSnapshot::new));

        public TeamSnapshot {
            members = members == null ? List.of() : List.copyOf(members);
        }
    }
}