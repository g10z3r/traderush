package traderush.game.shop.generation;

public final class ShopGenerationException extends RuntimeException {
    public ShopGenerationException(String message) {
        super(message);
    }

    public ShopGenerationException(String message, Throwable cause) {
        super(message, cause);
    }
}
