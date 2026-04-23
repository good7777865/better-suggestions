package me.shurik.bettersuggestions.utils;

import net.minecraft.core.Registry;

public class RegistryUtils {
    public static <T> String getName(Registry<T> registry, T entry) {
        return registry.getKey(entry).toString();
    }
}
