package traderush.platform.ui.shop;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import traderush.TradeRush;

/**
 * Client → server: player wants to trade the selected offer at a shop.
 * {@code fixedReward} is the reward value pre-selected when offers were sent,
 * so the server awards the same amount that was displayed in the UI.
 */
public record ShopTradeActionPayload(
        String shopId,
        String offerId,
        int fixedReward
)
        implements CustomPacketPayload {

    public static final Type<ShopTradeActionPayload> TYPE = new Type<>(
            TradeRush.id("shop_trade_action")
    );
    public static final StreamCodec<RegistryFriendlyByteBuf, ShopTradeActionPayload> CODEC = StreamCodec
            .ofMember(
                    ShopTradeActionPayload::write,
                    ShopTradeActionPayload::read
            );

    private void write(RegistryFriendlyByteBuf buf) {
        buf.writeUtf(shopId == null ? "" : shopId);
        buf.writeUtf(offerId == null ? "" : offerId);
        buf.writeInt(fixedReward);
    }

    private static ShopTradeActionPayload read(RegistryFriendlyByteBuf buf) {
        return new ShopTradeActionPayload(
                buf.readUtf(),
                buf.readUtf(),
                buf.readInt()
        );
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
