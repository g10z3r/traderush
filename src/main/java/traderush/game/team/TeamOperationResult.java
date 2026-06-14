package traderush.game.team;

import java.util.Objects;
import java.util.Optional;

public final class TeamOperationResult<T> {
    private final T value;
    private final TeamError error;

    private TeamOperationResult(T value, TeamError error) {
        this.value = value;
        this.error = error;
    }

    public static <T> TeamOperationResult<T> success(T value) {
        return new TeamOperationResult<>(
                Objects.requireNonNull(value, "success value cannot be null"),
                null
        );
    }

    public static <T> TeamOperationResult<T> error(TeamError error) {
        return new TeamOperationResult<>(
                null,
                Objects.requireNonNull(error, "error cannot be null")
        );
    }

    public Optional<T> getValue() {
        return Optional.ofNullable(value);
    }

    public Optional<TeamError> getError() {
        return Optional.ofNullable(error);
    }

    public boolean isSuccess() {
        return error == null;
    }

    public T value() {
        return Objects.requireNonNull(value, "no success value");
    }

    public TeamError error() {
        return Objects.requireNonNull(error, "no error");
    }
}
