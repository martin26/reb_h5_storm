package ai.isle_of_prayer;

import l2r.commons.util.Rnd;
import l2r.gameserver.ai.CtrlEvent;
import l2r.gameserver.ai.Mystic;
import l2r.gameserver.data.xml.holder.NpcHolder;
import l2r.gameserver.idfactory.IdFactory;
import l2r.gameserver.model.Creature;
import l2r.gameserver.model.Party;
import l2r.gameserver.model.Player;
import l2r.gameserver.model.instances.MonsterInstance;
import l2r.gameserver.model.instances.NpcInstance;

public class IsleOfPrayerMystic extends Mystic
{
	private boolean _penaltyMobsNotSpawned = true;
	private static final int PENALTY_MOBS[] = { 18364, 18365, 18366 };
	private static final int YELLOW_CRYSTAL = 9593;
	private static final int GREEN_CRYSTAL = 9594;
	private static final int RED_CRYSTAL = 9596;

	public IsleOfPrayerMystic(NpcInstance actor)
	{
		super(actor);
	}

	@Override
	protected void onEvtAttacked(Creature attacker, int damage)
	{
		NpcInstance actor = getActor();
		if(_penaltyMobsNotSpawned && attacker.getPlayer() != null && actor.getY() > 161800)
		{
			Party party = attacker.getPlayer().getParty();
			if(party != null && party.size() > 2)
			{
				_penaltyMobsNotSpawned = false;
				int count = party.size() > 4 ? 4 : 2;
				for(int i = 0; i < count; i++)
					try
					{
						MonsterInstance npc = new MonsterInstance(IdFactory.getInstance().getNextId(), NpcHolder.getInstance().getTemplate(PENALTY_MOBS[Rnd.get(PENALTY_MOBS.length)]));
						npc.setSpawnedLoc(((MonsterInstance) actor).getMinionPosition());
						npc.setReflection(actor.getReflection());
						npc.setCurrentHpMp(npc.getMaxHp(), npc.getMaxMp(), true);
						npc.spawnMe(npc.getSpawnedLoc());
						npc.getAI().notifyEvent(CtrlEvent.EVT_AGGRESSION, attacker, Rnd.get(1, 100));
					}
					catch(Exception e)
					{
						e.printStackTrace();
					}
			}
		}

		super.onEvtAttacked(attacker, damage);
	}

	@Override
	protected void onEvtDead(Creature killer)
	{
		_penaltyMobsNotSpawned = true;
		if (killer != null)
		{
			final Player player = killer.getPlayer();
			if (player != null)
			{
				final NpcInstance actor = getActor();
				switch (actor.getNpcId())
				{
					case 22261: // Seychelles
						if (Rnd.chance(12))
							actor.dropItem(player, GREEN_CRYSTAL, 1);
						break;
					case 22265: // Chrysocolla
						if (Rnd.chance(6))
							actor.dropItem(player, RED_CRYSTAL, 1);
						break;
					case 22260: // Kleopora
						if (Rnd.chance(23))
							actor.dropItem(player, YELLOW_CRYSTAL, 1);
						break;
					case 22262: // Naiad
						if (Rnd.chance(12))
							actor.dropItem(player, GREEN_CRYSTAL, 1);
						break;
					case 22264: // Castalia
						if (Rnd.chance(12))
							actor.dropItem(player, GREEN_CRYSTAL, 1);
						break;
					case 22266: // Pythia
						if (Rnd.chance(5))
							actor.dropItem(player, RED_CRYSTAL, 1);
						break;
					case 22257: // Island Guardian
						if (Rnd.chance(21))
							actor.dropItem(player, YELLOW_CRYSTAL, 1);
						break;
					case 22258: // White Sand Mirage
						if (Rnd.chance(22))
							actor.dropItem(player, YELLOW_CRYSTAL, 1);
						break;
				}
			}
		}
		super.onEvtDead(killer);
	}
}