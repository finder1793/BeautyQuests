package fr.skytasul.quests.questers;

import fr.skytasul.quests.api.questers.QuesterProvider;
import fr.skytasul.quests.api.quests.QuestStartResult;
import fr.skytasul.quests.structure.QuestImplementation;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public interface QuesterStrategy extends QuesterProvider {

	QuestStartResult testStart(@NotNull Player p, @NotNull QuestImplementation quest);

}
