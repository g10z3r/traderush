package traderush.platform.storage.shop;

import com.mojang.serialization.Codec;
import java.util.Optional;
import net.minecraft.resources.Identifier;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraft.world.level.saveddata.SavedDataType;
import traderush.TradeRush;
import traderush.game.shop.ShopStateSnapshot;

public final class ShopSavedData extends SavedData {
    private static final String DATA_PATH = "shop_state";

    private static final Codec<ShopSavedData> CODEC = ShopStateSnapshotCodec.SNAPSHOT
            .xmap(ShopSavedData::new, ShopSavedData::snapshot);

    private static final SavedDataType<ShopSavedData> TYPE = new SavedDataType<>(
            Identifier.fromNamespaceAndPath(TradeRush.MOD_ID, DATA_PATH),
            ShopSavedData::new,
            CODEC,
            null
    );

    private ShopStateSnapshot snapshot;

    public ShopSavedData() {
        this(ShopStateSnapshot.empty());
    }

    public ShopSavedData(ShopStateSnapshot snapshot) {
        this.snapshot = snapshot == null ? ShopStateSnapshot.empty() : snapshot;
    }

    public static Optional<ShopSavedData> find(MinecraftServer server) {
        ServerLevel overworld = server.getLevel(ServerLevel.OVERWORLD);

        if (overworld == null) {
            throw new IllegalStateException("Overworld is not available.");
        }

        return Optional.ofNullable(overworld.getDataStorage().get(TYPE));
    }

    public static ShopSavedData getOrCreate(MinecraftServer server) {
        ServerLevel overworld = server.getLevel(ServerLevel.OVERWORLD);

        if (overworld == null) {
            throw new IllegalStateException("Overworld is not available.");
        }

        return overworld.getDataStorage().computeIfAbsent(TYPE);
    }

    public ShopStateSnapshot snapshot() {
        return snapshot;
    }

    public void setSnapshot(ShopStateSnapshot snapshot) {
        this.snapshot = snapshot == null ? ShopStateSnapshot.empty() : snapshot;
        setDirty();
    }
}
