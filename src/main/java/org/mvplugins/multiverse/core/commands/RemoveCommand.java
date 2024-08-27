package org.mvplugins.multiverse.core.commands;

import java.util.Collections;

import co.aikar.commands.annotation.CommandAlias;
import co.aikar.commands.annotation.CommandCompletion;
import co.aikar.commands.annotation.CommandPermission;
import co.aikar.commands.annotation.Conditions;
import co.aikar.commands.annotation.Description;
import co.aikar.commands.annotation.Optional;
import co.aikar.commands.annotation.Single;
import co.aikar.commands.annotation.Subcommand;
import co.aikar.commands.annotation.Syntax;
import com.dumptruckman.minecraft.util.Logging;
import jakarta.inject.Inject;
import org.jetbrains.annotations.NotNull;
import org.jvnet.hk2.annotations.Service;

import org.mvplugins.multiverse.core.commandtools.MVCommandIssuer;
import org.mvplugins.multiverse.core.commandtools.MVCommandManager;
import org.mvplugins.multiverse.core.commandtools.MultiverseCommand;
import org.mvplugins.multiverse.core.commandtools.flags.CommandFlag;
import org.mvplugins.multiverse.core.commandtools.flags.ParsedCommandFlags;
import org.mvplugins.multiverse.core.utils.MVCorei18n;
import org.mvplugins.multiverse.core.utils.result.Async;
import org.mvplugins.multiverse.core.world.MultiverseWorld;
import org.mvplugins.multiverse.core.world.WorldManager;
import org.mvplugins.multiverse.core.world.helpers.PlayerWorldTeleporter;

@Service
@CommandAlias("mv")
class RemoveCommand extends MultiverseCommand {

    private final WorldManager worldManager;
    private final PlayerWorldTeleporter playerWorldTeleporter;

    private final CommandFlag REMOVE_PLAYERS_FLAG = flag(CommandFlag.builder("--remove-players")
            .addAlias("-r")
            .build());

    @Inject
    RemoveCommand(
            @NotNull MVCommandManager commandManager,
            @NotNull WorldManager worldManager,
            @NotNull PlayerWorldTeleporter playerWorldTeleporter) {
        super(commandManager);
        this.worldManager = worldManager;
        this.playerWorldTeleporter = playerWorldTeleporter;
    }

    @CommandAlias("mvremove")
    @Subcommand("remove")
    @CommandPermission("multiverse.core.remove")
    @CommandCompletion("@mvworlds:scope=both")
    @Syntax("<world>")
    @Description("{@@mv-core.remove.description}")
    void onRemoveCommand(
            MVCommandIssuer issuer,

            @Single
            @Conditions("mvworlds:scope=both @flags:groupName=mvremovecommand")
            @Syntax("<world>")
            @Description("{@@mv-core.remove.world.description}")
            MultiverseWorld world,

            @Optional
            @Syntax("[--remove-players]")
            @Description(/* TODO */"")
            String[] flags) {
        ParsedCommandFlags parsedFlags = parseFlags(flags);

        var future = parsedFlags.hasFlag(REMOVE_PLAYERS_FLAG)
                ? worldManager.getLoadedWorld(world)
                .map(playerWorldTeleporter::removeFromWorld)
                .getOrElse(Async.completedFuture(Collections.emptyList()))
                : Async.completedFuture(Collections.emptyList());

        future.thenRun(() -> doWorldRemoving(issuer, world));
    }

    private void doWorldRemoving(MVCommandIssuer issuer, MultiverseWorld world) {
        worldManager.removeWorld(world)
                .onSuccess(removedWorldName -> {
                    Logging.fine("World remove success: " + removedWorldName);
                    issuer.sendInfo(MVCorei18n.REMOVE_SUCCESS, "{world}", removedWorldName);
                }).onFailure(failure -> {
                    Logging.fine("World remove failure: " + failure);
                    issuer.sendError(failure.getFailureMessage());
                });
    }
}
