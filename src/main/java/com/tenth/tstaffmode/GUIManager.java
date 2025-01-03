package com.tenth.tstaffmode;

import com.tenth.tstaffmode.GUIS.PunishmentGUI;
import com.tenth.tstaffmode.GUIS.StaffUtilitiesGUI;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;

public class GUIManager {
    private final FileConfiguration config;

    public GUIManager(FileConfiguration config) {
        this.config = config;
    }

    public void openPunishmentMenu(Player staff, Player target) {
        if (config.contains("punishment-menu")) {
            staff.sendMessage(Component.text("Menu not configured or improperly named.").color(NamedTextColor.RED));
        } else {
            PunishmentGUI gui = new PunishmentGUI(config, target);
            gui.getInventory(config.getInt("staff-commands-menu.sizex"),
                            config.getInt("staff-commands-menu.sizey"),
                            config.getString("staff-commands-menu.title"))
                    .open(staff);
        }
    }

    public void openStaffCommandsMenu(Player staff) {
        if (!config.contains("staff-commands-menu")) {
            staff.sendMessage(Component.text("Menu not configured or improperly named.").color(NamedTextColor.RED));
        } else {
            StaffUtilitiesGUI gui = new StaffUtilitiesGUI(config);
            gui.getInventory(config.getInt("staff-commands-menu.sizex"),
                            config.getInt("staff-commands-menu.sizey"),
                            config.getString("staff-commands-menu.title"))
                    .open(staff);
        }
    }
}
