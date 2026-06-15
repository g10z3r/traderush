package traderush.game.sale;

import java.util.Objects;

public final class SaleOperationResult<T> {
    private final T value;
    private final SaleError error;

    private SaleOperationResult(T value, SaleError error) {
        this.value = value;
        this.error = error;
    }

    public static <T> SaleOperationResult<T> success(T value) {
        return new SaleOperationResult<>(
                Objects.requireNonNull(value, "success value cannot be null"),
                null
        );
    }

    public static <T> SaleOperationResult<T> failure(SaleError error) {
        return new SaleOperationResult<>(
                null,
                Objects.requireNonNull(error, "sale error cannot be null")
        );
    }

    public boolean isSuccess() {
        return error == null;
    }

    public boolean isFailure() {
        return error != null;
    }

    public T value() {
        if (isFailure()) {
            throw new IllegalStateException("Cannot get value from failed sale operation.");
        }

        return value;
    }

    public SaleError error() {
        if (isSuccess()) {
            throw new IllegalStateException("Cannot get error from successful sale operation.");
        }

        return error;
    }
}