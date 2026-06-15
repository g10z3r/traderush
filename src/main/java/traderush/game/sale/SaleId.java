package traderush.game.sale;

import java.util.Objects;
import java.util.UUID;

public record SaleId(UUID value) {
    public SaleId {
        Objects.requireNonNull(value, "sale id cannot be null");
    }

    public static SaleId fromUuid(UUID value) {
        return new SaleId(value);
    }

    public static SaleId fromString(String value) {
        return new SaleId(UUID.fromString(value));
    }

    @Override
    public String toString() {
        return value.toString();
    }
}
