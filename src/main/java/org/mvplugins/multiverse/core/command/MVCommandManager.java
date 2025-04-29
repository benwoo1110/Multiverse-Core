package org.mvplugins.multiverse.core.command;

import java.util.List;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.CommandCompletions;
import co.aikar.commands.CommandContexts;
import co.aikar.commands.CommandHelp;
import co.aikar.commands.CommandIssuer;
import co.aikar.commands.HelpEntry;
import co.aikar.commands.LogLevel;
import co.aikar.commands.PaperCommandManager;
import co.aikar.commands.RootCommand;
import com.dumptruckman.minecraft.util.Logging;
import jakarta.inject.Inject;
import jakarta.inject.Provider;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;
import org.jvnet.hk2.annotations.Service;

import org.mvplugins.multiverse.core.MultiverseCore;
import org.mvplugins.multiverse.core.command.flag.CommandFlagsManager;
import org.mvplugins.multiverse.core.command.queue.CommandQueueManager;
import org.mvplugins.multiverse.core.config.CoreConfig;
import org.mvplugins.multiverse.core.locale.PluginLocales;
import org.mvplugins.multiverse.core.world.WorldManager;
import org.mvplugins.multiverse.core.world.helpers.WorldNameChecker;

/**
 * Main class to manage permissions.
 */
@Service
public class MVCommandManager extends PaperCommandManager {

    private final CommandFlagsManager flagsManager;
    private final CommandQueueManager commandQueueManager;
    private final Provider<MVCommandContexts> commandContextsProvider;
    private final Provider<MVCommandCompletions> commandCompletionsProvider;
    private final MVCommandPermissions commandPermissions;
    private final CoreConfig config;
    private final PluginLocales pluginLocales;

    @Inject
    MVCommandManager(
            @NotNull MultiverseCore plugin,
            @NotNull CommandFlagsManager flagsManager,
            @NotNull CommandQueueManager commandQueueManager,
            @NotNull Provider<MVCommandContexts> commandContextsProvider,
            @NotNull Provider<MVCommandCompletions> commandCompletionsProvider,
            @NotNull WorldManager worldManager,
            @NotNull WorldNameChecker worldNameChecker,
            @NotNull MVCommandPermissions commandPermissions,
            @NotNull CoreConfig config
            ) {
        super(plugin);
        this.flagsManager = flagsManager;
        this.commandQueueManager = commandQueueManager;
        this.commandContextsProvider = commandContextsProvider;
        this.commandCompletionsProvider = commandCompletionsProvider;
        this.commandPermissions = commandPermissions;
        this.config = config;
        this.pluginLocales = new PluginLocales(this);
        this.locales = this.pluginLocales;
        this.pluginLocales.loadLanguages();

        MVCommandConditions.load(this, worldManager, worldNameChecker);
        this.enableUnstableAPI("help");
        this.setDefaultExceptionHandler(new MVDefaultExceptionHandler());
    }

    /**
     * Registers a list of commands and handles {@link LegacyAliasCommand} based on config option.
     * @param commands  The commands to register
     * @param <T>       The type of the commands
     */
    public <T extends BaseCommand> void registerAllCommands(List<T> commands) {
        List<T> noLegacy = commands.stream().filter(command -> !(command instanceof LegacyAliasCommand)).toList();
        if (!config.getShowLegacyAliases()) {
            noLegacy.forEach(this::registerCommand);
            return;
        }

        List<T> legacy = commands.stream().filter(command -> command instanceof LegacyAliasCommand).toList();
        List<T> noLegacyInheritance = noLegacy.stream()
                .filter(command -> legacy.stream().noneMatch(
                        legacyAliasCommand -> command.getClass().isAssignableFrom(legacyAliasCommand.getClass())))
                .toList();

        legacy.forEach(this::registerCommand);
        noLegacyInheritance.forEach(this::registerCommand);
    }

    @Override
    public void registerCommand(BaseCommand command, boolean force) {
        if (command instanceof LegacyAliasCommand && !config.getShowLegacyAliases()) {
            return;
        }
        super.registerCommand(command, force);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public PluginLocales getLocales() {
        return this.pluginLocales;
    }

    /**
     * Gets class responsible for flag handling.
     *
     * @return A not-null {@link CommandFlagsManager}.
     */
    public synchronized @NotNull CommandFlagsManager getFlagsManager() {
        return flagsManager;
    }

    /**
     * Manager for command that requires /mv confirm before execution.
     *
     * @return A non-null {@link CommandQueueManager}.
     */
    public synchronized @NotNull CommandQueueManager getCommandQueueManager() {
        return commandQueueManager;
    }

    public synchronized @NotNull MVCommandPermissions getCommandPermissions() {
        return commandPermissions;
    }

    /**
     * Gets class responsible for parsing string args into objects.
     *
     * @return A not-null {@link CommandContexts}.
     */
    @Override
    public synchronized @NotNull MVCommandContexts getCommandContexts() {
        if (this.contexts == null) {
            this.contexts = commandContextsProvider.get();
        }
        return (MVCommandContexts) this.contexts;
    }

    /**
     * Gets class responsible for tab-completion handling.
     *
     * @return A not-null {@link CommandCompletions}.
     */
    @Override
    public synchronized @NotNull MVCommandCompletions getCommandCompletions() {
        if (this.completions == null) {
            this.completions = commandCompletionsProvider.get();
        }
        return (MVCommandCompletions) this.completions;
    }

    @Override
    public boolean hasPermission(CommandIssuer issuer, String permission) {
        return commandPermissions.hasPermission(issuer, permission);
    }

    /**
     * Standardise usage command formatting for all mv modules.
     *
     * @param help The target {@link CommandHelp}.
     */
    public void showUsage(@NotNull CommandHelp help) {
        List<HelpEntry> entries = help.getHelpEntries();
        if (entries.size() == 1) {
            getHelpFormatter().showDetailedHelp(help, entries.get(0));
            return;
        }
        help.showHelp();
    }

    public @NotNull MVCommandIssuer getConsoleCommandIssuer() {
        return getCommandIssuer(Bukkit.getConsoleSender());
    }

    @Override
    public @NotNull MVCommandIssuer getCommandIssuer(Object issuer) {
        if (!(issuer instanceof CommandSender)) {
            throw new IllegalArgumentException(issuer.getClass().getName() + " is not a Command Issuer.");
        } else {
            return new MVCommandIssuer(this, (CommandSender)issuer);
        }
    }

    @Override
    public RootCommand createRootCommand(String cmd) {
        return new MVRootCommand(this, cmd);
    }

    @Override
    public void log(LogLevel level, String message, Throwable throwable) {
        Logging.finer("[%s] %s", level, message);
        if (throwable != null) {
            throwable.printStackTrace();
        }
    }
}
