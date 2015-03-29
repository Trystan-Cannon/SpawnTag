package io.github.trystancannon.spawntag.core;

import io.github.trystancannon.spawntag.event.TagTimerEndEvent;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.player.PlayerMoveEvent;

/**
 * @author Trystan
 */
public class SpawnTagRegion implements Listener {
    /** Name of the region. If this is a spawn, it will be the name of the world. */
    private final String name;
    
    /** Center location in the world of the square region. */
    private final Location location;
    
    /** Radius of the square region in blocks. */
    private final int radius;
    
    /** SpawnTag plugin from which each region is instantiated via in-game commands. */
    private final SpawnTag plugin;
    
    /**
     * Creates a new spawn tag region which is a square with the given radius,
     * spanning from the bottom for the world to its top.
     * 
     * @param name Name of the region.
     * @param location Center of the region's square.
     * @param radius Radius (side length / 2) of the region.
     * @param plugin
     */
    public SpawnTagRegion(String name, Location location, int radius, SpawnTag plugin) {
        this.name = name;
        this.location = location;
        this.radius = radius;
        this.plugin = plugin;
    }
    
    /**
     * Prevents players who are tagged from carrying any kind of movement within
     * the region. Also, any attempt to enter the region will be prevented until
     * the tag timer has finished.
     * 
     * @param playerMove Movement event triggered by a player.
     */
    @EventHandler
    public void onPlayerMove(PlayerMoveEvent playerMove) {
        Player player = playerMove.getPlayer();
        Location playerLocation = playerMove.getTo();
        
        if (SpawnTag.getTaggedPlayers().get(player.getUniqueId()) != null && isLocationWithinRegion(playerLocation)) {
            playerMove.setCancelled(true);
            player.sendMessage(ChatColor.RED + "You're still tagged!");
        }
    }
    
    /**
     * CHecks if the attack was a tag, then tagging the player and preventing
     * them from entering the region while they are still tagged.
     * 
     * @param attack 
     */
    @EventHandler
    public void onAttack(EntityDamageByEntityEvent attack) {
        if (attack.getDamager() instanceof Player && attack.getEntity() instanceof Player) {
            Player attackedPlayer = (Player) attack.getEntity();
            TaggedPlayer taggedPlayer = SpawnTag.getTaggedPlayers().get(attackedPlayer.getUniqueId());
            
            // If the player is already tagged, cancel their current timer
            // task before creating a new one via tagPlayer().
            if (taggedPlayer != null) {
                Bukkit.getScheduler().cancelTask(taggedPlayer.getOriginEvent().getTimerTaskId());
            }
            
            plugin.tagPlayer((Player) attack.getEntity());
        }
    }
    
    /**
     * Deletes the <code>TaggedPlayer</code> object corresponding to the tagged
     * player whose timer just ended.
     * 
     * @param tagTimerEnd 
     */
    @EventHandler
    public void onTagTimerEnd(TagTimerEndEvent tagTimerEnd) {
        SpawnTag.getTaggedPlayers().remove(tagTimerEnd.getTaggedPlayerID());
    }
    
    /**
     * @param location
     * @return Whether or not <code>location</code> is within this spawn tag region.
     */
    public boolean isLocationWithinRegion(Location location) {
        // Make sure, among other things, that the location is within the same world
        // as this region.
        return location.getWorld() == this.location.getWorld() &&
               location.getBlockX() >= this.location.getBlockX() - radius &&
               location.getBlockX() <= this.location.getBlockX() + radius &&
               location.getBlockZ() >= this.location.getBlockZ() - radius &&
               location.getBlockZ() <= this.location.getBlockZ() + radius;
    }
    
    /**
     * @return The location of this region's center.
     */
    public Location getLocation() {
        return location;
    }
}
