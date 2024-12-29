package fr.skytasul.quests.npcs.navigation;

import org.bukkit.Location;

public class Node implements Comparable<Node> {
    public final Location location;
    public Node parent;
    public double gCost;  // Cost from start to this node
    public double hCost;  // Estimated cost from this node to end
    
    public Node(Location location) {
        this.location = location;
    }
    
    public double getFCost() {
        return gCost + hCost;
    }
    
    @Override
    public int compareTo(Node other) {
        int fCompare = Double.compare(getFCost(), other.getFCost());
        if (fCompare == 0) {
            return Double.compare(hCost, other.hCost);
        }
        return fCompare;
    }
    
    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof Node)) return false;
        Node other = (Node) obj;
        return location.equals(other.location);
    }
    
    @Override
    public int hashCode() {
        return location.hashCode();
    }
}
