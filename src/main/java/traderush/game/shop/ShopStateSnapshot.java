package traderush.game.shop;

import java.util.List;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

public record ShopStateSnapshot(
        int version,
        List<OfferShopSnapshot> offerShops,
        List<ContractShopSnapshot> contractShops
) {
    public static final int CURRENT_VERSION = 1;

    public static final Codec<ShopStateSnapshot> CODEC = RecordCodecBuilder.create(
            instance -> instance.group(
                    Codec.INT.fieldOf("version").forGetter(ShopStateSnapshot::version),
                    OfferShopSnapshot.CODEC.listOf()
                            .fieldOf("offerShops")
                            .forGetter(ShopStateSnapshot::offerShops),
                    ContractShopSnapshot.CODEC.listOf()
                            .fieldOf("contractShops")
                            .forGetter(ShopStateSnapshot::contractShops)
            ).apply(instance, ShopStateSnapshot::new)
    );

    public ShopStateSnapshot {
        offerShops = offerShops == null ? List.of() : List.copyOf(offerShops);
        contractShops = contractShops == null ? List.of() : List.copyOf(contractShops);
    }

    public static ShopStateSnapshot empty() {
        return new ShopStateSnapshot(CURRENT_VERSION, List.of(), List.of());
    }

    public record OfferShopSnapshot(
            String id,
            String name,
            String dimensionId,
            int x,
            int y,
            int z
    ) {
        public static final Codec<OfferShopSnapshot> CODEC = RecordCodecBuilder.create(
                instance -> instance.group(
                        Codec.STRING.fieldOf("id").forGetter(OfferShopSnapshot::id),
                        Codec.STRING.fieldOf("name")
                                .forGetter(OfferShopSnapshot::name),
                        Codec.STRING.fieldOf("dimensionId")
                                .forGetter(OfferShopSnapshot::dimensionId),
                        Codec.INT.fieldOf("x").forGetter(OfferShopSnapshot::x),
                        Codec.INT.fieldOf("y").forGetter(OfferShopSnapshot::y),
                        Codec.INT.fieldOf("z").forGetter(OfferShopSnapshot::z)
                ).apply(instance, OfferShopSnapshot::new)
        );
    }

    public record ContractShopSnapshot(
            String id,
            String name,
            String dimensionId,
            int x,
            int y,
            int z,
            String owner,
            List<String> tags
    ) {
        public static final Codec<ContractShopSnapshot> CODEC = RecordCodecBuilder.create(
                instance -> instance.group(
                        Codec.STRING.fieldOf("id").forGetter(ContractShopSnapshot::id),
                        Codec.STRING.fieldOf("name")
                                .forGetter(ContractShopSnapshot::name),
                        Codec.STRING.fieldOf("dimensionId")
                                .forGetter(ContractShopSnapshot::dimensionId),
                        Codec.INT.fieldOf("x").forGetter(ContractShopSnapshot::x),
                        Codec.INT.fieldOf("y").forGetter(ContractShopSnapshot::y),
                        Codec.INT.fieldOf("z").forGetter(ContractShopSnapshot::z),
                        Codec.STRING.fieldOf("owner")
                                .forGetter(ContractShopSnapshot::owner),
                        Codec.STRING.listOf().fieldOf("tags").forGetter(ContractShopSnapshot::tags)
                ).apply(instance, ContractShopSnapshot::new)
        );

        public ContractShopSnapshot {
            tags = tags == null ? List.of() : List.copyOf(tags);
        }
    }
}