package traderush;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import traderush.game.team.InMemoryTeamRepository;
import traderush.game.team.TeamService;
import traderush.platform.command.StatusCommand;
import traderush.platform.command.TeamCommand;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TradeRush implements ModInitializer {
	public static final String MOD_ID = "trade-rush";
	public static final String COMMAND_ROOT = MOD_ID.replace("-", "");

	// This logger is used to write text to the console and the log file.
	// It is considered best practice to use your mod id as the logger's name.
	// That way, it's clear which mod wrote info, warnings, and errors.
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

	@Override
	public void onInitialize() {
		// This code runs as soon as Minecraft is in a mod-load-ready state.
		// However, some things (like resources) may still be uninitialized.
		// Proceed with mild caution.

		TeamService teamService = new TeamService(new InMemoryTeamRepository(), () -> {});

		CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> {
			StatusCommand.register(dispatcher);
		});

		TeamCommand.register(() -> teamService);

		LOGGER.info("Hello Fabric world!");
	}
}