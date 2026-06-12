package traderush.game.team;

import java.util.Objects;
import java.util.Optional;

public final class TeamOperationResult<T> {
    private final T value;
    private final TeamError err;

    private TeamOperationResult(T value, TeamError err) {
        this.value = value;
        this.err = err;
    }

    public static <T> TeamOperationResult<T> success(T value) {
        return new TeamOperationResult<>(
                Objects.requireNonNull(value, "success value cannot be null"), null);
    }

    public static <T> TeamOperationResult<T> error(TeamError err) {
        return new TeamOperationResult<>(null,
                Objects.requireNonNull(err, "error cannot be null"));
    }

    public Optional<T> getValue() {
        return Optional.ofNullable(value);
    }

    public Optional<TeamError> getError() {
        return Optional.ofNullable(err);
    }
}
