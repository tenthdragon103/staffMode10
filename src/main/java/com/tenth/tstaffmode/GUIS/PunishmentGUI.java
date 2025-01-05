package com.tenth.tstaffmode.GUIS;

import com.tenth.tstaffmode.GUIManager;
import fr.minuskube.inv.ClickableItem;
import fr.minuskube.inv.SmartInventory;
import fr.minuskube.inv.content.InventoryContents;
import fr.minuskube.inv.content.InventoryProvider;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.List;
import java.util.Map;

public class PunishmentGUI implements InventoryProvider {
    private final FileConfiguration config;
    private final Player target;
    private final GUIManager guiManager;

    public PunishmentGUI(FileConfiguration config, GUIManager guiManager, Player target) {
        this.config = config;
        this.guiManager = guiManager;
        this.target = target;
    }

    public SmartInventory getInventory(int x, int y, String title) {
        return SmartInventory.builder()
                .id("PunishmentGUI")
                .provider(this)
                .manager(guiManager)
                .size(y, x)
                .title(title)
                .build();
    }

    @Override
    public void init(Player player, InventoryContents inventoryContents) {
        if (config.contains("punishment-menu.buttons")) {
            List<Map<?, ?>> buttons = config.getMapList("punishment-menu.buttons");
            for (Map<?, ?> button : buttons) {
                int posx = (int) button.get("posx");
                int posy = (int) button.get("posy");
                boolean close = (boolean) button.get("closeonclick");
                String materialName = (String) button.get("material");
                String name = (String) button.get("name");
                String command = (String) button.get("command");

                // Create the item
                ItemStack item = new ItemStack(Material.valueOf(materialName.toUpperCase()));
                ItemMeta meta = item.getItemMeta();
                if (meta != null) {
                    meta.displayName(Component.text(name).color(NamedTextColor.GOLD));
                    item.setItemMeta(meta);
                }

                // Add to inventory
                inventoryContents.set(posy, posx, ClickableItem.of(item, e -> {
                    if (close) {
                        player.closeInventory();
                    }
                    Bukkit.dispatchCommand(player, command.replace("{player}", target.getName()));
                }));
            }
        } else {
            player.sendMessage(Component.text("No buttons configured in the staff commands menu.").color(NamedTextColor.RED));
        }
    }

    @Override
    public void update(Player player, InventoryContents inventoryContents) {
        // Optional: Add live updates to the GUI here, if needed
    }
}
