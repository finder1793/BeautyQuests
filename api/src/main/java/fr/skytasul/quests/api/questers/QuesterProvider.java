package fr.skytasul.quests.api.questers;

import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import fr.skytasul.quests.api.players.PlayersManager;

public interface QuesterProvider {

	public @NotNull TopLevelQuester getTopLevelQuester(@NotNull Quester quester);

	public default @NotNull TopLevelQuester getTopLevelQuester(@NotNull Player player) {
		return getTopLevelQuester(PlayersManager.getPlayerAccount(player));
	}

}
