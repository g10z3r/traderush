package traderush.platform.command;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.CompletableFuture;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import traderush.TradeRush;
import traderush.game.player.PlayerId;
import traderush.game.team.Team;
import traderush.game.team.TeamOperationResult;
import traderush.game.team.TeamService;
import traderush.platform.TeamMessages;

public final class TeamCommand {

    private TeamCommand() {}

    public static void register(Supplier<TeamService> teamServiceSupplier) {
        CommandRegistrationCallback.EVENT.register(
            (dispatcher, registryAccess, environment) -> {
                dispatcher.register(
                    root(TradeRush.COMMAND_ROOT, teamServiceSupplier)
                );
                dispatcher.register(root("tr", teamServiceSupplier));
            }
        );
    }

    private static LiteralArgumentBuilder<CommandSourceStack> root(
        String literal,
        Supplier<TeamService> teamServiceSupplier
    ) {
        return Commands.literal(literal).then(
            Commands.literal("team")
                .then(
                    Commands.literal("create").then(
                        Commands.argument(
                            "name",
                            StringArgumentType.greedyString()
                        ).executes(context ->
                            createTeam(
                                context.getSource(),
                                teamServiceSupplier.get(),
                                StringArgumentType.getString(context, "name")
                            )
                        )
                    )
                )
                .then(
                    Commands.literal("join").then(
                        teamNameArgument("name", teamServiceSupplier).executes(
                            context ->
                                joinTeam(
                                    context.getSource(),
                                    teamServiceSupplier.get(),
                                    StringArgumentType.getString(
                                        context,
                                        "name"
                                    )
                                )
                        )
                    )
                )
                .then(
                    Commands.literal("leave").executes(context ->
                        leaveTeam(
                            context.getSource(),
                            teamServiceSupplier.get()
                        )
                    )
                )
                .then(
                    Commands.literal("rename").then(
                        currentTeamNameArgument(
                            "currentName",
                            teamServiceSupplier
                        ).then(
                            Commands.argument(
                                "newName",
                                StringArgumentType.greedyString()
                            ).executes(context ->
                                renameTeam(
                                    context.getSource(),
                                    teamServiceSupplier.get(),
                                    StringArgumentType.getString(
                                        context,
                                        "currentName"
                                    ),
                                    StringArgumentType.getString(
                                        context,
                                        "newName"
                                    )
                                )
                            )
                        )
                    )
                )
                .then(
                    Commands.literal("delete")
                        .then(
                            Commands.literal("force").then(
                                teamNameArgument(
                                    "name",
                                    teamServiceSupplier
                                ).executes(context ->
                                    deleteTeam(
                                        context.getSource(),
                                        teamServiceSupplier.get(),
                                        StringArgumentType.getString(
                                            context,
                                            "name"
                                        ),
                                        true
                                    )
                                )
                            )
                        )
                        .then(
                            teamNameArgument(
                                "name",
                                teamServiceSupplier
                            ).executes(context ->
                                deleteTeam(
                                    context.getSource(),
                                    teamServiceSupplier.get(),
                                    StringArgumentType.getString(
                                        context,
                                        "name"
                                    ),
                                    false
                                )
                            )
                        )
                )
                .then(
                    Commands.literal("list").executes(context ->
                        listTeams(
                            context.getSource(),
                            teamServiceSupplier.get()
                        )
                    )
                )
        );
    }

    private static RequiredArgumentBuilder<
        CommandSourceStack,
        String
    > teamNameArgument(String name, Supplier<TeamService> teamServiceSupplier) {
        return Commands.argument(
            name,
            StringArgumentType.greedyString()
        ).suggests(teamNameSuggestions(teamServiceSupplier));
    }

    private static RequiredArgumentBuilder<
        CommandSourceStack,
        String
    > currentTeamNameArgument(
        String name,
        Supplier<TeamService> teamServiceSupplier
    ) {
        return Commands.argument(name, StringArgumentType.string()).suggests(
            quotedTeamNameSuggestions(teamServiceSupplier)
        );
    }

    private static SuggestionProvider<CommandSourceStack> teamNameSuggestions(
        Supplier<TeamService> teamServiceSupplier
    ) {
        return (context, builder) ->
            suggestTeamNames(teamServiceSupplier, builder, false);
    }

    private static SuggestionProvider<
        CommandSourceStack
    > quotedTeamNameSuggestions(Supplier<TeamService> teamServiceSupplier) {
        return (context, builder) ->
            suggestTeamNames(teamServiceSupplier, builder, true);
    }

    private static CompletableFuture<Suggestions> suggestTeamNames(
        Supplier<TeamService> teamServiceSupplier,
        SuggestionsBuilder builder,
        boolean quoteWhenNeeded
    ) {
        try {
            TeamService teamService = teamServiceSupplier.get();
            String remaining = builder.getRemaining();
            String normalizedRemaining = normalizeSuggestionPrefix(
                remaining
            ).toLowerCase(Locale.ROOT);

            for (Team team : teamService.listTeams()) {
                String teamName = team.getName();

                if (
                    teamName
                        .toLowerCase(Locale.ROOT)
                        .startsWith(normalizedRemaining)
                ) {
                    builder.suggest(
                        formatTeamNameSuggestion(
                            teamName,
                            quoteWhenNeeded &&
                                shouldQuoteSuggestion(remaining, teamName)
                        )
                    );
                }
            }
        } catch (IllegalStateException ignored) {
            // Runtime is not initialized yet.
        }

        return builder.buildFuture();
    }

    private static String normalizeSuggestionPrefix(String remaining) {
        String prefix = remaining.startsWith("\"")
            ? remaining.substring(1)
            : remaining;

        return prefix.replace("\\\"", "\"").replace("\\\\", "\\");
    }

    private static boolean shouldQuoteSuggestion(
        String remaining,
        String teamName
    ) {
        return (
            remaining.startsWith("\"") ||
            teamName.chars().anyMatch(Character::isWhitespace) ||
            teamName.contains("\"") ||
            teamName.contains("\\")
        );
    }

    private static String formatTeamNameSuggestion(
        String teamName,
        boolean quote
    ) {
        if (!quote) {
            return teamName;
        }

        return (
            "\"" + teamName.replace("\\", "\\\\").replace("\"", "\\\"") + "\""
        );
    }

    private static int createTeam(
        CommandSourceStack source,
        TeamService teamService,
        String name
    ) {
        TeamOperationResult<Team> result = teamService.createTeam(name);
        sendTeamResult(source, result, TeamMessages::created);

        return result.isSuccess() ? Command.SINGLE_SUCCESS : 0;
    }

    private static int joinTeam(
        CommandSourceStack source,
        TeamService teamService,
        String name
    ) throws CommandSyntaxException {
        ServerPlayer player = source.getPlayerOrException();
        PlayerId playerId = PlayerId.fromUuid(player.getUUID());

        TeamOperationResult<Team> result = teamService.joinTeam(playerId, name);
        sendTeamResult(source, result, TeamMessages::joined);

        return result.isSuccess() ? Command.SINGLE_SUCCESS : 0;
    }

    private static int leaveTeam(
        CommandSourceStack source,
        TeamService teamService
    ) throws CommandSyntaxException {
        ServerPlayer player = source.getPlayerOrException();
        PlayerId playerId = PlayerId.fromUuid(player.getUUID());

        TeamOperationResult<Team> result = teamService.leaveTeam(playerId);
        sendTeamResult(source, result, TeamMessages::left);

        return result.isSuccess() ? Command.SINGLE_SUCCESS : 0;
    }

    private static int renameTeam(
        CommandSourceStack source,
        TeamService teamService,
        String currentName,
        String newName
    ) {
        TeamOperationResult<Team> result = teamService.renameTeam(
            currentName,
            newName
        );
        sendTeamResult(source, result, TeamMessages::renamed);

        return result.isSuccess() ? Command.SINGLE_SUCCESS : 0;
    }

    private static int deleteTeam(
        CommandSourceStack source,
        TeamService teamService,
        String name,
        boolean force
    ) {
        TeamOperationResult<Team> result = teamService.deleteTeam(name, force);
        Function<Team, Component> successMessage = force
            ? TeamMessages::forceDeleted
            : TeamMessages::deleted;
        sendTeamResult(source, result, successMessage);

        return result.isSuccess() ? Command.SINGLE_SUCCESS : 0;
    }

    private static int listTeams(
        CommandSourceStack source,
        TeamService teamService
    ) {
        List<Team> teams = teamService.listTeams();

        if (teams.isEmpty()) {
            source.sendSuccess(
                () -> Component.literal("No teams created yet."),
                false
            );
            return Command.SINGLE_SUCCESS;
        }

        source.sendSuccess(() -> Component.literal("Teams:"), false);

        for (Team team : teams) {
            String line = formatTeamSummary(source, team);
            source.sendSuccess(() -> Component.literal(line), false);
        }

        return Command.SINGLE_SUCCESS;
    }

    private static String formatTeamSummary(
        CommandSourceStack source,
        Team team
    ) {
        var players = team.getPlayers();
        String members = players.isEmpty()
            ? "none"
            : players
                  .stream()
                  .map(playerId -> resolvePlayerName(source, playerId))
                  .collect(Collectors.joining(", "));

        return (
            "- " +
            team.getName() +
            " | id: " +
            team.getId() +
            " | members: " +
            players.size() +
            " | score: " +
            team.getScore() +
            System.lineSeparator() +
            "  " +
            members
        );
    }

    private static String resolvePlayerName(
        CommandSourceStack source,
        PlayerId playerId
    ) {
        if (source.getServer() != null) {
            ServerPlayer onlinePlayer = source
                .getServer()
                .getPlayerList()
                .getPlayer(playerId.value());

            if (onlinePlayer != null) {
                return onlinePlayer.getGameProfile().name();
            }
        }

        return playerId.toString();
    }

    private static void sendTeamResult(
        CommandSourceStack source,
        TeamOperationResult<Team> result,
        Function<Team, Component> successMessage
    ) {
        if (result.isSuccess()) {
            Team team = result.value();

            source.sendSuccess(() -> successMessage.apply(team), false);

            return;
        }

        source.sendFailure(TeamMessages.componentFor(result.error()));
    }
}
