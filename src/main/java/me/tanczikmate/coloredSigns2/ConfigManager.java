package me.tanczikmate.coloredSigns2;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;

public class ConfigManager {

    private final ColoredSigns2 plugin;
    private double maxDistance;
    private boolean preserveVanillaGuiColors;
    private boolean enableTabSuggestions;
    private boolean allowMagicCodes;

    public ConfigManager(ColoredSigns2 plugin) {
        this.plugin = plugin;
        loadConfig();
    }

    public void loadConfig() {
        plugin.reloadConfig();
        FileConfiguration config = plugin.getConfig();

        maxDistance = config.getDouble("settings.max-distance", 4.5);
        preserveVanillaGuiColors = config.getBoolean("settings.preserve-vanilla-gui-colors", true);
        enableTabSuggestions = config.getBoolean("settings.enable-tab-suggestions", true);
        allowMagicCodes = config.getBoolean("formatting.allow-magic-codes", false);
    }

    public void sendMessage(CommandSender sender, String key, TagResolver... tags) {
        FileConfiguration config = plugin.getConfig();
        String prefix = config.getString("messages.prefix", "<gold>[ColoredSigns]</gold> ");
        String rawMessage = config.getString("messages." + key, "");

        if (rawMessage.isEmpty()) {
            return;
        }

        Component component = MiniMessage.miniMessage().deserialize(prefix + rawMessage, tags);
        sender.sendMessage(component);
    }

    public double getMaxDistance() {
        return maxDistance;
    }

    public boolean isPreserveVanillaGuiColors() {
        return preserveVanillaGuiColors;
    }

    public boolean isEnableTabSuggestions() {
        return enableTabSuggestions;
    }

    public boolean isAllowMagicCodes() {
        return allowMagicCodes;
    }
}
