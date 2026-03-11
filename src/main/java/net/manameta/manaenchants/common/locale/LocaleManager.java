package net.manameta.manaenchants.common.locale;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.translation.TranslationStore;
import net.manameta.manaenchants.ManaEnchants;
import net.manameta.manaenchants.common.config.ConfigData;
import org.bukkit.Bukkit;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.text.MessageFormat;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import net.kyori.adventure.key.Key;
import net.kyori.adventure.translation.GlobalTranslator;

public final class LocaleManager {

    private static final Map<String, Map<String, String>> locales = new HashMap<>();
    private static final Key TRANSLATION_KEY = Key.key("manaenchants.locale");

    private LocaleManager() {}

    /**
     * Loads all locales from disk and bundled defaults, and registers them with Adventure's GlobalTranslator
     */
    public static void loadAllLocales() {
        locales.clear();

        // Load bundled default language
        loadBundledLocale();

        File parent = ManaEnchants.getInstance().getDataFolder();
        loadLocaleFolder(new File(parent, "locale"));

        // Register with Adventure translator
        registerAdventureTranslations();
    }

    private static void loadLocaleFolder(@NotNull File folder) {
        if (!folder.exists() || !folder.isDirectory()) return;

        File[] files = folder.listFiles(f -> f.isFile() && f.getName().endsWith(".properties"));
        if (files == null) return;

        for (File file : files) {
            String localeKey = file.getName().replace(".properties", "");
            try (InputStream in = new FileInputStream(file)) {
                merge(localeKey, loadProperties(in));
            } catch (FileNotFoundException e) {
                throw new RuntimeException(e);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private static void loadBundledLocale() {
        String defaultLanguage = ConfigData.get().getDefaultLocale().getLanguage();
        String path = "locale/" + defaultLanguage + ".properties";

        try (InputStream in = LocaleManager.class.getClassLoader().getResourceAsStream(path)) {
            if (in == null) return;

            merge(defaultLanguage, loadProperties(in));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void merge(String localeKey, Map<String, String> messages) {
        locales.computeIfAbsent(localeKey, k -> new HashMap<>()).putAll(messages);
    }

    private static @NotNull Map<String, String> loadProperties(InputStream in) throws IOException {
        Properties props = new Properties();
        props.load(new InputStreamReader(in, StandardCharsets.UTF_8));

        Map<String, String> map = new HashMap<>();
        for (String key : props.stringPropertyNames()) {
            map.put(key, props.getProperty(key));
        }
        return map;
    }

    /**
     * Registers all loaded messages with Adventure GlobalTranslator for translatable components
     */
    private static void registerAdventureTranslations() {

        TranslationStore.StringBased<MessageFormat> registry = TranslationStore.messageFormat(TRANSLATION_KEY);

        for (Map.Entry<String, Map<String, String>> entry : locales.entrySet()) {
            Locale locale = Locale.forLanguageTag(entry.getKey());
            for (Map.Entry<String, String> kv : entry.getValue().entrySet()) {
                registry.register(kv.getKey(), locale, new MessageFormat(kv.getValue()));
            }
        }

        GlobalTranslator.translator().removeSource(registry);
        GlobalTranslator.translator().addSource(registry);
    }

    /**
     * Resolve a key for a given locale with fallback to language and default
     */
    public static String resolve(@NotNull Locale locale, String key) {
        String full = locale.toString();
        String lang = locale.getLanguage();

        String value = getFrom(full, key);
        if (value != null) return value;

        value = getFrom(lang, key);
        if (value != null) return value;

        value = getFrom(ConfigData.get().getDefaultLocale().getLanguage(), key);
        if (value != null) return value;

        if (ConfigData.get().getStrict()) {
            ManaEnchants.getInstance().getLogger().warning("Could not resolve the key: " + key + " for locale: " + locale);
        }
        return key;
    }

    private static @Nullable String getFrom(String localeKey, String key) {
        Map<String, String> map = locales.get(localeKey);
        return map != null ? map.get(key) : null;
    }

    /**
     * Get a Paper Component with optional placeholder replacements
     */
    @Contract("_, _, _, _ -> new")
    public static @NotNull Component get(@NotNull Locale locale, String key, TextColor color, Component... replacements) {
        String raw = resolve(locale, key);
        if (replacements == null || replacements.length == 0) {
            return Component.text(raw, color);
        }

        Pattern pattern = Pattern.compile("\\{(\\d+)}");
        Matcher matcher = pattern.matcher(raw);

        Component result = Component.empty().color(color);
        int lastIndex = 0;

        while (matcher.find()) {
            if (matcher.start() > lastIndex) {
                result = result.append(Component.text(raw.substring(lastIndex, matcher.start())));
            }

            int index = Integer.parseInt(matcher.group(1));
            if (index >= 0 && index < replacements.length) {
                result = result.append(replacements[index]);
            } else {
                result = result.append(Component.text(matcher.group(0)));
            }

            lastIndex = matcher.end();
        }

        if (lastIndex < raw.length()) {
            result = result.append(Component.text(raw.substring(lastIndex)));
        }

        return result;
    }

    /**
     * Reload locales from disk and refresh Adventure translations
     */
    public static void reload() {
        loadAllLocales();
    }
}