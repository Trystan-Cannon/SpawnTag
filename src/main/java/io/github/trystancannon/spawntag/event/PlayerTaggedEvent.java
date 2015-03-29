package io.github.trystancannon.spawntag.event;

import io.github.trystancannon.spawntag.core.SpawnTag;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

/**
 * @author Trystan
 */
public class PlayerTaggedEvent extends Event {
    private static final HandlerList handlers = new HandlerList();
    private final Player taggedPlayer;
    
    private final int timerTaskId;
    private final TaggedPlayerTimer timer;
    
    public PlayerTaggedEvent(SpawnTag plugin, Player taggedPlayer) {
        this.taggedPlayer = taggedPlayer;
        
        this.timer = new TaggedPlayerTimer(taggedPlayer.getUniqueId());
        timerTaskId = Bukkit.getScheduler().scheduleSyncDelayedTask(plugin, timer, 400L);
    }
    
    @Override
    public HandlerList getHandlers() {
        return handlers;
    }
    
    public int getTimerTaskId() {
        return timerTaskId;
    }
    
    public TaggedPlayerTimer getTimer() {
        return timer;
    }
    
    public Player getPlayer() {
        return taggedPlayer;
    }
    
    public static HandlerList getHandlerList() {
        return handlers;
    }
}
