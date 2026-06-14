package traderush.runtime;

import net.minecraft.core.BlockPos;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.levelgen.Heightmap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import traderush.game.shop.InMemoryShopRepository;
import traderush.game.shop.OfferShop;
import traderush.game.shop.ShopId;
import traderush.game.shop.ShopLocation;
import traderush.game.shop.ShopRepository;
import traderush.game.shop.ShopService;
import traderush.game.shop.ShopStateMapper;
import traderush.game.shop.ShopStateSnapshot;
import traderush.game.team.InMemoryTeamRepository;
import traderush.game.team.TeamRepository;
import traderush.game.team.TeamService;
import traderush.game.team.TeamStateMapper;
import traderush.game.team.TeamStateSnapshot;
import traderush.platform.storage.TradeRushPersistence;

import java.util.Optional;
import java.util.UUID;

public final class TradeRushRuntime {
    private static final Logger LOGGER = LoggerFactory.getLogger(TradeRushRuntime.class);

    private static final String OVERWORLD_DIMENSION_ID = "minecraft:overworld";
    private static final int DEFAULT_SHOP_OFFSET = 15;

    private final MinecraftServer server;
    private final TradeRushPersistence persistence;
    private final TeamRepository teamRepository;
    private final TeamService teamService;
    private final ShopRepository shopRepository;
    private final ShopService shopService;

    private TradeRushRuntime(MinecraftServer server, TradeRushPersistence persistence) {
        this.server = server;
        this.persistence = persistence;

        this.teamRepository = new InMemoryTeamRepository();
        loadTeamsSafely();
        this.teamService = new TeamService(teamRepository, this::saveTeamsSafely);

        this.shopRepository = new InMemoryShopRepository();
        loadShopsSafely();

        if (shopRepository.getAllOfferShops().isEmpty()) {
            spawnDefaultOfferShopSafely();
        }

        this.shopService = new ShopService(shopRepository, this::saveShopsSafely);
    }

    public static TradeRushRuntime create(MinecraftServer server) {
        return new TradeRushRuntime(server, TradeRushPersistence.create(server));
    }

    public TeamService teamService() {
        return teamService;
    }

    public ShopService shopService() {
        return shopService;
    }

    public void saveStateSafely() {
        saveTeamsSafely();
        saveShopsSafely();
    }

    private void loadTeamsSafely() {
        try {
            Optional<TeamStateSnapshot> snapshot = persistence.teamStateStore().load();

            if (snapshot.isEmpty()) {
                LOGGER.info("No existing TradeRush team state found.");
                return;
            }

            TeamStateMapper.restoreInto(teamRepository, snapshot.get());
        } catch (Exception exception) {
            LOGGER.error("Failed to load TradeRush team state.", exception);
        }
    }

    private void saveTeamsSafely() {
        try {
            TeamStateSnapshot snapshot = TeamStateMapper.toSnapshot(teamRepository);
            persistence.teamStateStore().save(snapshot);
        } catch (Exception exception) {
            LOGGER.error("Failed to save TradeRush team state.", exception);
        }
    }

    private void loadShopsSafely() {
        try {
            Optional<ShopStateSnapshot> snapshot = persistence.shopStateStore().load();

            if (snapshot.isEmpty()) {
                LOGGER.info("No existing TradeRush shop state found.");
                return;
            }

            ShopStateMapper.restoreInto(shopRepository, snapshot.get());
        } catch (Exception exception) {
            LOGGER.error("Failed to load TradeRush shop state.", exception);
        }
    }

    private void saveShopsSafely() {
        try {
            ShopStateSnapshot snapshot = ShopStateMapper.toSnapshot(shopRepository);
            persistence.shopStateStore().save(snapshot);
        } catch (Exception exception) {
            LOGGER.error("Failed to save TradeRush shop state.", exception);
        }
    }

    private void spawnDefaultOfferShopSafely() {
        try {
            ServerLevel overworld = server.getLevel(ServerLevel.OVERWORLD);

            if (overworld == null) {
                LOGGER.warn("Cannot spawn default offer shop: overworld not available.");
                return;
            }

            BlockPos shopPos = findShopPosition(overworld);
            ShopLocation location = new ShopLocation(
                    OVERWORLD_DIMENSION_ID,
                    shopPos.getX(),
                    shopPos.getY(),
                    shopPos.getZ()
            );

            OfferShop shop = new OfferShop(
                    ShopId.fromUuid(UUID.randomUUID()),
                    "Market",
                    location
            );

            shopRepository.put(shop);
            overworld.setBlock(shopPos, Blocks.GOLD_BLOCK.defaultBlockState(), 3);
            saveShopsSafely();

            LOGGER.info(
                    "Spawned default offer shop '{}' at {}.",
                    shop.getName(),
                    shopPos
            );
        } catch (Exception exception) {
            LOGGER.error("Failed to spawn default offer shop.", exception);
        }
    }

    private static BlockPos findShopPosition(ServerLevel overworld) {
        BlockPos spawn = overworld.getLevelData().getRespawnData().pos();
        int x = spawn.getX() + DEFAULT_SHOP_OFFSET;
        int z = spawn.getZ() + DEFAULT_SHOP_OFFSET;
        int y = overworld.getHeight(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, x, z);
        return new BlockPos(x, y, z);
    }
}
