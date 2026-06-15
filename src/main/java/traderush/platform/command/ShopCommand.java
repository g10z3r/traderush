package traderush.platform.command;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import java.util.List;
import java.util.function.Supplier;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import traderush.TradeRush;
import traderush.game.shop.ContractShop;
import traderush.game.shop.OfferShop;
import traderush.game.shop.Shop;
import traderush.game.shop.ShopService;

public final class ShopCommand {
    private ShopCommand() {}

    public static void register(Supplier<ShopService> shopServiceSupplier) {
        CommandRegistrationCallback.EVENT
                .register((dispatcher, registryAccess, environment) -> {
                    dispatcher.register(
                            root(TradeRush.COMMAND_ROOT, shopServiceSupplier)
                    );
                    dispatcher.register(root("tr", shopServiceSupplier));
                });
    }

    private static LiteralArgumentBuilder<CommandSourceStack> root(
            String literal,
            Supplier<ShopService> shopServiceSupplier
    ) {
        return Commands.literal(literal)
                .then(
                        Commands.literal("shop")
                                .then(
                                        Commands.literal("list")
                                                .executes(
                                                        context -> listShops(
                                                                context.getSource(),
                                                                shopServiceSupplier
                                                                        .get()
                                                        )
                                                )
                                )
                );
    }

    private static int listShops(
            CommandSourceStack source,
            ShopService shopService
    ) {
        List<Shop> shops = shopService.listShops();

        if (shops.isEmpty()) {
            source.sendSuccess(
                    () -> Component.literal("No shops registered."),
                    false
            );
            return Command.SINGLE_SUCCESS;
        }

        source.sendSuccess(
                () -> Component.literal("Shops (" + shops.size() + "):"),
                false
        );

        for (Shop shop : shops) {
            String line = formatShopSummary(shop);
            source.sendSuccess(() -> Component.literal(line), false);
        }

        return Command.SINGLE_SUCCESS;
    }

    private static String formatShopSummary(Shop shop) {
        return "- " + shop.getName() + " | type: " + shopType(shop) + " | id: "
                + shop.getId()
                + " | location: " + shop.getLocation().toBlockPosString();
    }

    private static String shopType(Shop shop) {
        if (shop instanceof OfferShop) {
            return "offer";
        }

        if (shop instanceof ContractShop) {
            return "contract";
        }

        return "unknown";
    }
}
