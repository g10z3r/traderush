package traderush.game.shop;

import java.util.Arrays;
import java.util.Locale;
import java.util.Optional;

public enum ShopTag {
    MILITARY("military"),
    FARMING("farming"),
    ALCHEMY("alchemy"),
    MINING("mining");

    private final String serializedName;

    ShopTag(String serializedName) {
        this.serializedName = serializedName;
    }

    public String getSerializedName() {
        return serializedName;
    }

    public static Optional<ShopTag> fromSerializedName(String value) {
        if (value == null || value.isBlank()) {
            return Optional.empty();
        }

        String normalized = value.trim().toLowerCase(Locale.ROOT);

        return Arrays.stream(values())
                .filter(tag -> tag.serializedName.equals(normalized))
                .findFirst();
    }
}
