package l2r.gameserver.model.entity.olympiad;


import l2r.commons.threading.RunnableImpl;
import l2r.gameserver.Announcements;
import l2r.gameserver.instancemanager.OlympiadHistoryManager;
import l2r.gameserver.model.entity.Hero;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ValidationTask extends RunnableImpl
{
	private static final Logger _log = LoggerFactory.getLogger(ValidationTask.class);

	@Override
	public void runImpl() throws Exception
	{
		OlympiadHistoryManager.getInstance().switchData();

		OlympiadDatabase.sortHerosToBe();
		OlympiadDatabase.saveNobleData();
		if(Hero.getInstance().computeNewHeroes(Olympiad._heroesToBe))
			_log.warn("Olympiad: Error while computing new heroes!");
		Announcements.getInstance().announceToAll("Olympiad Validation Period has ended");

		Olympiad._period = 0;
		Olympiad._currentCycle++;

		OlympiadDatabase.cleanupNobles();
		OlympiadDatabase.loadNoblesRank();
		OlympiadDatabase.setNewOlympiadEnd();

		Olympiad.init();
		OlympiadDatabase.save();
	}
}