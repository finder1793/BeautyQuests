package fr.skytasul.quests.api.events.progress;

import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;
import fr.skytasul.quests.api.questers.TopLevelQuester;
import fr.skytasul.quests.api.quests.Quest;

/**
 * Called when the stage of a player is cancelled
 */
public class QuesterResetQuestEvent extends QuesterEvent {

	public QuesterResetQuestEvent(@NotNull Quest quest, @NotNull TopLevelQuester quester) {
		super(quest, quester);
	}

	@Override
	public HandlerList getHandlers() {
		return handlers;
	}

	public static HandlerList getHandlerList() {
		return handlers;
	}

	private static final HandlerList handlers = new HandlerList();

}
