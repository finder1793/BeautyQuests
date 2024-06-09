package fr.skytasul.quests.api.questers;

import fr.skytasul.quests.api.quests.Quest;
import fr.skytasul.quests.api.quests.QuestDatas;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.UnmodifiableView;
import java.util.Collection;
import java.util.concurrent.CompletableFuture;

// TODO add placeholders
public interface Quester {

	public boolean isOnline();

	public @NotNull String getDebugName();

	public boolean hasQuestDatas(@NotNull Quest quest);

	public @Nullable QuestDatas getQuestDatasIfPresent(@NotNull Quest quest);

	public @NotNull QuestDatas getQuestDatas(@NotNull Quest quest); // should be read-only

	public @NotNull CompletableFuture<QuestDatas> removeQuestDatas(@NotNull Quest quest);

	public @NotNull CompletableFuture<QuestDatas> removeQuestDatas(int id);

	public @UnmodifiableView @NotNull Collection<QuestDatas> getQuestsDatas();

}
