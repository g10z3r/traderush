package traderush.platform.entity;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundAddEntityPacket;
import net.minecraft.server.level.ServerEntity;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.decoration.HangingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.gamerules.GameRules;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import traderush.platform.registry.TradeRushEntities;
import traderush.platform.registry.TradeRushItems;

public final class TeamRatingPaintingEntity extends HangingEntity {
    public static final int WIDTH_BLOCKS = 4;
    public static final int HEIGHT_BLOCKS = 4;
    private static final double DEPTH = 0.0625D;
    private static final double WALL_OFFSET = 0.46875D;

    public TeamRatingPaintingEntity(
            EntityType<TeamRatingPaintingEntity> entityType,
            Level level
    ) {
        super(entityType, level);
    }

    public TeamRatingPaintingEntity(
            Level level,
            BlockPos pos,
            Direction direction
    ) {
        super(TradeRushEntities.TEAM_RATING_PAINTING, level, pos);
        setDirection(direction);
    }

    @Override
    protected void addAdditionalSaveData(ValueOutput output) {
        output.store(
                "facing",
                Direction.LEGACY_ID_CODEC_2D,
                getDirection()
        );
        super.addAdditionalSaveData(output);
    }

    @Override
    protected void readAdditionalSaveData(ValueInput input) {
        Direction direction = input
                .read("facing", Direction.LEGACY_ID_CODEC_2D)
                .orElse(Direction.SOUTH);
        super.readAdditionalSaveData(input);
        setDirection(direction);
    }

    @Override
    protected AABB calculateBoundingBox(BlockPos pos, Direction direction) {
        Vec3 wallCenter = Vec3.atCenterOf(pos)
                .relative(direction, -WALL_OFFSET);
        Vec3 center = wallCenter
                .relative(
                        direction.getCounterClockWise(),
                        offsetForSize(WIDTH_BLOCKS)
                )
                .relative(Direction.UP, offsetForSize(HEIGHT_BLOCKS));

        Direction.Axis axis = direction.getAxis();
        double xSize = axis == Direction.Axis.X ? DEPTH : WIDTH_BLOCKS;
        double ySize = HEIGHT_BLOCKS;
        double zSize = axis == Direction.Axis.Z ? DEPTH : WIDTH_BLOCKS;

        return AABB.ofSize(center, xSize, ySize, zSize);
    }

    @Override
    public void dropItem(ServerLevel serverLevel, Entity breaker) {
        if (!serverLevel.getGameRules().get(GameRules.ENTITY_DROPS)) {
            return;
        }

        playSound(SoundEvents.PAINTING_BREAK, 1.0F, 1.0F);

        if (breaker instanceof Player player && player.hasInfiniteMaterials()) {
            return;
        }

        spawnAtLocation(
                serverLevel,
                new ItemStack(TradeRushItems.TEAM_RATING_PAINTING),
                0.0F
        );
    }

    @Override
    public void playPlacementSound() {
        playSound(SoundEvents.PAINTING_PLACE, 1.0F, 1.0F);
    }

    @Override
    public void snapTo(double x, double y, double z, float yaw, float pitch) {
        setPos(x, y, z);
    }

    @Override
    public Vec3 trackingPosition() {
        return Vec3.atLowerCornerOf(getPos());
    }

    @Override
    public Packet<ClientGamePacketListener> getAddEntityPacket(
            ServerEntity entity
    ) {
        return new ClientboundAddEntityPacket(
                this,
                getDirection().get3DDataValue(),
                getPos()
        );
    }

    @Override
    public void recreateFromPacket(ClientboundAddEntityPacket packet) {
        super.recreateFromPacket(packet);
        setDirection(Direction.from3DDataValue(packet.getData()));
    }

    @Override
    public ItemStack getPickResult() {
        return new ItemStack(TradeRushItems.TEAM_RATING_PAINTING);
    }

    private static double offsetForSize(int size) {
        return size % 2 == 0 ? 0.5D : 0.0D;
    }
}
