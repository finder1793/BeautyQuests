package fr.skytasul.quests.stages;

import fr.skytasul.quests.BeautyQuests;
import fr.skytasul.quests.api.QuestsPlugin;
import fr.skytasul.quests.api.editors.TextEditor;
import fr.skytasul.quests.api.editors.parsers.DurationParser.MinecraftTimeUnit;
import fr.skytasul.quests.api.gui.ItemUtils;
import fr.skytasul.quests.api.localization.Lang;
import fr.skytasul.quests.api.options.QuestOption;
import fr.skytasul.quests.api.questers.PlayerQuester;
import fr.skytasul.quests.api.questers.Quester;
import fr.skytasul.quests.api.questers.TopLevelQuester;
import fr.skytasul.quests.api.requirements.RequirementList;
import fr.skytasul.quests.api.stages.AbstractStage;
import fr.skytasul.quests.api.stages.StageController;
import fr.skytasul.quests.api.stages.StageDescriptionPlaceholdersContext;
import fr.skytasul.quests.api.stages.creation.StageCreation;
import fr.skytasul.quests.api.stages.creation.StageCreationContext;
import fr.skytasul.quests.api.stages.creation.StageGuiLine;
import fr.skytasul.quests.api.utils.MinecraftVersion;
import fr.skytasul.quests.api.utils.Utils;
import fr.skytasul.quests.api.utils.XMaterial;
import fr.skytasul.quests.api.utils.messaging.PlaceholderRegistry;
import fr.skytasul.quests.api.utils.messaging.PlaceholdersContext.QuesterPlaceholdersContext;
import fr.skytasul.quests.api.utils.progress.HasProgress;
import fr.skytasul.quests.api.utils.progress.ProgressPlaceholders;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitTask;
import org.jetbrains.annotations.NotNull;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class StagePlayTime extends AbstractStage implements HasProgress {

	private final long playTicks;
	private final TimeMode timeMode;

	private Map<TopLevelQuester, BukkitTask> tasks = new HashMap<>();

	public StagePlayTime(StageController controller, long ticks, TimeMode timeMode) {
		super(controller);
		this.playTicks = ticks;
		this.timeMode = timeMode;
	}

	public long getTicksToPlay() {
		return playTicks;
	}

	@Override
	public @NotNull String getDefaultDescription(@NotNull StageDescriptionPlaceholdersContext context) {
		return Lang.SCOREBOARD_PLAY_TIME.toString();
	}

	@Override
	protected void createdPlaceholdersRegistry(@NotNull PlaceholderRegistry placeholders) {
		super.createdPlaceholdersRegistry(placeholders);
		placeholders.registerIndexedContextual("time_remaining_human", QuesterPlaceholdersContext.class,
				context -> Utils.millisToHumanString(getQuesterAmount(context.getQuester())));
		ProgressPlaceholders.registerProgress(placeholders, "time", this);
	}

	private long getRemaining(TopLevelQuester quester) {
		switch (timeMode) {
			case ONLINE:
				long remaining = getData(quester, "remainingTime", Long.class);
				long lastJoin = getData(quester, "lastJoin", Long.class);
				long playedTicks = (System.currentTimeMillis() - lastJoin) / 50;
				return remaining - playedTicks;
			case OFFLINE:
				World world = Bukkit.getWorld(getData(quester, "worldUuid", UUID.class));
				if (world == null) {
					QuestsPlugin.getPlugin().getLoggerExpanded()
							.warning("Cannot get remaining time of " + quester.getDebugName()
									+ " for " + controller + " because the world has changed.",
									quester.hashCode() + hashCode() + "time", 15);
					return -1;
				}

				long startTime = getData(quester, "worldStartTime", Long.class);
				long elapsedTicks = world.getGameTime() - startTime;
				return playTicks - elapsedTicks;
			case REALTIME:
				startTime = getData(quester, "startTime", Long.class);
				elapsedTicks = (System.currentTimeMillis() - startTime) / 50;
				return playTicks - elapsedTicks;
		}
		throw new UnsupportedOperationException();
	}

	private void launchTask(TopLevelQuester quester, long remaining) {
		if (tasks.containsKey(quester))
			return;
		tasks.put(quester,
				Bukkit.getScheduler().runTaskLater(BeautyQuests.getInstance(), () -> getController().finishStage(quester),
				remaining < 0 ? 0 : remaining));
	}

	@Override
	public long getQuesterAmount(@NotNull Quester quester) {
		return getRemaining(getTopLevelQuester(quester)) * 50L;
	}

	@Override
	public long getTotalAmount() {
		return playTicks * 50L;
	}

	@Override
	public void joined(PlayerQuester quester) {
		super.joined(quester);
		TopLevelQuester topLevel = getTopLevelQuester(quester);
		if (timeMode == TimeMode.ONLINE) {
			if (!tasks.containsKey(topLevel))
				getController().updateObjective(topLevel, "lastJoin", System.currentTimeMillis());
			// if the quester is already in the tasks map, it means it has several players in it
			// and someone already joined before => no need to update lastJoin
		}
		launchTask(topLevel, getRemaining(topLevel));
	}

	@Override
	public void left(PlayerQuester quester) {
		super.left(quester);

		TopLevelQuester topLevel = getTopLevelQuester(quester);

		if (!isLast(topLevel, quester.getPlayer()))
			return;

		BukkitTask task = tasks.remove(topLevel);
		if (task != null) {
			cancelTask(topLevel, task);
		}else {
			QuestsPlugin.getPlugin().getLoggerExpanded().warning("Unavailable task in \"Play Time\" stage {} for player {}",
					toString(), topLevel.getDebugName());
		}
	}

	private boolean isLast(TopLevelQuester quester, Player player) {
		Collection<Player> online = quester.getOnlinePlayers();

		if (online.isEmpty())
			return true;
		if (online.size() == 1)
			return online.iterator().next().equals(player);
		return false;
	}

	private void cancelTask(TopLevelQuester quester, BukkitTask task) {
		task.cancel();
		if (timeMode == TimeMode.ONLINE)
			getController().updateObjective(quester, "remainingTime", getRemaining(quester));
	}

	@Override
	public void initPlayerDatas(TopLevelQuester quester, Map<String, Object> datas) {
		super.initPlayerDatas(quester, datas);
		switch (timeMode) {
			case ONLINE:
				datas.put("remainingTime", playTicks);
				datas.put("lastJoin", System.currentTimeMillis());
				break;
			case OFFLINE:
				World world = Bukkit.getWorlds().get(0);
				datas.put("worldStartTime", world.getGameTime());
				datas.put("worldUuid", world.getUID().toString());
				break;
			case REALTIME:
				datas.put("startTime", System.currentTimeMillis());
				break;
		}
	}

	@Override
	public void unload() {
		super.unload();
		tasks.forEach(this::cancelTask);
		tasks.clear();
	}

	@Override
	public void setValidationRequirements(@NotNull RequirementList validationRequirements) {
		super.setValidationRequirements(validationRequirements);
		if (!validationRequirements.isEmpty())
			QuestsPlugin.getPlugin().getLogger().warning(validationRequirements.size()
					+ " requirements are set for a \"play time\" stage, but requirements are unsupported for this stage type.\n"
					+ controller.toString());
	}

	@Override
	protected void serialize(ConfigurationSection section) {
		section.set("playTicks", playTicks);
		section.set("timeMode", timeMode.name());
	}

	public static StagePlayTime deserialize(ConfigurationSection section, StageController controller) {
		return new StagePlayTime(controller, section.getLong("playTicks"),
				TimeMode.valueOf(section.getString("timeMode", "ONLINE").toUpperCase()));
	}

	public enum TimeMode {
		ONLINE(Lang.stagePlayTimeModeOnline.toString()),
		OFFLINE(Lang.stagePlayTimeModeOffline.toString()) {
			@Override
			public boolean isActive() {
				// no way to get full world time before 1.16.5
				return MinecraftVersion.MAJOR > 16 || (MinecraftVersion.MAJOR == 16 && MinecraftVersion.MINOR == 5);
			}
		},
		REALTIME(Lang.stagePlayTimeModeRealtime.toString());

		private final String description;

		private TimeMode(String description) {
			this.description = description;
		}

		public boolean isActive() {
			return true;
		}
	}

	public static class Creator extends StageCreation<StagePlayTime> {

		private long ticks;
		private TimeMode timeMode = TimeMode.ONLINE;

		private int slotTicks;
		private int slotTimeMode;

		public Creator(@NotNull StageCreationContext<StagePlayTime> context) {
			super(context);
		}

		@Override
		public void setupLine(@NotNull StageGuiLine line) {
			super.setupLine(line);

			line.refreshItemName(SLOT_REQUIREMENTS,
					"§n" + Lang.validationRequirements + "§c " + Lang.Disabled.toString().toUpperCase());

			slotTicks = line.setItem(7, ItemUtils.item(XMaterial.CLOCK, Lang.changeTicksRequired.toString()), event -> {
				Lang.GAME_TICKS.send(event.getPlayer());
				new TextEditor<>(event.getPlayer(), event::reopen, obj -> {
					setTicks(obj);
					event.reopen();
				}, MinecraftTimeUnit.TICK.getParser()).start();
			});

			slotTimeMode = line.setItem(8,
							ItemUtils.item(XMaterial.COMMAND_BLOCK, Lang.stagePlayTimeChangeTimeMode.toString(),
									QuestOption.formatNullableValue(timeMode.description, timeMode == TimeMode.ONLINE)),
							event -> {
						TimeMode next = timeMode;
						do {
							next = TimeMode.values()[(next.ordinal() + 1) % TimeMode.values().length];
						} while (!next.isActive());
						setTimeMode(next);
					});
		}

		public void setTicks(long ticks) {
			this.ticks = ticks;
			getLine().refreshItemLoreOptionValue(slotTicks, Lang.Ticks.quickFormat("ticks", ticks));
		}

		public void setTimeMode(TimeMode timeMode) {
			if (this.timeMode != timeMode) {
				this.timeMode = timeMode;
				getLine().refreshItemLore(slotTimeMode,
						QuestOption.formatNullableValue(timeMode.description, timeMode == TimeMode.ONLINE));
			}
		}

		@Override
		public void start(Player p) {
			super.start(p);
			Lang.GAME_TICKS.send(p);
			new TextEditor<>(p, context::removeAndReopenGui, obj -> {
				setTicks(obj);
				context.reopenGui();
			}, MinecraftTimeUnit.TICK.getParser()).start();
		}

		@Override
		public void edit(StagePlayTime stage) {
			super.edit(stage);
			setTicks(stage.playTicks);
			setTimeMode(stage.timeMode);
		}

		@Override
		public StagePlayTime finishStage(StageController controller) {
			return new StagePlayTime(controller, ticks, timeMode);
		}

	}

}
