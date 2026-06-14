package traderush.game.shop;

import java.util.Objects;

public abstract class Shop {
    private final ShopId id;
    private final String name;
    private final ShopLocation location;

    protected Shop(
            ShopId id,
            String name,
            ShopLocation location
    ) {
        this.id = Objects.requireNonNull(id, "shop id cannot be null");
        this.name = Objects.requireNonNull(name, "shop name cannot be null");
        this.location = Objects.requireNonNull(location, "shop location cannot be null");
    }

    public ShopId getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public ShopLocation getLocation() {
        return location;
    }
}
