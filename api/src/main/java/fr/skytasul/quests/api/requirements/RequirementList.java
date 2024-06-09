package fr.skytasul.quests.api.requirements;

import java.util.*;
import java.util.Map.Entry;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import fr.skytasul.quests.api.QuestsPlugin;
import fr.skytasul.quests.api.localization.Lang;
import fr.skytasul.quests.api.quests.Quest;
import fr.skytasul.quests.api.serializable.SerializableObject;
import fr.skytasul.quests.api.utils.messaging.MessageType;
import fr.skytasul.quests.api.utils.messaging.MessageUtils;

public class RequirementList extends ArrayList<@NotNull AbstractRequirement> {

	private static final long serialVersionUID = 5568034962195448395L;

	public RequirementList() {}

	public RequirementList(@NotNull Collection<@NotNull AbstractRequirement> requirements) {
		super(requirements);
	}

	public boolean allMatch(@NotNull Player p, boolean sendReason) {
		Entry<Boolean, String> entry = allMatchEntry(p, sendReason);
		if (entry.getKey())
			return true;

		if (entry.getValue() != null && sendReason)
			MessageUtils.sendMessage(p, entry.getValue(), MessageType.DefaultMessageType.PREFIXED);

		return false;
	}

	public Map.Entry<Boolean, String> allMatchEntry(@NotNull Player p, boolean fetchReason) {
		boolean match = true;
		String reason = null;
		for (AbstractRequirement requirement : this) {
			try {
				if (!requirement.isValid() || !requirement.test(p)) {
					match = false;
					reason = requirement.getReason(p);
					if (!fetchReason || reason != null)
						break;

					// if we are here, it means a reason has not yet been found
					// so we continue until a reason is sent OR there is no more requirement
				}
			} catch (Exception ex) {
				QuestsPlugin.getPlugin().getLoggerExpanded().severe(
						"Cannot test requirement " + requirement.getClass().getSimpleName() + " for player " + p.getName(),
						ex);
				match = false;
				reason = "error";
				break;
			}
		}
		return new AbstractMap.SimpleEntry<>(match, reason);
	}

	public boolean anyMatch(@NotNull Player p) {
		for (AbstractRequirement requirement : this) {
			try {
				if (requirement.isValid() && requirement.test(p))
					return true;
			} catch (Exception ex) {
				QuestsPlugin.getPlugin().getLoggerExpanded().severe(
						"Cannot test requirement " + requirement.getClass().getSimpleName() + " for player " + p.getName(),
						ex);
			}
		}
		return false;
	}

	public void attachQuest(@NotNull Quest quest) {
		forEach(requirement -> requirement.attach(quest));
	}

	public void detachQuest() {
		forEach(requirement -> requirement.detach());
	}

	public String getSizeString() {
		return getSizeString(size());
	}

	public @NotNull List<Map<String, Object>> serialize() {
		return SerializableObject.serializeList(this);
	}

	public static RequirementList deserialize(@NotNull List<Map<?, ?>> mapList) {
		return new RequirementList(SerializableObject.deserializeList(mapList, AbstractRequirement::deserialize));
	}

	public static String getSizeString(int size) {
		return Lang.requirements.quickFormat("amount", size);
	}

}
