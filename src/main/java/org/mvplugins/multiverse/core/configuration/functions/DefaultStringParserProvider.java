package org.mvplugins.multiverse.core.configuration.functions;

import java.util.HashMap;
import java.util.Map;

import co.aikar.commands.ACFUtil;
import io.vavr.control.Try;

/**
 * Provides default string parsers for common types.
 */
public final class DefaultStringParserProvider {

    private static final Map<Class<?>, NodeStringParser<?>> PARSERS = new HashMap<>();

    /**
     * Adds a default string parser for the given type.
     *
     * @param clazz     The type.
     * @param parser    The string parser.
     */
    public static void addDefaultStringParser(Class<?> clazz, NodeStringParser<?> parser) {
        PARSERS.put(clazz, parser);
    }

    /**
     * Gets the default string parser for the given type.
     *
     * @param clazz The type.
     * @param <T>   The type.
     * @return The default string parser for the given type, or null if no default string parser exists.
     */
    public static <T> NodeStringParser<T> getDefaultStringParser(Class<T> clazz) {
        if (clazz.isEnum()) {
            // Special case for enums
            return (NodeStringParser<T>) ENUM_STRING_PARSER;
        }
        return (NodeStringParser<T>) PARSERS.get(clazz);
    }

    private static final NodeStringParser<Enum> ENUM_STRING_PARSER = (input, type) -> Try.of(
            () -> Enum.valueOf(type, input.toUpperCase()));

    private static final NodeStringParser<String> STRING_STRING_PARSER = (input, type) -> Try.of(
            () -> input);

    private static final NodeStringParser<Boolean> BOOLEAN_STRING_PARSER = (input, type) -> Try.of(
            () -> ACFUtil.isTruthy(String.valueOf(input).toLowerCase()));

    private static final NodeStringParser<Integer> INTEGER_STRING_PARSER = (input, type) -> Try.of(
            () -> ACFUtil.parseInt(input));

    private static final NodeStringParser<Double> DOUBLE_STRING_PARSER = (input, type) -> Try.of(
            () -> ACFUtil.parseDouble(input));

    private static final NodeStringParser<Float> FLOAT_STRING_PARSER = (input, type) -> Try.of(
            () -> ACFUtil.parseFloat(input));

    private static final NodeStringParser<Long> LONG_STRING_PARSER = (input, type) -> Try.of(
            () -> ACFUtil.parseLong(input));

    static {
        addDefaultStringParser(String.class, STRING_STRING_PARSER);
        addDefaultStringParser(Boolean.class, BOOLEAN_STRING_PARSER);
        addDefaultStringParser(Integer.class, INTEGER_STRING_PARSER);
        addDefaultStringParser(Double.class, DOUBLE_STRING_PARSER);
        addDefaultStringParser(Float.class, FLOAT_STRING_PARSER);
        addDefaultStringParser(Long.class, LONG_STRING_PARSER);
    }

    private DefaultStringParserProvider() {
        // Prevent instantiation as this is a static utility class
    }
}