package traderush.platform.ui.shop;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import traderush.TradeRush;

/** Server → client: result of a trade attempt. */
public record ShopTradeResultPayload(
        boolean success,
        String message,
        long pointsAwarded
)
        implements CustomPacketPayload {

    public static final Type<ShopTradeResultPayload> TYPE = new Type<>(
            TradeRush.id("shop_trade_result")
    );
    public static final StreamCodec<RegistryFriendlyByteBuf, ShopTradeResultPayload> CODEC = StreamCodec
            .ofMember(
                    ShopTradeResultPayload::write,
                    ShopTradeResultPayload::read
            );

    private void write(RegistryFriendlyByteBuf buf) {
        buf.writeBoolean(success);
        buf.writeUtf(message == null ? "" : message);
        buf.writeLong(pointsAwarded);
    }

    private static ShopTradeResultPayload read(RegistryFriendlyByteBuf buf) {
        return new ShopTradeResultPayload(
                buf.readBoolean(),
                buf.readUtf(),
                buf.readLong()
        );
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
