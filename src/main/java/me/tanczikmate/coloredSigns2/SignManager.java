package me.tanczikmate.coloredSigns2;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.block.Sign;
import org.bukkit.block.sign.Side;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.SignChangeEvent;

public class SignManager implements Listener {

    @EventHandler
    public void onSignChange(SignChangeEvent event) {
        Player player = event.getPlayer();

        if (!player.hasPermission("coloredsigns2.use")) return;

        if (!(event.getBlock().getState() instanceof Sign oldSign)) return;
        Side side = event.getSide();

        for (int i = 0; i < 4; i++) {
            Component oldComponent = oldSign.getSide(side).line(i);
            Component newComponent = event.line(i);

            if (newComponent == null) continue;

            String oldPureText = net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer.plainText().serialize(oldComponent);
            String newText = LegacyComponentSerializer.legacyAmpersand().serialize(newComponent);

            if (newText.equals(oldPureText)) {
                event.line(i, oldComponent);
            }

            else if (newText.startsWith(oldPureText) && !oldPureText.isEmpty()) {
                String addedText = newText.substring(oldPureText.length());

                Component coloredAddedPart = LegacyComponentSerializer.legacyAmpersand().deserialize(addedText);

                Component finalComponent = oldComponent.append(coloredAddedPart);

                event.line(i, finalComponent);
            }

            else {
                Component coloredNewComponent = LegacyComponentSerializer.legacyAmpersand().deserialize(newText);
                event.line(i, coloredNewComponent);
            }
        }
    }
}