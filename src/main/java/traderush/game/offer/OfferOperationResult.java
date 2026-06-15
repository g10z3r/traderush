package traderush.game.offer;

import java.util.Objects;

public final class OfferOperationResult<T> {
    private final T value;
    private final OfferError error;

    private OfferOperationResult(T value, OfferError error) {
        this.value = value;
        this.error = error;
    }

    public static <T> OfferOperationResult<T> success(T value) {
        return new OfferOperationResult<>(
                Objects.requireNonNull(value, "success value cannot be null"),
                null
        );
    }

    public static <T> OfferOperationResult<T> failure(OfferError error) {
        return new OfferOperationResult<>(
                null,
                Objects.requireNonNull(error, "offer error cannot be null")
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
            throw new IllegalStateException("Cannot get value from failed offer operation.");
        }

        return value;
    }

    public OfferError error() {
        if (isSuccess()) {
            throw new IllegalStateException("Cannot get error from successful offer operation.");
        }

        return error;
    }
}
