package ai.residences.fortress.siege;

import l2r.gameserver.model.Creature;
import l2r.gameserver.model.entity.events.impl.FortressSiegeEvent;
import l2r.gameserver.model.entity.residence.Fortress;
import l2r.gameserver.model.instances.NpcInstance;
import l2r.gameserver.network.serverpackets.components.NpcString;
import l2r.gameserver.network.serverpackets.components.SystemMsg;
import l2r.gameserver.scripts.Functions;
import l2r.gameserver.tables.SkillTable;
import npc.model.residences.SiegeGuardInstance;
import ai.residences.SiegeGuardRanger;

/**
 * @author VISTALL
 * @date 16:39/17.04.2011
 */
public class ArcherCaption extends SiegeGuardRanger
{
	public ArcherCaption(NpcInstance actor)
	{
		super(actor);

		actor.addListener(FortressSiegeEvent.RESTORE_BARRACKS_LISTENER);
	}

	@Override
	public void onEvtSpawn()
	{
		super.onEvtSpawn();
		SiegeGuardInstance actor = getActor();

		FortressSiegeEvent siegeEvent = actor.getEvent(FortressSiegeEvent.class);
		if(siegeEvent == null)
			return;

		if(siegeEvent.getResidence().getFacilityLevel(Fortress.GUARD_BUFF) > 0)
			actor.doCast(SkillTable.getInstance().getInfo(5432, siegeEvent.getResidence().getFacilityLevel(Fortress.GUARD_BUFF)), actor, false);

		siegeEvent.barrackAction(0, false);
	}

	@Override
	public void onEvtDead(Creature killer)
	{
		SiegeGuardInstance actor = getActor();
		FortressSiegeEvent siegeEvent = actor.getEvent(FortressSiegeEvent.class);
		if(siegeEvent == null)
			return;

		siegeEvent.barrackAction(0, true);

		siegeEvent.broadcastTo(SystemMsg.THE_BARRACKS_HAVE_BEEN_SEIZED, FortressSiegeEvent.ATTACKERS, FortressSiegeEvent.DEFENDERS);

		Functions.npcSayInRange(actor, 1000, NpcString.YOU_MAY_HAVE_BROKEN_OUR_ARROWS_BUT_YOU_WILL_NEVER_BREAK_OUR_WILL_ARCHERS_RETREAT);

		super.onEvtDead(killer);

		siegeEvent.checkBarracks();
	}
}
