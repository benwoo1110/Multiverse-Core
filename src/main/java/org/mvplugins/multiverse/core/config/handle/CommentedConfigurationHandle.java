package org.mvplugins.multiverse.core.config.handle;

import java.io.IOException;
import java.nio.file.Path;
import java.util.logging.Logger;

import io.github.townyadvanced.commentedconfiguration.CommentedConfiguration;
import io.vavr.control.Try;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import org.mvplugins.multiverse.core.config.migration.ConfigMigrator;
import org.mvplugins.multiverse.core.config.node.CommentedNode;
import org.mvplugins.multiverse.core.config.node.NodeGroup;
import org.mvplugins.multiverse.core.config.node.ValueNode;

/**
 * Configuration handle for commented YAML files.
 */
public class CommentedConfigurationHandle extends FileConfigurationHandle<CommentedConfiguration> {

    /**
     * Creates a new builder for a {@link CommentedConfigurationHandle}.
     *
     * @param configPath    The path to the config file.
     * @param nodes         The nodes.
     * @return The builder.
     */
    public static @NotNull Builder builder(@NotNull Path configPath, @NotNull NodeGroup nodes) {
        return new Builder(configPath, nodes);
    }

    protected CommentedConfigurationHandle(
            @NotNull Path configPath,
            @Nullable Logger logger,
            @NotNull NodeGroup nodes,
            @Nullable ConfigMigrator migrator) {
        super(configPath, logger, nodes, migrator);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void loadConfigObject() throws IOException {
        config = new CommentedConfiguration(configPath, logger);
        if (!config.load()) {
            throw new IOException("Failed to load commented config file " + configPath
                    + ". See console for details.");
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Try<Void> save() {
        return Try.run(() -> {
            config = new CommentedConfiguration(configPath, logger);
            nodes.forEach(node -> {
                if (node instanceof CommentedNode typedNode) {
                    if (typedNode.getComments().length > 0) {
                        config.addComment(typedNode.getPath(), typedNode.getComments());
                    }
                }
                if (node instanceof ValueNode valueNode) {
                    serializeNodeToConfig(valueNode);
                }
            });
            config.save();
        });
    }

    /**
     * Builder for {@link CommentedConfigurationHandle}.
     */
    public static class Builder extends FileConfigurationHandle.Builder<CommentedConfiguration, Builder> {

        protected Builder(@NotNull Path configPath, @NotNull NodeGroup nodes) {
            super(configPath, nodes);
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public @NotNull CommentedConfigurationHandle build() {
            return new CommentedConfigurationHandle(configPath, logger, nodes, migrator);
        }
    }
}
