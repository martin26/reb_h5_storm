package l2r.gameserver.stats.conditions;

import l2r.gameserver.model.Creature;
import l2r.gameserver.stats.Env;

public class ConditionTargetPlayer extends Condition
{
	private final boolean _flag;

	public ConditionTargetPlayer(boolean flag)
	{
		_flag = flag;
	}

	@Override
	protected boolean testImpl(Env env)
	{
		Creature target = env.target;
		return target != null && target.isPlayer() == _flag;
	}
}
