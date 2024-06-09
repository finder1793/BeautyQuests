package fr.skytasul.quests.structure;

import fr.skytasul.quests.QuestsConfigurationImplementation;
import fr.skytasul.quests.api.QuestsConfiguration;
import fr.skytasul.quests.api.QuestsPlugin;
import fr.skytasul.quests.api.events.progress.QuesterSetStageEvent;
import fr.skytasul.quests.api.localization.Lang;
import fr.skytasul.quests.api.options.description.DescriptionSource;
import fr.skytasul.quests.api.questers.OfflineQuesterException;
import fr.skytasul.quests.api.questers.PlayerQuester;
import fr.skytasul.quests.api.questers.Quester;
import fr.skytasul.quests.api.questers.TopLevelQuester;
import fr.skytasul.quests.api.quests.QuestDatas;
import fr.skytasul.quests.api.quests.branches.EndingStage;
import fr.skytasul.quests.api.quests.branches.QuestBranch;
import fr.skytasul.quests.api.requirements.Actionnable;
import fr.skytasul.quests.api.rewards.InterruptingBranchException;
import fr.skytasul.quests.api.stages.StageController;
import fr.skytasul.quests.api.utils.messaging.DefaultErrors;
import fr.skytasul.quests.api.utils.messaging.MessageUtils;
import fr.skytasul.quests.api.utils.messaging.PlaceholderRegistry;
import fr.skytasul.quests.players.AdminMode;
import fr.skytasul.quests.utils.QuestUtils;
import org.apache.commons.lang.Validate;
import org.bukkit.Bukkit;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.UnmodifiableView;
import java.util.*;
import java.util.stream.Collectors;

public class QuestBranchImplementation implements QuestBranch {

	private final List<EndingStageImplementation> endStages = new ArrayList<>(5);
	private final List<StageControllerImplementation> regularStages = new ArrayList<>(15);

	private final List<Quester> asyncReward = new ArrayList<>(5);

	private final @NotNull BranchesManagerImplementation manager;

	public QuestBranchImplementation(@NotNull BranchesManagerImplementation manager) {
		this.manager = manager;
	}

	@Override
	public @NotNull QuestImplementation getQuest() {
		return manager.getQuest();
	}

	@Override
	public @NotNull BranchesManagerImplementation getManager() {
		return manager;
	}

	public int getStageSize(){
		return regularStages.size();
	}

	@Override
	public int getId() {
		return manager.getId(this);
	}

	public void addRegularStage(@NotNull StageControllerImplementation<?> stage) {
		Validate.notNull(stage, "Stage cannot be null !");
		regularStages.add(stage);
		stage.load();
	}

	public void addEndStage(@NotNull StageControllerImplementation<?> stage, @NotNull QuestBranchImplementation linked) {
		Validate.notNull(stage, "Stage cannot be null !");
		endStages.add(new EndingStageImplementation(stage, linked));
		stage.load();
	}

	@Override
	public @NotNull @UnmodifiableView List<@NotNull StageController> getRegularStages() {
		return (List) regularStages;
	}

	@Override
	public @NotNull StageControllerImplementation<?> getRegularStage(int id) {
		return regularStages.get(id);
	}

	@Override
	public @NotNull @UnmodifiableView List<EndingStage> getEndingStages() {
		return (List) endStages;
	}

	@Override
	public @NotNull StageController getEndingStage(int id) {
		return endStages.get(id).getStage(); // TODO beware index out of bounds
	}

	public @Nullable QuestBranchImplementation getLinkedBranch(@NotNull StageController endingStage) {
		return endStages.stream().filter(end -> end.getStage().equals(endingStage)).findAny().get().getBranch();
	}

	public int getRegularStageId(StageController stage) {
		return regularStages.indexOf(stage);
	}

	public int getEndingStageId(StageController stage) {
		for (int i = 0; i < endStages.size(); i++) {
			EndingStage endingStage = endStages.get(i);
			if (endingStage.getStage().equals(stage))
				return i;
		}
		return -1;
	}

	public boolean isEndingStage(StageController stage) {
		return endStages.stream().anyMatch(end -> end.getStage().equals(stage));
	}

	@Override
	public @NotNull String getDescriptionLine(@NotNull Quester quester, @NotNull DescriptionSource source) {
		QuestDatas datas;
		if (!quester.hasQuestDatas(getQuest()) || (datas = quester.getQuestDatas(getQuest())).getBranch() != getId())
			throw new IllegalArgumentException("quester does not have this branch launched");
		if (asyncReward.contains(quester))
			return Lang.SCOREBOARD_ASYNC_END.toString();
		if (datas.isInEndingStages()) {
			return endStages.stream()
					.map(stage -> stage.getStage().getDescriptionLine(quester, source))
					.filter(Objects::nonNull)
					.collect(Collectors.joining("{nl}" + Lang.SCOREBOARD_BETWEEN_BRANCHES + " {nl}"));
		}
		if (datas.getStage() < 0)
			return "§cerror: no stage set for branch " + getId();
		if (datas.getStage() >= regularStages.size()) return "§cerror: datas do not match";

		String descriptionLine = regularStages.get(datas.getStage()).getDescriptionLine(quester, source);
		return MessageUtils.format(QuestsConfiguration.getConfig().getStageDescriptionConfig().getStageDescriptionFormat(),
				PlaceholderRegistry.of("stage_index", datas.getStage() + 1, "stage_amount", regularStages.size(),
						"stage_description", descriptionLine == null ? "" : descriptionLine));
	}

	@Override
	public boolean hasStageLaunched(@Nullable Quester quester, @NotNull StageController stage) {
		if (quester == null)
			return false;

		if (asyncReward.contains(quester))
			return false;
		if (!quester.hasQuestDatas(getQuest()))
			return false;

		QuestDatas datas = quester.getQuestDatas(getQuest());
		if (datas.getBranch() != getId())
			return false;

		if (datas.isInEndingStages())
			return isEndingStage(stage);

		return getRegularStageId(stage) == datas.getStage();
	}

	public void remove(@NotNull TopLevelQuester quester, boolean end) {
		if (!quester.hasQuestDatas(getQuest()))
			return;
		QuestDatas datas = quester.getQuestDatas(getQuest());
		if (end) {
			if (datas.isInEndingStages()) {
				endStages.forEach(x -> x.getStage().end(quester));
			} else if (datas.getStage() >= 0 && datas.getStage() < regularStages.size())
				getRegularStage(datas.getStage()).end(quester);
		}
		datas.setBranch(-1);
		datas.setStage(-1);
	}

	public void start(@NotNull TopLevelQuester quester) {
		quester.getQuestDatas(getQuest()).setBranch(getId());
		if (!regularStages.isEmpty()){
			setQuesterStage(quester, regularStages.get(0));
		}else {
			setQuesterEndingStages(quester);
		}
	}

	@Override
	public void finishQuesterStage(@NotNull Quester quester, @NotNull StageController stage) {
		OfflineQuesterException.ensureQuesterOnline(quester);
		QuestsPlugin.getPlugin().getLoggerExpanded().debug("Next stage for {} (coming from {})", quester.getDebugName(),
				stage.toString());

		QuestDatas datas = quester.getQuestDatas(getQuest());
		if (datas.getBranch() != getId() || (datas.isInEndingStages() && !isEndingStage(stage))
				|| (!datas.isInEndingStages() && datas.getStage() != getRegularStageId(stage))) {
			QuestsPlugin.getPlugin().getLoggerExpanded().warning(
					"Trying to finish stage {} for {}, but the player didn't have started it.", stage.toString(),
					quester.getDebugName());
			return;
		}

		AdminMode.broadcast("Quester {} has finished the stage %s of quest %d", quester.getDebugName(), stage.getFlowId(),
				getQuest().getId());

		TopLevelQuester topLevel = getQuest().getQuesterProvider().getTopLevelQuester(quester);

		datas.addQuestFlow(stage);
		if (isEndingStage(stage)) { // ending stage
			for (EndingStageImplementation end : endStages) {
				if (end.getStage() != stage)
					end.getStage().end(topLevel);
			}
		}
		datas.setStage(-1);
		endStage(topLevel, (StageControllerImplementation<?>) stage, () -> {
			if (!manager.getQuest().hasStarted(topLevel))
				return;
			if (regularStages.contains(stage)){ // not ending stage - continue the branch or finish the quest
				int newId = getRegularStageId(stage) + 1;
				if (newId == regularStages.size()){
					if (endStages.isEmpty()){
						remove(topLevel, false);
						getQuest().finish(topLevel);
						return;
					}
					setQuesterEndingStages(topLevel);
				}else {
					setQuesterStage(topLevel, regularStages.get(newId));
				}
			}else { // ending stage - redirect to other branch
				remove(topLevel, false);
				QuestBranchImplementation branch = getLinkedBranch(stage);
				if (branch == null){
					getQuest().finish(topLevel);
					return;
				}
				branch.start(topLevel);
			}
			manager.callQuestUpdated(topLevel);
		});
	}

	private void endStage(@NotNull TopLevelQuester quester, @NotNull StageControllerImplementation<?> stage,
			@NotNull Runnable runAfter) {
		OfflineQuesterException.ensureQuesterOnline(quester);

		if (stage.getStage().hasAsyncEnd() && !(quester instanceof PlayerQuester))
			throw new UnsupportedOperationException("Cannot have async rewards for group quests"); // TODO global async

		Collection<Player> players = quester.getOnlinePlayers();

		stage.end(quester);
		stage.getStage().getValidationRequirements().stream()
				.filter(Actionnable.class::isInstance)
				.map(Actionnable.class::cast).forEach(x -> players.forEach(p -> x.trigger(p)));
		if (stage.getStage().hasAsyncEnd()) {
			Player p = ((PlayerQuester) quester).getPlayer();
			// TODO use a thread pool instead
			new Thread(() -> {
				QuestsPlugin.getPlugin().getLoggerExpanded()
						.debug("Using " + Thread.currentThread().getName() + " as the thread for async rewards.");
				asyncReward.add(quester);
				try {
					List<String> given = stage.getStage().getRewards().giveRewards(p);
					if (!given.isEmpty() && QuestsConfiguration.getConfig().getQuestsConfig().stageEndRewardsMessage())
						Lang.FINISHED_OBTAIN.quickSend(p, "rewards",
								MessageUtils.itemsToFormattedString(given.toArray(new String[0])));
				} catch (InterruptingBranchException ex) {
					QuestsPlugin.getPlugin().getLoggerExpanded().debug(
							"Interrupted branching in async stage end for " + p.getName() + " via " + ex.toString());
					return;
				} catch (Exception e) {
					DefaultErrors.sendGeneric(p, "giving async rewards");
					QuestsPlugin.getPlugin().getLoggerExpanded()
							.severe("An error occurred while giving stage async end rewards.", e);
				} finally {
					// by using the try-catch, we ensure that "asyncReward#remove" is called
					// otherwise, the player would be completely stuck
					asyncReward.remove(quester);
				}
				QuestUtils.runSync(runAfter);
			}, "BQ stage end " + quester.getDebugName()).start();
		} else {
			try {
				stage.getStage().getRewards().giveRewards(players).forEach((p, given) -> {
					if (QuestsConfiguration.getConfig().getQuestsConfig().stageEndRewardsMessage())
						Lang.FINISHED_OBTAIN.quickSend(p, "rewards",
								MessageUtils.itemsToFormattedString(given.toArray(new String[0])));
				});
				runAfter.run();
			} catch (InterruptingBranchException ex) {
				QuestsPlugin.getPlugin().getLoggerExpanded().debug(
						"Interrupted branching in async stage end for " + quester.getDebugName() + " via " + ex.toString());
			}
		}
	}

	@Override
	public void setQuesterStage(@NotNull TopLevelQuester quester, @NotNull StageController stage) {
		OfflineQuesterException.ensureQuesterOnline(quester);

		QuestDatas questDatas = quester.getQuestDatas(getQuest());
		if (questDatas.getBranch() != getId())
			throw new IllegalStateException("The player is not in the right branch");

		if (QuestsConfiguration.getConfig().getQuestsConfig().playerQuestUpdateMessage() && questDatas.getStage() != -1)
			quester.getOnlinePlayers().forEach(this::playNextStage);
		questDatas.setStage(getRegularStageId(stage));
		((StageControllerImplementation<?>) stage).start(quester);
		Bukkit.getPluginManager().callEvent(new QuesterSetStageEvent(getQuest(), quester, stage));
	}

	@Override
	public void setQuesterEndingStages(@NotNull TopLevelQuester quester) {
		OfflineQuesterException.ensureQuesterOnline(quester);

		QuestDatas datas = quester.getQuestDatas(getQuest());
		if (datas.getBranch() != getId())
			throw new IllegalStateException("The player is not in the right branch");

		if (QuestsConfiguration.getConfig().getQuestsConfig().playerQuestUpdateMessage())
			quester.getOnlinePlayers().forEach(this::playNextStage);

		datas.setInEndingStages();
		for (EndingStageImplementation endStage : endStages) {
			endStage.getStage().start(quester);
			Bukkit.getPluginManager().callEvent(new QuesterSetStageEvent(getQuest(), quester, endStage.getStage()));
		}
	}

	private void playNextStage(@NotNull Player p) {
		QuestUtils.playPluginSound(p.getLocation(), QuestsConfiguration.getConfig().getQuestsConfig().nextStageSound(),
				0.5F);
		if (QuestsConfigurationImplementation.getConfiguration().showNextParticles())
			QuestsConfigurationImplementation.getConfiguration().getParticleNext().send(p, Arrays.asList(p));
		Lang.QUEST_UPDATED.send(p, getQuest());
	}

	public void remove(){
		regularStages.forEach(StageControllerImplementation::unload);
		regularStages.clear();
		endStages.forEach(end -> end.getStage().unload());
		endStages.clear();
	}

	public void save(@NotNull ConfigurationSection section) {
		ConfigurationSection stagesSection = section.createSection("stages");
		for (int i = 0; i < regularStages.size(); i++) {
			try {
				regularStages.get(i).getStage().save(stagesSection.createSection(Integer.toString(i)));
			}catch (Exception ex) {
				QuestsPlugin.getPlugin().getLoggerExpanded()
						.severe("Error when serializing the stage " + i + " for the quest " + getQuest().getId(), ex);
				QuestsPlugin.getPlugin().notifySavingFailure();
			}
		}

		ConfigurationSection endSection = section.createSection("endingStages");
		for (int i = 0; i < endStages.size(); i++) {
			EndingStageImplementation en = endStages.get(i);
			try{
				ConfigurationSection stageSection = endSection.createSection(Integer.toString(i));
				en.getStage().getStage().save(stageSection);
				QuestBranchImplementation branchLinked = en.getBranch();
				if (branchLinked != null)
					stageSection.set("branchLinked", branchLinked.getId());
			}catch (Exception ex){
				QuestsPlugin.getPlugin().getLoggerExpanded()
						.severe("Error when serializing the ending stage " + i + " for the quest " + getQuest().getId(), ex);
				QuestsPlugin.getPlugin().notifySavingFailure();
			}
		}
	}

	@Override
	public String toString() {
		return "QuestBranch{regularStages=" + regularStages.size() + ",endingStages=" + endStages.size() + "}";
	}

	public boolean load(@NotNull ConfigurationSection section) {
		ConfigurationSection stagesSection;
		if (section.isList("stages")) { // TODO migration 0.19.3
			List<Map<?, ?>> stages = section.getMapList("stages");
			section.set("stages", null);
			stagesSection = section.createSection("stages");
			stages.stream()
					.sorted((x, y) -> {
						int xid = (Integer) x.get("order");
						int yid = (Integer) y.get("order");
						if (xid < yid) return -1;
						if (xid > yid) return 1;
						throw new IllegalArgumentException("Two stages with same order " + xid);
					}).forEach(branch -> {
						int order = (Integer) branch.remove("order");
						stagesSection.createSection(Integer.toString(order), branch);
					});
		}else {
			stagesSection = section.getConfigurationSection("stages");
		}

		for (int id : stagesSection.getKeys(false).stream().map(Integer::parseInt).sorted().collect(Collectors.toSet())) {
			try{
				addRegularStage(StageControllerImplementation.loadFromConfig(this,
						stagesSection.getConfigurationSection(Integer.toString(id))));
			}catch (Exception ex){
				QuestsPlugin.getPlugin().getLoggerExpanded().severe(
						"Error when deserializing the stage " + id + " for the quest " + manager.getQuest().getId(), ex);
				QuestsPlugin.getPlugin().notifyLoadingFailure();
				return false;
			}
		}

		ConfigurationSection endingStagesSection = null;
		if (section.isList("endingStages")) { // TODO migration 0.19.3
			List<Map<?, ?>> endingStages = section.getMapList("endingStages");
			section.set("endingStages", null);
			endingStagesSection = section.createSection("endingStages");
			int i = 0;
			for (Map<?, ?> stage : endingStages) {
				endingStagesSection.createSection(Integer.toString(i++), stage);
			}
		}else if (section.contains("endingStages")) {
			endingStagesSection = section.getConfigurationSection("endingStages");
		}

		if (endingStagesSection != null) {
			for (String key : endingStagesSection.getKeys(false)) {
				try{
					ConfigurationSection stage = endingStagesSection.getConfigurationSection(key);
					QuestBranchImplementation branchLinked = stage.contains("branchLinked") ? manager.getBranch(stage.getInt("branchLinked")) : null;
					addEndStage(StageControllerImplementation.loadFromConfig(this, stage), branchLinked);
				}catch (Exception ex){
					QuestsPlugin.getPlugin().getLoggerExpanded().severe(
							"Error when deserializing an ending stage for the quest " + manager.getQuest().getId(), ex);
					QuestsPlugin.getPlugin().notifyLoadingFailure();
					return false;
				}
			}
		}

		return true;
	}

}