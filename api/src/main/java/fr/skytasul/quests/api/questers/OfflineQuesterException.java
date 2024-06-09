package fr.skytasul.quests.api.questers;

public class OfflineQuesterException extends RuntimeException {

	private static final long serialVersionUID = -1943336164907620748L;

	public OfflineQuesterException(Quester quester) {
		super(quester.getDebugName() + " is offline");
	}

	public OfflineQuesterException(String message) {
		super(message);
	}

	public static void ensureQuesterOnline(Quester quester) throws OfflineQuesterException {
		if (!quester.isOnline())
			throw new OfflineQuesterException(quester);
	}

}
