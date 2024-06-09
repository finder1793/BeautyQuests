package fr.skytasul.quests.api.events.progress;

import org.jetbrains.annotations.NotNull;
import fr.skytasul.quests.api.events.quests.QuestEvent;
import fr.skytasul.quests.api.questers.TopLevelQuester;
import fr.skytasul.quests.api.quests.Quest;

public abstract class QuesterEvent extends QuestEvent {

	private @NotNull TopLevelQuester quester;

	protected QuesterEvent(@NotNull Quest quest, @NotNull TopLevelQuester quester) {
		super(quest);
		this.quester = quester;
	}

	public @NotNull TopLevelQuester getQuester() {
		return quester;
	}

}
