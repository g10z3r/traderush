package traderush.platform.ui.rating;

import net.minecraft.network.RegistryFriendlyByteBuf;

public record TeamRatingRow(int place, String teamName, long score) {
    private static final int MAX_STRING_LENGTH = 256;

    public TeamRatingRow {
        place = Math.max(1, place);
        teamName = normalize(teamName);
    }

    public void write(RegistryFriendlyByteBuf buf) {
        buf.writeVarInt(place);
        writeString(buf, teamName);
        buf.writeLong(score);
    }

    public static TeamRatingRow read(RegistryFriendlyByteBuf buf) {
        return new TeamRatingRow(
                buf.readVarInt(),
                readString(buf),
                buf.readLong()
        );
    }

    private static void writeString(RegistryFriendlyByteBuf buf, String value) {
        buf.writeUtf(normalize(value), MAX_STRING_LENGTH);
    }

    private static String readString(RegistryFriendlyByteBuf buf) {
        return buf.readUtf(MAX_STRING_LENGTH);
    }

    private static String normalize(String value) {
        return value == null ? "" : value;
    }
}
