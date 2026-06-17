package traderush.game.item;

import java.util.Objects;

public record ItemId(String value) {
    public ItemId {
        Objects.requireNonNull(value, "item id cannot be null");

        if (value.isBlank()) {
            throw new IllegalArgumentException("item id cannot be blank");
        }
    }

    public static ItemId fromString(String value) {
        return new ItemId(value);
    }

    @Override
    public String toString() {
        return value;
    }
}
