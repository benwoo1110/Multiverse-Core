package com.onarandombox.MultiverseCore.commands;

import co.aikar.commands.annotation.CommandAlias;
import co.aikar.commands.annotation.CommandCompletion;
import co.aikar.commands.annotation.CommandPermission;
import co.aikar.commands.annotation.Conditions;
import co.aikar.commands.annotation.Description;
import co.aikar.commands.annotation.Single;
import co.aikar.commands.annotation.Subcommand;
import co.aikar.commands.annotation.Syntax;
import com.dumptruckman.minecraft.util.Logging;
import com.onarandombox.MultiverseCore.commandtools.MVCommandIssuer;
import com.onarandombox.MultiverseCore.commandtools.MVCommandManager;
import com.onarandombox.MultiverseCore.commandtools.MultiverseCommand;
import com.onarandombox.MultiverseCore.worldnew.WorldManager;
import jakarta.inject.Inject;
import org.jetbrains.annotations.NotNull;
import org.jvnet.hk2.annotations.Service;

@Service
@CommandAlias("mv")
public class RemoveCommand extends MultiverseCommand {

    private final WorldManager worldManager;

    @Inject
    public RemoveCommand(@NotNull MVCommandManager commandManager, @NotNull WorldManager worldManager) {
        super(commandManager);
        this.worldManager = worldManager;
    }

    @Subcommand("remove")
    @CommandPermission("multiverse.core.remove")
    @CommandCompletion("@mvworlds:scope=both")
    @Syntax("<world>")
    @Description("{@@mv-core.remove.description}")
    public void onRemoveCommand(MVCommandIssuer issuer,

                                @Single
                                //@Conditions("mvworlds:scope=both")
                                @Syntax("<world>")
                                @Description("{@@mv-core.remove.world.description}")
                                String worldName
    ) {
        worldManager.removeWorld(worldName)
                .onSuccess((success) -> {
                    Logging.fine("World remove success: " + success);
                    issuer.sendInfo(success.getReasonMessage());
                }).onFailure((failure) -> {
                    Logging.fine("World remove failure: " + failure);
                    issuer.sendError(failure.getReasonMessage());
                });
    }
}
