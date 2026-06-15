package traderush.platform.generation;

import java.util.List;
import java.util.UUID;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import traderush.game.shop.OfferShop;
import traderush.game.shop.ShopId;
import traderush.game.shop.ShopLocation;
import traderush.game.shop.ShopRepository;
import traderush.game.shop.generation.ContractShopSpec;
import traderush.game.shop.generation.DefaultShopGenerationPlan;
import traderush.game.shop.generation.OfferShopSpec;
import traderush.game.shop.generation.ShopGenerationException;
import traderush.game.shop.generation.ShopGenerationPlacement;
import traderush.game.shop.generation.ShopGenerationPlan;
import traderush.game.shop.generation.ShopGenerationPlanner;
import traderush.game.shop.generation.ShopSpec;
import traderush.platform.protection.ShopProtectionBypass;

/**
 * Orchestrates shop generation for a new world. Writes shops into the
 * repository and places gold-block markers in the world. Does NOT trigger saves
 * — the caller is responsible for persisting state.
 */
public final class MinecraftShopGenerationCoordinator {
    private static final Logger LOGGER = LoggerFactory
            .getLogger(MinecraftShopGenerationCoordinator.class);

    private final MinecraftServer server;

    public MinecraftShopGenerationCoordinator(MinecraftServer server) {
        this.server = server;
    }

    /**
     * Plans and executes placements for the default shop generation plan.
     *
     * @throws ShopGenerationException if a location cannot be resolved for any
     *                                 shop
     */
    public void execute(ShopRepository shopRepository) {
        ShopGenerationPlan plan = DefaultShopGenerationPlan.create();
        ShopGenerationPlanner planner = new ShopGenerationPlanner(
                new MinecraftShopLocationSelector(server)
        );

        List<ShopGenerationPlacement> placements = planner.planPlacements(plan);

        for (ShopGenerationPlacement placement : placements) {
            executePlacement(placement, shopRepository);
        }

        LOGGER.info(
                "Shop generation complete. {} shop(s) placed.",
                placements.size()
        );
    }

    private void executePlacement(
            ShopGenerationPlacement placement,
            ShopRepository shopRepository
    ) {
        ShopSpec spec = placement.spec();
        ShopLocation location = placement.location();

        if (spec instanceof OfferShopSpec) {
            OfferShop shop = new OfferShop(
                    ShopId.fromUuid(UUID.randomUUID()),
                    spec.displayName(),
                    location
            );

            shopRepository.put(shop);
            placeMarkerBlock(location);

            LOGGER.info(
                    "Placed offer shop '{}' (key: {}) at {}.",
                    spec.displayName(),
                    spec.generationKey(),
                    location.toBlockPosString()
            );
        } else if (spec instanceof ContractShopSpec) {
            // TODO: Phase 2 — contract shop generation
            LOGGER.warn(
                    "Contract shop generation not yet implemented (key: {}).",
                    spec.generationKey()
            );
        }
    }

    private void placeMarkerBlock(ShopLocation location) {
        Identifier dimId = Identifier.parse(location.dimensionId());
        ResourceKey<Level> dimKey = ResourceKey
                .create(Registries.DIMENSION, dimId);
        ServerLevel level = server.getLevel(dimKey);

        if (level == null) {
            LOGGER.warn(
                    "Cannot place shop marker block: dimension '{}' not found.",
                    location.dimensionId()
            );
            return;
        }

        BlockPos pos = new BlockPos(location.x(), location.y(), location.z());
        ShopProtectionBypass
                .run(
                        () -> level.setBlock(
                                pos,
                                Blocks.GOLD_BLOCK.defaultBlockState(),
                                3
                        )
                );
    }
}
