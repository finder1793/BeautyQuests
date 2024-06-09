package fr.skytasul.quests.api.events.progress;

import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;
import fr.skytasul.quests.api.questers.TopLevelQuester;
import fr.skytasul.quests.api.quests.Quest;
import fr.skytasul.quests.api.stages.StageController;

/**
 * Called when a quester finish a stage
 */
public class QuesterSetStageEvent extends QuesterEvent {

	private final @NotNull StageController stage;

	public QuesterSetStageEvent(@NotNull Quest quest, @NotNull TopLevelQuester quester, @NotNull StageController stage) {
		super(quest, quester);
		this.stage = stage;
	}

	public @NotNull StageController getStage() {
		return stage;
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
