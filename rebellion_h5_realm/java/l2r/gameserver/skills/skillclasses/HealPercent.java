package l2r.gameserver.skills.skillclasses;

import l2r.gameserver.model.Creature;
import l2r.gameserver.model.Skill;
import l2r.gameserver.model.instances.residences.SiegeFlagInstance;
import l2r.gameserver.network.serverpackets.SystemMessage2;
import l2r.gameserver.network.serverpackets.components.SystemMsg;
import l2r.gameserver.nexus_engine.events.engine.EventManager;
import l2r.gameserver.nexus_interface.NexusEvents;
import l2r.gameserver.stats.Stats;
import l2r.gameserver.templates.StatsSet;

import java.util.List;

public class HealPercent extends Skill
{
	private final boolean _ignoreHpEff;

	public HealPercent(StatsSet set)
	{
		super(set);
		_ignoreHpEff = set.getBool("ignoreHpEff", true);
	}

	@Override
	public boolean checkCondition(Creature activeChar, Creature target, boolean forceUse, boolean dontMove, boolean first)
	{
		if((activeChar.isPlayable() && target.isMonster()) || target.isDoor() || target instanceof SiegeFlagInstance)
			return false;
		return super.checkCondition(activeChar, target, forceUse, dontMove, first);
	}

	@Override
	public void useSkill(Creature activeChar, List<Creature> targets)
	{
		for(Creature target : targets)
			if(target != null)
			{
				if(target.isDead() || target.isHealBlocked() && target != activeChar.getPet())
					continue;

				getEffects(activeChar, target, getActivateRate() > 0, false);

				double hp = _power * target.getMaxHp() / 100.;
				double newHp = hp * (!_ignoreHpEff ? target.calcStat(Stats.HEAL_EFFECTIVNESS, 100., activeChar, this) : 100.) / 100.;
				double addToHp = Math.max(0, Math.min(newHp, target.calcStat(Stats.HP_LIMIT, null, null) * target.getMaxHp() / 100. - target.getCurrentHp()));

				if(addToHp > 0)
				{
					target.setCurrentHp(addToHp + target.getCurrentHp(), false);
					
					// Nexus support for healing classes.
					if (activeChar != target && NexusEvents.isInEvent(target) && NexusEvents.isInEvent(activeChar))
					{
						if (EventManager.getInstance().getCurrentMainEvent().getBoolean("allowHealers"))
						{
							if (activeChar.getPlayer() != null && target.getPlayer() != null && activeChar.getPlayer().getEventInfo() != null && activeChar.getPlayer().getEventInfo().isPriest())
							{
								int currentHealAmount = (int) (activeChar.getPlayer().getEventInfo().getHealAmount() + addToHp);
								activeChar.getPlayer().getEventInfo().setHealAmount(currentHealAmount);
							}
						}
					}
				}
				
				if(target.isPlayer())
				{
					if(activeChar != target)
						target.sendPacket(new SystemMessage2(SystemMsg.S2_HP_HAS_BEEN_RESTORED_BY_C1).addString(activeChar.getName()).addInteger(Math.round(addToHp)));
					else
						activeChar.sendPacket(new SystemMessage2(SystemMsg.S1_HP_HAS_BEEN_RESTORED).addInteger(Math.round(addToHp)));
				}
			}

		if(isSSPossible())
			activeChar.unChargeShots(isMagic());
	}
}