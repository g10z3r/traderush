package traderush.platform.storage.team;

import net.minecraft.world.level.saveddata.SavedData;
import traderush.game.team.TeamStateSnapshot;
import com.mojang.serialization.Codec;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.saveddata.SavedDataType;
import java.util.Optional;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import traderush.TradeRush;

public final class TeamSavedData extends SavedData {
    private static final String DATA_PATH = "team_state";

    private static final Codec<TeamSavedData> CODEC = TeamStateSnapshotCodec.SNAPSHOT.xmap(
            TeamSavedData::new,
            TeamSavedData::snapshot
    );

    private static final SavedDataType<TeamSavedData> TYPE = new SavedDataType<>(
            Identifier.fromNamespaceAndPath(TradeRush.MOD_ID, DATA_PATH),
            TeamSavedData::new,
            CODEC,
            null
    );

    private TeamStateSnapshot snapshot;

    public TeamSavedData() {
        this(TeamStateSnapshot.empty());
    }

    public TeamSavedData(TeamStateSnapshot snapshot) {
        this.snapshot = snapshot == null ? TeamStateSnapshot.empty() : snapshot;
    }

    public static Optional<TeamSavedData> find(MinecraftServer server) {
        ServerLevel overworld = server.getLevel(ServerLevel.OVERWORLD);

        if (overworld == null) {
            throw new IllegalStateException("Overworld is not available.");
        }

        return Optional.ofNullable(overworld.getDataStorage().get(TYPE));
    }

    public static TeamSavedData getOrCreate(MinecraftServer server) {
        ServerLevel overworld = server.getLevel(ServerLevel.OVERWORLD);

        if (overworld == null) {
            throw new IllegalStateException("Overworld is not available.");
        }

        return overworld.getDataStorage().computeIfAbsent(TYPE);
    }

    public TeamStateSnapshot snapshot() {
        return snapshot;
    }

    public void setSnapshot(TeamStateSnapshot snapshot) {
        this.snapshot = snapshot == null ? TeamStateSnapshot.empty() : snapshot;
        setDirty();
    }
}
