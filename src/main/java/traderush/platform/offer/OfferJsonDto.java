package traderush.platform.offer;

import java.util.List;

/**
 * Raw DTOs used when deserialising offer JSON files. Not part of the domain.
 */
public final class OfferJsonDto {

    private OfferJsonDto() {}

    public record ItemRequirementDto(String item, int quantity) {
    }

    public record LimitedOfferDto(
            String id,
            String name,
            String description,
            int minReward,
            int maxReward,
            List<List<ItemRequirementDto>> requirements,
            int completionThreshold,
            List<String> activationEvents
    ) {
    }

    public record TimedOfferDto(
            String id,
            String name,
            String description,
            int minReward,
            int maxReward,
            List<List<ItemRequirementDto>> requirements,
            int minAvailabilityDurationSeconds,
            int maxAvailabilityDurationSeconds,
            long completionThreshold,
            boolean repeatable,
            List<String> activationEvents
    ) {
    }
}
