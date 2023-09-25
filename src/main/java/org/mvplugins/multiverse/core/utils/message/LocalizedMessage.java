package org.mvplugins.multiverse.core.utils.message;

import java.util.Objects;

import co.aikar.commands.ACFUtil;
import co.aikar.commands.CommandIssuer;
import co.aikar.locales.MessageKey;
import co.aikar.locales.MessageKeyProvider;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import org.mvplugins.multiverse.core.commandtools.PluginLocales;

final class LocalizedMessage extends Message implements MessageKeyProvider {

    private final @NotNull MessageKeyProvider messageKeyProvider;

    LocalizedMessage(
            @NotNull MessageKeyProvider messageKeyProvider,
            @NotNull String message,
            @NotNull MessageReplacement... replacements) {
        super(message, replacements);
        this.messageKeyProvider = messageKeyProvider;
    }

    @Override
    public MessageKey getMessageKey() {
        return messageKeyProvider.getMessageKey();
    }

    @Override
    public @NotNull String[] getReplacements(@NotNull PluginLocales locales, @Nullable CommandIssuer commandIssuer) {
        return toReplacementsArray(locales, commandIssuer, replacements);
    }

    @Override
    public @NotNull String formatted(@NotNull PluginLocales locales, @Nullable CommandIssuer commandIssuer) {
        Objects.requireNonNull(locales, "locales must not be null");

        String[] parsedReplacements = getReplacements(locales, commandIssuer);
        if (parsedReplacements.length == 0) {
            return locales.getMessage(commandIssuer, getMessageKey());
        }
        return ACFUtil.replaceStrings(locales.getMessage(commandIssuer, getMessageKey()), parsedReplacements);
    }

    private static String[] toReplacementsArray(
            @NotNull PluginLocales locales,
            @Nullable CommandIssuer commandIssuer,
            @NotNull MessageReplacement... replacements) {
        String[] replacementsArray = new String[replacements.length * 2];
        int i = 0;
        for (MessageReplacement replacement : replacements) {
            replacementsArray[i++] = replacement.getKey();
            replacementsArray[i++] = replacement.getReplacement().fold(
                    str -> str,
                    message -> message.formatted(locales, commandIssuer));
        }
        return replacementsArray;
    }
}