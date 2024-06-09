package fr.skytasul.quests.api.utils.messaging;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import fr.skytasul.quests.api.players.PlayerAccount;
import fr.skytasul.quests.api.players.PlayersManager;
import fr.skytasul.quests.api.questers.PlayerQuester;
import fr.skytasul.quests.api.questers.Quester;

public interface PlaceholdersContext {

	static final @NotNull PlaceholdersContext DEFAULT_CONTEXT = of(null, true, null);

	@Nullable
	CommandSender getActor();

	boolean replacePluginPlaceholders();

	@Nullable MessageType getMessageType();

	static PlaceholdersContext of(@Nullable CommandSender actor, boolean replacePluginPlaceholders,
			@Nullable MessageType messageType) {
		return new PlaceholdersContext() {

			@Override
			public boolean replacePluginPlaceholders() {
				return replacePluginPlaceholders;
			}

			@Override
			public @Nullable CommandSender getActor() {
				return actor;
			}

			@Override
			public @Nullable MessageType getMessageType() {
				return messageType;
			}
		};
	}

	static PlayerPlaceholdersContext of(@Nullable Player actor, boolean replacePluginPlaceholders,
			@Nullable MessageType messageType) {
		return new PlayerPlaceholdersContext() {

			@Override
			public boolean replacePluginPlaceholders() {
				return replacePluginPlaceholders;
			}

			@Override
			public @NotNull Player getActor() {
				return actor;
			}

			@Override
			public @Nullable MessageType getMessageType() {
				return messageType;
			}
		};
	}

	public interface QuesterPlaceholdersContext extends PlaceholdersContext {

		@NotNull
		Quester getQuester();

		@Override
		default @Nullable CommandSender getActor() {
			return getQuester() instanceof PlayerQuester ? ((PlayerQuester) getQuester()).getPlayer() : null;
		}

	}

	public interface PlayerPlaceholdersContext extends PlaceholdersContext, QuesterPlaceholdersContext {

		@Override
		@Nullable
		Player getActor();

		@NotNull
		default PlayerAccount getPlayerAccount() {
			return PlayersManager.getPlayerAccount(getActor());
		}

		@Override
		default @NotNull Quester getQuester() {
			return getPlayerAccount();
		}

	}

}
