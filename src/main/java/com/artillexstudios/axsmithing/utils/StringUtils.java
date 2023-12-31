package com.artillexstudios.axsmithing.utils;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextDecoration;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import net.md_5.bungee.api.ChatColor;
import org.jetbrains.annotations.NotNull;

public class StringUtils {
    private static final LegacyComponentSerializer LEGACY_COMPONENT_SERIALIZER = LegacyComponentSerializer.builder().character('\u00a7').useUnusualXRepeatedCharacterHexFormat().hexColors().build();
    private static final LegacyComponentSerializer LEGACY_FORMATTER = LegacyComponentSerializer.legacyAmpersand().toBuilder().useUnusualXRepeatedCharacterHexFormat().hexColors().build();
    private static final MiniMessage MINI_MESSAGE = MiniMessage.miniMessage();

    @NotNull
    public static String format(@NotNull String message) {
        message = message.replace('\u00a7', '&');
        message = toLegacy(MINI_MESSAGE.deserialize(message).applyFallbackStyle(TextDecoration.ITALIC.withState(false)));
        message = LEGACY_FORMATTER.serialize(LEGACY_FORMATTER.deserialize(message));
        message = ChatColor.translateAlternateColorCodes('&', message);

        return message;
    }

    @NotNull
    public static Component formatToComponent(@NotNull String message) {
        return toComponent(format(message));
    }

    public static @NotNull String toLegacy(@NotNull Component component) {
        return LEGACY_COMPONENT_SERIALIZER.serialize(component);
    }

    public static @NotNull Component toComponent(@NotNull String message) {
        return LEGACY_COMPONENT_SERIALIZER.deserialize(message);
    }
}
