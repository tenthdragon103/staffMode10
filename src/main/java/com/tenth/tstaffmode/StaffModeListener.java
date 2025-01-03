package com.tenth.tstaffmode;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.UUID;

public class StaffModeListener implements Listener {

    private final StaffMode plugin;

    public StaffModeListener(StaffMode plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        UUID uuid = player.getUniqueId();

        if (plugin.getSavedInventories().containsKey(uuid)) {
            //do something if the player is in staffmode and leaves the game
        }
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        UUID uuid = player.getUniqueId();

        if (player.hasPermission("staffmode.updater") && plugin.getDescription().getVersion().equals(plugin.getNewPluginVersion())) {
            plugin.notifyAdminUpdate(player);
        }

        if (plugin.getSavedInventories().containsKey(uuid)) {
            player.sendMessage(Component.text("Staff mode is still enabled.").color(NamedTextColor.GREEN));
            player.addPotionEffect(new PotionEffect(PotionEffectType.NIGHT_VISION, Integer.MAX_VALUE, 1, false, false));
        }
    }

    @EventHandler
    public void onPlayerDeath(PlayerDeathEvent event) {
        Player player = event.getEntity();
        UUID uuid = player.getUniqueId();

        player.addPotionEffect(new PotionEffect(PotionEffectType.NIGHT_VISION, Integer.MAX_VALUE, 1, false, false));

        if (plugin.getSavedInventories().containsKey(uuid)) {
            // Prevent inventory drop by clearing drops
            event.getDrops().clear();
            // Optionally, send a message or handle accordingly
            // TODO: 9/20/2024 make this actually work, the stupid death chest plugin just swipes all the players items and keeps them
            player.sendMessage(Component.text("You died in staff mode. Your inventory has been kept. **death chest plugin intervenes with this, so probably didnt work**").color(NamedTextColor.RED));
        }
    }
}
