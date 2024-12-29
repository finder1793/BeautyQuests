package fr.skytasul.quests.npcs.traits;

import org.bukkit.Location;
import fr.skytasul.quests.api.npcs.BqInternalNpc;

public abstract class NPCTrait {
    protected final BqInternalNpc npc;
    
    public NPCTrait(BqInternalNpc npc) {
        this.npc = npc;
    }
    
    public abstract void onSpawn();
    public abstract void onDespawn();
    public abstract void onMove(Location from, Location to);
    public abstract void tick();
}
