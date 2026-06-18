package traderush.platform.ui.shop;

import java.util.ArrayList;
import java.util.List;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import traderush.TradeRush;

/**
 * Server → client packet carrying the full list of offers for a shop.
 */
public record ShopOffersPayload(
        String shopId,
        String shopName,
        long serverTick,
        List<ShopOfferEntry> offers
) implements CustomPacketPayload {

    public static final Type<ShopOffersPayload> TYPE = new Type<>(
            TradeRush.id("shop_offers")
    );
    public static final StreamCodec<RegistryFriendlyByteBuf, ShopOffersPayload> CODEC = StreamCodec
            .ofMember(
                    ShopOffersPayload::write,
                    ShopOffersPayload::read
            );

    public ShopOffersPayload {
        shopId = shopId == null ? "" : shopId;
        shopName = shopName == null ? "" : shopName;
        offers = offers == null ? List.of() : List.copyOf(offers);
    }

    private void write(RegistryFriendlyByteBuf buf) {
        buf.writeUtf(shopId);
        buf.writeUtf(shopName);
        buf.writeLong(serverTick);
        buf.writeVarInt(offers.size());

        for (ShopOfferEntry entry : offers) {
            entry.write(buf);
        }
    }

    private static ShopOffersPayload read(RegistryFriendlyByteBuf buf) {
        String shopId = buf.readUtf();
        String shopName = buf.readUtf();
        long serverTick = buf.readLong();
        int count = buf.readVarInt();
        List<ShopOfferEntry> offers = new ArrayList<>(count);

        for (int i = 0; i < count; i++) {
            offers.add(ShopOfferEntry.read(buf));
        }

        return new ShopOffersPayload(
                shopId,
                shopName,
                serverTick,
                List.copyOf(offers)
        );
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
