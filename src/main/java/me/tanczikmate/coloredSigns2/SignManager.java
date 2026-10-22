package me.tanczikmate.coloredSigns2;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.bukkit.block.Sign;
import org.bukkit.block.sign.Side;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.SignChangeEvent;

import static me.tanczikmate.coloredSigns2.ColoredSigns2.PERMISSION;

public class SignManager implements Listener {

    private final ColoredSigns2 plugin;

    public SignManager(ColoredSigns2 plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onSignChange(SignChangeEvent event) {
        // Skip internal events dispatched by EditsignCommand
        if (EditsignCommand.EDITING_INTERNAL.get()) {
            return;
        }

        Player player = event.getPlayer();

        if (!player.hasPermission(PERMISSION)) return;

        if (!(event.getBlock().getState() instanceof Sign oldSign)) return;
        Side side = event.getSide();

        boolean preserveColors = plugin.getConfigManager().isPreserveVanillaGuiColors();

        for (int i = 0; i < 4; i++) {
            Component oldComponent = oldSign.getSide(side).line(i);
            Component newComponent = event.line(i);

            if (newComponent == null) continue;

            String oldPureText = PlainTextComponentSerializer.plainText().serialize(oldComponent);
            String newText = LegacyComponentSerializer.legacyAmpersand().serialize(newComponent);

            if (preserveColors && newText.equals(oldPureText)) {
                event.line(i, oldComponent);
            }
            else if (preserveColors && newText.startsWith(oldPureText) && !oldPureText.isEmpty()) {
                String addedText = newText.substring(oldPureText.length());

                Component coloredAddedPart = ColoredSigns2.formatHexAndLegacy(addedText);

                Component finalComponent = oldComponent.append(coloredAddedPart);

                event.line(i, finalComponent);
            }
            else {
                Component coloredNewComponent = ColoredSigns2.formatHexAndLegacy(newText);
                event.line(i, coloredNewComponent);
            }
        }
    }
}
