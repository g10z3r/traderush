package traderush.game.offer;

import java.util.Objects;
import java.util.UUID;

public record ActiveOfferId(UUID value) {
    public ActiveOfferId {
        Objects.requireNonNull(value, "active offer id cannot be null");
    }

    public static ActiveOfferId newId() {
        return new ActiveOfferId(UUID.randomUUID());
    }

    public static ActiveOfferId fromString(String value) {
        return new ActiveOfferId(UUID.fromString(value));
    }

    @Override
    public String toString() {
        return value.toString();
    }
}