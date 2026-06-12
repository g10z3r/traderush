package traderush.game.player;

import java.util.UUID;

public record PlayerId(UUID value) {
    public static PlayerId fromUuid(UUID value) {
        return new PlayerId(value);
    }

    public static PlayerId fromString(String value) {
        return new PlayerId(UUID.fromString(value));
    }

    @Override
    public String toString() {
        return value.toString();
    }
}
