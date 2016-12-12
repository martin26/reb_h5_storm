package ai;

import l2r.commons.util.Rnd;
import l2r.gameserver.ai.CtrlEvent;
import l2r.gameserver.ai.DefaultAI;
import l2r.gameserver.model.Creature;
import l2r.gameserver.model.Player;
import l2r.gameserver.model.instances.NpcInstance;
import l2r.gameserver.scripts.Functions;
import l2r.gameserver.tables.SpawnTable;
import l2r.gameserver.utils.Location;

import java.util.List;

/**
 * AI 18811	Guardian of the Altar - Спавит рандомных охранников если атакован
 * - если у игрока есть Protection Souls Pendant 14848 - спавнит мини-рб
 * - не использует random walk
 * - не отвечает на атаки
 * 
 * @author pchayka
 */
public class GuardianAltar extends DefaultAI
{

	private static final int DarkShamanVarangka = 18808;

	public GuardianAltar(NpcInstance actor)
	{
		super(actor);
		actor.setIsInvul(true);
	}

	@Override
	protected void onEvtAttacked(Creature attacker, int damage)
	{
		NpcInstance actor = getActor();
		if(attacker == null)
			return;

		Player player = attacker.getPlayer();

		if(Rnd.chance(40) && player.getInventory().destroyItemByItemId(14848, 1L))
		{
			List<NpcInstance> around = actor.getAroundNpc(1500, 300);
			if(around != null && !around.isEmpty())
				for(NpcInstance npc : around)
					if(npc.getNpcId() == 18808)
					{
						Functions.npcSay(actor, "I can sense the presence of Dark Shaman already!");
						return;
					}

			try
			{
				NpcInstance npc = SpawnTable.spawnSingle(DarkShamanVarangka, Location.findPointToStay(actor, 400, 420));
				if(attacker.isPet() || attacker.isSummon())
					npc.getAI().notifyEvent(CtrlEvent.EVT_AGGRESSION, attacker, Rnd.get(2, 100));
				npc.getAI().notifyEvent(CtrlEvent.EVT_AGGRESSION, attacker.getPlayer(), Rnd.get(1, 100));
			}
			catch(Exception e)
			{
				e.printStackTrace();
			}

		}
		else if(Rnd.chance(5))
		{
			List<NpcInstance> around = actor.getAroundNpc(1000, 300);
			if(around != null && !around.isEmpty())
				for(NpcInstance npc : around)
					if(npc.getNpcId() == 22702)
						return;

			for(int i = 0; i < 2; i++)
				try
				{
					NpcInstance npc = SpawnTable.spawnSingle(22702, Location.findPointToStay(actor, 150, 160));
					if(attacker.isPet() || attacker.isSummon())
						npc.getAI().notifyEvent(CtrlEvent.EVT_AGGRESSION, attacker, Rnd.get(2, 100));
					npc.getAI().notifyEvent(CtrlEvent.EVT_AGGRESSION, attacker.getPlayer(), Rnd.get(1, 100));
				}
				catch(Exception e)
				{
					e.printStackTrace();
				}
		}
		return;
	}

	@Override
	protected boolean randomWalk()
	{
		return false;
	}
}