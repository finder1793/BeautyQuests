import fr.skytasul.quests.api.npcs.BqInternalNpc;
import fr.skytasul.quests.api.npcs.BqInternalNpcFactory;
import fr.skytasul.quests.api.npcs.BqInternalNpcFactoryCreatable;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import java.util.Collection;
import java.util.UUID;

public class PacketNPCFactory implements BqInternalNpcFactory, BqInternalNpcFactoryCreatable {
    
    @Override
    public String getName() {
        return "Packet NPCs";
    }

    @Override
    public Collection<String> getIDs() {
        return PacketNPCManager.getInstance().getNPCIds();
    }

    @Override
    public BqInternalNpc fetchNPC(String id) {
        return PacketNPCManager.getInstance().getNPC(id);
    }

    @Override
    public boolean isNPC(Entity entity) {
        return PacketNPCManager.getInstance().isNPC(entity);
    }

    @Override
    public BqInternalNpc create(Location location, EntityType type, String name, String skin) {
        String id = UUID.randomUUID().toString();
        PacketNPC npc = new PacketNPC(id, location, type, name);
        PacketNPCManager.getInstance().registerNPC(npc);
        return npc;
    }

    @Override
    public int getTimeToWaitForNPCs() {
        return 0;
    }
}
