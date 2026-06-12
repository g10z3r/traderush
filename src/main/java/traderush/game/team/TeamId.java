package traderush.game.team;

import java.util.UUID;

public record TeamId(UUID value) {
    public static TeamId fromUuid(UUID value) {
        return new TeamId(value);
    }

    public static TeamId fromString(String value) {
        return new TeamId(UUID.fromString(value));
    }

    @Override
    public String toString() {
        return value.toString();
    }
}
