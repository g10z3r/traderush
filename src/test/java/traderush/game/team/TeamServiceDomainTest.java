package traderush.game.team;

import java.util.UUID;
import traderush.game.player.PlayerId;
import traderush.platform.ui.TeamUiBroadcasterDomainTest;
import traderush.platform.ui.rating.TeamRatingBookSnapshot;
import traderush.platform.ui.rating.TeamRatingBookSnapshot.Row;
import traderush.platform.ui.rating.TeamRatingBookSnapshots;

public final class TeamServiceDomainTest {

    private TeamServiceDomainTest() {}

    public static void main(String[] args) {
        createsJoinsLeavesAndDeletesTeams();
        renamesEmptyTeamWithoutChangingIdentityOrScore();
        renamesNonEmptyTeamWithoutChangingMembers();
        rejectsDuplicateAndInvalidRenames();
        rejectsDeleteOfNonEmptyTeamsWithoutForce();
        buildsRatingBookSnapshotFromTeamRanking();
        TeamUiBroadcasterDomainTest.run();
    }

    private static void createsJoinsLeavesAndDeletesTeams() {
        Harness harness = new Harness();
        PlayerId player = player("00000000-0000-0000-0000-000000000001");

        Team alpha = assertSuccess(harness.service.createTeam("  Alpha  "));
        assertEquals("Alpha", alpha.getName(), "create should trim names");
        assertEquals(1, harness.stateChanges, "create should save state");
        assertError(
                TeamError.TEAM_ALREADY_EXISTS,
                harness.service.createTeam("alpha")
        );
        assertError(
                TeamError.TEAM_INVALID_NAME,
                harness.service.createTeam("ab")
        );

        Team joined = assertSuccess(harness.service.joinTeam(player, "Alpha"));
        assertEquals(
                alpha.getId(),
                joined.getId(),
                "join should target the named team"
        );
        assertTrue(
                joined.getPlayers().contains(player),
                "join should add the player"
        );
        assertError(
                TeamError.PLAYER_ALREADY_IN_TEAM,
                harness.service.joinTeam(player, alpha.getId())
        );

        Team beta = assertSuccess(harness.service.createTeam("Beta"));
        assertSuccess(harness.service.joinTeam(player, beta.getId()));
        assertFalse(
                alpha.getPlayers().contains(player),
                "joining another team should leave the old team"
        );
        assertTrue(
                beta.getPlayers().contains(player),
                "joining another team should add the new team"
        );

        Team left = assertSuccess(harness.service.leaveTeam(player));
        assertEquals(
                beta.getId(),
                left.getId(),
                "leave should return the previous team"
        );
        assertFalse(
                beta.getPlayers().contains(player),
                "leave should remove the player"
        );
        assertError(
                TeamError.PLAYER_NOT_IN_TEAM,
                harness.service.leaveTeam(player)
        );

        assertSuccess(harness.service.deleteTeam("Alpha", false));
        assertError(
                TeamError.TEAM_NOT_FOUND,
                harness.service.joinTeam(player, "Alpha")
        );
    }

    private static void renamesEmptyTeamWithoutChangingIdentityOrScore() {
        Harness harness = new Harness();
        Team team = assertSuccess(harness.service.createTeam("Alpha"));
        TeamId id = team.getId();
        team.addPoints(42);
        harness.repository.put(team);

        Team renamed = assertSuccess(
                harness.service.renameTeam(id, "  Omega  ")
        );

        assertEquals(id, renamed.getId(), "rename should keep the id");
        assertEquals(
                "Omega",
                renamed.getName(),
                "rename should trim the new name"
        );
        assertEquals(42L, renamed.getScore(), "rename should keep score");
        assertTrue(
                renamed.getPlayers().isEmpty(),
                "rename should not add members"
        );
        assertTrue(
                harness.repository.getByName("Alpha").isEmpty(),
                "old name index should be removed"
        );
        assertTrue(
                harness.repository.getByName("omega").isPresent(),
                "new name index should be searchable case-insensitively"
        );
    }

    private static void renamesNonEmptyTeamWithoutChangingMembers() {
        Harness harness = new Harness();
        PlayerId player = player("00000000-0000-0000-0000-000000000002");
        Team team = assertSuccess(harness.service.createTeam("Alpha"));
        assertSuccess(harness.service.joinTeam(player, team.getId()));

        Team renamed = assertSuccess(
                harness.service.renameTeam(team.getId(), "Omega")
        );

        assertEquals(
                team.getId(),
                renamed.getId(),
                "non-empty rename should keep the id"
        );
        assertEquals(
                "Omega",
                renamed.getName(),
                "non-empty rename should update the name"
        );
        assertEquals(
                1,
                renamed.getPlayers().size(),
                "non-empty rename should keep member count"
        );
        assertTrue(
                renamed.getPlayers().contains(player),
                "non-empty rename should keep members"
        );
        assertTrue(
                harness.repository.getByName("Alpha").isEmpty(),
                "old non-empty team name index should be removed"
        );
        assertTrue(
                harness.repository.getByName("omega").isPresent(),
                "new non-empty team name index should be searchable case-insensitively"
        );
    }

    private static void rejectsDuplicateAndInvalidRenames() {
        Harness harness = new Harness();
        Team alpha = assertSuccess(harness.service.createTeam("Alpha"));
        Team beta = assertSuccess(harness.service.createTeam("Beta"));

        assertError(
                TeamError.TEAM_ALREADY_EXISTS,
                harness.service.renameTeam(beta.getId(), "alpha")
        );
        assertEquals(
                "Beta",
                beta.getName(),
                "duplicate rename should not change state"
        );

        assertError(
                TeamError.TEAM_INVALID_NAME,
                harness.service.renameTeam(beta.getId(), "ab")
        );
        assertEquals(
                "Beta",
                beta.getName(),
                "invalid rename should not change state"
        );

        assertSuccess(harness.service.renameTeam(alpha.getId(), " Alpha "));
        assertEquals(
                "Alpha",
                alpha.getName(),
                "same-name rename should be a no-op success"
        );
    }

    private static void rejectsDeleteOfNonEmptyTeamsWithoutForce() {
        Harness harness = new Harness();
        PlayerId player = player("00000000-0000-0000-0000-000000000003");
        Team team = assertSuccess(harness.service.createTeam("Alpha"));
        assertSuccess(harness.service.joinTeam(player, team.getId()));

        assertError(
                TeamError.TEAM_NOT_EMPTY,
                harness.service.deleteTeam(team.getId())
        );
        assertTrue(
                harness.repository.getById(team.getId()).isPresent(),
                "non-force delete should keep non-empty team"
        );

        assertSuccess(harness.service.deleteTeam(team.getId(), true));
        assertTrue(
                harness.repository.getById(team.getId()).isEmpty(),
                "force delete should preserve existing admin/debug behavior"
        );
    }

    private static void buildsRatingBookSnapshotFromTeamRanking() {
        Harness harness = new Harness();

        TeamRatingBookSnapshot empty = TeamRatingBookSnapshots
                .create(harness.service);
        assertTrue(empty.runtimeReady(), "empty snapshot should be ready");
        assertTrue(
                empty.rows().isEmpty(),
                "empty snapshot should have no rows"
        );

        Team alpha = assertSuccess(harness.service.createTeam("Alpha"));
        Team beta = assertSuccess(harness.service.createTeam("Beta"));
        Team gamma = assertSuccess(harness.service.createTeam("Gamma"));

        assertSuccess(harness.service.awardPoints(alpha.getId(), 10));
        assertSuccess(harness.service.awardPoints(beta.getId(), 20));
        assertSuccess(harness.service.awardPoints(gamma.getId(), 20));

        TeamRatingBookSnapshot snapshot = TeamRatingBookSnapshots
                .create(harness.service);

        assertEquals(
                3,
                snapshot.rows().size(),
                "snapshot should include teams"
        );
        assertRow(snapshot.rows().get(0), 1, "Beta", 20L);
        assertRow(snapshot.rows().get(1), 2, "Gamma", 20L);
        assertRow(snapshot.rows().get(2), 3, "Alpha", 10L);
    }

    private static void assertRow(
            Row row,
            int place,
            String teamName,
            long score
    ) {
        assertEquals(place, row.place(), "unexpected rating place");
        assertEquals(teamName, row.teamName(), "unexpected rating team");
        assertEquals(score, row.score(), "unexpected rating score");
    }

    private static PlayerId player(String uuid) {
        return PlayerId.fromUuid(UUID.fromString(uuid));
    }

    private static Team assertSuccess(TeamOperationResult<Team> result) {
        if (!result.isSuccess()) {
            throw new AssertionError(
                    "Expected success, got error: " + result.error()
            );
        }

        return result.value();
    }

    private static void assertError(
            TeamError expected,
            TeamOperationResult<Team> result
    ) {
        if (result.isSuccess()) {
            throw new AssertionError(
                    "Expected error " + expected + ", got success: "
                            + result.value().getName()
            );
        }

        assertEquals(expected, result.error(), "unexpected team error");
    }

    private static void assertTrue(boolean value, String message) {
        if (!value) {
            throw new AssertionError(message);
        }
    }

    private static void assertFalse(boolean value, String message) {
        if (value) {
            throw new AssertionError(message);
        }
    }

    private static void assertEquals(
            Object expected,
            Object actual,
            String message
    ) {
        if (!expected.equals(actual)) {
            throw new AssertionError(
                    message + ": expected <" + expected + "> but got <" + actual
                            + ">"
            );
        }
    }

    private static final class Harness {

        private final InMemoryTeamRepository repository = new InMemoryTeamRepository();
        private int stateChanges;
        private final TeamService service = new TeamService(
                repository,
                () -> stateChanges++
        );
    }
}
