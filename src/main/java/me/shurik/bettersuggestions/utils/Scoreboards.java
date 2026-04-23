package me.shurik.bettersuggestions.utils;

import me.shurik.bettersuggestions.Server;
import net.minecraft.world.scores.Objective;
import net.minecraft.world.scores.ReadOnlyScoreInfo;
import net.minecraft.world.scores.ScoreHolder;
import net.minecraft.world.scores.Scoreboard;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collection;

public class Scoreboards {
    public interface ScoreboardValue {
        String getObjective();
        int getScore();
    }

    public record ScoreboardScoreContainer(Objective objective, ReadOnlyScoreInfo score) implements ScoreboardValue {
        @Override
        public String getObjective() {
            return objective.getName();
        }

        @Override
        public int getScore() {
            return score.value();
        }
    }

    // Base methods
    @SuppressWarnings("unchecked")
    public static net.minecraft.world.scores.Scoreboard getInstance() {
        // Use reflection to avoid compile-time DataResourceStore dependency (removed in Fabric API 0.145.4)
        try {
            java.lang.reflect.Method m = Server.INSTANCE.getClass().getMethod("getScoreboard");
            return (net.minecraft.world.scores.Scoreboard) m.invoke(Server.INSTANCE);
        } catch (Exception e) { throw new RuntimeException(e); }
    }
    public static Collection<Objective> getObjectives() { return getInstance().getObjectives(); }

    @Nullable
    public static Objective getObjective(String name) {
        return getObjectives().stream().filter(o -> o.getName().equals(name)).findFirst().orElse(null);
    }

    @Nullable
    public static ReadOnlyScoreInfo getScore(String name, Objective objective) {
        return getInstance().getPlayerScoreInfo(ScoreHolder.forNameOnly(name), objective);
    }

    public static Collection<ScoreboardScoreContainer> getScores(String name) {
        Collection<ScoreboardScoreContainer> scores = new ArrayList<>();
        for (Objective objective : getObjectives()) {
            ReadOnlyScoreInfo score = getScore(name, objective);
            if (score != null) scores.add(new ScoreboardScoreContainer(objective, score));
        }
        return scores;
    }

    public static void setScore(String name, Objective objective, int value) {
        getInstance().getOrCreatePlayerScore(ScoreHolder.forNameOnly(name), objective).set(value);
    }

    @Nullable
    public static ReadOnlyScoreInfo getScore(String name, String objectiveName) {
        Objective objective = getObjective(objectiveName);
        if (objective == null) return null;
        return getScore(name, objective);
    }

    public static boolean setScore(String name, String objectiveName, int value) {
        Objective objective = getObjective(objectiveName);
        if (objective == null) return false;
        setScore(name, objective, value);
        return true;
    }
}
