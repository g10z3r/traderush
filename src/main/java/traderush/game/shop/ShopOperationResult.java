package traderush.game.shop;

import java.util.Objects;

public final class ShopOperationResult<T> {
    private final T value;
    private final ShopError error;

    private ShopOperationResult(T value, ShopError error) {
        this.value = value;
        this.error = error;
    }

    public static <T> ShopOperationResult<T> success(T value) {
        return new ShopOperationResult<>(
                Objects.requireNonNull(value, "success value cannot be null"),
                null
        );
    }

    public static <T> ShopOperationResult<T> failure(ShopError error) {
        return new ShopOperationResult<>(
                null,
                Objects.requireNonNull(error, "shop error cannot be null")
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
            throw new IllegalStateException("Cannot get value from failed shop operation.");
        }

        return value;
    }

    public ShopError error() {
        if (isSuccess()) {
            throw new IllegalStateException("Cannot get error from successful shop operation.");
        }

        return error;
    }
}
