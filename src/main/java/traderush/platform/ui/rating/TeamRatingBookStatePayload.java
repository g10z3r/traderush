package traderush.platform.ui.rating;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import traderush.TradeRush;

public record TeamRatingBookStatePayload(
        TeamRatingBookSnapshot snapshot
) implements CustomPacketPayload {
    public static final Type<TeamRatingBookStatePayload> TYPE = new Type<>(
            TradeRush.id("team_rating_book_state")
    );
    public static final StreamCodec<RegistryFriendlyByteBuf, TeamRatingBookStatePayload> CODEC = StreamCodec
            .ofMember(
                    TeamRatingBookStatePayload::write,
                    TeamRatingBookStatePayload::read
            );

    public TeamRatingBookStatePayload {
        snapshot = snapshot == null ? TeamRatingBookSnapshot.EMPTY : snapshot;
    }

    private void write(RegistryFriendlyByteBuf buf) {
        snapshot.write(buf);
    }

    private static TeamRatingBookStatePayload read(
            RegistryFriendlyByteBuf buf
    ) {
        return new TeamRatingBookStatePayload(TeamRatingBookSnapshot.read(buf));
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
