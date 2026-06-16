package traderush.platform.storage.world;

import com.mojang.serialization.Codec;
import java.util.Optional;
import net.minecraft.resources.Identifier;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraft.world.level.saveddata.SavedDataType;
import traderush.TradeRush;
import traderush.game.world.WorldStateSnapshot;

public final class WorldSavedData extends SavedData {
    private static final String DATA_PATH = "world_state";

    private static final Codec<WorldSavedData> CODEC = WorldStateSnapshotCodec.SNAPSHOT
            .xmap(WorldSavedData::new, WorldSavedData::snapshot);

    private static final SavedDataType<WorldSavedData> TYPE = new SavedDataType<>(
            Identifier.fromNamespaceAndPath(TradeRush.MOD_ID, DATA_PATH),
            WorldSavedData::new,
            CODEC,
            null
    );

    private WorldStateSnapshot snapshot;

    public WorldSavedData() {
        this(WorldStateSnapshot.empty());
    }

    public WorldSavedData(WorldStateSnapshot snapshot) {
        this.snapshot = snapshot == null ? WorldStateSnapshot.empty()
                : snapshot;
    }

    public static Optional<WorldSavedData> find(MinecraftServer server) {
        ServerLevel overworld = server.getLevel(ServerLevel.OVERWORLD);

        if (overworld == null) {
            throw new IllegalStateException("Overworld is not available.");
        }

        return Optional.ofNullable(overworld.getDataStorage().get(TYPE));
    }

    public static WorldSavedData getOrCreate(MinecraftServer server) {
        ServerLevel overworld = server.getLevel(ServerLevel.OVERWORLD);

        if (overworld == null) {
            throw new IllegalStateException("Overworld is not available.");
        }

        return overworld.getDataStorage().computeIfAbsent(TYPE);
    }

    public WorldStateSnapshot snapshot() {
        return snapshot;
    }

    public void setSnapshot(WorldStateSnapshot snapshot) {
        this.snapshot = snapshot == null ? WorldStateSnapshot.empty()
                : snapshot;
        setDirty();
    }
}
