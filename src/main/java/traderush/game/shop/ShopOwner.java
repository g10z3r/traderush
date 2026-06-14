package traderush.game.shop;

// TODO: Phase 2
public record ShopOwner(String value) {
    public static ShopOwner fromString(String value) {
        return new ShopOwner(value);
    }

    @Override
    public String toString() {
        return value;
    }
}
