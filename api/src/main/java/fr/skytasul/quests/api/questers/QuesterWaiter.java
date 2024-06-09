package fr.skytasul.quests.api.questers;

import org.jetbrains.annotations.NotNull;

public interface QuesterWaiter {

	// TODO figure out why was this for lmao

	@NotNull
	TopLevelQuester getAttachedQuester();

	void completed(@NotNull PlayerQuester quester);

	boolean isCompleted();

	void runAtEnd(@NotNull Runnable run);

}
