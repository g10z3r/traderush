package traderush.platform.storage.shop;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import traderush.game.shop.ShopStateSnapshot;

public final class ShopStateSnapshotCodec {
    public static final Codec<ShopStateSnapshot.OfferShopSnapshot> OFFER_SHOP_SNAPSHOT =
            RecordCodecBuilder.create(
                    instance -> instance.group(
                            Codec.STRING.fieldOf("id")
                                    .forGetter(ShopStateSnapshot.OfferShopSnapshot::id),
                            Codec.STRING.fieldOf("name")
                                    .forGetter(ShopStateSnapshot.OfferShopSnapshot::name),
                            Codec.STRING.fieldOf("dimensionId")
                                    .forGetter(ShopStateSnapshot.OfferShopSnapshot::dimensionId),
                            Codec.INT.fieldOf("x")
                                    .forGetter(ShopStateSnapshot.OfferShopSnapshot::x),
                            Codec.INT.fieldOf("y")
                                    .forGetter(ShopStateSnapshot.OfferShopSnapshot::y),
                            Codec.INT.fieldOf("z")
                                    .forGetter(ShopStateSnapshot.OfferShopSnapshot::z)
                    ).apply(instance, ShopStateSnapshot.OfferShopSnapshot::new)
            );

    public static final Codec<ShopStateSnapshot.ContractShopSnapshot> CONTRACT_SHOP_SNAPSHOT =
            RecordCodecBuilder.create(
                    instance -> instance.group(
                            Codec.STRING.fieldOf("id")
                                    .forGetter(ShopStateSnapshot.ContractShopSnapshot::id),
                            Codec.STRING.fieldOf("name")
                                    .forGetter(ShopStateSnapshot.ContractShopSnapshot::name),
                            Codec.STRING.fieldOf("dimensionId")
                                    .forGetter(ShopStateSnapshot.ContractShopSnapshot::dimensionId),
                            Codec.INT.fieldOf("x")
                                    .forGetter(ShopStateSnapshot.ContractShopSnapshot::x),
                            Codec.INT.fieldOf("y")
                                    .forGetter(ShopStateSnapshot.ContractShopSnapshot::y),
                            Codec.INT.fieldOf("z")
                                    .forGetter(ShopStateSnapshot.ContractShopSnapshot::z),
                            Codec.STRING.fieldOf("owner")
                                    .forGetter(ShopStateSnapshot.ContractShopSnapshot::owner),
                            Codec.STRING.listOf().fieldOf("tags")
                                    .forGetter(ShopStateSnapshot.ContractShopSnapshot::tags)
                    ).apply(instance, ShopStateSnapshot.ContractShopSnapshot::new)
            );

    public static final Codec<ShopStateSnapshot> SNAPSHOT = RecordCodecBuilder.create(
            instance -> instance.group(
                    Codec.INT.fieldOf("version")
                            .forGetter(ShopStateSnapshot::version),
                    OFFER_SHOP_SNAPSHOT.listOf().fieldOf("offerShops")
                            .forGetter(ShopStateSnapshot::offerShops),
                    CONTRACT_SHOP_SNAPSHOT.listOf().fieldOf("contractShops")
                            .forGetter(ShopStateSnapshot::contractShops)
            ).apply(instance, ShopStateSnapshot::new)
    );

    private ShopStateSnapshotCodec() {}
}
