package fr.skytasul.quests.npcs.navigation;

import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.util.Vector;
import fr.skytasul.quests.npcs.PacketNPC;

public class NPCNavigator {
    private final PacketNPC npc;
    private final PathFinder pathFinder;
    private Path currentPath;
    private double speed = 0.2;
    
    public NPCNavigator(PacketNPC npc) {
        this.npc = npc;
        this.pathFinder = new PathFinder();
    }
    
    public void tick() {
        if (currentPath == null) return;
        
        Location current = npc.getLocation();
        Vector movement = currentPath.getNextMovement(current);
        
        if (movement == null) {
            currentPath = null;
            return;
        }
        
        movement.multiply(speed);
        if (!handleCollision(current, movement)) {
            npc.teleport(current.add(movement));
        }
    }
    
    private boolean handleCollision(Location location, Vector movement) {
        Block block = location.add(movement).getBlock();
        if (block.getType().isSolid()) {
            if (movement.getY() <= 0) {
                movement.setY(0.5);
                return false;
            }
            return true;
        }
        return false;
    }
    
    public void navigateTo(Location target) {
        currentPath = pathFinder.findPath(npc.getLocation(), target);
    }
}
