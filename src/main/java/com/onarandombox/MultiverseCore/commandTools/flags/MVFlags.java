package com.onarandombox.MultiverseCore.commandtools.flags;

import co.aikar.commands.InvalidCommandArgument;
import com.onarandombox.MultiverseCore.MultiverseCore;
import com.onarandombox.MultiverseCore.utils.webpaste.PasteServiceType;
import org.bukkit.WorldType;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.stream.Collectors;

public class MVFlags {

    private static MultiverseCore multiverse;

    public static void setCoreInstance(MultiverseCore plugin) {
        multiverse = plugin;
    }

    public static final CommandFlag<String> SEED = new RequiredCommandFlag<String>
            ("Seed", "-s", String.class) {
        @Override
        public Collection<String> suggestValue() {
            return Arrays.asList("seed", String.valueOf(new Random().nextLong()));
        }

        @Override
        public String getValue(@NotNull String input) throws FlagParseFailedException {
            return input;
        }
    };

    public static final CommandFlag<String> RANDOM_SEED = new OptionalCommandFlag<String>
            ("Seed", "-s", String.class) {
        @Override
        public Collection<String> suggestValue() {
            return Arrays.asList("seed", String.valueOf(new Random().nextLong()));
        }

        @Override
        public String getValue(@NotNull String input) throws FlagParseFailedException {
            return input;
        }
    };

    public static final CommandFlag<WorldType> WORLD_TYPE = new RequiredCommandFlag<WorldType>
            ("WorldType", "-t", WorldType.class) {

        private final Map<String, WorldType> typeAlias = new HashMap<String, WorldType>(4){{
            put("normal", WorldType.NORMAL);
            put("flat", WorldType.FLAT);
            put("largebiomes", WorldType.LARGE_BIOMES);
            put("amplified", WorldType.AMPLIFIED);
        }};

        @Override
        public Collection<String> suggestValue() {
            return typeAlias.keySet();
        }

        @Override
        public WorldType getValue(@NotNull String input) throws FlagParseFailedException {
            WorldType type = typeAlias.get(input);
            if (type != null) {
                return type;
            }
            try {
                return WorldType.valueOf(input.toUpperCase());
            }
            catch (IllegalArgumentException e) {
                throw new FlagParseFailedException("'%s' is not a valid. See /mv env for available World Type.", input);
            }
        }

        @Override
        public WorldType getDefaultValue() {
            return WorldType.NORMAL;
        }
    };

    public static final CommandFlag<String> GENERATOR = new RequiredCommandFlag<String>
            ("Generator", "-g", String.class) {
        @Override
        public Collection<String> suggestValue() {
            return multiverse.getMVWorldManager().getAvailableWorldGenerators();
        }

        @Override
        public String getValue(@NotNull String input) throws FlagParseFailedException {
            String[] genArray = input.split(":");
            String generator = genArray[0];
            String generatorId = (genArray.length > 1) ? genArray[1] : "";
            try {
                if (multiverse.getMVWorldManager().getChunkGenerator(generator, generatorId, "test") == null) {
                    throw new Exception();
                }
            } catch (Exception e) {
                throw new FlagParseFailedException("Invalid generator string '%s'. See /mv gens for available generators.", input);
            }
            return input;
        }
    };

    public static final CommandFlag<Boolean> GENERATE_STRUCTURES = new RequiredCommandFlag<Boolean>
            ("GenerateStructures", "-a", Boolean.class) {
        @Override
        public Collection<String> suggestValue() {
            return Arrays.asList("true", "false");
        }

        @Override
        public Boolean getValue(@NotNull String input) throws FlagParseFailedException {
            return input.equalsIgnoreCase("true");
        }

        @Override
        public Boolean getDefaultValue() {
            return true;
        }
    };

    public static final CommandFlag<Boolean> SPAWN_ADJUST = new NoValueCommandFlag<Boolean>
            ("AdjustSpawn", "-n", Boolean.class) {
        @Override
        public Boolean getValue() throws FlagParseFailedException {
            return false;
        }

        @Override
        public Boolean getDefaultValue() {
            return true;
        }
    };

    public static final CommandFlag<PasteServiceType> PASTE_SERVICE_TYPE = new OptionalCommandFlag<PasteServiceType>
            ("PasteServiceType", "--paste", PasteServiceType.class) {

        private final List<String> pasteTypes = Arrays.stream(PasteServiceType.values())
                .filter(pt -> pt != PasteServiceType.NONE)
                .map(p -> p.toString().toLowerCase())
                .collect(Collectors.toList());

        @Override
        public Collection<String> suggestValue() {
            return pasteTypes;
        }

        @Override
        public PasteServiceType getValue(@NotNull String input) throws FlagParseFailedException {
            try {
                return PasteServiceType.valueOf(input.toUpperCase());
            }
            catch (IllegalArgumentException e) {
                throw new InvalidCommandArgument(String.format("Invalid paste service type '%s'.", input));
            }
        }

        @Override
        public PasteServiceType getValue() throws FlagParseFailedException {
            return PasteServiceType.PASTEGG;
        }

        @Override
        public PasteServiceType getDefaultValue() {
            return PasteServiceType.NONE;
        }
    }.addAlias("-p");

    public static final CommandFlag<Boolean> INCLUDE_PLUGIN_LIST = new NoValueCommandFlag<Boolean>
            ("IncludePlugins", "--include-plugin-list", Boolean.class) {
        @Override
        public Boolean getValue() throws FlagParseFailedException {
            return true;
        }

        @Override
        public Boolean getDefaultValue() {
            return true;
        }
    }.addAlias("-pl");
}