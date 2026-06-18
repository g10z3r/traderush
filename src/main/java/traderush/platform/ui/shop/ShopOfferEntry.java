package traderush.platform.ui.shop;

import java.util.List;
import net.minecraft.network.RegistryFriendlyByteBuf;
import traderush.game.offer.ActiveOffer;
import traderush.game.offer.ItemRequirement;
import traderush.game.offer.Offer;
import traderush.game.offer.OfferKind;
import traderush.game.offer.OfferUnit;
import traderush.game.offer.TimedActiveOffer;

/**
 * A serialisable snapshot of a single offer, used for the shop UI packet.
 * {@code fixedReward} comes from the active offer's pre-selected reward.
 */
public record ShopOfferEntry(
        String id,
        String name,
        String description,
        int minReward,
        int maxReward,
        int fixedReward,
        long expiresAtTick,
        OfferKind kind,
        List<UnitEntry> units
) {

    public static final long NO_EXPIRY_TICK = -1L;

    public ShopOfferEntry {
        units = units == null ? List.of() : List.copyOf(units);
    }

    public static ShopOfferEntry from(Offer offer, ActiveOffer activeOffer) {
        List<UnitEntry> units = offer.getUnits()
                .stream()
                .map(UnitEntry::from)
                .toList();

        return new ShopOfferEntry(
                offer.getId().toString(),
                offer.getName(),
                offer.getDescription(),
                offer.getRewardRange().minReward(),
                offer.getRewardRange().maxReward(),
                Math.toIntExact(activeOffer.getRewardPerUnit()),
                expiresAtTick(activeOffer),
                offer.getKind(),
                units
        );
    }

    private static long expiresAtTick(ActiveOffer activeOffer) {
        if (activeOffer instanceof TimedActiveOffer timed) {
            return timed.getEndsAtTick();
        }

        return NO_EXPIRY_TICK;
    }

    public void write(RegistryFriendlyByteBuf buf) {
        buf.writeUtf(id);
        buf.writeUtf(name);
        buf.writeUtf(description);
        buf.writeInt(minReward);
        buf.writeInt(maxReward);
        buf.writeInt(fixedReward);
        buf.writeLong(expiresAtTick);
        buf.writeUtf(kind.name());
        buf.writeVarInt(units.size());

        for (UnitEntry unit : units) {
            unit.write(buf);
        }
    }

    public static ShopOfferEntry read(RegistryFriendlyByteBuf buf) {
        String id = buf.readUtf();
        String name = buf.readUtf();
        String description = buf.readUtf();
        int minReward = buf.readInt();
        int maxReward = buf.readInt();
        int fixedReward = buf.readInt();
        long expiresAtTick = buf.readLong();
        OfferKind kind = OfferKind.valueOf(buf.readUtf());
        int unitCount = buf.readVarInt();
        List<UnitEntry> units = new java.util.ArrayList<>(unitCount);

        for (int i = 0; i < unitCount; i++) {
            units.add(UnitEntry.read(buf));
        }

        return new ShopOfferEntry(
                id,
                name,
                description,
                minReward,
                maxReward,
                fixedReward,
                expiresAtTick,
                kind,
                List.copyOf(units)
        );
    }

    public record UnitEntry(List<RequirementEntry> requirements) {
        public UnitEntry {
            requirements = requirements == null
                    ? List.of()
                    : List.copyOf(requirements);
        }

        public static UnitEntry from(OfferUnit unit) {
            return new UnitEntry(
                    unit.requirements()
                            .stream()
                            .map(RequirementEntry::from)
                            .toList()
            );
        }

        public void write(RegistryFriendlyByteBuf buf) {
            buf.writeVarInt(requirements.size());

            for (RequirementEntry req : requirements) {
                req.write(buf);
            }
        }

        public static UnitEntry read(RegistryFriendlyByteBuf buf) {
            int count = buf.readVarInt();
            List<RequirementEntry> reqs = new java.util.ArrayList<>(count);

            for (int i = 0; i < count; i++) {
                reqs.add(RequirementEntry.read(buf));
            }

            return new UnitEntry(List.copyOf(reqs));
        }
    }

    public record RequirementEntry(String itemId, int quantity) {
        public static RequirementEntry from(ItemRequirement req) {
            return new RequirementEntry(req.itemId().value(), req.quantity());
        }

        public void write(RegistryFriendlyByteBuf buf) {
            buf.writeUtf(itemId);
            buf.writeInt(quantity);
        }

        public static RequirementEntry read(RegistryFriendlyByteBuf buf) {
            return new RequirementEntry(buf.readUtf(), buf.readInt());
        }
    }
}
