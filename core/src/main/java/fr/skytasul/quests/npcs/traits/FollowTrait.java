package fr.skytasul.quests.npcs.traits;

import org.bukkit.entity.Player;
import fr.skytasul.quests.api.npcs.BqInternalNpc;
import fr.skytasul.quests.npcs.motion.NPCMotion;

public class FollowTrait extends NPCTrait {
    private Player target;
    private double followDistance = 2.0;
    private NPCMotion motion;
    
    public FollowTrait(BqInternalNpc npc) {
        super(npc);
        this.motion = new NPCMotion(npc);
    }
    
    public void setTarget(Player player) {
        this.target = player;
    }
    
    @Override
    public void tick() {
        if (target == null || !target.isOnline()) return;
        motion.setTarget(target.getLocation().add(target.getLocation().getDirection().multiply(-followDistance)));
    }
}

