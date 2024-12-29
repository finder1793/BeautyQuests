package fr.skytasul.quests.npcs.motion;

import org.bukkit.Location;
import fr.skytasul.quests.api.npcs.BqInternalNpc;
public class NPCMotion {
    private final BqInternalNpc npc;
    private Location target;
    private double speed = 0.2;
    private boolean paused;
    
    public NPCMotion(BqInternalNpc npc) {
        this.npc = npc;
    }
    
    public void setTarget(Location target) {
        this.target = target;
    }
    
    public void setSpeed(double speed) {
        this.speed = speed;
    }
    
    public boolean hasReachedTarget() {
        return target == null;
    }
    
    public void setPaused(boolean paused) {
        this.paused = paused;
    }
    
    public void tick() {
        if (paused || target == null) return;
        
        Location current = npc.getLocation();
        if (current.distanceSquared(target) <= speed * speed) {
            npc.teleport(target);
            target = null;
            return;
        }
        
        Location next = current.clone();
        next.add(target.clone().subtract(current).toVector().normalize().multiply(speed));
        npc.teleport(next);
    }
}
