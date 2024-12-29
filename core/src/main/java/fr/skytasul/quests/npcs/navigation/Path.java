package fr.skytasul.quests.npcs.navigation;

import org.bukkit.Location;
import org.bukkit.util.Vector;
import java.util.List;

public class Path {
    private final List<Location> points;
    private int currentPoint = 0;
    
    public Path(List<Location> points) {
        this.points = points;
    }
    
    public Vector getNextMovement(Location current) {
        if (currentPoint >= points.size()) {
            return null;
        }
        
        Location target = points.get(currentPoint);
        if (current.distanceSquared(target) < 0.1) {
            currentPoint++;
            return getNextMovement(current);
        }
        
        return target.toVector().subtract(current.toVector()).normalize();
    }
    
    public boolean isFinished() {
        return currentPoint >= points.size();
    }
    
    public void reset() {
        currentPoint = 0;
    }
    
    public List<Location> getPoints() {
        return points;
    }
}
