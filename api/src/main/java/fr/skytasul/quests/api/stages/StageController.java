package fr.skytasul.quests.api.stages;

import fr.skytasul.quests.api.options.description.DescriptionSource;
import fr.skytasul.quests.api.questers.Quester;
import fr.skytasul.quests.api.questers.TopLevelQuester;
import fr.skytasul.quests.api.quests.branches.QuestBranch;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public interface StageController {

	public @NotNull QuestBranch getBranch();

	public @NotNull AbstractStage getStage();

	public @NotNull StageType<?> getStageType();

	public void finishStage(@NotNull Quester quester);

	public boolean hasStarted(@NotNull Quester quester);

	public void updateObjective(@NotNull TopLevelQuester quester, @NotNull String dataKey, @Nullable Object dataValue);

	public @Nullable String getDescriptionLine(@NotNull Quester quester, @NotNull DescriptionSource source);

	public <T> @Nullable T getData(@NotNull Quester quester, @NotNull String dataKey, @Nullable Class<T> dataType);

	public @NotNull String getFlowId();

}
