package fr.skytasul.quests.npcs.traits;

import org.bukkit.Location;
import org.bukkit.entity.FishHook;
import fr.skytasul.quests.api.npcs.BqInternalNpc;

public class FishingTrait extends NPCTrait {
    private boolean fishing = false;
    private FishHook hook;
    private Location fishingSpot;
    
    public FishingTrait(BqInternalNpc npc) {
        super(npc);
    }
    
    public void startFishing(Location spot) {
        if (fishing) return;
        
        this.fishingSpot = spot;
        this.fishing = true;
        npc.lookAt(spot);
        // Implement fishing animation and hook spawning
    }
    
    public void stopFishing() {
        if (!fishing) return;
        
        this.fishing = false;
        if (hook != null) {
            hook.remove();
            hook = null;
        }
    }
    
    @Override
    public void tick() {
        if (fishing && hook != null) {
            // Implement fishing logic like bobbing and catch attempts
        }
    }
}
