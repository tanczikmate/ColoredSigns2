package me.tanczikmate.coloredSigns2;

import io.papermc.paper.command.brigadier.BasicCommand;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import org.bukkit.Bukkit;
import org.bukkit.block.Block;
import org.bukkit.block.Sign;
import org.bukkit.block.sign.Side;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.block.SignChangeEvent;
import org.jspecify.annotations.NonNull;

import java.util.ArrayList;
import java.util.List;

public class EditsignCommand implements BasicCommand {

    public static final String BYPASS_WAXED_PERMISSION = "coloredsigns2.bypass.waxed";
    public static final ThreadLocal<Boolean> EDITING_INTERNAL = ThreadLocal.withInitial(() -> false);

    private final ColoredSigns2 plugin;

    public EditsignCommand(ColoredSigns2 plugin) {
        this.plugin = plugin;
    }

    @Override
    public void execute(CommandSourceStack source, String @NonNull [] args) {
        CommandSender sender = source.getSender();
        ConfigManager config = plugin.getConfigManager();

        if (!(source.getExecutor() instanceof Player player)) {
            config.sendMessage(sender, "only-players");
            return;
        }

        int maxDist = (int) Math.ceil(config.getMaxDistance());
        Block block = player.getTargetBlockExact(maxDist);
        if (block == null || !(block.getState() instanceof Sign sign)) {
            config.sendMessage(player, "must-look-at-sign");
            return;
        }

        if (args.length == 0) {
            config.sendMessage(player, "usage");
            return;
        }

        int signLine;
        try {
            signLine = Integer.parseInt(args[0]);
        } catch (NumberFormatException e) {
            config.sendMessage(player, "invalid-line");
            return;
        }

        if (signLine < 1 || signLine > 4) {
            config.sendMessage(player, "invalid-line");
            return;
        }

        if (sign.isWaxed() && !player.hasPermission(BYPASS_WAXED_PERMISSION)) {
            config.sendMessage(player, "waxed-sign");
            return;
        }

        String fullText = args.length > 1 ? args[1] : "";

        Side lookingAtSide = sign.getInteractableSideFor(player.getLocation());
        Side side = (lookingAtSide == Side.BACK) ? Side.BACK : Side.FRONT;

        Component formattedText = fullText.isEmpty()
                ? Component.empty()
                : ColoredSigns2.formatHexAndLegacy(fullText);

        // Prepare 4 lines representing the proposed sign content for SignChangeEvent.
        // This allows protection plugins (WorldGuard, GriefPrevention, Towny, etc.) to validate permissions
        // and logging plugins (CoreProtect) to record the change.
        List<Component> newLines = new ArrayList<>(4);
        for (int i = 0; i < 4; i++) {
            newLines.add(i == signLine - 1 ? formattedText : sign.getSide(side).line(i));
        }

        SignChangeEvent event = new SignChangeEvent(sign.getBlock(), player, newLines, side);

        EDITING_INTERNAL.set(true);
        try {
            Bukkit.getPluginManager().callEvent(event);
        } finally {
            EDITING_INTERNAL.remove();
        }

        if (event.isCancelled()) {
            config.sendMessage(player, "no-permission");
            return;
        }

        Component finalLine = event.line(signLine - 1);
        sign.getSide(side).line(signLine - 1, finalLine);
        sign.update();

        if (fullText.isEmpty()) {
            config.sendMessage(player, "line-cleared", Placeholder.parsed("line", String.valueOf(signLine)));
        } else {
            config.sendMessage(player, "line-set",
                    Placeholder.parsed("line", String.valueOf(signLine)),
                    Placeholder.component("text", finalLine));
        }
    }
}
