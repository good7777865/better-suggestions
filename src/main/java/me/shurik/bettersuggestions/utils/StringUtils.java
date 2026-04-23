package me.shurik.bettersuggestions.utils;

import com.google.common.base.Strings;
import net.minecraft.ChatFormatting;
import net.minecraft.core.UUIDUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.phys.Vec3;

import java.util.Arrays;
import java.util.Collection;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

public class StringUtils {
    private static final Pattern WHITESPACE_PATTERN = Pattern.compile("(\\s+)");

    public static boolean isUUID(String string) {
        return string.matches("[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}");
    }

    public static boolean isBlockPos(String string) {
        return string.matches("^-?\\d+ -?\\d+ -?\\d+$");
    }

    public static BlockPos parseBlockPos(String string) {
        String[] split = string.split(" ");
        return new BlockPos(Integer.parseInt(split[0]), Integer.parseInt(split[1]), Integer.parseInt(split[2]));
    }

    public static boolean isPosition(String string) {
        return string.matches("^-?\\d+(?:\\.\\d+)? -?\\d+(?:\\.\\d+)? -?\\d+(?:\\.\\d+)?$");
    }

    public static Vec3 parsePosition(String string) {
        String[] split = string.split(" ");
        return new Vec3(Double.parseDouble(split[0]), Double.parseDouble(split[1]), Double.parseDouble(split[2]));
    }

    // ChatInputSuggestor::getStartOfCurrentWord
    public static int getStartOfCurrentWord(String input) {
        if (Strings.isNullOrEmpty(input)) {
            return 0;
        }
        int i = 0;
        Matcher matcher = WHITESPACE_PATTERN.matcher(input);
        while (matcher.find()) {
            i = matcher.end();
        }
        return i;
    }

    public static Component formatString(String string, ChatFormatting formatting) { return Component.literal(string).withStyle(formatting); }
    public static Component formatTranslation(String translation, ChatFormatting formatting) { return Component.translatable(translation).withStyle(formatting); }
    public static Component formatInt(int i, ChatFormatting formatting) { return Component.literal(Integer.toString(i)).withStyle(formatting); }
    public static Component formatFloat(float d, ChatFormatting formatting) { return Component.literal(String.format("%.2f", d)).withStyle(formatting); }
    public static Component formatDouble(double d, ChatFormatting formatting) { return Component.literal(String.format("%.5f", d)).withStyle(formatting); }

    public static Component formatPos(Vec3 pos) {
        return Component.translatable("%s %s %s", formatDouble(pos.x, ChatFormatting.RED), formatDouble(pos.y, ChatFormatting.GREEN), formatDouble(pos.z, ChatFormatting.BLUE));
    }

    public static Component formatUuidAsIntArray(UUID uuid) {
        int[] ints = UUIDUtil.uuidToIntArray(uuid);
        return Component.translatable("[%s, %s, %s, %s]", formatInt(ints[0], ChatFormatting.GOLD), formatInt(ints[1], ChatFormatting.GOLD), formatInt(ints[2], ChatFormatting.GOLD), formatInt(ints[3], ChatFormatting.GOLD));
    }

    public static Component formatStrings(Collection<String> strings, ChatFormatting formatting) {
        return formatStrings(strings.stream(), strings.size(), formatting);
    }

    public static Component formatStrings(String[] strings, ChatFormatting formatting) {
        return formatStrings(Arrays.stream(strings), strings.length, formatting);
    }

    private static Component formatStrings(Stream<String> strings, final int count, ChatFormatting formatting) {
        MutableComponent text = Component.literal("[");
        AtomicInteger i = new AtomicInteger();
        strings.forEach(string -> {
            text.append(Component.literal(string).withStyle(formatting));
            if (i.getAndIncrement() < count - 1) {
                text.append(Component.literal(", "));
            }
        });
        text.append(Component.literal("]"));
        return text;
    }

    public static Component joinTexts(Collection<Component> texts) {
        MutableComponent text = Component.literal("[");
        AtomicInteger i = new AtomicInteger();
        texts.forEach(t -> {
            text.append(t);
            if (i.getAndIncrement() < texts.size() - 1) {
                text.append(Component.literal(", "));
            }
        });
        text.append(Component.literal("]"));
        return text;
    }
}
