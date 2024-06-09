package fr.skytasul.quests.api.events.accounts;

import fr.skytasul.quests.api.players.PlayerAccount;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.jetbrains.annotations.NotNull;
import java.util.Objects;

public abstract class PlayerAccountEvent extends Event {

	protected final @NotNull PlayerAccount account;

	protected PlayerAccountEvent(@NotNull PlayerAccount account) {
		this.account = account;
	}

	public boolean isAccountOnline() {
		return account.isOnline();
	}

	public @NotNull Player getPlayer() {
		if (!account.isOnline())
			throw new IllegalStateException("Account " + account.getDebugName() + " is not currently used");

		return Objects.requireNonNull(account.getPlayer());
	}

	public @NotNull PlayerAccount getPlayerAccount() {
		return account;
	}

}