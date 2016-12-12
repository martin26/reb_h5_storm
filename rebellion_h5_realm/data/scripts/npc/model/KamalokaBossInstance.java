package npc.model;

import l2r.gameserver.ThreadPoolManager;
import l2r.gameserver.model.Creature;
import l2r.gameserver.model.Player;
import l2r.gameserver.model.World;
import l2r.gameserver.network.serverpackets.SystemMessage;
import l2r.gameserver.templates.npc.NpcTemplate;
import l2r.gameserver.utils.Util;

import java.util.concurrent.ScheduledFuture;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class KamalokaBossInstance extends LostCaptainInstance
{

	private static final Logger _log = LoggerFactory.getLogger(KamalokaBossInstance.class);

	private ScheduledFuture<?> _manaRegen;

	public KamalokaBossInstance(int objectId, NpcTemplate template)
	{
		super(objectId, template);
		_manaRegen = ThreadPoolManager.getInstance().scheduleAtFixedRate(new ManaRegen(), 20000, 20000);
	}

	@Override
	public boolean isRaid()
	{
		return false;
	}

	@Override
	protected void onDeath(Creature killer)
	{
		if(_manaRegen != null)
		{
			_manaRegen.cancel(false);
			_manaRegen = null;
		}

		super.onDeath(killer);
	}

	private class ManaRegen implements Runnable
	{
		@Override
		public void run()
		{
			try
			{
				for(Player p : World.getAroundPlayers(KamalokaBossInstance.this))
				{
					if(p.isHealBlocked())
						continue;
					int addMp = getAddMp();
					if(addMp <= 0)
						return;
					double newMp = Util.constrain(p.getMaxMp() - p.getCurrentMp(), 0, addMp);
					if(newMp > 0)
						p.setCurrentMp(newMp + p.getCurrentMp());
					p.sendPacket(new SystemMessage(SystemMessage.S1_MPS_HAVE_BEEN_RESTORED).addNumber(Math.round(newMp)));
				}
			}
			catch(Exception e)
			{
				_log.error("", e);
			}
		}

		private int getAddMp()
		{
			switch(getLevel())
			{
				case 23:
				case 26:
					return 6;
				case 33:
				case 36:
					return 10;
				case 43:
				case 46:
					return 13;
				case 53:
				case 56:
					return 16; // С потолка
				case 63:
				case 66:
					return 19; // С потолка
				case 73:
					return 22; // С потолка
				default:
					return 0;
			}
		}
	}
}