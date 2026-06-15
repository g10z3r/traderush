package traderush.game.offer;

import java.util.UUID;

public record OfferId(UUID value) {
    public static OfferId fromUuid(UUID value) {
        return new OfferId(value);
    }

    public static OfferId fromString(String value) {
        return new OfferId(UUID.fromString(value));
    }

    @Override
    public String toString() {
        return value.toString();
    }
}
