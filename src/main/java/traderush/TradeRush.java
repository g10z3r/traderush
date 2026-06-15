package traderush;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.minecraft.resources.Identifier;
import net.minecraft.server.MinecraftServer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import traderush.platform.command.ShopCommand;
import traderush.platform.command.StatusCommand;
import traderush.platform.command.TeamCommand;
import traderush.platform.protection.MinecraftShopBlockProtection;
import traderush.platform.registry.TradeRushBlocks;
import traderush.platform.registry.TradeRushMenus;
import traderush.platform.ui.team.TeamManagementNetworking;
import traderush.runtime.TradeRushRuntime;

public class TradeRush implements ModInitializer {

    public static final String MOD_ID = "trade-rush";
    public static final String COMMAND_ROOT = MOD_ID.replace("-", "");
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    private static TradeRushRuntime runtime;

    @Override
    public void onInitialize() {
        LOGGER.info("Initializing TradeRush");

        TradeRushBlocks.register();
        TradeRushMenus.register();
        TeamManagementNetworking.registerPayloadTypes();
        TeamManagementNetworking.registerServerHandlers();

        ServerLifecycleEvents.SERVER_STARTED.register(this::startRuntime);
        ServerLifecycleEvents.SERVER_STOPPING.register(this::stopRuntime);

        TeamCommand.register(() -> runtime().teamService());
        ShopCommand.register(() -> runtime().shopService());
        MinecraftShopBlockProtection.register(() -> runtime().shopService());

        CommandRegistrationCallback.EVENT
                .register((dispatcher, registryAccess, environment) -> {
                    StatusCommand.register(dispatcher);
                });

        LOGGER.info("Hello Fabric world!");
    }

    private void startRuntime(MinecraftServer server) {
        runtime = TradeRushRuntime.create(server);
        LOGGER.info("TradeRush runtime initialized");
    }

    private void stopRuntime(MinecraftServer server) {
        if (runtime != null) {
            runtime.saveStateSafely();
            runtime = null;
        }

        LOGGER.info("TradeRush runtime stopped");
    }

    public static Identifier id(String path) {
        return Identifier.fromNamespaceAndPath(MOD_ID, path);
    }

    public static TradeRushRuntime runtime() {
        if (runtime == null) {
            throw new IllegalStateException(
                    "TradeRush runtime is not initialized yet"
            );
        }

        return runtime;
    }
}
