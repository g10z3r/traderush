package traderush.runtime;

import java.util.Optional;
import net.minecraft.server.MinecraftServer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import traderush.game.shop.InMemoryShopRepository;
import traderush.game.shop.ShopRepository;
import traderush.game.shop.ShopService;
import traderush.game.shop.ShopStateMapper;
import traderush.game.shop.ShopStateSnapshot;
import traderush.game.shop.generation.ShopGenerationException;
import traderush.game.team.InMemoryTeamRepository;
import traderush.game.team.TeamRepository;
import traderush.game.team.TeamService;
import traderush.game.team.TeamStateMapper;
import traderush.game.team.TeamStateSnapshot;
import traderush.game.world.ManagementBlockLocation;
import traderush.game.world.WorldStateMapper;
import traderush.game.world.WorldStateSnapshot;
import traderush.platform.generation.MinecraftShopGenerationCoordinator;
import traderush.platform.generation.MinecraftSpawnLocator;
import traderush.platform.generation.MinecraftTeamBlockPlacer;
import traderush.platform.storage.TradeRushPersistence;

public final class TradeRushRuntime {
    private static final Logger LOGGER = LoggerFactory
            .getLogger(TradeRushRuntime.class);

    private final TradeRushPersistence persistence;
    private final TeamRepository teamRepository;
    private final TeamService teamService;
    private final ShopRepository shopRepository;
    private final ShopService shopService;
    private Optional<ManagementBlockLocation> managementBlockLocation;

    private TradeRushRuntime(
            MinecraftServer server,
            TradeRushPersistence persistence
    ) {
        this.persistence = persistence;

        this.teamRepository = new InMemoryTeamRepository();
        loadTeamsSafely();
        this.teamService = new TeamService(
                teamRepository,
                this::saveTeamsSafely
        );

        this.shopRepository = new InMemoryShopRepository();
        boolean shopStateFound = loadShopsSafely();

        if (!shopStateFound) {
            setupSpawnSafely(server);

            boolean generated = runShopGenerationSafely(server);

            if (generated) {
                saveShopsSafely();
            }
        }

        this.shopService = new ShopService(
                shopRepository,
                this::saveShopsSafely
        );

        managementBlockLocation = loadWorldStateSafely();

        if (managementBlockLocation.isEmpty()) {
            managementBlockLocation = placeManagementBlockSafely(server);

            if (managementBlockLocation.isPresent()) {
                saveWorldStateSafely();
            }
        }
    }

    public static TradeRushRuntime create(MinecraftServer server) {
        return new TradeRushRuntime(
                server,
                TradeRushPersistence.create(server)
        );
    }

    public TeamService teamService() {
        return teamService;
    }

    public ShopService shopService() {
        return shopService;
    }

    public Optional<ManagementBlockLocation> managementBlockLocation() {
        return managementBlockLocation;
    }

    public void saveStateSafely() {
        saveTeamsSafely();
        saveShopsSafely();
    }

    private void loadTeamsSafely() {
        try {
            Optional<TeamStateSnapshot> snapshot = persistence.teamStateStore()
                    .load();

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
            TeamStateSnapshot snapshot = TeamStateMapper
                    .toSnapshot(teamRepository);
            persistence.teamStateStore().save(snapshot);
        } catch (Exception exception) {
            LOGGER.error("Failed to save TradeRush team state.", exception);
        }
    }

    /**
     * @return true if saved shop state was found and restored; false if no
     *         state existed
     */
    private boolean loadShopsSafely() {
        try {
            Optional<ShopStateSnapshot> snapshot = persistence.shopStateStore()
                    .load();

            if (snapshot.isEmpty()) {
                LOGGER.info("No existing TradeRush shop state found.");
                return false;
            }

            ShopStateMapper.restoreInto(shopRepository, snapshot.get());
            return true;
        } catch (Exception exception) {
            LOGGER.error("Failed to load TradeRush shop state.", exception);
            return false;
        }
    }

    private void saveShopsSafely() {
        try {
            ShopStateSnapshot snapshot = ShopStateMapper
                    .toSnapshot(shopRepository);
            persistence.shopStateStore().save(snapshot);
        } catch (Exception exception) {
            LOGGER.error("Failed to save TradeRush shop state.", exception);
        }
    }

    private Optional<ManagementBlockLocation> loadWorldStateSafely() {
        try {
            Optional<WorldStateSnapshot> snapshot = persistence
                    .worldStateStore()
                    .load();

            if (snapshot.isEmpty()) {
                LOGGER.info("No existing TradeRush world state found.");
                return Optional.empty();
            }

            return WorldStateMapper.extractManagementBlock(snapshot.get());
        } catch (Exception exception) {
            LOGGER.error("Failed to load TradeRush world state.", exception);
            return Optional.empty();
        }
    }

    private void saveWorldStateSafely() {
        try {
            WorldStateSnapshot snapshot = WorldStateMapper
                    .toSnapshot(managementBlockLocation);
            persistence.worldStateStore().save(snapshot);
        } catch (Exception exception) {
            LOGGER.error("Failed to save TradeRush world state.", exception);
        }
    }

    /**
     * @return the placed location, or empty if placement failed
     */
    private Optional<ManagementBlockLocation> placeManagementBlockSafely(
            MinecraftServer server
    ) {
        try {
            LOGGER.info("Placing team management block for new world...");
            return Optional.of(new MinecraftTeamBlockPlacer(server).place());
        } catch (Exception exception) {
            LOGGER.error("Failed to place team management block.", exception);
            return Optional.empty();
        }
    }

    private void setupSpawnSafely(MinecraftServer server) {
        try {
            LOGGER.info("Setting up spawn location for new world...");
            new MinecraftSpawnLocator().findAndSetSpawn(server.overworld());
        } catch (Exception exception) {
            LOGGER.error("Failed to set up spawn location.", exception);
        }
    }

    /**
     * @return true if generation succeeded; false if it failed (state should
     *         not be saved)
     */
    private boolean runShopGenerationSafely(MinecraftServer server) {
        try {
            LOGGER.info("Running shop generation for new world...");
            new MinecraftShopGenerationCoordinator(server)
                    .execute(shopRepository);
            return true;
        } catch (ShopGenerationException exception) {
            LOGGER.error("Shop generation failed.", exception);
            return false;
        } catch (Exception exception) {
            LOGGER.error("Unexpected error during shop generation.", exception);
            return false;
        }
    }
}
