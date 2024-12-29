package fr.skytasul.quests.npcs.navigation;

import org.bukkit.Location;
import java.util.*;

public class PathFinder {
    private static final int MAX_ITERATIONS = 1000;
    
    public Path findPath(Location start, Location end) {
        PriorityQueue<Node> openSet = new PriorityQueue<>();
        Set<Node> closedSet = new HashSet<>();
        
        Node startNode = new Node(start);
        startNode.gCost = 0;
        startNode.hCost = getDistance(start, end);
        
        openSet.add(startNode);
        
        int iterations = 0;
        while (!openSet.isEmpty() && iterations++ < MAX_ITERATIONS) {
            Node current = openSet.poll();
            
            if (current.location.distanceSquared(end) < 0.5) {
                return reconstructPath(current);
            }
            
            closedSet.add(current);
            
            for (Node neighbor : getNeighbors(current)) {
                if (closedSet.contains(neighbor)) continue;
                
                double tentativeGCost = current.gCost + getDistance(current.location, neighbor.location);
                
                if (!openSet.contains(neighbor) || tentativeGCost < neighbor.gCost) {
                    neighbor.parent = current;
                    neighbor.gCost = tentativeGCost;
                    neighbor.hCost = getDistance(neighbor.location, end);
                    
                    if (!openSet.contains(neighbor)) {
                        openSet.add(neighbor);
                    }
                }
            }
        }
        
        return null;
    }
    
    private Path reconstructPath(Node endNode) {
        List<Location> path = new ArrayList<>();
        Node current = endNode;
        
        while (current != null) {
            path.add(0, current.location);
            current = current.parent;
        }
        
        return new Path(path);
    }
    
    private double getDistance(Location a, Location b) {
        return a.distance(b);
    }
    
    private List<Node> getNeighbors(Node node) {
        List<Node> neighbors = new ArrayList<>();
        Location loc = node.location;
        
        // Add adjacent positions
        for (double x = -1; x <= 1; x++) {
            for (double z = -1; z <= 1; z++) {
                if (x == 0 && z == 0) continue;
                
                Location newLoc = loc.clone().add(x, 0, z);
                if (isWalkable(newLoc)) {
                    neighbors.add(new Node(newLoc));
                }
            }
        }
        
        return neighbors;
    }
    
    private boolean isWalkable(Location location) {
        return !location.getBlock().getType().isSolid() && 
               !location.clone().add(0, 1, 0).getBlock().getType().isSolid() &&
               location.clone().add(0, -1, 0).getBlock().getType().isSolid();
    }
}
