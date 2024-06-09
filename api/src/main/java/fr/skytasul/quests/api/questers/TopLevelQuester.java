package fr.skytasul.quests.api.questers;

import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.UnmodifiableView;
import java.util.Collection;
import java.util.stream.Collectors;

public interface TopLevelQuester extends Quester {

	@NotNull
	@UnmodifiableView
	Collection<PlayerQuester> getSubQuesters();

	@NotNull
	@UnmodifiableView
	default Collection<Player> getOnlinePlayers() {
		return getSubQuesters().stream().filter(PlayerQuester::isOnline).map(PlayerQuester::getPlayer)
				.collect(Collectors.toList());
	}

	@NotNull
	QuesterWaiter createWaiter(); // TODO what?

}
