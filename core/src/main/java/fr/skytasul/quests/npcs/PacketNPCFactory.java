package fr.skytasul.quests.npcs;

import fr.skytasul.quests.api.npcs.BqInternalNpcFactory;
import fr.skytasul.quests.api.npcs.BqInternalNpc;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import java.util.Collection;
import java.util.UUID;

public class PacketNPCFactory implements BqInternalNpcFactory {

    @Override
    public BqInternalNpc create(String id, Location location) {
        return new PacketNPC(id, location);
    }

    @Override 
    public boolean isValidId(String id) {
        try {
            Integer.parseInt(id);
            return true;
        } catch (NumberFormatException ex) {
            return false;
        }
    }

    @Override
    public String generateId() {
        return String.valueOf(System.currentTimeMillis());
    }

}
