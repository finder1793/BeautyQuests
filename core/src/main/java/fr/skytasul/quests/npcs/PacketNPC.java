import com.github.retrooper.packetevents.PacketEvents;
import fr.skytasul.quests.api.npcs.BqInternalNpc;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;


public class PacketNPC implements BqInternalNpc {
    private final int entityId;
    private final String internalId;
    private Location location;
    private String name;
    private EntityType type;
    private final Set<UUID> visibleTo = new HashSet<>();
    private boolean spawned = true;

    public PacketNPC(String internalId, Location location, EntityType type, String name) {
        this.entityId = Entity.nextEntityId();
        this.internalId = internalId;
        this.location = location;
        this.type = type;
        this.name = name;
    }

    @Override
    public String getInternalId() {
        return internalId;
    }

    @Override 
    public Location getLocation() {
        return location;
    }

    @Override
    public boolean isSpawned() {
        return spawned;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public boolean setNavigationPaused(boolean paused) {
        return true;
    }

    public void showTo(Player player) {
        if (visibleTo.add(player.getUniqueId())) {
            PacketEvents.getAPI().getPlayerManager().sendPacket(player, createSpawnPacket());
            PacketEvents.getAPI().getPlayerManager().sendPacket(player, createMetadataPacket());
        }
    }

    public void hideFrom(Player player) {
        if (visibleTo.remove(player.getUniqueId())) {
            PacketEvents.getAPI().getPlayerManager().sendPacket(player, createDespawnPacket());
        }
    }
}
