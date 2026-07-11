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

import java.util.concurrent.CompletableFuture;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class ColoredSigns2 extends JavaPlugin {

    private static final Pattern HEX_PATTERN = Pattern.compile("&#([A-Fa-f0-9]{6})");

    @Override
    public void onEnable() {
        getServer().getPluginManager().registerEvents(new SignManager(), this);

        getLifecycleManager().registerEventHandler(LifecycleEvents.COMMANDS, event -> {
            final Commands commands = event.registrar();

            var commandNode = Commands.literal("tabla")
                    .then(Commands.literal("1").then(Commands.argument("szöveg", StringArgumentType.greedyString())
                            .suggests((ctx, builder) -> getSignLineSuggestion(ctx, builder, 1))
                            .executes(ctx -> runCommand(ctx, "1"))))

                    .then(Commands.literal("2").then(Commands.argument("szöveg", StringArgumentType.greedyString())
                            .suggests((ctx, builder) -> getSignLineSuggestion(ctx, builder, 2))
                            .executes(ctx -> runCommand(ctx, "2"))))

                    .then(Commands.literal("3").then(Commands.argument("szöveg", StringArgumentType.greedyString())
                            .suggests((ctx, builder) -> getSignLineSuggestion(ctx, builder, 3))
                            .executes(ctx -> runCommand(ctx, "3"))))

                    .then(Commands.literal("4").then(Commands.argument("szöveg", StringArgumentType.greedyString())
                            .suggests((ctx, builder) -> getSignLineSuggestion(ctx, builder, 4))
                            .executes(ctx -> runCommand(ctx, "4"))))
                    .build();

            commands.register(commandNode, "Tábla szerkesztése");

            commands.register(
                    Commands.literal("tábla")
                            .redirect(commandNode)
                            .build(),
                    "Tábla szerkesztése"
            );
        });

        getLogger().info("A ColoredSigns2 plugin sikeresen betöltődött!");
    }

    private int runCommand(CommandContext<CommandSourceStack> ctx, String sor) {
        String szoveg = ctx.getArgument("szöveg", String.class);
        new TablaCommand().execute(ctx.getSource(), new String[]{sor, szoveg});
        return 1;
    }

    private CompletableFuture<Suggestions> getSignLineSuggestion(CommandContext<CommandSourceStack> ctx, SuggestionsBuilder builder, int lineNum) {
        if (!(ctx.getSource().getExecutor() instanceof Player player)) {
            return builder.buildFuture();
        }

        Block block = player.getTargetBlockExact(4);
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