package l2r.gameserver.model.instances;

import l2r.gameserver.model.Creature;
import l2r.gameserver.model.Player;
import l2r.gameserver.model.Skill;
import l2r.gameserver.model.entity.events.impl.DominionSiegeEvent;
import l2r.gameserver.model.entity.events.objects.TerritoryWardObject;
import l2r.gameserver.model.pledge.Clan;
import l2r.gameserver.templates.npc.NpcTemplate;

public class TerritoryWardInstance extends NpcInstance
{
	private final TerritoryWardObject _territoryWard;

	public TerritoryWardInstance(int objectId, NpcTemplate template, TerritoryWardObject territoryWardObject)
	{
		super(objectId, template);
		setHasChatWindow(false);
		_territoryWard = territoryWardObject;
	}

	@Override
	public void reduceCurrentHp(double damage, Creature attacker, Skill skill, boolean awake, boolean standUp, boolean directHp, boolean canReflect, boolean transferDamage, boolean isDot, boolean sendMessage)
	{
		if (skill != null)
			return;
		
		super.reduceCurrentHp(damage, attacker, skill, awake, standUp, directHp, canReflect, transferDamage, isDot, sendMessage);
	}
	
	@Override
	public void onDeath(Creature killer)
	{
		super.onDeath(killer);
		Player player = killer.getPlayer();
		if(player == null)
			return;

		if(_territoryWard.canPickUp(player))
		{
			_territoryWard.pickUp(player);
			decayMe();
		}
	}

	@Override
	protected void onDecay()
	{
		decayMe();

		_spawnAnimation = 2;
	}

	@Override
	public boolean isAttackable(Creature attacker)
	{
		return isAutoAttackable(attacker);
	}

	@Override
	public boolean isAutoAttackable(Creature attacker)
	{
		if (attacker.getPlayer() == null)
			return false;
		
		if (attacker.getPlayer().isDead() || attacker.getPlayer().isInZonePeace() || attacker.getPlayer().isMounted() || attacker.getPlayer().getActiveWeaponFlagAttachment() != null)
			return false;
		
		DominionSiegeEvent siegeEvent = getEvent(DominionSiegeEvent.class);
		if(siegeEvent == null)
			return false;
		DominionSiegeEvent siegeEvent2 = attacker.getEvent(DominionSiegeEvent.class);
		if(siegeEvent2 == null)
			return false;
		if(siegeEvent == siegeEvent2)
			return false;
		if(siegeEvent2.getResidence().getOwner() != attacker.getClan())
			return false;
		return true;
	}

	@Override
	public boolean isInvul()
	{
		return false;
	}

	@Override
	public Clan getClan()
	{
		return null;
	}
}
