package traderush.platform.registry;

import net.fabricmc.fabric.api.creativetab.v1.CreativeModeTabEvents;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import traderush.TradeRush;
import traderush.platform.block.ShopBlock;
import traderush.platform.block.TeamManagementBlock;

public final class TradeRushBlocks {
    public static final String TEAM_MANAGEMENT_BLOCK_PATH = "team_management_block";
    public static final Identifier TEAM_MANAGEMENT_BLOCK_ID = TradeRush
            .id(TEAM_MANAGEMENT_BLOCK_PATH);
    public static final TeamManagementBlock TEAM_MANAGEMENT_BLOCK = new TeamManagementBlock(
            BlockBehaviour.Properties.of()
                    .setId(blockKey(TEAM_MANAGEMENT_BLOCK_ID))
                    .strength(3.0F, 6.0F)
                    .sound(SoundType.METAL)
    );
    public static final BlockItem TEAM_MANAGEMENT_BLOCK_ITEM = new BlockItem(
            TEAM_MANAGEMENT_BLOCK,
            new Item.Properties().setId(itemKey(TEAM_MANAGEMENT_BLOCK_ID))
                    .useBlockDescriptionPrefix()
    );

    public static final String SHOP_BLOCK_PATH = "shop_block";
    public static final Identifier SHOP_BLOCK_ID = TradeRush
            .id(SHOP_BLOCK_PATH);
    public static final ShopBlock SHOP_BLOCK = new ShopBlock(
            BlockBehaviour.Properties.of()
                    .setId(blockKey(SHOP_BLOCK_ID))
                    .strength(5.0F, 1200.0F)
                    .sound(SoundType.METAL)
    );
    public static final BlockItem SHOP_BLOCK_ITEM = new BlockItem(
            SHOP_BLOCK,
            new Item.Properties().setId(itemKey(SHOP_BLOCK_ID))
                    .useBlockDescriptionPrefix()
    );

    private TradeRushBlocks() {}

    public static void register() {
        Registry.register(
                BuiltInRegistries.BLOCK,
                TEAM_MANAGEMENT_BLOCK_ID,
                TEAM_MANAGEMENT_BLOCK
        );
        Registry.register(
                BuiltInRegistries.ITEM,
                TEAM_MANAGEMENT_BLOCK_ID,
                TEAM_MANAGEMENT_BLOCK_ITEM
        );
        Registry.register(BuiltInRegistries.BLOCK, SHOP_BLOCK_ID, SHOP_BLOCK);
        Registry.register(
                BuiltInRegistries.ITEM,
                SHOP_BLOCK_ID,
                SHOP_BLOCK_ITEM
        );
        CreativeModeTabEvents
                .modifyOutputEvent(CreativeModeTabs.FUNCTIONAL_BLOCKS)
                .register(output -> {
                    output.accept(TEAM_MANAGEMENT_BLOCK_ITEM);
                    output.accept(SHOP_BLOCK_ITEM);
                });
    }

    private static ResourceKey<Block> blockKey(Identifier id) {
        return ResourceKey.create(Registries.BLOCK, id);
    }

    private static ResourceKey<Item> itemKey(Identifier id) {
        return ResourceKey.create(Registries.ITEM, id);
    }
}
