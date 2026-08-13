package me.tanczikmate.coloredSigns2;

import io.papermc.paper.command.brigadier.BasicCommand;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.block.Block;
import org.bukkit.block.Sign;
import org.bukkit.block.sign.Side;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jspecify.annotations.NonNull;

public class SigneditCommand implements BasicCommand {

    @Override
    public void execute(CommandSourceStack source, String @NonNull [] args) {
        CommandSender sender = source.getSender();

        if (!(source.getExecutor() instanceof Player player)) {
            sender.sendMessage(Component.text(
                    "Only players can use this command!", NamedTextColor.RED));
            return;
        }

        Block block = player.getTargetBlockExact(4);
        if (block == null || !(block.getState() instanceof Sign sign)) {
            player.sendMessage(Component.text(
                    "You must be looking at a sign to use this command.", NamedTextColor.RED));
            return;
        }

        int signLine = Integer.parseInt(args[0]);
        String fullText = args[1];

        Side lookingAtSide = sign.getInteractableSideFor(player.getLocation());
        Side side = Side.FRONT;
        if (lookingAtSide == Side.BACK) {
            side = Side.BACK;
        }

        Component formattedText = ColoredSigns2.formatHexAndLegacy(fullText);

        sign.getSide(side).line(signLine - 1, formattedText);
        sign.update();

        player.sendMessage(Component.text()
                .append(Component.text("Set line ", NamedTextColor.GREEN))
                .append(Component.text(signLine, NamedTextColor.WHITE))
                .append(Component.text(" to: ", NamedTextColor.GREEN))
                .append(formattedText)
                .build()
        );
    }
}