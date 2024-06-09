package fr.skytasul.quests.api.quests;

import java.io.File;
import java.util.List;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.UnmodifiableView;
import fr.skytasul.quests.api.questers.Quester;

public interface QuestsManager {

	@NotNull
	File getSaveFolder();

	@NotNull
	@UnmodifiableView
	List<@NotNull Quest> getQuests();

	@Nullable
	Quest getQuest(int id);

	void addQuest(@NotNull Quest quest);

	@NotNull
	List<@NotNull Quest> getQuestsStarted(@NotNull Quester quester);

	@NotNull
	List<@NotNull Quest> getQuestsStarted(@NotNull Quester quester, boolean hide,
			boolean withoutScoreboard);

	@NotNull
	List<@NotNull Quest> getQuestsFinished(@NotNull Quester quester, boolean hide);

	@NotNull
	List<@NotNull Quest> getQuestsNotStarted(@NotNull Quester quester, boolean hide,
			boolean clickableAndRedoable);

	void updateQuestsStarted(@NotNull Quester acc, boolean withoutScoreboard, @NotNull List<@NotNull Quest> list);

	int getStartedSize(@NotNull Quester quester);

}
