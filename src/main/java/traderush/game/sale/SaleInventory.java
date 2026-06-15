package traderush.game.sale;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.ArrayList;

import traderush.game.item.ItemId;
import traderush.game.offer.OfferUnit;
import traderush.game.offer.ItemRequirement;

final class SaleInventory {
    private final Map<ItemId, Integer> quantitiesByItemId = new LinkedHashMap<>();

    private SaleInventory() {}

    public static SaleInventory fromItems(List<SaleItemStack> items) {
        SaleInventory inventory = new SaleInventory();

        if (items == null) {
            return inventory;
        }

        for (SaleItemStack item : items) {
            if (item == null) {
                continue;
            }

            inventory.quantitiesByItemId.merge(
                    item.itemId(),
                    item.quantity(),
                    Integer::sum
            );
        }

        return inventory;
    }

    public int maxUnitsFor(OfferUnit unit) {
        int maxUnits = Integer.MAX_VALUE;

        for (ItemRequirement requirement : unit.requirements()) {
            int available = quantitiesByItemId.getOrDefault(requirement.itemId(), 0);
            maxUnits = Math.min(maxUnits, available / requirement.quantity());
        }

        return maxUnits == Integer.MAX_VALUE ? 0 : maxUnits;
    }

    public boolean consume(List<ItemRequirement> requirements) {
        if (!canConsume(requirements)) {
            return false;
        }

        for (ItemRequirement requirement : requirements) {
            quantitiesByItemId.computeIfPresent(
                    requirement.itemId(),
                    (itemId, quantity) -> quantity - requirement.quantity()
            );
        }

        quantitiesByItemId.entrySet().removeIf(entry -> entry.getValue() <= 0);

        return true;
    }

    public List<SaleItemStack> remainingItems() {
        List<SaleItemStack> remaining = new ArrayList<>();

        for (Map.Entry<ItemId, Integer> entry : quantitiesByItemId.entrySet()) {
            remaining.add(new SaleItemStack(entry.getKey(), entry.getValue()));
        }

        return List.copyOf(remaining);
    }

    private boolean canConsume(List<ItemRequirement> requirements) {
        for (ItemRequirement requirement : requirements) {
            int available = quantitiesByItemId.getOrDefault(requirement.itemId(), 0);

            if (available < requirement.quantity()) {
                return false;
            }
        }

        return true;
    }
}
