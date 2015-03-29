package io.github.trystancannon.spawntag.event;

import java.util.UUID;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;

/**
 * @author Trystan
 */
public final class TaggedPlayerTimer implements Runnable {
    private final UUID taggedPlayerID;
    private int scheduledId;
    
    public TaggedPlayerTimer(UUID playerID) {
        this.taggedPlayerID = playerID;
    }
    
    public void setScheduledId(int scheduledId) {
        this.scheduledId = scheduledId;
    }
    
    public int getScheduledId() {
        return scheduledId;
    }
    
    @Override
    public void run() {
        Bukkit.getPlayer(taggedPlayerID).sendMessage(ChatColor.GREEN + "You're no longer tagged! You may enter the spawn.");
        Bukkit.getPluginManager().callEvent(new TagTimerEndEvent(taggedPlayerID));
    }
}