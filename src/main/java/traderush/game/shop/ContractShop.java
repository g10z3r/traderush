package traderush.game.shop;

import java.util.LinkedHashSet;
import java.util.Collections;
import java.util.Set;

public final class ContractShop extends Shop {
    private final Set<ShopTag> tags;
    private final ShopOwner owner;

    public ContractShop(
            ShopId id,
            String name,
            ShopLocation location,
            ShopOwner owner,
            Set<ShopTag> tags

    ) {
        super(id, name, location);

        if (tags == null || tags.isEmpty()) {
            throw new IllegalArgumentException("tags cannot be null or empty");
        }

        if (owner == null) {
            throw new IllegalArgumentException("owner cannot be null");
        }

        this.owner = owner;
        this.tags = Collections.unmodifiableSet(new LinkedHashSet<>(tags));
    }

    public Set<ShopTag> getTags() {
        return tags;
    }

    public ShopOwner getOwner() {
        return owner;
    }
}
