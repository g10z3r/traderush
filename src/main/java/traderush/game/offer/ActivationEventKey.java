package traderush.game.offer;

import java.util.Locale;

public record ActivationEventKey(String value) {
    public ActivationEventKey {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(
                    "activation event key cannot be blank"
            );
        }

        value = value.trim().toLowerCase(Locale.ROOT);
    }

    public static ActivationEventKey of(String value) {
        return new ActivationEventKey(value);
    }

    @Override
    public String toString() {
        return value;
    }
}
