package traderush.platform.ui.team;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentSerialization;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import traderush.TradeRush;

public record TeamManagementStatePayload(
    TeamManagementSnapshot snapshot,
    Component message,
    boolean error
) implements CustomPacketPayload {
    public static final Type<TeamManagementStatePayload> TYPE = new Type<>(
        TradeRush.id("team_management_state")
    );
    public static final StreamCodec<
        RegistryFriendlyByteBuf,
        TeamManagementStatePayload
    > CODEC = StreamCodec.ofMember(
        TeamManagementStatePayload::write,
        TeamManagementStatePayload::read
    );

    public TeamManagementStatePayload {
        snapshot = snapshot == null ? TeamManagementSnapshot.EMPTY : snapshot;
        message = message == null ? Component.empty() : message;
    }

    private void write(RegistryFriendlyByteBuf buf) {
        snapshot.write(buf);
        ComponentSerialization.TRUSTED_STREAM_CODEC.encode(buf, message);
        buf.writeBoolean(error);
    }

    private static TeamManagementStatePayload read(
        RegistryFriendlyByteBuf buf
    ) {
        return new TeamManagementStatePayload(
            TeamManagementSnapshot.read(buf),
            ComponentSerialization.TRUSTED_STREAM_CODEC.decode(buf),
            buf.readBoolean()
        );
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
