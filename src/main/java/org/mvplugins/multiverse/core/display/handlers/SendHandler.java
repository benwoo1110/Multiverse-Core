package org.mvplugins.multiverse.core.display.handlers;

import java.util.List;

import co.aikar.commands.BukkitCommandIssuer;
import org.jetbrains.annotations.NotNull;
import org.mvplugins.multiverse.core.command.MVCommandIssuer;

/**
 * Handles the sending of all content to the command sender.
 */
@FunctionalInterface
public interface SendHandler {
    /**
     * Sends all the content to the given command sender.
     *
     * @param issuer    The target which the content will be displayed to.
     * @param content   The content to display.
     */
    void send(@NotNull MVCommandIssuer issuer, @NotNull List<String> content);
}
