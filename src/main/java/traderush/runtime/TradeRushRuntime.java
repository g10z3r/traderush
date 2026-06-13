package traderush.runtime;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import traderush.game.team.TeamRepository;
import traderush.game.team.TeamService;
import traderush.platform.storage.TradeRushPersistence;
import net.minecraft.server.MinecraftServer;
import java.util.Optional;
import traderush.game.team.TeamStateSnapshot;
import traderush.game.team.TeamStateMapper;
import traderush.game.team.InMemoryTeamRepository;

public final class TradeRushRuntime {
    private static final Logger LOGGER = LoggerFactory.getLogger(TradeRushRuntime.class);

    private final TradeRushPersistence persistence;
    private final TeamRepository teamRepository;
    private final TeamService teamService;

    private TradeRushRuntime(TradeRushPersistence persistence) {
        this.persistence = persistence;
        this.teamRepository = new InMemoryTeamRepository();

        loadTeamsSafely();

        this.teamService = new TeamService(teamRepository, this::saveTeamsSafely);
    }

    public static TradeRushRuntime create(MinecraftServer server) {
        return new TradeRushRuntime(TradeRushPersistence.create(server));
    }

    public TeamService teamService() {
        return teamService;
    }

    public void saveStateSafely() {
        saveTeamsSafely();
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
}
