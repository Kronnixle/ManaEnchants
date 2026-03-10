package net.manameta.manaenchants.common.helpers;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.jetbrains.annotations.NotNull;
import org.jspecify.annotations.NonNull;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.function.Supplier;

/**
 * @since 1.0.0
 * @author _Kron
 * @version 1.0.0
 */
public class TimeHelpers {
    /**
     * We use System.currentTimeMillis - startedTime to get a formated version (Component) of the
     * time difference.
     *
     * @param startedTime the time to compare with.
     * @return the formated time
     */
    public static Component formatTimeLocalized(long startedTime) {
        long currentTime = System.currentTimeMillis();
        long diffMillis = Math.abs(currentTime - startedTime);
        long totalSeconds = diffMillis / 1000;

        int years   = (int) (totalSeconds / (60 * 60 * 24 * 365));
        int months  = (int) ((totalSeconds / (60 * 60 * 24 * 30)) % 12);
        int days    = (int) ((totalSeconds / (60 * 60 * 24)) % 30);
        int hours   = (int) ((totalSeconds / (60 * 60)) % 24);
        int minutes = (int) ((totalSeconds / 60) % 60);
        int seconds = (int) (totalSeconds % 60);

        List<Component> parts = new ArrayList<>(2);

        addPart(parts, years,   () -> unitComponent(years,   "time.year",   "time.years"));
        addPart(parts, months,  () -> unitComponent(months,  "time.month",  "time.months"));
        addPart(parts, days,    () -> unitComponent(days,    "time.day",    "time.days"));
        addPart(parts, hours,   () -> unitComponent(hours,   "time.hour",   "time.hours"));
        addPart(parts, minutes, () -> unitComponent(minutes, "time.minute", "time.minutes"));

        if (parts.size() < 2 && seconds > 0) {
            parts.add(unitComponent(seconds, "time.second", "time.seconds"));
        }

        if (parts.isEmpty()) {
            return Component.translatable("time.now", NamedTextColor.WHITE);
        }

        Component result = parts.getFirst();
        if (parts.size() == 2) {
            result = result.append(Component.space()).append(parts.get(1));
        }

        return result.append(Component.space())
                .append(Component.translatable("time.ago", NamedTextColor.WHITE));
    }

    private static void addPart(@NonNull Collection<? super Component> parts, int value, Supplier<? extends Component> supplier) {
        if (parts.size() < 2 && value > 0) {
            parts.add(supplier.get());
        }
    }
    /**
     * Helper for pluralized unit components.
     */
    private static @NotNull Component unitComponent(int value, String singularKey, String pluralKey) {
        String key = (value == 1) ? singularKey : pluralKey;
        return Component.text(value + " ", NamedTextColor.WHITE).append(Component.translatable(key, NamedTextColor.WHITE));
    }
}