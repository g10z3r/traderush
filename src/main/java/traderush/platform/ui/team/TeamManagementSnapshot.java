package traderush.platform.ui.team;

import java.util.List;
import net.minecraft.network.RegistryFriendlyByteBuf;

public record TeamManagementSnapshot(
    List<TeamRow> teams,
    String selectedTeamId,
    String currentTeamId,
    String currentTeamName
) {
    private static final int MAX_STRING_LENGTH = 256;
    public static final TeamManagementSnapshot EMPTY = new TeamManagementSnapshot(
        List.of(),
        "",
        "",
        ""
    );

    public TeamManagementSnapshot {
        teams = teams == null ? List.of() : List.copyOf(teams);
        selectedTeamId = normalize(selectedTeamId);
        currentTeamId = normalize(currentTeamId);
        currentTeamName = normalize(currentTeamName);
    }

    public TeamRow selectedTeam() {
        return teams.stream()
            .filter(team -> team.id().equals(selectedTeamId))
            .findFirst()
            .orElse(null);
    }

    public void write(RegistryFriendlyByteBuf buf) {
        buf.writeVarInt(teams.size());

        for (TeamRow team : teams) {
            team.write(buf);
        }

        writeString(buf, selectedTeamId);
        writeString(buf, currentTeamId);
        writeString(buf, currentTeamName);
    }

    public static TeamManagementSnapshot read(RegistryFriendlyByteBuf buf) {
        int teamCount = buf.readVarInt();
        ImmutableListBuilder<TeamRow> teams = new ImmutableListBuilder<>(teamCount);

        for (int i = 0; i < teamCount; i++) {
            teams.add(TeamRow.read(buf));
        }

        return new TeamManagementSnapshot(
            teams.build(),
            readString(buf),
            readString(buf),
            readString(buf)
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

    public record TeamRow(
        String id,
        String name,
        long score,
        int memberCount,
        List<MemberEntry> members
    ) {
        public TeamRow {
            id = normalize(id);
            name = normalize(name);
            members = members == null ? List.of() : List.copyOf(members);
        }

        private void write(RegistryFriendlyByteBuf buf) {
            writeString(buf, id);
            writeString(buf, name);
            buf.writeLong(score);
            buf.writeVarInt(memberCount);
            buf.writeVarInt(members.size());

            for (MemberEntry member : members) {
                member.write(buf);
            }
        }

        private static TeamRow read(RegistryFriendlyByteBuf buf) {
            String id = readString(buf);
            String name = readString(buf);
            long score = buf.readLong();
            int memberCount = buf.readVarInt();
            int memberEntries = buf.readVarInt();
            ImmutableListBuilder<MemberEntry> members = new ImmutableListBuilder<>(memberEntries);

            for (int i = 0; i < memberEntries; i++) {
                members.add(MemberEntry.read(buf));
            }

            return new TeamRow(id, name, score, memberCount, members.build());
        }
    }

    public record MemberEntry(String id, String displayName) {
        public MemberEntry {
            id = normalize(id);
            displayName = normalize(displayName);
        }

        private void write(RegistryFriendlyByteBuf buf) {
            writeString(buf, id);
            writeString(buf, displayName);
        }

        private static MemberEntry read(RegistryFriendlyByteBuf buf) {
            return new MemberEntry(readString(buf), readString(buf));
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
