package io.github.trystancannon.spawntag.core;

import io.github.trystancannon.spawntag.event.PlayerTaggedEvent;

import org.bukkit.entity.Player;

/**
 * Container for a player who has been tagged. A <code>TaggedPlayer</code>
 * object holds the tagged player's <code>Player</code> object as well
 * as the original <code>PlayerTaggedEvent</code> which fired when the
 * player was tagged.
 * 
 * @author Trystan
 */
public class TaggedPlayer {
    private final PlayerTaggedEvent origin;
    private final Player me;
    
    public TaggedPlayer(PlayerTaggedEvent origin) {
        this.origin = origin;
        this.me = origin.getPlayer();
    }
    
    /**
     * @return The player who is tagged.
     */
    public Player getPlayer() {
        return me;
    }
    
    /**
     * @return The original <code>PlayerTaggedEvent</code> which fired
     * when this player was tagged.
     */
    public PlayerTaggedEvent getOriginEvent() {
        return origin;
    }
}
