package fr.skytasul.quests.questers;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import fr.skytasul.quests.api.questers.PlayerQuester;
import fr.skytasul.quests.api.questers.Quester;
import fr.skytasul.quests.api.questers.QuesterWaiter;
import fr.skytasul.quests.api.questers.TopLevelQuester;
import fr.skytasul.quests.api.quests.Quest;
import fr.skytasul.quests.api.quests.QuestDatas;
import fr.skytasul.quests.api.quests.QuestStartResult;
import fr.skytasul.quests.structure.QuestImplementation;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.UnmodifiableView;
import java.util.Arrays;
import java.util.Collection;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

public final class DefaultQuesterStrategy implements QuesterStrategy {

	public static final DefaultQuesterStrategy INSTANCE = new DefaultQuesterStrategy();

	private final Cache<Quester, TopLevelQuester> cache = CacheBuilder.newBuilder()
			.maximumSize(5000)
			.expireAfterAccess(5, TimeUnit.MINUTES)
			.build();

	private DefaultQuesterStrategy() {}

	@Override
	public @NotNull TopLevelQuester getTopLevelQuester(@NotNull Quester quester) {
		if (quester instanceof TopLevelQuester)
			return (TopLevelQuester) quester;

		TopLevelQuester top = cache.getIfPresent(quester);
		if (top != null)
			return top;

		if (!(quester instanceof PlayerQuester))
			throw new IllegalArgumentException("Quester is of unsupported type " + quester.getClass().getSimpleName());

		top = new WrappedQuester((PlayerQuester) quester);
		cache.put(quester, top);
		return top;
	}

	@Override
	public QuestStartResult testStart(@NotNull Player p, @NotNull QuestImplementation quest) {
		return quest.testStartSingle(p);
	}

	private class WrappedQuester implements TopLevelQuester {

		private final PlayerQuester wrapped;
		private final @NotNull @UnmodifiableView Collection<PlayerQuester> sub;

		public WrappedQuester(PlayerQuester wrapped) {
			this.wrapped = wrapped;
			this.sub = Arrays.asList(wrapped);
		}

		@Override
		public boolean isOnline() {
			return wrapped.isOnline();
		}

		@Override
		public @NotNull String getDebugName() {
			return wrapped.getDebugName();
		}

		@Override
		public boolean hasQuestDatas(@NotNull Quest quest) {
			return wrapped.hasQuestDatas(quest);
		}

		@Override
		public @Nullable QuestDatas getQuestDatasIfPresent(@NotNull Quest quest) {
			return wrapped.getQuestDatasIfPresent(quest);
		}

		@Override
		public @NotNull QuestDatas getQuestDatas(@NotNull Quest quest) {
			return wrapped.getQuestDatas(quest);
		}

		@Override
		public @NotNull CompletableFuture<QuestDatas> removeQuestDatas(@NotNull Quest quest) {
			return wrapped.removeQuestDatas(quest);
		}

		@Override
		public @NotNull CompletableFuture<QuestDatas> removeQuestDatas(int id) {
			return wrapped.removeQuestDatas(id);
		}

		@Override
		public @NotNull @UnmodifiableView Collection<QuestDatas> getQuestsDatas() {
			return wrapped.getQuestsDatas();
		}

		@Override
		public @NotNull @UnmodifiableView Collection<PlayerQuester> getSubQuesters() {
			return sub;
		}

		@Override
		public @NotNull QuesterWaiter createWaiter() {
			throw new UnsupportedOperationException();
		}

	}

}
