package io.github.trystancannon.spawntag.event;

import java.util.UUID;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

/**
 * @author Trystan
 */
public class TagTimerEndEvent extends Event {
    private static final HandlerList handlers = new HandlerList();
    private final UUID taggedPlayerID;
    
    public TagTimerEndEvent(UUID taggedPlayerID) {
        this.taggedPlayerID = taggedPlayerID;
    }
    
    public UUID getTaggedPlayerID() {
        return taggedPlayerID;
    }
    
    @Override
    public HandlerList getHandlers() {
        return handlers;
    }
    
    public static HandlerList getHandlerList() {
        return handlers;
    }
    
}
