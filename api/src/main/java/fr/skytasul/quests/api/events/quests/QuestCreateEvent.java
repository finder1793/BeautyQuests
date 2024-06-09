package fr.skytasul.quests.api.events.quests;

import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;
import fr.skytasul.quests.api.quests.Quest;

/**
 * Called when a quest is created
 */
public class QuestCreateEvent extends QuestEvent implements Cancellable {

	private boolean cancel, edit = false;
	private @NotNull Player creator;

	public QuestCreateEvent(@NotNull Quest quest, @NotNull Player creator, boolean edit) {
		super(quest);
		this.creator = creator;
		this.edit = edit;
	}

	@Override
	public boolean isCancelled(){
		return cancel;
	}

	@Override
	public void setCancelled(boolean paramBoolean){
		this.cancel = paramBoolean;
	}

	public @NotNull Player getCreator() {
		return creator;
	}

	public boolean isEdited(){
		return edit;
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
