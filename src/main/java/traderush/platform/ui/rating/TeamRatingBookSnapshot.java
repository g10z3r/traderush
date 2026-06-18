package traderush.platform.ui.rating;

import java.util.ArrayList;
import java.util.List;
import net.minecraft.network.RegistryFriendlyByteBuf;

public record TeamRatingBookSnapshot(
        List<TeamRatingRow> rows,
        boolean runtimeReady
) {
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

        for (TeamRatingRow row : rows) {
            row.write(buf);
        }
    }

    public static TeamRatingBookSnapshot read(RegistryFriendlyByteBuf buf) {
        boolean runtimeReady = buf.readBoolean();
        int rowCount = buf.readVarInt();
        List<TeamRatingRow> rows = new ArrayList<>(Math.max(0, rowCount));

        for (int i = 0; i < rowCount; i++) {
            rows.add(TeamRatingRow.read(buf));
        }

        return new TeamRatingBookSnapshot(rows, runtimeReady);
    }
}
