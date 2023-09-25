package org.mvplugins.multiverse.core.world.options;

import java.util.Random;

import org.bukkit.World;
import org.bukkit.WorldType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Options for customizing the creation of a new world.
 */
public class CreateWorldOptions {

    /**
     * Creates a new {@link CreateWorldOptions} instance with the given world name.
     *
     * @param worldName The name of the world to create.
     * @return A new {@link CreateWorldOptions} instance.
     */
    public static @NotNull CreateWorldOptions worldName(@NotNull String worldName) {
        return new CreateWorldOptions(worldName);
    }

    private final String worldName;
    private World.Environment environment = World.Environment.NORMAL;
    private boolean generateStructures = true;
    private String generator = null;
    private long seed;
    private boolean useSpawnAdjust = true;
    private WorldType worldType = WorldType.NORMAL;

    /**
     * Creates a new {@link CreateWorldOptions} instance with the given world name.
     *
     * @param worldName The name of the world to create.
     */
    CreateWorldOptions(@NotNull String worldName) {
        this.worldName = worldName;
        this.seed = (new Random()).nextLong();
    }

    /**
     * Gets the name of the world to create.
     *
     * @return The name of the world to create.
     */
    public @NotNull String worldName() {
        return worldName;
    }

    /**
     * Sets the environment of the world to create.
     *
     * @param environmentInput  The environment of the world to create.
     * @return This {@link CreateWorldOptions} instance.
     */
    public @NotNull CreateWorldOptions environment(@NotNull World.Environment environmentInput) {
        this.environment = environmentInput;
        return this;
    }

    /**
     * Gets the environment of the world to create.
     *
     * @return The environment of the world to create.
     */
    public @NotNull World.Environment environment() {
        return environment;
    }

    /**
     * Sets whether structures such as NPC villages should be generated.
     *
     * @param generateStructuresInput   Whether structures such as NPC villages should be generated.
     * @return This {@link CreateWorldOptions} instance.
     */
    public @NotNull CreateWorldOptions generateStructures(boolean generateStructuresInput) {
        this.generateStructures = generateStructuresInput;
        return this;
    }

    /**
     * Gets whether structures such as NPC villages should be generated.
     *
     * @return Whether structures such as NPC villages should be generated.
     */
    public boolean generateStructures() {
        return generateStructures;
    }

    /**
     * Sets the custom generator plugin and its parameters.
     *
     * @param generatorInput    The custom generator plugin and its parameters.
     * @return This {@link CreateWorldOptions} instance.
     */
    public @NotNull CreateWorldOptions generator(@Nullable String generatorInput) {
        this.generator = generatorInput;
        return this;
    }

    /**
     * Gets the custom generator plugin and its parameters.
     *
     * @return The custom generator plugin and its parameters.
     */
    public @Nullable String generator() {
        return generator;
    }

    /**
     * Sets the seed of the world to create. If the seed is a number, it will be parsed as a long. Otherwise, it will be
     * hashed.
     *
     * @param seedInput The seed of the world to create.
     * @return This {@link CreateWorldOptions} instance.
     */
    public @NotNull CreateWorldOptions seed(@Nullable String seedInput) {
        if (seedInput == null) {
            return this;
        }
        try {
            this.seed = Long.parseLong(seedInput);
        } catch (NumberFormatException numberformatexception) {
            this.seed = seedInput.hashCode();
        }
        return this;
    }

    /**
     * Sets the seed of the world to create.
     *
     * @param seedInput The seed of the world to create.
     * @return This {@link CreateWorldOptions} instance.
     */
    public @NotNull CreateWorldOptions seed(long seedInput) {
        this.seed = seedInput;
        return this;
    }

    /**
     * Gets the seed of the world to create.
     *
     * @return The seed of the world to create.
     */
    public long seed() {
        return seed;
    }

    /**
     * Sets whether multiverse will search for a safe spawn location.
     *
     * @param useSpawnAdjustInput   Whether multiverse will search for a safe spawn location.
     * @return This {@link CreateWorldOptions} instance.
     */
    public @NotNull CreateWorldOptions useSpawnAdjust(boolean useSpawnAdjustInput) {
        this.useSpawnAdjust = useSpawnAdjustInput;
        return this;
    }

    /**
     * Gets whether multiverse will search for a safe spawn location.
     *
     * @return Whether multiverse will search for a safe spawn location.
     */
    public boolean useSpawnAdjust() {
        return useSpawnAdjust;
    }

    /**
     * Sets the world type.
     *
     * @param worldTypeInput    The world type.
     * @return This {@link CreateWorldOptions} instance.
     */
    public @NotNull CreateWorldOptions worldType(@NotNull WorldType worldTypeInput) {
        this.worldType = worldTypeInput;
        return this;
    }

    /**
     * Gets the world type.
     *
     * @return The world type.
     */
    public @NotNull WorldType worldType() {
        return worldType;
    }
}