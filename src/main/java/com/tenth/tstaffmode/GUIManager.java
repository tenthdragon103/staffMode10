package com.tenth.tstaffmode;

import com.tenth.tstaffmode.GUIS.PunishmentGUI;
import com.tenth.tstaffmode.GUIS.StaffUtilitiesGUI;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import fr.minuskube.inv.InventoryManager;

public class GUIManager extends InventoryManager {

    private final FileConfiguration config;
    private final JavaPlugin plugin;

    public GUIManager(FileConfiguration config, JavaPlugin plugin) {
        super(plugin); // Initialize InventoryManager with the plugin
        this.plugin = plugin;
        this.config = config;
    }

    public void openPunishmentMenu(Player staff, Player target) {
        if (!config.contains("punishment-menu")) {
            staff.sendMessage(Component.text("Punishment menu not configured or improperly named.")
                    .color(NamedTextColor.RED));
            return;
        }

        // Create and open the PunishmentGUI
        PunishmentGUI gui = new PunishmentGUI(config, this, target);
        gui.getInventory(
                config.getInt("punishment-menu.sizex"),
                config.getInt("punishment-menu.sizey"),
                config.getString("punishment-menu.title")
        ).open(staff);
    }

    public void openStaffCommandsMenu(Player staff) {
        if (!config.contains("staff-commands-menu")) {
            staff.sendMessage(Component.text("Staff commands menu not configured or improperly named.")
                    .color(NamedTextColor.RED));
            return;
        }

        // Create and open the StaffUtilitiesGUI
        StaffUtilitiesGUI gui = new StaffUtilitiesGUI(config, this);
        gui.getInventory(
                config.getInt("staff-commands-menu.sizex"),
                config.getInt("staff-commands-menu.sizey"),
                config.getString("staff-commands-menu.title")
        ).open(staff);
    }
}
