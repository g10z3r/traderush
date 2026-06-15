package traderush.game.offer;

public record RewardRange(int minReward, int maxReward) {
    public RewardRange {
        if (minReward < 0) {
            throw new IllegalArgumentException(
                    "minimum reward cannot be negative"
            );
        }

        if (maxReward < minReward) {
            throw new IllegalArgumentException(
                    "maximum reward cannot be less than minimum reward"
            );
        }
    }

    public boolean contains(long reward) {
        return reward >= minReward && reward <= maxReward;
    }
}
