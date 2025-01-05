package com.tenth.tstaffmode.GUIS;

import com.tenth.tstaffmode.GUIManager;
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
    private final GUIManager guiManager;

    public StaffUtilitiesGUI(FileConfiguration config, GUIManager guiManager) {
        this.config = config;
        this.guiManager = guiManager;
    }

    public SmartInventory getInventory(int x, int y, String title) {
        return SmartInventory.builder()
                .id("StaffUtilitiesGUI")
                .provider(this)
                .manager(guiManager)
                .size(y, x)
                .title(title)
                .build();
    }

    @Override
    public void init(Player player, InventoryContents inventoryContents) {
        if (config.contains("staff-commands-menu.buttons")) {
            List<Map<?, ?>> buttons = config.getMapList("staff-commands-menu.buttons");
            for (Map<?, ?> button : buttons) {
                int posx = (int) button.get("posx");
                int posy = (int) button.get("posy");
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
                inventoryContents.set(posx, posy, ClickableItem.of(item, e -> {
                    e.setCancelled(true);
                    player.closeInventory();
                    player.performCommand(command);
                }));
            }

        } else {
            player.sendMessage(Component.text("No buttons configured in the staff commands menu.").color(NamedTextColor.RED));
        }
        inventoryContents.set(7, 1, ClickableItem.of(new ItemStack(Material.APPLE), e -> {
            e.setCancelled(true);
            player.closeInventory();
            player.sendMessage("button pressed");
        }));
    }

    @Override
    public void update(Player player, InventoryContents inventoryContents) {
        // Optional: Add live updates to the GUI here, if needed
    }
}
