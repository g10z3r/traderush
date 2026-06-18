package traderush.platform.ui.rating;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import traderush.TradeRush;

public record TeamRatingPaintingStatePayload(
        TeamRatingPaintingSnapshot snapshot
) implements CustomPacketPayload {
    public static final Type<TeamRatingPaintingStatePayload> TYPE = new Type<>(
            TradeRush.id("team_rating_painting_state")
    );
    public static final StreamCodec<RegistryFriendlyByteBuf, TeamRatingPaintingStatePayload> CODEC = StreamCodec
            .ofMember(
                    TeamRatingPaintingStatePayload::write,
                    TeamRatingPaintingStatePayload::read
            );

    public TeamRatingPaintingStatePayload {
        snapshot = snapshot == null ? TeamRatingPaintingSnapshot.EMPTY
                : snapshot;
    }

    private void write(RegistryFriendlyByteBuf buf) {
        snapshot.write(buf);
    }

    private static TeamRatingPaintingStatePayload read(
            RegistryFriendlyByteBuf buf
    ) {
        return new TeamRatingPaintingStatePayload(
                TeamRatingPaintingSnapshot.read(buf)
        );
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
