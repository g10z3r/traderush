package traderush.platform.ui.rating;

import java.util.ArrayList;
import java.util.List;
import net.minecraft.network.RegistryFriendlyByteBuf;

public record TeamRatingPaintingSnapshot(
        List<TeamRatingRow> rows,
        boolean runtimeReady
) {
    public static final int MAX_VISIBLE_ROWS = 8;
    public static final TeamRatingPaintingSnapshot EMPTY = new TeamRatingPaintingSnapshot(
            List.of(),
            true
    );
    public static final TeamRatingPaintingSnapshot RUNTIME_NOT_READY = new TeamRatingPaintingSnapshot(
            List.of(),
            false
    );

    public TeamRatingPaintingSnapshot {
        rows = rows == null ? List.of() : List.copyOf(rows);
    }

    public void write(RegistryFriendlyByteBuf buf) {
        buf.writeBoolean(runtimeReady);
        buf.writeVarInt(rows.size());

        for (TeamRatingRow row : rows) {
            row.write(buf);
        }
    }

    public static TeamRatingPaintingSnapshot read(RegistryFriendlyByteBuf buf) {
        boolean runtimeReady = buf.readBoolean();
        int rowCount = buf.readVarInt();
        List<TeamRatingRow> rows = new ArrayList<>(Math.max(0, rowCount));

        for (int i = 0; i < rowCount; i++) {
            rows.add(TeamRatingRow.read(buf));
        }

        return new TeamRatingPaintingSnapshot(rows, runtimeReady);
    }
}
