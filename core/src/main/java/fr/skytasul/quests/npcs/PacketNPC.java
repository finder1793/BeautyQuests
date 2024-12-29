package fr.skytasul.quests.npcs;


import java.util.HashMap;
import java.util.Map;
import org.bukkit.Location;
import fr.skytasul.quests.api.npcs.BqInternalNpc;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import com.github.retrooper.packetevents.PacketEvents;
import com.github.retrooper.packetevents.protocol.entity.data.EntityData;
import com.github.retrooper.packetevents.protocol.entity.data.EntityDataTypes;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerEntityAnimation;
import java.util.ArrayList;
import java.util.List;
import com.github.retrooper.packetevents.event.PacketListenerAbstract;
import com.github.retrooper.packetevents.event.PacketReceiveEvent;
import com.github.retrooper.packetevents.protocol.packettype.PacketType;
import com.github.retrooper.packetevents.wrapper.play.client.WrapperPlayClientInteractEntity;
import fr.skytasul.quests.api.npcs.NpcClickType;
import fr.skytasul.quests.npcs.navigation.NPCNavigator;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerEntityMetadata;
import com.github.retrooper.packetevents.protocol.player.GameMode;
import com.github.retrooper.packetevents.protocol.player.InteractAction;
import com.mojang.authlib.GameProfile;
import com.mojang.authlib.properties.Property;
import org.bukkit.inventory.ItemStack;
import fr.skytasul.quests.npcs.traits.NPCTrait;
import fr.skytasul.quests.npcs.motion.NPCMotion;

public class PacketNPC implements BqInternalNpc {
    private final String id;
    private Location location;
    private final Map<Class<? extends NPCTrait>, NPCTrait> traits = new HashMap<>();
    private final int entityId;
    private final UUID uuid;
    private GameProfile gameProfile;
    private Property[] skinProperties;
    private final Set<NPCClickHandler> clickHandlers = new HashSet<>();
    private final NPCNavigator navigator;
    
    public PacketNPC(String id, Location location) {
        this.id = id;
        this.location = location;
        this.entityId = PacketEvents.getAPI().getPlayerManager().getNextEntityId();
        this.uuid = UUID.randomUUID();
        this.navigator = new NPCNavigator(this);
        
        // Add default traits
        addTrait(new LookCloseTrait(this));
        addTrait(new CommandTrait(this));
    }
    
    public <T extends NPCTrait> T getTrait(Class<T> traitClass) {
        return traitClass.cast(traits.get(traitClass));
    }
    
    public void addTrait(NPCTrait trait) {
        traits.put(trait.getClass(), trait);
    }
    
    public void removeTrait(Class<? extends NPCTrait> traitClass) {
        NPCTrait trait = traits.remove(traitClass);
        if (trait != null) {
            trait.onDespawn();
        }
    }
    
    @Override
    public void tick() {
        navigator.tick();
        traits.values().forEach(NPCTrait::tick);
    }
    
    @Override
    public boolean setNavigationPaused(boolean paused) {
        navigator.setPaused(paused);
        return true;
    }

    @Override
    public String getInternalId() {
        return id;
    }

    @Override 
    public Location getLocation() {
        return location;
    }

    @Override
    public boolean isSpawned() {
        return true;
    }

    @Override
    public String getName() {
        return null;
    }

    public void spawn(Player player) {
        // Spawn entity packet
        WrapperPlayServerSpawnEntity spawnPacket = new WrapperPlayServerSpawnEntity(
            entityId,
            uuid,
            EntityTypes.PLAYER,
            location.getX(),
            location.getY(),
            location.getZ(),
            location.getYaw(),
            location.getPitch(),
            0
        );
        
        // Metadata packet for player properties
        WrapperPlayServerEntityMetadata metadataPacket = createMetadataPacket();
        
        // Send packets
        PacketEvents.getAPI().getPlayerManager().sendPacket(player, spawnPacket);
        PacketEvents.getAPI().getPlayerManager().sendPacket(player, metadataPacket);
    }
    
    public void updateLocation(Location newLoc) {
        if (!newLoc.equals(this.location)) {
            this.location = newLoc;
            
            WrapperPlayServerEntityTeleport teleportPacket = new WrapperPlayServerEntityTeleport(
                entityId,
                location.getX(),
                location.getY(),
                location.getZ(),
                location.getYaw(),
                location.getPitch(),
                true
            );
            
            broadcastPacket(teleportPacket);
        }
    }

    private WrapperPlayServerEntityMetadata createMetadataPacket() {
        List<EntityData> metadata = new ArrayList<>();
        
        // Add player metadata
        metadata.add(new EntityData(0, EntityDataTypes.BYTE, (byte) 0x00));
        metadata.add(new EntityData(16, EntityDataTypes.BYTE, (byte) 0x7F)); // Skin parts all visible
        
        return new WrapperPlayServerEntityMetadata(entityId, metadata);
    }
    
    public void playAnimation(Animation animation) {
        WrapperPlayServerEntityAnimation packet = new WrapperPlayServerEntityAnimation(
            entityId,
            animation.getId()
        );
        
        broadcastPacket(packet);
    }
    
    public enum Animation {
        SWING_MAIN_ARM(0),
        TAKE_DAMAGE(1),
        LEAVE_BED(2),
        SWING_OFFHAND(3),
        CRITICAL_EFFECT(4),
        MAGIC_CRITICAL_EFFECT(5);
        
        private final int id;
        
        Animation(int id) {
            this.id = id;
        }
        
        public int getId() {
            return id;
        }
    }

    public void setSkin(String texture, String signature) {
        this.skinProperties = new Property[]{
            new Property("textures", texture, signature)
        };
        updatePlayerInfo();
    }
    
    private void updatePlayerInfo() {
        PlayerInfo playerInfo = new PlayerInfo(
            uuid,
            gameProfile.getName(),
            skinProperties,
            GameMode.SURVIVAL,
            0
        );
        
        WrapperPlayServerPlayerInfo packet = new WrapperPlayServerPlayerInfo(
            PlayerInfoAction.ADD_PLAYER,
            Collections.singletonList(playerInfo)
        );
        
        broadcastPacket(packet);
    }
    
    public void setEquipment(EquipmentSlot slot, ItemStack item) {
        Pair<EnumItemSlot, ItemStack> equipment = new Pair<>(
            EnumItemSlot.valueOf(slot.name()),
            ItemStackSerializer.serializeItemStack(item)
        );
        
        WrapperPlayServerEntityEquipment packet = new WrapperPlayServerEntityEquipment(
            entityId,
            Collections.singletonList(equipment)
        );
        
        broadcastPacket(packet);
    }
    
    private void broadcastPacket(Object packet) {
        location.getWorld().getPlayers().stream()
            .filter(p -> p.getLocation().distanceSquared(location) < 64 * 64)
            .forEach(p -> PacketEvents.getAPI().getPlayerManager().sendPacket(p, packet));
    }
    
    public enum EquipmentSlot {
        MAINHAND,
        OFFHAND,
        BOOTS,
        LEGGINGS,
        CHESTPLATE,
        HELMET
    }

    public void registerClickHandler(NPCClickHandler handler) {
        clickHandlers.add(handler);
    }
    
    private class InteractionListener extends PacketListenerAbstract {
        @Override
        public void onPacketReceive(PacketReceiveEvent event) {
            if (event.getPacketType() == PacketType.Play.Client.INTERACT_ENTITY) {
                WrapperPlayClientInteractEntity packet = new WrapperPlayClientInteractEntity(event);
                if (packet.getEntityId() == entityId) {
                    Player player = (Player) event.getPlayer();
                    NpcClickType clickType = convertClickType(packet.getAction());
                    
                    clickHandlers.forEach(handler -> 
                        handler.onClick(player, clickType));
                }
            }
        }
    }
    
    private NpcClickType convertClickType(InteractionType action) {
        switch (action) {
            case ATTACK: return NpcClickType.LEFT;
            case INTERACT: return NpcClickType.RIGHT;
            case INTERACT_AT: return NpcClickType.RIGHT;
            default: return NpcClickType.RIGHT;
        }
    }
    
    @FunctionalInterface
    public interface NPCClickHandler {
        void onClick(Player player, NpcClickType clickType);
    }

    public void navigateTo(Location target) {
        navigator.navigateTo(target);
    }
    
    public NPCNavigator getNavigator() {
        return navigator;
    }
}