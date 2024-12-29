package fr.skytasul.quests.npcs.traits;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import fr.skytasul.quests.api.npcs.BqInternalNpc;
import java.util.ArrayList;
import java.util.List;

public class CommandTrait extends NPCTrait {
    private List<String> commands = new ArrayList<>();
    
    public CommandTrait(BqInternalNpc npc) {
        super(npc);
    }
    
    public void addCommand(String command) {
        commands.add(command);
    }
    
    public void executeCommands(Player player) {
        for (String cmd : commands) {
            String parsed = cmd.replace("%player%", player.getName());
            Bukkit.dispatchCommand(Bukkit.getConsoleSender(), parsed);
        }
    }
}
