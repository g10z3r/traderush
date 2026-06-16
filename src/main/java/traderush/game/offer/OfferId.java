package traderush.game.offer;

import java.nio.charset.StandardCharsets;
import java.util.UUID;

public record OfferId(UUID value) {
    public static OfferId fromUuid(UUID value) {
        return new OfferId(value);
    }

    public static OfferId fromString(String value) {
        return new OfferId(UUID.fromString(value));
    }

    /**
     * Creates a deterministic {@code OfferId} from any string key (e.g. a short
     * alphanumeric id loaded from JSON). Uses a type-3 UUID derived from the
     * key bytes so equality is stable across restarts.
     */
    public static OfferId fromName(String name) {
        return new OfferId(
                UUID.nameUUIDFromBytes(name.getBytes(StandardCharsets.UTF_8))
        );
    }

    @Override
    public String toString() {
        return value.toString();
    }
}
