package fr.skytasul.quests.api.stages;

import org.jetbrains.annotations.NotNull;
import fr.skytasul.quests.api.questers.PlayerQuester;
import fr.skytasul.quests.api.questers.Quester;
import fr.skytasul.quests.api.questers.TopLevelQuester;

public interface StageHandler {

	default void stageStart(@NotNull TopLevelQuester quester, @NotNull StageController stage) {}

	default void stageEnd(@NotNull TopLevelQuester quester, @NotNull StageController stage) {}

	default void stageJoin(@NotNull PlayerQuester p, @NotNull StageController stage) {}

	default void stageLeave(@NotNull PlayerQuester p, @NotNull StageController stage) {}

	default void stageLoad(@NotNull StageController stage) {}

	default void stageUnload(@NotNull StageController stage) {}

	default void stageUpdated(@NotNull Quester quester, @NotNull StageController stage) {}

}