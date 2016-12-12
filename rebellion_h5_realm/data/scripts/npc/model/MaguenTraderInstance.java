package npc.model;

import l2r.gameserver.model.Player;
import l2r.gameserver.model.instances.NpcInstance;
import l2r.gameserver.scripts.Functions;
import l2r.gameserver.templates.npc.NpcTemplate;
import l2r.gameserver.tables.SpawnTable;

/**
 * @author pchayka
 */
public final class MaguenTraderInstance extends NpcInstance
{
	public MaguenTraderInstance(int objectId, NpcTemplate template)
	{
		super(objectId, template);
	}

	@Override
	public void onBypassFeedback(Player player, String command)
	{
		if(!canBypassCheck(player, this))
			return;

		if(command.equalsIgnoreCase("request_collector"))
		{
			if(Functions.getItemCount(player, 15487) > 0)
				showChatWindow(player, "default/32735-2.htm");
			else
				Functions.addItem(player, 15487, 1);
		}
		else if(command.equalsIgnoreCase("request_maguen"))
		{
			SpawnTable.spawnSingle(18839, getSpawnedLoc().findPointToStay(40, 100), getReflection()); // wild maguen
			showChatWindow(player, "default/32735-3.htm");
		}
		else
			super.onBypassFeedback(player, command);
	}
}