package com.onarandombox.MultiverseCore.commands;

import co.aikar.commands.BukkitCommandIssuer;
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
public class RemoveCommand extends MultiverseCommand {

    private final MVWorldManager worldManager;

    @Inject
    public RemoveCommand(@NotNull MVCommandManager commandManager, @NotNull MVWorldManager worldManager) {
        super(commandManager);
        this.worldManager = worldManager;
    }

    @Subcommand("remove")
    @CommandPermission("multiverse.core.remove")
    @CommandCompletion("@mvworlds:scope=both")
    @Syntax("<world>")
    @Description("Unloads a world from Multiverse and removes it from worlds.yml, this does NOT DELETE the world folder.")
    public void onRemoveCommand(BukkitCommandIssuer issuer,

                                @Single
                                @Conditions("mvworlds:scope=both")
                                @Syntax("<world>")
                                @Description("World you want to remove from mv's knowledge.")
                                String worldName
    ) {
        if (!this.worldManager.removeWorldFromConfig(worldName)) {
            issuer.sendMessage(String.format("%sError trying to remove world from config!", ChatColor.RED));
            return;
        }
        issuer.sendMessage(String.format("World '%s' is removed from config!", worldName));
    }
}
