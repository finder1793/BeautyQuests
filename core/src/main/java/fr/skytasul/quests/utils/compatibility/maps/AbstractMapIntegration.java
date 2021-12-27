package fr.skytasul.quests.utils.compatibility.maps;

import org.bukkit.Location;

import fr.skytasul.quests.BeautyQuests;
import fr.skytasul.quests.api.QuestsHandler;
import fr.skytasul.quests.options.OptionStarterNPC;
import fr.skytasul.quests.structure.Quest;
import fr.skytasul.quests.utils.DebugUtils;

public abstract class AbstractMapIntegration implements QuestsHandler {
	
	@Override
	public void questLoaded(Quest quest) {
		if (!quest.hasOption(OptionStarterNPC.class)) return;
		if (quest.isHidden()) {
			DebugUtils.logMessage("No marker created for quest " + quest.getID() + " : quest is hid");
			return;
		}
		
		Location lc = quest.getOptionValueOrDef(OptionStarterNPC.class).getLocation();
		if (lc == null) {
			BeautyQuests.logger.warning("Cannot create map marker for quest #" + quest.getID() + " (" + quest.getName() + ")");
		}else {
			addMarker(quest, lc);
		}
	}
	
	@Override
	public void questUnload(Quest quest) {
		if (!quest.isHidden() && quest.hasOption(OptionStarterNPC.class)) removeMarker(quest);
	}
	
	protected abstract void addMarker(Quest quest, Location lc);
	
	protected abstract void removeMarker(Quest quest);
	
}
