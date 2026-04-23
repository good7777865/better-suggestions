package me.shurik.bettersuggestions.utils;

import me.shurik.bettersuggestions.client.data.ClientScoreboardValue;
import net.minecraft.network.RegistryFriendlyByteBuf;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class ByteBufUtils {
    public static void writeScoreboardValue(RegistryFriendlyByteBuf buf, Scoreboards.ScoreboardValue container) {
        buf.writeUtf(container.getObjective());
        buf.writeInt(container.getScore());
    }

    public static ClientScoreboardValue readScoreboardValue(RegistryFriendlyByteBuf buf) {
        return new ClientScoreboardValue(buf.readUtf(32767), buf.readInt());
    }

    public static <T> RegistryFriendlyByteBuf writeCollection(RegistryFriendlyByteBuf buffer, Collection<T> collection, BiConsumer<RegistryFriendlyByteBuf, T> writer) {
        buffer.writeInt(collection.size());
        for (T t : collection) {
            writer.accept(buffer, t);
        }
        return buffer;
    }

    public static <T> Collection<T> readCollection(RegistryFriendlyByteBuf buffer, Function<RegistryFriendlyByteBuf, T> reader) {
        int size = buffer.readInt();
        Collection<T> collection = new ArrayList<>(size);
        for (int i = 0; i < size; i++) {
            collection.add(reader.apply(buffer));
        }
        return collection;
    }

    public static <T> Set<T> readSet(RegistryFriendlyByteBuf buffer, Function<RegistryFriendlyByteBuf, T> reader) {
        int size = buffer.readInt();
        return IntStream.range(0, size).mapToObj(i -> reader.apply(buffer)).collect(Collectors.toSet());
    }
}
