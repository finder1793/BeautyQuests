package fr.skytasul.quests.npcs.traits;

import org.bukkit.Location;
import org.bukkit.entity.Player;
import fr.skytasul.quests.api.npcs.BqInternalNpc;

public class LookCloseTrait extends NPCTrait {
    private double range = 5.0;
    private boolean enabled = true;
    
    public LookCloseTrait(BqInternalNpc npc) {
        super(npc);
    }
    
    @Override
    public void tick() {
        if (!enabled) return;
        
        Location npcLoc = npc.getLocation();
        Player closest = null;
        double closestDist = Double.MAX_VALUE;
        
        for (Player player : npcLoc.getWorld().getPlayers()) {
            double dist = player.getLocation().distanceSquared(npcLoc);
            if (dist < range * range && dist < closestDist) {
                closest = player;
                closestDist = dist;
            }
        }
        
        if (closest != null) {
            Location target = closest.getEyeLocation();
            npc.lookAt(target);
        }
    }
}
