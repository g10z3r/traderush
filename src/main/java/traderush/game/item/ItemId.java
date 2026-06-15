package traderush.game.item;

import java.util.Objects;
import java.util.UUID;

public record ItemId(UUID value) {
    public ItemId {
        Objects.requireNonNull(value, "item id cannot be null");
    }

    public static ItemId fromUuid(UUID value) {
        return new ItemId(value);
    }

    public static ItemId fromString(String value) {
        return new ItemId(UUID.fromString(value));
    }

    @Override
    public String toString() {
        return value.toString();
    }
}
