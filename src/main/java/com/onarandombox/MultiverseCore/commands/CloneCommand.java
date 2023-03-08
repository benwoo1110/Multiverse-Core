package com.onarandombox.MultiverseCore.commands;

import co.aikar.commands.CommandIssuer;
import co.aikar.commands.annotation.CommandAlias;
import co.aikar.commands.annotation.CommandCompletion;
import co.aikar.commands.annotation.CommandPermission;
import co.aikar.commands.annotation.Conditions;
import co.aikar.commands.annotation.Description;
import co.aikar.commands.annotation.Single;
import co.aikar.commands.annotation.Subcommand;
import co.aikar.commands.annotation.Syntax;
import com.onarandombox.MultiverseCore.api.MVWorldManager;
import com.onarandombox.MultiverseCore.commandtools.MVCommandManager;
import com.onarandombox.MultiverseCore.commandtools.MultiverseCommand;
import jakarta.inject.Inject;
import org.bukkit.ChatColor;
import org.jetbrains.annotations.NotNull;
import org.jvnet.hk2.annotations.Service;

@Service
@CommandAlias("mv")
public class CloneCommand extends MultiverseCommand {

    private final MVWorldManager worldManager;

    @Inject
    public CloneCommand(@NotNull MVCommandManager commandManager, @NotNull MVWorldManager worldManager) {
        super(commandManager);
        this.worldManager = worldManager;
    }

    @Subcommand("clone")
    @CommandPermission("multiverse.core.clone")
    @CommandCompletion("@mvworlds:scope=both @empty")
    @Syntax("<world> <new world name>")
    @Description("Clones a world.")
    public void onCloneCommand(CommandIssuer issuer,

                               @Conditions("validWorldName:scope=both")
                               @Syntax("<world>")
                               @Description("The target world to clone.")
                               String worldName,

                               @Single
                               @Conditions("validWorldName:scope=new")
                               @Syntax("<new world name>")
                               @Description("The new cloned world name.")
                               String newWorldName
    ) {
        issuer.sendMessage(String.format("Cloning world '%s' to '%s'...", worldName, newWorldName));

        if (!this.worldManager.cloneWorld(worldName, newWorldName)) {
            issuer.sendMessage(String.format("%sWorld could not be cloned! See console for more details.", ChatColor.RED));
            return;
        }
        issuer.sendMessage(String.format("%sCloned world '%s'!", ChatColor.GREEN, newWorldName));
    }
}
