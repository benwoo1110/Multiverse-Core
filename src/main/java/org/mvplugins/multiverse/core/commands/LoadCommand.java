package org.mvplugins.multiverse.core.commands;

import co.aikar.commands.annotation.CommandAlias;
import co.aikar.commands.annotation.CommandCompletion;
import co.aikar.commands.annotation.CommandPermission;
import co.aikar.commands.annotation.Conditions;
import co.aikar.commands.annotation.Description;
import co.aikar.commands.annotation.Single;
import co.aikar.commands.annotation.Subcommand;
import co.aikar.commands.annotation.Syntax;
import com.dumptruckman.minecraft.util.Logging;
import jakarta.inject.Inject;
import org.jetbrains.annotations.NotNull;
import org.jvnet.hk2.annotations.Service;

import org.mvplugins.multiverse.core.command.LegacyAliasCommand;
import org.mvplugins.multiverse.core.command.MVCommandIssuer;
import org.mvplugins.multiverse.core.command.MVCommandManager;
import org.mvplugins.multiverse.core.locale.MVCorei18n;
import org.mvplugins.multiverse.core.locale.message.MessageReplacement.Replace;
import org.mvplugins.multiverse.core.world.WorldManager;

@Service
class LoadCommand extends CoreCommand {

    private final WorldManager worldManager;

    @Inject
    LoadCommand(@NotNull WorldManager worldManager) {
        this.worldManager = worldManager;
    }

    @Subcommand("load")
    @CommandPermission("multiverse.core.load")
    @CommandCompletion("@mvworlds:scope=unloaded")
    @Syntax("<world>")
    @Description("{@@mv-core.load.description}")
    void onLoadCommand(
            MVCommandIssuer issuer,

            @Single
            @Conditions("worldname:scope=unloaded")
            @Syntax("<world>")
            @Description("{@@mv-core.load.world.description}")
            String worldName) {
        issuer.sendInfo(MVCorei18n.LOAD_LOADING, Replace.WORLD.with(worldName));
        worldManager.loadWorld(worldName)
                .onSuccess(newWorld -> {
                    Logging.fine("World load success: " + newWorld);
                    issuer.sendInfo(MVCorei18n.LOAD_SUCCESS, Replace.WORLD.with(newWorld.getName()));
                }).onFailure(failure -> {
                    Logging.fine("World load failure: " + failure);
                    issuer.sendError(failure.getFailureMessage());
                });
    }

    @Service
    private static final class LegacyAlias extends LoadCommand implements LegacyAliasCommand {
        @Inject
        LegacyAlias(@NotNull WorldManager worldManager) {
            super(worldManager);
        }

        @Override
        @CommandAlias("mvload")
        void onLoadCommand(MVCommandIssuer issuer, String worldName) {
            super.onLoadCommand(issuer, worldName);
        }
    }
}
