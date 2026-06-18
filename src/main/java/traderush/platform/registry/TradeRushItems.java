package traderush.platform.registry;

import net.fabricmc.fabric.api.creativetab.v1.CreativeModeTabEvents;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.Item;
import traderush.TradeRush;
import traderush.platform.item.TeamRatingBook;
import traderush.platform.item.TeamRatingPaintingItem;

public final class TradeRushItems {
    public static final String TEAM_RATING_BOOK_PATH = "team_rating_book";
    public static final Identifier TEAM_RATING_BOOK_ID = TradeRush
            .id(TEAM_RATING_BOOK_PATH);
    public static final TeamRatingBook TEAM_RATING_BOOK = new TeamRatingBook(
            new Item.Properties()
                    .setId(itemKey(TEAM_RATING_BOOK_ID))
                    .stacksTo(1)
    );
    public static final String TEAM_RATING_PAINTING_PATH = "team_rating_painting";
    public static final Identifier TEAM_RATING_PAINTING_ID = TradeRush
            .id(TEAM_RATING_PAINTING_PATH);
    public static final TeamRatingPaintingItem TEAM_RATING_PAINTING = new TeamRatingPaintingItem(
            new Item.Properties()
                    .setId(itemKey(TEAM_RATING_PAINTING_ID))
                    .stacksTo(16)
    );

    private TradeRushItems() {}

    public static void register() {
        Registry.register(
                BuiltInRegistries.ITEM,
                TEAM_RATING_BOOK_ID,
                TEAM_RATING_BOOK
        );
        Registry.register(
                BuiltInRegistries.ITEM,
                TEAM_RATING_PAINTING_ID,
                TEAM_RATING_PAINTING
        );
        CreativeModeTabEvents
                .modifyOutputEvent(CreativeModeTabs.TOOLS_AND_UTILITIES)
                .register(output -> {
                    output.accept(TEAM_RATING_BOOK);
                    output.accept(TEAM_RATING_PAINTING);
                });
    }

    private static ResourceKey<Item> itemKey(Identifier id) {
        return ResourceKey.create(Registries.ITEM, id);
    }
}
