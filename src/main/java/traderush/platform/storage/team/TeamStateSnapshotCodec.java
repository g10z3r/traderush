package traderush.platform.storage.team;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import traderush.game.team.TeamStateSnapshot;

public final class TeamStateSnapshotCodec {
    public static final Codec<TeamStateSnapshot.TeamSnapshot> TEAM_SNAPSHOT = RecordCodecBuilder.create(
            instance -> instance.group(
                    Codec.STRING.fieldOf("id").forGetter(TeamStateSnapshot.TeamSnapshot::id),
                    Codec.STRING.fieldOf("name").forGetter(TeamStateSnapshot.TeamSnapshot::name),
                    Codec.LONG.fieldOf("score").forGetter(TeamStateSnapshot.TeamSnapshot::score),
                    Codec.STRING.listOf().fieldOf("members").forGetter(TeamStateSnapshot.TeamSnapshot::members)
            ).apply(instance, TeamStateSnapshot.TeamSnapshot::new)
    );

    public static final Codec<TeamStateSnapshot> SNAPSHOT = RecordCodecBuilder.create(
            instance -> instance.group(
                    Codec.INT.fieldOf("version").forGetter(TeamStateSnapshot::version),
                    TEAM_SNAPSHOT.listOf().fieldOf("teams").forGetter(TeamStateSnapshot::teams)
            ).apply(instance, TeamStateSnapshot::new)
    );

    private TeamStateSnapshotCodec() {
    }
}
