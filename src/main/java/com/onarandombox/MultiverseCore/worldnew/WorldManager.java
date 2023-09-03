package com.onarandombox.MultiverseCore.worldnew;

import com.dumptruckman.minecraft.util.Logging;
import com.google.common.base.Strings;
import com.onarandombox.MultiverseCore.utils.file.FileUtils;
import com.onarandombox.MultiverseCore.utils.result.Result;
import com.onarandombox.MultiverseCore.world.WorldNameChecker;
import com.onarandombox.MultiverseCore.worldnew.config.WorldConfig;
import com.onarandombox.MultiverseCore.worldnew.config.WorldsConfigManager;
import com.onarandombox.MultiverseCore.worldnew.options.CreateWorldOptions;
import com.onarandombox.MultiverseCore.worldnew.options.ImportWorldOptions;
import com.onarandombox.MultiverseCore.worldnew.results.CreateWorldResult;
import com.onarandombox.MultiverseCore.worldnew.results.DeleteWorldResult;
import com.onarandombox.MultiverseCore.worldnew.results.LoadWorldResult;
import com.onarandombox.MultiverseCore.worldnew.results.RemoveWorldResult;
import com.onarandombox.MultiverseCore.worldnew.results.UnloadWorldResult;
import io.vavr.control.Option;
import jakarta.inject.Inject;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.WorldCreator;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jvnet.hk2.annotations.Service;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class WorldManager {

    private final Map<String, OfflineWorld> offlineWorldsMap;
    private final Map<String, MVWorld> worldsMap;
    private final WorldsConfigManager worldsConfigManager;

    @Inject
    WorldManager(@NotNull WorldsConfigManager worldsConfigManager) {
        this.offlineWorldsMap = new HashMap<>();
        this.worldsMap = new HashMap<>();
        this.worldsConfigManager = worldsConfigManager;
    }

    public void initAllWorlds() {
        populateWorldFromConfig();
        loadDefaultWorlds();
        getOfflineWorlds().forEach(world -> {
            if (isMVWorld(world) || !world.getAutoLoad()) {
                return;
            }
            loadWorld(world).onFailure((failure) -> {
                Logging.severe("Failed to load world %s: %s", world.getName(), failure);
            });
        });
        saveWorldsConfig();
    }

    private void populateWorldFromConfig() {
        worldsConfigManager.load();
        worldsConfigManager.getAllWorldConfigs().forEach(worldConfig -> {
            getMVWorld(worldConfig.getWorldName())
                    .peek(mvWorld -> mvWorld.setWorldConfig(worldConfig));
            getOfflineWorld(worldConfig.getWorldName())
                    .peek(offlineWorld -> offlineWorld.setWorldConfig(worldConfig))
                    .onEmpty(() -> {
                        OfflineWorld offlineWorld = new OfflineWorld(worldConfig.getWorldName(), worldConfig);
                        offlineWorldsMap.put(offlineWorld.getName(), offlineWorld);
                    });
        });
    }

    private void loadDefaultWorlds() {
        Bukkit.getWorlds().forEach((world) -> {
            if (isOfflineWorld(world.getName())) {
                return;
            }
            importWorld(ImportWorldOptions.worldName(world.getName())
                    .environment(world.getEnvironment())
                    .generator("") // TODO: Get default generators from bukkit.yml
            );
        });
    }

    /**
     * Creates a new world.
     *
     * @param options   The options for customizing the creation of a new world.
     */
    public Result<CreateWorldResult.Success, CreateWorldResult.Failure> createWorld(CreateWorldOptions options) {
        if (!WorldNameChecker.isValidWorldName(options.worldName())) {
            return Result.failure(CreateWorldResult.Failure.INVALID_WORLDNAME);
        }

        if (getMVWorld(options.worldName()).isDefined()) {
            return Result.failure(CreateWorldResult.Failure.WORLD_EXIST_LOADED);
        }

        if (getOfflineWorld(options.worldName()).isDefined()) {
            return Result.failure(CreateWorldResult.Failure.WORLD_EXIST_OFFLINE);
        }

        File worldFolder = new File(Bukkit.getWorldContainer(), options.worldName());
        if (worldFolder.exists()) {
            return Result.failure(CreateWorldResult.Failure.WORLD_EXIST_FOLDER);
        }

        // Create bukkit world
        World world = WorldCreator.name(options.worldName())
                .environment(options.environment())
                .generateStructures(options.generateStructures())
                .generator(Strings.isNullOrEmpty(options.generator()) ? null : options.generator())
                .seed(options.seed())
                .type(options.worldType())
                .createWorld();
        if (world == null) {
            Logging.severe("Failed to create world: " + options.worldName());
            return Result.failure(CreateWorldResult.Failure.BUKKIT_CREATION_FAILED);
        }
        Logging.fine("Loaded bukkit world: " + world.getName());

        // Our multiverse world
        MVWorld mvWorld = newMVWorld(world);
        mvWorld.getWorldConfig().setGenerator(options.generator());
        saveWorldsConfig();
        return Result.success(CreateWorldResult.Success.CREATED);
    }

    public Result<CreateWorldResult.Success, CreateWorldResult.Failure> importWorld(ImportWorldOptions options) {
        // Create bukkit world
        World world = WorldCreator.name(options.worldName())
                .environment(options.environment())
                .generator(Strings.isNullOrEmpty(options.generator()) ? null : options.generator())
                .createWorld();
        if (world == null) {
            Logging.severe("Failed to create world: " + options.worldName());
            return Result.failure(CreateWorldResult.Failure.BUKKIT_CREATION_FAILED);
        }
        Logging.fine("Loaded bukkit world: " + world.getName());

        // Our multiverse world
        MVWorld mvWorld = newMVWorld(world);
        mvWorld.getWorldConfig().setGenerator(options.generator());
        saveWorldsConfig();
        return Result.success(CreateWorldResult.Success.CREATED);
    }

    private MVWorld newMVWorld(World world) {
        WorldConfig worldConfig = worldsConfigManager.addWorldConfig(world.getName());

        OfflineWorld offlineWorld = new OfflineWorld(world.getName(), worldConfig);
        offlineWorldsMap.put(offlineWorld.getName(), offlineWorld);

        MVWorld mvWorld = new MVWorld(world, worldConfig);
        worldsMap.put(mvWorld.getName(), mvWorld);
        return mvWorld;
    }

    public Result<LoadWorldResult.Success, LoadWorldResult.Failure> loadWorld(@NotNull String worldName) {
        return getOfflineWorld(worldName)
                .map(this::loadWorld)
                .getOrElse(() -> Result.failure(LoadWorldResult.Failure.WORLD_NON_EXISTENT));
    }

    public Result<LoadWorldResult.Success, LoadWorldResult.Failure> loadWorld(@NotNull OfflineWorld offlineWorld) {
        if (isMVWorld(offlineWorld)) {
            Logging.severe("World already loaded: " + offlineWorld.getName());
            return Result.failure(LoadWorldResult.Failure.WORLD_EXIST_LOADED);
        }

        // TODO: Reduce copy paste from createWorld method
        World world = WorldCreator.name(offlineWorld.getName())
                .environment(offlineWorld.getEnvironment())
                .generator(Strings.isNullOrEmpty(offlineWorld.getGenerator()) ? null : offlineWorld.getGenerator())
                .seed(offlineWorld.getSeed())
                .createWorld();
        if (world == null) {
            Logging.severe("Failed to create world: " + offlineWorld.getName());
            return Result.failure(LoadWorldResult.Failure.BUKKIT_CREATION_FAILED);
        }
        Logging.fine("Loaded bukkit world: " + world.getName());

        // Our multiverse world
        WorldConfig worldConfig = worldsConfigManager.getWorldConfig(offlineWorld.getName());
        MVWorld mvWorld = new MVWorld(world, worldConfig);
        worldsMap.put(mvWorld.getName(), mvWorld);

        saveWorldsConfig();
        return Result.success(LoadWorldResult.Success.LOADED);
    }

    public Result<UnloadWorldResult.Success, UnloadWorldResult.Failure> unloadWorld(@NotNull String worldName) {
        return getMVWorld(worldName)
                .map(this::unloadWorld)
                .getOrElse(() -> Result.failure(UnloadWorldResult.Failure.WORLD_NON_EXISTENT));
    }

    public Result<UnloadWorldResult.Success, UnloadWorldResult.Failure> unloadWorld(@NotNull MVWorld world) {
        World bukkitWorld = world.getBukkitWorld().getOrNull();
        // TODO: removePlayersFromWorld?
        if (!Bukkit.unloadWorld(bukkitWorld, true)) {
            Logging.severe("Failed to unload world: " + world.getName());
            return Result.failure(UnloadWorldResult.Failure.BUKKIT_UNLOAD_FAILED);
        }
        MVWorld mvWorld = worldsMap.remove(world.getName());
        if (mvWorld == null) {
            Logging.severe("Failed to remove world from map: " + world.getName());
            return Result.failure(UnloadWorldResult.Failure.WORLD_NON_EXISTENT);
        }
        Logging.fine("Unloaded world: " + world.getName());

        mvWorld.getWorldConfig().deferenceMVWorld();
        return Result.success(UnloadWorldResult.Success.UNLOADED);
    }

    public Result<RemoveWorldResult.Success, UnloadWorldResult.Failure> removeWorld(@NotNull String worldName) {
        return getOfflineWorld(worldName)
                .map(this::removeWorld)
                .getOrElse(() -> Result.failure(RemoveWorldResult.Failure.WORLD_NON_EXISTENT));
    }

    public Result<RemoveWorldResult.Success, UnloadWorldResult.Failure> removeWorld(@NotNull OfflineWorld world) {
        MVWorld mvWorld = getMVWorld(world).getOrNull();
        if (mvWorld != null) {
            var result = unloadWorld(mvWorld);
            if (result.isFailure()) {
                return Result.failure(result.getFailureReason());
            }
        }

        // Remove world from config
        offlineWorldsMap.remove(world.getName());
        worldsConfigManager.deleteWorldConfig(world.getName());
        saveWorldsConfig();

        return Result.success(RemoveWorldResult.Success.REMOVED);
    }

    public Result<DeleteWorldResult.Success, UnloadWorldResult.Failure> deleteWorld(@NotNull String worldName) {
        return getMVWorld(worldName)
                .map(this::deleteWorld)
                .getOrElse(() -> Result.failure(DeleteWorldResult.Failure.WORLD_NON_EXISTENT));
    }

    public Result<DeleteWorldResult.Success, UnloadWorldResult.Failure> deleteWorld(@NotNull MVWorld world) {
        File worldFolder = world.getBukkitWorld().map(World::getWorldFolder).getOrNull();
        if (worldFolder == null || !WorldNameChecker.isValidWorldFolder(worldFolder)) {
            Logging.severe("Failed to get world folder for world: " + world.getName());
            return Result.failure(DeleteWorldResult.Failure.WORLD_FOLDER_NOT_FOUND);
        }

        var result = removeWorld(world);
        if (result.isFailure()) {
            return Result.failure(result.getFailureReason());
        }

        // Erase world files from disk
        // TODO: Config options to keep certain files
        if (!FileUtils.deleteFolder(worldFolder)) {
            Logging.severe("Failed to delete world folder: " + worldFolder);
            return Result.failure(DeleteWorldResult.Failure.FAILED_TO_DELETE_FOLDER);
        }

        return Result.success(DeleteWorldResult.Success.DELETED);
    }

    public Option<OfflineWorld> getOfflineOnlyWorld(@Nullable String worldName) {
        return isMVWorld(worldName) ? Option.none() : Option.of(offlineWorldsMap.get(worldName));
    }

    public Collection<OfflineWorld> getOfflineOnlyWorlds() {
        return offlineWorldsMap.values().stream().filter(world -> !world.isLoaded()).toList();
    }

    public Option<OfflineWorld> getOfflineWorld(@Nullable String worldName) {
        return Option.of(offlineWorldsMap.get(worldName));
    }

    public Collection<OfflineWorld> getOfflineWorlds() {
        return offlineWorldsMap.values();
    }

    public boolean isOfflineWorld(@Nullable String worldName) {
        return offlineWorldsMap.containsKey(worldName);
    }

    public Option<MVWorld> getMVWorld(@Nullable World world) {
        return world == null ? Option.none() : Option.of(worldsMap.get(world.getName()));
    }

    public Option<MVWorld> getMVWorld(@Nullable OfflineWorld world) {
        return world == null ? Option.none() : Option.of(worldsMap.get(world.getName()));
    }

    public Option<MVWorld> getMVWorld(@Nullable String worldName) {
        return Option.of(worldsMap.get(worldName));
    }

    public Collection<MVWorld> getMVWorlds() {
        return worldsMap.values();
    }

    public boolean isMVWorld(@Nullable OfflineWorld world) {
        return world != null && isMVWorld(world.getName());
    }

    public boolean isMVWorld(@Nullable String worldName) {
        return worldsMap.containsKey(worldName);
    }

    public void saveWorldsConfig() {
        worldsConfigManager.save();
    }
}
