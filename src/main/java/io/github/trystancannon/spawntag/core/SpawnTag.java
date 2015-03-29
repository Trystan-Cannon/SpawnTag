package io.github.trystancannon.spawntag.core;

import io.github.trystancannon.spawntag.event.PlayerTaggedEvent;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Scanner;
import java.util.UUID;
import java.util.logging.Level;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.plugin.java.JavaPlugin;

/**
 * @author Trystan
 */
public class SpawnTag extends JavaPlugin {
    /**
     * List of all of the current spawn tag regions.
     */
    private final HashMap<String, SpawnTagRegion> regions;
    
    /**
     * List of all currently tagged players. An entry is removed when the
     * player's tag timer is up.
     */
    private static final HashMap<UUID, TaggedPlayer> taggedPlayers = new HashMap<>();
    
    public SpawnTag() {
        regions = new HashMap<>();
    }
    
    /**
     * Returns the currently tagged players list.
     * @return Currently tagged players list.
     */
    public static HashMap<UUID, TaggedPlayer> getTaggedPlayers() {
        return taggedPlayers;
    }
    
    /**
     * Tags the player, preventing them from entering a spawn tag region until
     * they have avoided being tagged for 20 seconds.
     * 
     * @param player Player to be tagged.
     */
    public void tagPlayer(Player player) {
        if (player.isOnline()) {
            PlayerTaggedEvent tagEvent = new PlayerTaggedEvent(this, player);
            
            taggedPlayers.put(player.getUniqueId(), new TaggedPlayer(tagEvent));
            Bukkit.getPluginManager().callEvent(tagEvent);

            sendLabeledMessage(player, ChatColor.RED + "You've been tagged! Avoid being tagged for 20 seconds to enter the spawn.");
        }
    }
    
    /**
     * Sends a message to the given receiver with an attached label
     * that states that the message came from the SpawnTag plugin.
     * 
     * @param receiver
     * @param message
     */
    public void sendLabeledMessage(CommandSender receiver, String message) {
        receiver.sendMessage(ChatColor.GOLD + "[SpawnTag] " + ChatColor.RESET + message);
    }
    
    /**
     * Creates the plugin's data folder if it doesn't already exist, then
     * reads the config.txt file for saved spawn tag regions, creating them
     * if there are any.
     */
    @Override
    public void onEnable() {
        if (!getDataFolder().exists()) {
            getDataFolder().mkdirs();
            getLogger().log(Level.INFO, "Created data folder for SpawnTag.");
        }
        
        this.getServer().getLogger().info("Loading spawn tag regions...");
        loadRegionsFromFile();
    }
    
    @Override
    public void onDisable() {
    }
    
    /**
     * Invoked when a command specified in the plugin.yml file for this
     * plugin is executed. Executes whatever code desired for the given
     * command.
     * 
     * @param sender
     * @param command
     * @param label
     * @param args
     * @return Whether or not a command for this plugin was handled.
     */
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (sender instanceof Player) {
            Player playerIssuingCommand = (Player) sender;
            
            String worldName = "";
            if (args.length == 1) {
                worldName = args[0];
            }
            
            if (command.getName().equalsIgnoreCase("stspawncreate")) {
                handleRegionCreateCommand(playerIssuingCommand, worldName);
                return true;
            }
            else if (command.getName().equalsIgnoreCase("stspawndelete")) {
                handleRegionDeleteCommand(playerIssuingCommand, worldName);
                return true;
            }
        }
        
        return false;
    }
    
    /**
     * When a player invokes the region create command in-game, this method
     * does all of the argument checking and execution of their command.
     * 
     * @param playerIssuingCommand Player who is executing the command.
     * @param worldName Unadjusted name of the world around which a region will be created.
     */
    private void handleRegionCreateCommand(Player playerIssuingCommand, String worldName) {
        worldName = adjustWorldName(worldName);
        World world = getServer().getWorld(worldName);
        
        if (regions.get(worldName) == null && world != null) {
            createSpawnTagRegion(world, true);
            playerIssuingCommand.sendMessage(ChatColor.GOLD + "[Spawn Tag] Region around the spawn of " + world.getName() + " created successfully.");
        } else {
            playerIssuingCommand.sendMessage(ChatColor.RED + "[Spawn Tag] Error: Region around that spawn already exists or the world provided is nonexistent.");
        }
    }
    
    /**
     * Handles the argument checking and execution of the region deletion command
     * invoked by a player in-game.
     * 
     * @param playerIssuingCommand Player who is executing the command.
     * @param worldName Unadjusted name of the world whose region is to be deleted.
     */
    private void handleRegionDeleteCommand(Player playerIssuingCommand, String worldName) {
        worldName = adjustWorldName(worldName);
        
        if (regions.get(worldName) != null) {
            deleteSpawnTagRegion(worldName, true);
            playerIssuingCommand.sendMessage(ChatColor.GOLD + "[Spawn Tag] Region around the spawn of " + worldName + " deleted successfully.");
        } else {
            playerIssuingCommand.sendMessage(ChatColor.RED + "[Spawn Tag] Error: Region does not exist.");
        }
    }
    
    /**
     * Transforms the world name into one which is naturally created by the
     * server: world, world_nether, world_the_end. So, one could give this
     * method "Overworld," and it would return "world."
     * 
     * @param worldName
     * @return Adjusted name of the given world.
     */
    private String adjustWorldName(String worldName) {
        worldName = worldName.toLowerCase();
        
        switch (worldName) {
            case "overworld":
                worldName = "world";
                break;
           
            case "nether":
                worldName = "world_nether";
                break;
                
            case "end":
                worldName = "world_the_end";
                break;
        }
        
        return worldName;
    }
    
    /**
     * Creates a spawn tag region around the spawn of the given world.
     * 
     * @param world World in which this region is to be placed around its spawn.
     * @param updateConfig Whether or not to update the config file with this
     * new region.
     */
    private void createSpawnTagRegion(World world, boolean updateConfig) {
        SpawnTagRegion region = new SpawnTagRegion(world.getName(), world.getSpawnLocation(), getServer().getSpawnRadius(), this);
        getServer().getPluginManager().registerEvents(region, this);
        
        regions.put(world.getName(), region);
        if (updateConfig && !updateConfigFile()) {
            getLogger().info("[Spawn Tag] Failed to save region to file.");
        }
    }
    
    /**
     * Deletes the region with the given name. Updates the config file if
     * specified.
     * 
     * @param regionName Name of the region to delete.
     * @param updateConfig Whether or not to update the config file after deletion.
     */
    private void deleteSpawnTagRegion(String regionName, boolean updateConfig) {
        EntityDamageByEntityEvent.getHandlerList().unregister(regions.get(regionName));
        PlayerMoveEvent.getHandlerList().unregister(regions.get(regionName));
        
        regions.remove(regionName);
        if (updateConfig && !updateConfigFile()) {
            getLogger().info("[Spawn Tag] Failed to delete a region from the config file.");
        }
    }
    
    /**
     * Rewrites the config file with the current UUIDs of all regions in the
     * <code>regions</code> has table.
     */
    private boolean updateConfigFile() {
        File spawnTagConfigFile = new File(getDataFolder(), "config.txt");
        
        if (!spawnTagConfigFile.exists()) {
            try {
                spawnTagConfigFile.createNewFile();
            } catch (IOException failure) {
                getLogger().log(Level.INFO, "Couldn't create the file at {0}", spawnTagConfigFile.getAbsolutePath());
                return false;
            }
        }
        
        try (PrintWriter writer = new PrintWriter(getDataFolder() + "/config.txt", "UTF-8")) {
            for (SpawnTagRegion region : regions.values()) {
                writer.println(region.getLocation().getWorld().getUID());
            }
            
            writer.flush();
            writer.close();
        } catch (Exception failure) {
            getLogger().info("Couldn't write to the config file.");
            return false;
        }
        
        return true;
    }
    
    /**
     * Reads the contents of the config file and creates all of the regions
     * specified within it.
     * 
     * @return Load success.
     */
    private boolean loadRegionsFromFile() {
        File configFile = new File(getDataFolder(), "config.txt");
        Path configPath = Paths.get(getDataFolder().getAbsolutePath(), "config.txt");
        
        // TODO: Reading needs fixing.
        if (configFile.exists()) {
            try (Scanner fileScanner = new Scanner(configPath, StandardCharsets.UTF_8.name())) {
                while (fileScanner.hasNextLine()) {
                    createSpawnTagRegion(Bukkit.getWorld(UUID.fromString(fileScanner.nextLine())), false);
                }
            } catch (IOException failure) {
                return false;
            }
            
            return true;
        }
        
        return false;
    }
}
