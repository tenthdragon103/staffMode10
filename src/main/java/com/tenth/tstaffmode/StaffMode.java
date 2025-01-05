package com.tenth.tstaffmode;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import net.kyori.adventure.text.event.HoverEvent;
import net.md_5.bungee.api.ChatColor;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.codehaus.plexus.util.FileUtils;

import java.io.File;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import java.util.*;
import java.util.List;

public class StaffMode extends JavaPlugin implements CommandExecutor {
    private Map<UUID, ItemStack[]> savedInventories;
    private GUIManager guiManager;
    private File inventoryFile;
    private File GUIFile;
    private FileConfiguration inventoryConfig;
    private FileConfiguration GUIFileConfig;
    private String newPluginVersion;
    private String newPluginMinecraftVersion;
    private static final String GITHUB_API_URL = "https://api.github.com/repos/tenthdragon103/staffMode10/releases/latest";
    private static final String CURRENT_PLUGIN_JAR = "plugins/TStaffMode-1.2.jar";

    @Override
    public void onEnable() {
        //this method is ran when the plugin is enabled on the server. ie startup.
        savedInventories = new HashMap<>(); //init inv map

        this.getCommand("tstaffmode").setExecutor(this); //may need try/catch or a nonnull requirement. this is the base command.

        setupInventoryFile(); //setup inventories.yml file
        setupGUIFile(); //setup GUIConfig.yml file

        loadInventories(); //load previously saved inventories

        getServer().getPluginManager().registerEvents(new StaffModeListener(this), this);

        guiManager = new GUIManager(GUIFileConfig, this);
        guiManager.init();
        getLogger().info("TStaffMode enabled");
        searchForUpdate();
    }

    @Override
    public void onDisable() {
        saveInventories(); //save all inventories on plugin disable
        getLogger().info("Staff Inventories Saved.");
    }

    private void setupInventoryFile() {
        inventoryFile = new File(getDataFolder(), "inventories.yml");
        if (!inventoryFile.exists()) {
            inventoryFile.getParentFile().mkdirs();
            saveResource("inventories.yml", false);
        }
        inventoryConfig = YamlConfiguration.loadConfiguration(inventoryFile); //load the config file for inventories
    }

    private void setupGUIFile() {
        GUIFile = new File(getDataFolder(), "GUIConfig.yml");
        if (!GUIFile.exists()) {
            GUIFile.getParentFile().mkdirs();
            saveResource("GUIConfig.yml", false);
        }
        GUIFileConfig = YamlConfiguration.loadConfiguration(GUIFile); //load the config file for inventories
    }

    //loads all inventories from the yml to memory
    private void loadInventories() {
        ConfigurationSection section = inventoryConfig.getConfigurationSection("inventories");
        if (section != null) {
            for (String key : section.getKeys(false)) {
                UUID uuid;
                try {
                    uuid = UUID.fromString(key);
                } catch (IllegalArgumentException e) {
                    getLogger().warning("Invalid UUID in inventories.yml: " + key);
                    continue;
                }

                //List<ItemStack> items = section.getList(key, new ArrayList<>());
                //funny way to work around the error generated by the above line. ln 71-80 just checks everything to make sure its all the right type.
                List<?> rawList = section.getList(key, new ArrayList<>());
                List<ItemStack> items = new ArrayList<>();

                if (rawList != null) {
                    for (Object obj : rawList) {
                        if (obj instanceof ItemStack) {
                            items.add((ItemStack) obj);
                        }
                    }
                }

                ItemStack[] inventory = items.toArray(new ItemStack[0]);
                savedInventories.put(uuid, inventory);
            }
        }
    }

    //saves all inventories to the yml
    private void saveInventories() {
        ConfigurationSection section = inventoryConfig.createSection("inventories");

        for (Map.Entry<UUID, ItemStack[]> entry : savedInventories.entrySet()) {
            List<ItemStack> items = Arrays.asList(entry.getValue());
            section.set(entry.getKey().toString(), items);
        }

        try {
            inventoryConfig.save(inventoryFile);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void saveInventory(UUID uuid, ItemStack[] inventory) {
        savedInventories.put(uuid, inventory.clone());

        //save to yml
        ConfigurationSection section = inventoryConfig.getConfigurationSection("inventories");
        if (section == null) {
            section = inventoryConfig.createSection("inventories");
        }
        section.set(uuid.toString(), Arrays.asList(inventory));

        try {
            inventoryConfig.save(inventoryFile);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    //remove an individual player's saved inv
    private void removeSavedInventory(UUID uuid) {
        savedInventories.remove(uuid);

        ConfigurationSection section = inventoryConfig.getConfigurationSection("inventories");
        if (section != null) {
            section.set(uuid.toString(), null);
            try {
                inventoryConfig.save(inventoryFile);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public void notifyAdminUpdate(Player player) {
        Component message = Component.text("An update is available for StaffMode, allow automatic download and replacement from GitHub? ")
                .color(NamedTextColor.YELLOW)
                        .hoverEvent(HoverEvent.showText(Component.text("Plugin version " + newPluginVersion + " for Minecraft version " + newPluginMinecraftVersion)))
                .append(Component.text("[Allow]")
                        .color(NamedTextColor.GREEN)
                        .clickEvent(ClickEvent.runCommand("/tstaffmode updateAllow")));
        player.sendMessage(message);
    }

    public String getNewPluginVersion() {
        return  newPluginVersion;
    }

    //handles /staffmode commmand
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("Only players can use this command.");
            return true;
        }

        Player player = (Player) sender;

        //check perms
        if (!player.hasPermission("tstaffmode.use")) {
            player.sendMessage(Component.text("You dont have permission to use this command.").color(NamedTextColor.RED));
            return true;
        }

        if (args.length == 1 && args[0].equalsIgnoreCase("on")) {
            enableStaffMode(player);
        } else if (args.length == 1 && args[0].equalsIgnoreCase("off")) {
            disableStaffMode(player);
        } else if (args.length == 1 && args[0].equalsIgnoreCase("help")) {
            aboutHelpMessage(player);
        } else if (args.length == 1 && args[0].equals("updateAllow")) {
            if (!player.hasPermission("tstaffmode.updater")) {
                player.sendMessage(Component.text("You do not have permission to run this command. This incident will be reported.").color(NamedTextColor.RED));
                getLogger().warning("Non admin attempted to run restricted command! (" + player.getName() + ")");
            } else {
                if (updatePlugin()) {
                    player.sendMessage(Component.text("Update succeeded! Server restart is needed to complete the update. Restart now? ").color(NamedTextColor.GREEN)
                            .append(Component.text("[Restart]").color(NamedTextColor.RED).clickEvent(ClickEvent.runCommand("/restart"))));
                } else {
                    player.sendMessage(Component.text("Update failed! See console for further information.").color(NamedTextColor.RED));
                }
            }
        } else if (args.length == 1 && args[0].equalsIgnoreCase("updateSearch")) {
            searchForUpdate();
            player.sendMessage(Component.text("GitHub updates parsed. See console for info.").color(NamedTextColor.GREEN));
        } else if (args.length == 2 && args[0].equalsIgnoreCase("punish")) {
            if (!player.hasPermission("tstaffmode.punish")) {
                player.sendMessage(Component.text("You do not have permission to run this command.").color(NamedTextColor.RED));
            } else {
                Player target = Bukkit.getServer().getPlayer(args[1]);
                if (target != null) {
                    guiManager.openPunishmentMenu(player, target);
                } else {
                    player.sendMessage(Component.text("Target not found.").color(NamedTextColor.RED));
                }
            }
        } else if (args.length == 1 && args[0].equalsIgnoreCase("staffcmds")) {
            if (!player.hasPermission("tstaffmode.staffcmds")) {
                player.sendMessage(Component.text("You do not have permission to run this command.").color(NamedTextColor.RED));
            } else {
                guiManager.openStaffCommandsMenu(player);
            }
        } else {
            player.sendMessage(Component.text("Usage: /tstaffmode <on|off>, /tstaffmode punish <playerName>, /tstaffmode staffcmds").color(NamedTextColor.RED));
        }
        return true;
    }

    public void searchForUpdate() {
        try {
            // Open a connection to GitHub API
            // Use URI to parse and validate the URL
            URI apiUri = new URI(GITHUB_API_URL);

            // Convert URI to URL for the HttpURLConnection
            URL apiUrl = apiUri.toURL();

            // Open a connection to GitHub API
            HttpURLConnection connection = (HttpURLConnection) apiUrl.openConnection();
            connection.setRequestMethod("GET");
            connection.setRequestProperty("Accept", "application/json");

            // Read the response
            ObjectMapper objectMapper = new ObjectMapper();
            JsonNode rootNode = objectMapper.readTree(connection.getInputStream());

            // Extract relevant fields from the JSON response
            newPluginVersion = rootNode.get("tag_name").asText(); // e.g., "v1.2.0"
            String downloadUrl = rootNode.get("assets").get(0).get("browser_download_url").asText(); // First asset's download URL
            newPluginMinecraftVersion = rootNode.get("body").asText(); // Assuming Minecraft version is in the release notes

            String currentVersion = getDescription().getVersion();
            if (currentVersion.equals(newPluginVersion)) {
                getLogger().info("TStaffMode is up to date.");
            } else {
                getLogger().info("Found update: Version " + newPluginVersion + " for Minecraft " + newPluginMinecraftVersion);
                getLogger().info("Download URL: " + downloadUrl);
            }

        } catch (Exception e) {
            getLogger().warning("Failed to check for updates: " + e.getMessage());
        }

    }

    public boolean updatePlugin() {
        try {
            // Define the URI for the download URL
            URI downloadUri = new URI("https://github.com/tenthdragon103/staffMode10/releases/download/" + newPluginVersion + "/TStaffmode-" + newPluginVersion + ".jar");

            // Convert URI to URL
            URL downloadUrl = downloadUri.toURL();

            File newJar = new File("plugins/TStaffMode-" + newPluginVersion + ".jar");
            File oldJar = new File(CURRENT_PLUGIN_JAR);

            // Download the new JAR file
            FileUtils.copyURLToFile(downloadUrl, newJar);

            // Replace the old JAR
            if (oldJar.delete()) {
                getLogger().info("Plugin updated to version " + newPluginVersion + "! Restart the server to apply the update.");
                return true;
            } else {
                getLogger().warning("Failed to delete old plugin JAR.");
                return false;
            }
        } catch (Exception e) {
            getLogger().warning("Failed to update plugin: " + e.getMessage());
            return false;
        }
    }

    //enable staffmode for a player
    private void enableStaffMode(Player player) {
        UUID uuid = player.getUniqueId();

        //check if player is already in staffmode
        if (savedInventories.containsKey(uuid)) {
            player.sendMessage(Component.text("You are already in staff mode.").color(NamedTextColor.RED));
            return;
        }

        ItemStack[] currentInventory = player.getInventory().getContents();
        saveInventory(uuid, currentInventory);

        player.getInventory().clear();

        player.addPotionEffect(new PotionEffect(PotionEffectType.NIGHT_VISION, Integer.MAX_VALUE, 1, false, false));

        player.playSound(player.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 100, 1);
        player.sendMessage(Component.text("Staff mode enabled.").color(NamedTextColor.GREEN));
        getLogger().info(player.getName() + "has enabled staff mode.");
    }

    //disable staffmode for a player
    private void disableStaffMode(Player player) {
        UUID uuid = player.getUniqueId();

        //check if player is in staff mode
        if (!savedInventories.containsKey(uuid)) {
            player.sendMessage(Component.text("You are not in staff mode.").color(NamedTextColor.RED));
            return;
        }

        ItemStack[] savedInventory = savedInventories.get(uuid);
        player.getInventory().setContents(savedInventory);

        //remove saved inv from conf
        removeSavedInventory(uuid);

        player.removePotionEffect(PotionEffectType.NIGHT_VISION);

        player.playSound(player.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 100, 1);
        player.sendMessage(Component.text("Staff mode disabled.").color(NamedTextColor.GREEN));
        getLogger().info(player.getName() + "has disabled staff mode.");
    }

    //forcefully remove a player from staff mode. boolean to inform the player of removal or not.
    public void forceDisableStaffMode(Player player, boolean sendMessage) {
        UUID uuid = player.getUniqueId();

        if (!savedInventories.containsKey(uuid)) {
            if (sendMessage) {
                player.sendMessage(Component.text("Player is not in staff mode.").color(NamedTextColor.RED));
            }
            return;
        }

        // Restore the player's inventory
        ItemStack[] savedInventory = savedInventories.get(uuid);
        player.getInventory().setContents(savedInventory);

        // Remove from memory and config
        removeSavedInventory(uuid);

        if (sendMessage) {
            player.sendMessage(Component.text("Staff mode disabled for player " + player.getName() + ".").color(NamedTextColor.GREEN));
        }
    }

    public Map<UUID, ItemStack[]> getSavedInventories() {
        return savedInventories;
    }

    private void aboutHelpMessage(Player player) {
        player.sendMessage(Component.text("Plugin version is ").color(NamedTextColor.GREEN)
                .append(Component.text("1.2").color(NamedTextColor.YELLOW))
                .append(Component.text(" built for Minecraft version ").color(NamedTextColor.GREEN))
                .append(Component.text("1.21.3").color(NamedTextColor.YELLOW)));
    }
}
