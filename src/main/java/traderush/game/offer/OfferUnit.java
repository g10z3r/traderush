package traderush.game.offer;

import java.util.List;

public record OfferUnit(List<ItemRequirement> requirements) {
    public OfferUnit {
        if (requirements == null || requirements.isEmpty()) {
            throw new IllegalArgumentException(
                    "offer unit must contain at least one item requirement"
            );
        }

        requirements = List.copyOf(requirements);

        for (ItemRequirement requirement : requirements) {
            if (requirement == null) {
                throw new IllegalArgumentException(
                        "offer unit cannot contain null item requirement"
                );
            }
        }
    }

    public static OfferUnit of(ItemRequirement first, ItemRequirement... rest) {
        if (rest == null || rest.length == 0) {
            return new OfferUnit(List.of(first));
        }

        java.util.ArrayList<ItemRequirement> requirements = new java.util.ArrayList<>();
        requirements.add(first);
        requirements.addAll(List.of(rest));

        return new OfferUnit(requirements);
    }

    public List<ItemRequirement> requirementsForUnits(int units) {
        if (units <= 0) {
            throw new IllegalArgumentException("units must be positive");
        }

        return requirements.stream()
                .map(requirement -> requirement.multiply(units))
                .toList();
    }
}
