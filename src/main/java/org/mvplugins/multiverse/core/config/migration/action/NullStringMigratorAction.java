package org.mvplugins.multiverse.core.config.migration.action;

import com.dumptruckman.minecraft.util.Logging;
import org.bukkit.configuration.ConfigurationSection;

/**
 * Single migrator action changes a string value of "null" to an empty string.
 */
public final class NullStringMigratorAction implements MigratorAction {

    public static NullStringMigratorAction of(String path) {
        return new NullStringMigratorAction(path);
    }

    private final String path;

    private NullStringMigratorAction(String path) {
        this.path = path;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void migrate(ConfigurationSection config) {
        config.set(path, "null".equals(config.getString(path)) ? "" : config.getString(path));
        Logging.config("Converted %s to %s", path, config.getString(path));
    }
}
