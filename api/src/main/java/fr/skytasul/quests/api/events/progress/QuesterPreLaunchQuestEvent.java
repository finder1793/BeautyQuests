package fr.skytasul.quests.api.events.progress;

import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;
import fr.skytasul.quests.api.questers.TopLevelQuester;
import fr.skytasul.quests.api.quests.Quest;

/**
 * Called before a player starts a quest
 */
public class QuesterPreLaunchQuestEvent extends QuesterEvent implements Cancellable {

	private boolean cancel = false;
	private final @NotNull Player player;

	public QuesterPreLaunchQuestEvent(@NotNull Quest quest, @NotNull TopLevelQuester quester, @NotNull Player player) {
		super(quest, quester);
		this.player = player;
	}

	public @NotNull Player getPlayer() {
		return player;
	}

	@Override
	public boolean isCancelled(){
		return cancel;
	}

	@Override
	public void setCancelled(boolean paramBoolean){
		this.cancel = paramBoolean;
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
