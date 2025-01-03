package com.tenth.tstaffmode.GUIS;

import fr.minuskube.inv.ClickableItem;
import fr.minuskube.inv.SmartInventory;
import fr.minuskube.inv.content.InventoryContents;
import fr.minuskube.inv.content.InventoryProvider;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.List;
import java.util.Map;

public class StaffUtilitiesGUI implements InventoryProvider {

    private final FileConfiguration config;

    public StaffUtilitiesGUI(FileConfiguration config) {
        this.config = config;
    }

    public SmartInventory getInventory(int x, int y, String title) {
        return SmartInventory.builder()
                .id("StaffUtilitiesGUI")
                .provider(new StaffUtilitiesGUI(config))
                .size(y, x)
                .title(title)
                .build();
    }

    @Override
    public void init(Player player, InventoryContents inventoryContents) {
        if (config.contains("staff-commands-menu.buttons")) {
            List<Map<?, ?>> buttons = config.getMapList("staff-commands-menu.buttons");
            for (Map<?, ?> button : buttons) {
                int slot = (int) button.get("slot");
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
                inventoryContents.set(slot / 9, slot % 9, ClickableItem.of(item, e -> {

                    player.performCommand(command);
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
