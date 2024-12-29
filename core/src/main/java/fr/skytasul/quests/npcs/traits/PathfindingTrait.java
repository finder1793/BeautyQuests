package fr.skytasul.quests.npcs.traits;

import org.bukkit.Location;
import fr.skytasul.quests.api.npcs.BqInternalNpc;
import fr.skytasul.quests.npcs.motion.NPCMotion;

public class PathfindingTrait extends NPCTrait {
    private Location[] waypoints;
    private int currentPoint = 0;
    private NPCMotion motion;
    
    public PathfindingTrait(BqInternalNpc npc) {
        super(npc);
        this.motion = new NPCMotion(npc);
    }
    
    @Override
    public void tick() {
        if (waypoints == null || waypoints.length == 0) return;
        
        if (motion.hasReachedTarget()) {
            currentPoint = (currentPoint + 1) % waypoints.length;
            motion.setTarget(waypoints[currentPoint]);
        }
    }
}


