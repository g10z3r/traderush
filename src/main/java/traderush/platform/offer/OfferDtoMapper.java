package traderush.platform.offer;

import java.util.List;
import traderush.game.item.ItemId;
import traderush.game.offer.ActivationEventKey;
import traderush.game.offer.ItemRequirement;
import traderush.game.offer.LimitedOffer;
import traderush.game.offer.OfferId;
import traderush.game.offer.OfferUnit;
import traderush.game.offer.RewardRange;
import traderush.game.offer.TimedOffer;
import traderush.platform.offer.OfferJsonDto.ItemRequirementDto;
import traderush.platform.offer.OfferJsonDto.LimitedOfferDto;
import traderush.platform.offer.OfferJsonDto.TimedOfferDto;

public final class OfferDtoMapper {

    private OfferDtoMapper() {}

    public static LimitedOffer toLimitedOffer(LimitedOfferDto dto) {
        return new LimitedOffer(
                OfferId.fromName(dto.id()),
                dto.name(),
                dto.description(),
                new RewardRange(dto.minReward(), dto.maxReward()),
                toUnits(dto.requirements()),
                toActivationEvents(dto.activationEvents()),
                dto.completionThreshold()
        );
    }

    public static TimedOffer toTimedOffer(TimedOfferDto dto) {
        return new TimedOffer(
                OfferId.fromName(dto.id()),
                dto.name(),
                dto.description(),
                new RewardRange(dto.minReward(), dto.maxReward()),
                toUnits(dto.requirements()),
                toActivationEvents(dto.activationEvents()),
                dto.minAvailabilityDurationSeconds(),
                dto.maxAvailabilityDurationSeconds(),
                dto.completionThreshold(),
                dto.repeatable()
        );
    }

    private static List<OfferUnit> toUnits(
            List<List<ItemRequirementDto>> requirements
    ) {
        return requirements.stream()
                .map(OfferDtoMapper::toUnit)
                .toList();
    }

    private static OfferUnit toUnit(List<ItemRequirementDto> dtos) {
        List<ItemRequirement> reqs = dtos.stream()
                .map(
                        dto -> new ItemRequirement(
                                ItemId.fromString(dto.item()),
                                dto.quantity()
                        )
                )
                .toList();
        return new OfferUnit(reqs);
    }

    private static List<ActivationEventKey> toActivationEvents(
            List<String> events
    ) {
        if (events == null) {
            return List.of();
        }
        return events.stream()
                .map(ActivationEventKey::of)
                .toList();
    }
}
