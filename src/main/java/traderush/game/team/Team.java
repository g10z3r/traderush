package traderush.game.team;

import java.util.LinkedHashSet;
import java.util.Set;
import java.util.UUID;

import traderush.game.player.PlayerId;

public final class Team {
    private final TeamId id;
    private String name;
    private final Set<PlayerId> players;
    private long score;

    public Team(String name) {
        if (name == null || name.isEmpty()) {
            throw new IllegalArgumentException("Name cannot be null or empty");
        }

        this.id = TeamId.fromUuid(UUID.randomUUID());
        this.name = name.trim();
        this.players = new LinkedHashSet<>();
        this.score = 0;
    }

    Team(TeamId id, String name, Set<PlayerId> players, long score) {
        if (id == null) {
            throw new IllegalArgumentException("Id cannot be null");
        }

        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("Name cannot be null or empty");
        }

        if (score < 0) {
            throw new IllegalArgumentException("Score cannot be negative");
        }

        this.id = id;
        this.name = name.trim();
        this.players = new LinkedHashSet<>(players == null ? Set.of() : players);
        this.score = score;
    }

    public TeamId getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public Set<PlayerId> getPlayers() {
        return players;
    }

    public long getScore() {
        return score;
    }

    public void addPlayer(PlayerId playerId) {
        if (playerId == null) {
            throw new IllegalArgumentException("Player cannot be null");
        }

        if (players.contains(playerId)) {
            throw new IllegalArgumentException("Player already exists");
        }

        players.add(playerId);
    }

    public void removePlayer(PlayerId playerId) {
        if (playerId == null) {
            throw new IllegalArgumentException("Player cannot be null");
        }

        if (!players.contains(playerId)) {
            throw new IllegalArgumentException("Player does not exist");
        }

        players.remove(playerId);
    }

    public void addPoints(long points) {
        if (points < 0) {
            throw new IllegalArgumentException("Points cannot be negative");
        }

        score += points;
    }
}
