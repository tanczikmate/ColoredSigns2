package me.tanczikmate.coloredSigns2;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;
import io.papermc.paper.plugin.lifecycle.event.types.LifecycleEvents;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.block.Block;
import org.bukkit.block.Sign;
import org.bukkit.block.sign.Side;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class ColoredSigns2 extends JavaPlugin {

    private static final Pattern HEX_PATTERN = Pattern.compile("&#([A-Fa-f0-9]{6})");
    public static final String PERMISSION = "coloredsigns2.use";
    public static final String RELOAD_PERMISSION = "coloredsigns2.reload";

    private static ColoredSigns2 instance;
    private ConfigManager configManager;
    private EditsignCommand editsignCommand;

    public static ColoredSigns2 getInstance() {
        return instance;
    }

    public ConfigManager getConfigManager() {
        return configManager;
    }

    @Override
    public void onEnable() {
        instance = this;

        saveDefaultConfig();
        this.configManager = new ConfigManager(this);
        this.editsignCommand = new EditsignCommand(this);

        getServer().getPluginManager().registerEvents(new SignManager(this), this);

        getLifecycleManager().registerEventHandler(LifecycleEvents.COMMANDS, event -> {
            final Commands commands = event.registrar();

            var editsignNode = Commands.literal("editsign")
                    .requires(source -> source.getSender().hasPermission(PERMISSION))
                    .executes(ctx -> {
                        configManager.sendMessage(ctx.getSource().getSender(), "usage");
                        return 1;
                    });

            for (int i = 1; i <= 4; i++) {
                final String lineNumStr = String.valueOf(i);
                final int lineNum = i;

                editsignNode.then(Commands.literal(lineNumStr)
                        .executes(ctx -> runCommand(ctx, lineNumStr, ""))
                        .then(Commands.argument("text", StringArgumentType.greedyString())
                                .suggests((ctx, builder) -> getSignLineSuggestion(ctx, builder, lineNum))
                                .executes(ctx -> runCommand(ctx, lineNumStr, ctx.getArgument("text", String.class)))));
            }

            commands.register(editsignNode.build(), "Edit a sign", List.of("signedit", "se"));

            // Admin command: /coloredsigns reload
            var csNode = Commands.literal("coloredsigns")
                    .requires(source -> source.getSender().hasPermission(RELOAD_PERMISSION))
                    .executes(ctx -> {
                        configManager.sendMessage(ctx.getSource().getSender(), "usage");
                        return 1;
                    })
                    .then(Commands.literal("reload")
                            .executes(ctx -> {
                                configManager.loadConfig();
                                configManager.sendMessage(ctx.getSource().getSender(), "reload-success");
                                return 1;
                            }))
                    .build();

            commands.register(csNode, "ColoredSigns admin commands");
        });

        getLogger().info("ColoredSigns2 plugin loaded successfully!");
    }

    private int runCommand(CommandContext<CommandSourceStack> ctx, String line, String text) {
        editsignCommand.execute(ctx.getSource(), new String[]{line, text});
        return 1;
    }

    private CompletableFuture<Suggestions> getSignLineSuggestion(CommandContext<CommandSourceStack> ctx, SuggestionsBuilder builder, int lineNum) {
        if (!configManager.isEnableTabSuggestions()) {
            return builder.buildFuture();
        }

        if (!(ctx.getSource().getExecutor() instanceof Player player)) {
            return builder.buildFuture();
        }

        int maxDist = (int) Math.ceil(configManager.getMaxDistance());
        Block block = player.getTargetBlockExact(maxDist);
        if (block != null && block.getState() instanceof Sign sign) {
            Side lookingAtSide = sign.getInteractableSideFor(player.getLocation());
            Side side = (lookingAtSide == Side.BACK) ? Side.BACK : Side.FRONT;

            Component lineComponent = sign.getSide(side).line(lineNum - 1);

            String legacyText = LegacyComponentSerializer.legacyAmpersand().serialize(lineComponent);

            if (!legacyText.isEmpty()) {
                builder.suggest(legacyText);
            }
        }

        return builder.buildFuture();
    }

    public static Component formatHexAndLegacy(String text) {
        if (text == null) return Component.empty();

        if (instance != null && !instance.getConfigManager().isAllowMagicCodes()) {
            text = text.replaceAll("(?i)&k", "");
        }

        Matcher matcher = HEX_PATTERN.matcher(text);
        StringBuilder sb = new StringBuilder();

        while (matcher.find()) {
            String hexColor = matcher.group(1);
            StringBuilder replacement = new StringBuilder("&x");
            for (char c : hexColor.toCharArray()) {
                replacement.append("&").append(c);
            }
            matcher.appendReplacement(sb, replacement.toString());
        }
        matcher.appendTail(sb);

        return LegacyComponentSerializer.legacyAmpersand().deserialize(sb.toString());
    }
}
