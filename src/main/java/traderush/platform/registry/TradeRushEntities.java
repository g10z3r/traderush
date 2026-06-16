package traderush.platform.registry;

import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import traderush.TradeRush;
import traderush.platform.entity.TeamRatingPaintingEntity;

public final class TradeRushEntities {
    public static final String TEAM_RATING_PAINTING_PATH = "team_rating_painting";
    public static final Identifier TEAM_RATING_PAINTING_ID = TradeRush
            .id(TEAM_RATING_PAINTING_PATH);
    private static final ResourceKey<EntityType<?>> TEAM_RATING_PAINTING_KEY = ResourceKey
            .create(Registries.ENTITY_TYPE, TEAM_RATING_PAINTING_ID);
    public static final EntityType<TeamRatingPaintingEntity> TEAM_RATING_PAINTING = EntityType.Builder
            .<TeamRatingPaintingEntity>of(
                    TeamRatingPaintingEntity::new,
                    MobCategory.MISC
            )
            .sized(
                    TeamRatingPaintingEntity.WIDTH_BLOCKS,
                    TeamRatingPaintingEntity.HEIGHT_BLOCKS
            )
            .clientTrackingRange(10)
            .updateInterval(Integer.MAX_VALUE)
            .build(TEAM_RATING_PAINTING_KEY);

    private TradeRushEntities() {}

    public static void register() {
        Registry.register(
                BuiltInRegistries.ENTITY_TYPE,
                TEAM_RATING_PAINTING_ID,
                TEAM_RATING_PAINTING
        );
    }
}
