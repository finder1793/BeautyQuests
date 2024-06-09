package fr.skytasul.quests.api.events.quests;

import org.bukkit.event.Event;
import org.jetbrains.annotations.NotNull;
import fr.skytasul.quests.api.quests.Quest;

public abstract class QuestEvent extends Event {

	protected final @NotNull Quest quest;
	
	protected QuestEvent(@NotNull Quest quest) {
		this.quest = quest;
	}

	public @NotNull Quest getQuest() {
		return quest;
	}

}
