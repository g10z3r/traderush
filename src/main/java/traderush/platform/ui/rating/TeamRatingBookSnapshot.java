package traderush.platform.ui.rating;

import java.util.List;
import net.minecraft.network.RegistryFriendlyByteBuf;

public record TeamRatingBookSnapshot(
        List<Row> rows,
        boolean runtimeReady
) {
    private static final int MAX_STRING_LENGTH = 256;
    public static final TeamRatingBookSnapshot EMPTY = new TeamRatingBookSnapshot(
            List.of(),
            true
    );
    public static final TeamRatingBookSnapshot RUNTIME_NOT_READY = new TeamRatingBookSnapshot(
            List.of(),
            false
    );

    public TeamRatingBookSnapshot {
        rows = rows == null ? List.of() : List.copyOf(rows);
    }

    public void write(RegistryFriendlyByteBuf buf) {
        buf.writeBoolean(runtimeReady);
        buf.writeVarInt(rows.size());

        for (Row row : rows) {
            row.write(buf);
        }
    }

    public static TeamRatingBookSnapshot read(RegistryFriendlyByteBuf buf) {
        boolean runtimeReady = buf.readBoolean();
        int rowCount = buf.readVarInt();
        ImmutableListBuilder<Row> rows = new ImmutableListBuilder<>(rowCount);

        for (int i = 0; i < rowCount; i++) {
            rows.add(Row.read(buf));
        }

        return new TeamRatingBookSnapshot(rows.build(), runtimeReady);
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

    public record Row(int place, String teamName, long score) {
        public Row {
            place = Math.max(1, place);
            teamName = normalize(teamName);
        }

        private void write(RegistryFriendlyByteBuf buf) {
            buf.writeVarInt(place);
            writeString(buf, teamName);
            buf.writeLong(score);
        }

        private static Row read(RegistryFriendlyByteBuf buf) {
            return new Row(buf.readVarInt(), readString(buf), buf.readLong());
        }
    }

    private static final class ImmutableListBuilder<T> {
        private final java.util.ArrayList<T> values;

        private ImmutableListBuilder(int expectedSize) {
            this.values = new java.util.ArrayList<>(Math.max(0, expectedSize));
        }

        private void add(T value) {
            values.add(value);
        }

        private List<T> build() {
            return List.copyOf(values);
        }
    }
}
