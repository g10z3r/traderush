package traderush.platform.teamui;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import traderush.TradeRush;

public record TeamManagementActionPayload(Action action, String teamId, String value)
    implements CustomPacketPayload {
    private static final int MAX_VALUE_LENGTH = 256;

    public static final Type<TeamManagementActionPayload> TYPE = new Type<>(
        TradeRush.id("team_management_action")
    );
    public static final StreamCodec<
        RegistryFriendlyByteBuf,
        TeamManagementActionPayload
    > CODEC = StreamCodec.ofMember(
        TeamManagementActionPayload::write,
        TeamManagementActionPayload::read
    );

    public TeamManagementActionPayload {
        action = action == null ? Action.REFRESH : action;
        teamId = normalize(teamId);
        value = normalize(value);
    }

    public static TeamManagementActionPayload refresh(String selectedTeamId) {
        return new TeamManagementActionPayload(Action.REFRESH, selectedTeamId, "");
    }

    private void write(RegistryFriendlyByteBuf buf) {
        buf.writeEnum(action);
        buf.writeUtf(teamId, MAX_VALUE_LENGTH);
        buf.writeUtf(value, MAX_VALUE_LENGTH);
    }

    private static TeamManagementActionPayload read(RegistryFriendlyByteBuf buf) {
        return new TeamManagementActionPayload(
            buf.readEnum(Action.class),
            buf.readUtf(MAX_VALUE_LENGTH),
            buf.readUtf(MAX_VALUE_LENGTH)
        );
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    private static String normalize(String value) {
        return value == null ? "" : value;
    }

    public enum Action {
        CREATE,
        JOIN,
        LEAVE,
        DELETE_EMPTY,
        RENAME_EMPTY,
        REFRESH,
    }
}
