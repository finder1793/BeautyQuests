package fr.skytasul.quests.api.quests;

import java.util.Objects;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import fr.skytasul.quests.api.utils.messaging.MessageType;
import fr.skytasul.quests.api.utils.messaging.MessageUtils;

public class QuestStartResult {

	private static final QuestStartResult SUCCESS = new QuestStartResult(Status.SUCCESS, null);

	private final @NotNull Status status;
	private final @Nullable String reason;

	private QuestStartResult(@NotNull Status status, @Nullable String reason) {
		this.status = status;
		this.reason = reason;
	}

	public @NotNull Status getStatus() {
		return status;
	}

	public @Nullable String getReason() {
		return reason;
	}

	public boolean isSuccess() {
		return status == Status.SUCCESS;
	}

	public boolean isSuccessOrSendMessage(@NotNull Player player) {
		if (isSuccess())
			return true;
		MessageUtils.sendMessage(player, reason, MessageType.DefaultMessageType.PREFIXED);
		return false;
	}

	public static @NotNull QuestStartResult success() {
		return SUCCESS;
	}

	public static @NotNull QuestStartResult fail(@NotNull Status status) {
		return fail(status, null);
	}

	public static @NotNull QuestStartResult fail(@NotNull Status status, @Nullable String reason) {
		if (Objects.requireNonNull(status) == Status.SUCCESS)
			throw new IllegalArgumentException("Invalid fail status");

		return new QuestStartResult(status, reason);
	}

	public enum Status {
		SUCCESS, FAIL_REQUIREMENT, FAIL_TIMER, FAIL_ALREADY_STARTED, FAIL_CANNOT_RESTART, FAIL_LIMIT, FAIL_OTHER, CANCELLED;
	}

}
