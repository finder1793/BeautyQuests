package fr.skytasul.quests.api.stages;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import fr.skytasul.quests.api.options.description.DescriptionSource;
import fr.skytasul.quests.api.questers.Quester;
import fr.skytasul.quests.api.utils.messaging.MessageType;
import fr.skytasul.quests.api.utils.messaging.PlaceholdersContext;
import fr.skytasul.quests.api.utils.messaging.PlaceholdersContext.QuesterPlaceholdersContext;

public interface StageDescriptionPlaceholdersContext extends PlaceholdersContext, QuesterPlaceholdersContext {

	@NotNull
	DescriptionSource getDescriptionSource();

	static @NotNull StageDescriptionPlaceholdersContext of(boolean replacePluginPlaceholders, @NotNull Quester quester,
			@NotNull DescriptionSource source, @Nullable MessageType messageType) {
		return new StageDescriptionPlaceholdersContext() {

			@Override
			public boolean replacePluginPlaceholders() {
				return replacePluginPlaceholders;
			}

			@Override
			public @NotNull Quester getQuester() {
				return quester;
			}

			@Override
			public @NotNull DescriptionSource getDescriptionSource() {
				return source;
			}

			@Override
			public @Nullable MessageType getMessageType() {
				return messageType;
			}
		};
	}

}
