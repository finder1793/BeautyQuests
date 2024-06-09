package fr.skytasul.quests.api.events.quests;

import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;
import fr.skytasul.quests.api.quests.Quest;

/**
 * Called when a quest is removed<br>
 * <b>May be called in a quest editing</b>
 */
public class QuestRemoveEvent extends QuestEvent{

	public QuestRemoveEvent(@NotNull Quest quest) {
		super(quest);
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
