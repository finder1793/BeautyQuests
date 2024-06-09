package fr.skytasul.quests.api.questers;

import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import fr.skytasul.quests.api.players.PlayerAccount;

public interface PlayerQuester extends Quester {

	@NotNull
	PlayerAccount getAccount();

	@Nullable
	Player getPlayer();

}
