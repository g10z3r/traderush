package traderush.platform.storage.world;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import traderush.game.world.WorldStateSnapshot;

public final class WorldStateSnapshotCodec {
    public static final Codec<WorldStateSnapshot.ManagementBlockSnapshot> MANAGEMENT_BLOCK = RecordCodecBuilder
            .create(
                    instance -> instance.group(
                            Codec.STRING.fieldOf("dimensionId")
                                    .forGetter(
                                            WorldStateSnapshot.ManagementBlockSnapshot::dimensionId
                                    ),
                            Codec.INT.fieldOf("x")
                                    .forGetter(
                                            WorldStateSnapshot.ManagementBlockSnapshot::x
                                    ),
                            Codec.INT.fieldOf("y")
                                    .forGetter(
                                            WorldStateSnapshot.ManagementBlockSnapshot::y
                                    ),
                            Codec.INT.fieldOf("z")
                                    .forGetter(
                                            WorldStateSnapshot.ManagementBlockSnapshot::z
                                    )
                    )
                            .apply(
                                    instance,
                                    WorldStateSnapshot.ManagementBlockSnapshot::new
                            )
            );

    public static final Codec<WorldStateSnapshot> SNAPSHOT = RecordCodecBuilder
            .create(
                    instance -> instance.group(
                            MANAGEMENT_BLOCK.optionalFieldOf("managementBlock")
                                    .forGetter(
                                            WorldStateSnapshot::managementBlock
                                    )
                    ).apply(instance, WorldStateSnapshot::new)
            );

    private WorldStateSnapshotCodec() {}
}
