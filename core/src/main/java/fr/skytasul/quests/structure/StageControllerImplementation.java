package fr.skytasul.quests.structure;

import com.google.gson.JsonSyntaxException;
import fr.skytasul.quests.BeautyQuests;
import fr.skytasul.quests.api.QuestsAPI;
import fr.skytasul.quests.api.QuestsPlugin;
import fr.skytasul.quests.api.events.accounts.PlayerAccountJoinEvent;
import fr.skytasul.quests.api.events.accounts.PlayerAccountLeaveEvent;
import fr.skytasul.quests.api.options.description.DescriptionSource;
import fr.skytasul.quests.api.questers.OfflineQuesterException;
import fr.skytasul.quests.api.questers.PlayerQuester;
import fr.skytasul.quests.api.questers.Quester;
import fr.skytasul.quests.api.questers.TopLevelQuester;
import fr.skytasul.quests.api.quests.QuestDatas;
import fr.skytasul.quests.api.stages.*;
import fr.skytasul.quests.api.utils.CustomizedObjectTypeAdapter;
import fr.skytasul.quests.api.utils.messaging.MessageUtils;
import fr.skytasul.quests.utils.QuestUtils;
import fr.skytasul.quests.utils.compatibility.BQBackwardCompat;
import org.bukkit.Bukkit;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Consumer;

public class StageControllerImplementation<T extends AbstractStage> implements StageController, Listener {

	private final @NotNull QuestBranchImplementation branch;
	private final @NotNull StageType<T> type;

	private @Nullable T stage;

	public StageControllerImplementation(@NotNull QuestBranchImplementation branch, @NotNull StageType<T> type) {
		this.branch = Objects.requireNonNull(branch);
		this.type = Objects.requireNonNull(type);
	}

	public void setStage(@NotNull T stage) {
		if (this.stage != null)
			throw new IllegalStateException("Stage was already set");

		type.getStageClass().cast(stage); // to throw ClassCastException if needed

		this.stage = Objects.requireNonNull(stage);
	}

	@Override
	public @NotNull QuestBranchImplementation getBranch() {
		return branch;
	}

	@Override
	public @NotNull AbstractStage getStage() {
		if (stage == null)
			throw new IllegalStateException("Stage has not been loaded yet");
		return stage;
	}

	@Override
	public @NotNull StageType<T> getStageType() {
		if (type == null)
			throw new IllegalStateException("Stage has not been loaded yet");
		return type;
	}

	@Override
	public void finishStage(@NotNull Quester quester) {
		QuestUtils.runSync(() -> branch.finishQuesterStage(quester, this));
	}

	@Override
	public boolean hasStarted(@NotNull Quester quester) {
		return branch.hasStageLaunched(quester, this);
	}

	@Override
	public void updateObjective(@NotNull TopLevelQuester quester, @NotNull String dataKey, @Nullable Object dataValue) {
		OfflineQuesterException.ensureQuesterOnline(quester);

		Map<String, Object> datas = quester.getQuestDatas(branch.getQuest()).getStageDatas(getStorageId());
		if (datas == null) {
			QuestsPlugin.getPlugin().getLoggerExpanded().severe("Account {} did not have data for {}. Creating some.",
					quester.getDebugName(), toString());
			datas = new HashMap<>();
			stage.initPlayerDatas(quester, datas);
		}

		datas.put(dataKey, dataValue);
		quester.getQuestDatas(branch.getQuest()).setStageDatas(getStorageId(), datas);

		propagateStageHandlers(handler -> handler.stageUpdated(quester, this));
		// TODO send update to all players in quester
		branch.getManager().callQuestUpdated(quester);
	}

	@Override
	public <D> @Nullable D getData(@NotNull Quester quester, @NotNull String dataKey, @Nullable Class<D> dataType) {
		QuestDatas playerDatas = quester.getQuestDatas(branch.getQuest());
		Map<String, Object> datas = playerDatas.getStageDatas(getStorageId());

		if (datas == null) {
			if (!hasStarted(quester))
				throw new IllegalStateException("Trying to fetch data of not launched stage");

			TopLevelQuester topLevel = stage.getTopLevelQuester(quester);
			QuestsPlugin.getPlugin().getLoggerExpanded().severe("Account {} did not have data for {}. Creating some.",
					topLevel.getDebugName(), toString());
			datas = new HashMap<>();
			stage.initPlayerDatas(topLevel, datas);
			topLevel.getQuestDatas(branch.getQuest()).setStageDatas(getStorageId(), datas);
		}

		Object data = datas.get(dataKey);
		if (dataType == null) // case when we do not have explicit data type to match for: we can only do direct cast
			return (D) data;

		if (dataType.isInstance(data)) // easy: the data is directly compatible with the expected type
			return dataType.cast(data);

		// hard: the data is not compatible. It may be because the deserialization process previously did
		// not know the exact type to deserialize. Hence we go back to serialized to deserialize again, but
		// this time with the correct type.
		String serialized = CustomizedObjectTypeAdapter.serializeNullable(data);
		try {
			return CustomizedObjectTypeAdapter.deserializeNullable(serialized, dataType);
		} catch (JsonSyntaxException ex) {
			QuestsPlugin.getPlugin().getLoggerExpanded().severe(
					"Cannot convert data " + dataKey + " to " + dataType.toString() + ". Serialized form: " + serialized);
			throw ex;
		}
	}

	@Override
	public @Nullable String getDescriptionLine(@NotNull Quester quester, @NotNull DescriptionSource source) {
		try {
			String description = stage.getCustomText();
			if (description != null) {
				if (description.equals("none"))
					return null;
				description = "§e" + description;
			}

			StageDescriptionPlaceholdersContext context =
					StageDescriptionPlaceholdersContext.of(true, quester, source, null);
			if (description == null)
				description = stage.getDefaultDescription(context);

			return MessageUtils.finalFormat(description, stage.getPlaceholdersRegistry(), context);
		} catch (Exception ex) {
			QuestsPlugin.getPlugin().getLoggerExpanded().severe(
					"An error occurred while getting the description line for {} in {}", ex, quester.getDebugName(),
					toString());
			return "§a" + type.getName();
		}
	}

	private void propagateStageHandlers(@NotNull Consumer<@NotNull StageHandler> consumer) {
		Consumer<StageHandler> newConsumer = handler -> {
			try {
				consumer.accept(handler);
			} catch (Exception ex) {
				QuestsPlugin.getPlugin().getLoggerExpanded().severe("An error occurred while updating stage handler.", ex);
			}
		};
		QuestsAPI.getAPI().getQuestsHandlers().forEach(newConsumer);
		stage.getOptions().forEach(newConsumer);
	}

	public void start(@NotNull TopLevelQuester quester) {
		OfflineQuesterException.ensureQuesterOnline(quester);

		Map<String, Object> datas = new HashMap<>();
		stage.initPlayerDatas(quester, datas);
		quester.getQuestDatas(branch.getQuest()).setStageDatas(getStorageId(), datas);
		propagateStageHandlers(handler -> handler.stageStart(quester, this));
		stage.started(quester);
		for (Player p : quester.getOnlinePlayers())
			stage.sendStartMessage(p);
	}

	public void end(@NotNull TopLevelQuester quester) {
		OfflineQuesterException.ensureQuesterOnline(quester);

		quester.getQuestDatas(branch.getQuest()).setStageDatas(getStorageId(), null);
		propagateStageHandlers(handler -> handler.stageEnd(quester, this));
		stage.ended(quester);
	}

	public void joins(@NotNull PlayerQuester quester) {
		// TODO call when start on online players
		propagateStageHandlers(handler -> handler.stageJoin(quester, this));
		stage.joined(quester);
	}

	public void leaves(@NotNull PlayerQuester quester) {
		propagateStageHandlers(handler -> handler.stageLeave(quester, this));
		stage.left(quester);
	}

	public void load() {
		QuestUtils.autoRegister(stage);
		Bukkit.getPluginManager().registerEvents(this, BeautyQuests.getInstance());
		propagateStageHandlers(handler -> handler.stageLoad(this));
		stage.load();
	}

	public void unload() {
		QuestUtils.autoUnregister(stage);
		HandlerList.unregisterAll(this);
		propagateStageHandlers(handler -> handler.stageUnload(this));
		stage.unload();
	}

	@EventHandler
	public void onJoin(PlayerAccountJoinEvent e) {
		if (e.isFirstJoin())
			return;

		if (hasStarted(e.getPlayerAccount()))
			joins(e.getPlayerAccount());
	}

	@EventHandler
	public void onLeave(PlayerAccountLeaveEvent e) {
		if (hasStarted(e.getPlayerAccount()))
			leaves(e.getPlayerAccount());
	}

	@Override
	public @NotNull String getFlowId() {
		if (branch.isEndingStage(this))
			return "E" + branch.getEndingStageId(this);
		return Integer.toString(branch.getRegularStageId(this));
	}

	public int getStorageId() {
		return branch.isEndingStage(this) ? branch.getEndingStageId(this) : branch.getRegularStageId(this);
	}

	@Override
	public String toString() {
		return "stage " + getFlowId() + " (" + type.getID() + ") of quest " + branch.getQuest().getId() + ", branch "
				+ branch.getId();
	}

	public static @NotNull StageControllerImplementation<?> loadFromConfig(@NotNull QuestBranchImplementation branch,
			@NotNull ConfigurationSection section) {
		String typeID = section.getString("stageType");

		Optional<StageType<?>> stageType = QuestsAPI.getAPI().getStages().getType(typeID);

		if (!stageType.isPresent())
			stageType = BQBackwardCompat.loadStageFromConfig(typeID, section);

		return loadFromConfig(branch, section, stageType.orElseThrow(() -> new IllegalArgumentException("Unknown stage type " + typeID)));
	}

	private static <T extends AbstractStage> @NotNull StageControllerImplementation<T> loadFromConfig(
			@NotNull QuestBranchImplementation branch, @NotNull ConfigurationSection section, StageType<T> type) {
		// we need to separate into two methods to trick the generics

		StageControllerImplementation<T> controller = new StageControllerImplementation<>(branch, type);
		T stage = type.getLoader().supply(section, controller);
		controller.setStage(stage);
		stage.load(section);
		return controller;
	}

}
