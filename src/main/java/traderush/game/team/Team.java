package traderush.game.team;

import java.util.LinkedHashSet;
import java.util.Set;
import java.util.UUID;

import traderush.game.player.PlayerId;

public final class Team {
    private final TeamId id;
    private String name;
    private final Set<PlayerId> members;
    private long score;

    public Team(String name) {
        if (name == null || name.isEmpty()) {
            throw new IllegalArgumentException("Name cannot be null or empty");
        }

        this.id = TeamId.fromUuid(UUID.randomUUID());
        this.name = name.trim();
        this.members = new LinkedHashSet<>();
        this.score = 0;
    }

    public TeamId getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public Set<PlayerId> getMembers() {
        return members;
    }

    public long getScore() {
        return score;
    }

    public void addMember(PlayerId playerId) {
        if (playerId == null) {
            throw new IllegalArgumentException("Member cannot be null");
        }

        if (members.contains(playerId)) {
            throw new IllegalArgumentException("Member already exists");
        }

        members.add(playerId);
    }
    
    public void removeMember(PlayerId playerId) {
        if (playerId == null) {
            throw new IllegalArgumentException("Member cannot be null");
        }

        if (!members.contains(playerId)) {
            throw new IllegalArgumentException("Member does not exist");
        }

        members.remove(playerId);
    }

    public void addPoints(long points) {
        if (points < 0) {
            throw new IllegalArgumentException("Points cannot be negative");
        }

        score += points;
    }
}
