package l2r.gameserver.model.instances;

import l2r.gameserver.Announcements;
import l2r.gameserver.Config;
import l2r.gameserver.cache.Msg;
import l2r.gameserver.instancemanager.CoupleManager;
import l2r.gameserver.model.GameObjectsStorage;
import l2r.gameserver.model.Player;
import l2r.gameserver.model.entity.Couple;
import l2r.gameserver.model.items.Inventory;
import l2r.gameserver.network.serverpackets.InventoryUpdate;
import l2r.gameserver.network.serverpackets.MagicSkillUse;
import l2r.gameserver.network.serverpackets.NpcHtmlMessage;
import l2r.gameserver.network.serverpackets.components.CustomMessage;
import l2r.gameserver.templates.item.ItemTemplate;
import l2r.gameserver.templates.npc.NpcTemplate;

public class WeddingManagerInstance extends NpcInstance
{
	public WeddingManagerInstance(int objectId, NpcTemplate template)
	{
		super(objectId, template);
	}

	@Override
	public void showChatWindow(Player player, int val, Object... arg)
	{
		String filename = "wedding/start.htm";
		String replace = "";
		NpcHtmlMessage html = new NpcHtmlMessage(player, this);
		html.setFile(filename);
		html.replace("%replace%", replace);
		html.replace("%npcname%", getName());
		player.sendPacket(html);
	}

	@Override
	public void onBypassFeedback(Player player, String command)
	{
		if(!canBypassCheck(player, this))
			return;

		// standard msg
		String filename = "wedding/start.htm";
		String replace = "";

		// if player has no partner
		if(player.getPartnerId() == 0)
		{
			filename = "wedding/nopartner.htm";
			sendHtmlMessage(player, filename, replace);
			return;
		}

		Player ptarget = GameObjectsStorage.getPlayer(player.getPartnerId());

		// partner online ?
		if(ptarget == null || !ptarget.isOnline())
		{
			filename = "wedding/notfound.htm";
			sendHtmlMessage(player, filename, replace);
			return;
		}
		else if(player.isMaried()) // already married ?
		{
			filename = "wedding/already.htm";
			sendHtmlMessage(player, filename, replace);
			return;
		}
		else if(command.startsWith("AcceptWedding"))
		{
			// accept the wedding request
			player.setMaryAccepted(true);
			Couple couple = CoupleManager.getInstance().getCouple(player.getCoupleId());
			couple.marry();
			
			// messages to the couple
			player.sendMessage(new CustomMessage("l2r.gameserver.model.instances.L2WeddingManagerMessage", player));
			player.setMaried(true);
			player.setMaryRequest(false);
			
			ptarget.sendMessage(new CustomMessage("l2r.gameserver.model.instances.L2WeddingManagerMessage", ptarget));
			ptarget.setMaried(true);
			ptarget.setMaryRequest(false);
			
			if (Config.ON_WEDDING_GIVE_GIFT)
			{
				InventoryUpdate iu = new InventoryUpdate();
				InventoryUpdate iu2 = new InventoryUpdate();
				
				iu.addNewItem(player.getInventory().addItem(3144, 1)); // Moonflower Charm
				iu.addNewItem(player.getInventory().addItem(9140, 1)); // Salvation Bow
				iu2.addNewItem(ptarget.getInventory().addItem(3144, 1)); // Moonflower Charm
				iu2.addNewItem(ptarget.getInventory().addItem(9140, 1)); // Salvation Bow
				
				player.sendPacket(iu);
				ptarget.sendPacket(iu2);
			}
			
			//fireworks
			broadcastPacketToOthers(new MagicSkillUse(this, this, 2025, 1, 0, 0));
			
			// wedding march
			player.broadcastPacket(new MagicSkillUse(player, player, 2230, 1, 1, 0));
			ptarget.broadcastPacket(new MagicSkillUse(ptarget, ptarget, 2230, 1, 1, 0));
			
			if (Config.ANNOUNCE_WEDDING)
				Announcements.getInstance().announceByCustomMessage("l2r.gameserver.model.instances.L2WeddingManagerMessage.announce", new String[] { player.getName(), ptarget.getName() });
			
			if (Config.ENABLE_PLAYER_COUNTERS)
			{
				player.getCounters().addPoint("_Get_Married");
				ptarget.getCounters().addPoint("_Get_Married");
			}
			
			filename = "wedding/accepted.htm";
			replace = ptarget.getName();
			sendHtmlMessage(ptarget, filename, replace);
			return;
		}
		else if(player.isMaryRequest())
		{
			// check for formalwear
			if(Config.WEDDING_FORMALWEAR && !isWearingFormalWear(player))
			{
				filename = "wedding/noformal.htm";
				sendHtmlMessage(player, filename, replace);
				return;
			}
			filename = "wedding/ask.htm";
			player.setMaryRequest(false);
			ptarget.setMaryRequest(false);
			replace = ptarget.getName();
			sendHtmlMessage(player, filename, replace);
			return;
		}
		else if(command.startsWith("AskWedding"))
		{
			// check for formalwear
			if(Config.WEDDING_FORMALWEAR && !isWearingFormalWear(player))
			{
				filename = "wedding/noformal.htm";
				sendHtmlMessage(player, filename, replace);
				return;
			}
			else if(player.getAdena() < Config.WEDDING_PRICE)
			{
				player.sendPacket(Msg.YOU_DO_NOT_HAVE_ENOUGH_ADENA);
				return;
			}
			else
			{
				player.setMaryAccepted(true);
				ptarget.setMaryRequest(true);
				replace = ptarget.getName();
				filename = "wedding/requested.htm";
				player.reduceAdena(Config.WEDDING_PRICE, true);
				sendHtmlMessage(player, filename, replace);
				return;
			}
		}
		else if(command.startsWith("DeclineWedding"))
		{
			player.setMaryRequest(false);
			ptarget.setMaryRequest(false);
			player.setMaryAccepted(false);
			ptarget.setMaryAccepted(false);
			player.sendMessage(new CustomMessage("l2r.gameserver.model.instances.WeddingManagerInstance.message1", player));
			ptarget.sendMessage(new CustomMessage("l2r.gameserver.model.instances.WeddingManagerInstance.message2", player));
			replace = ptarget.getName();
			filename = "wedding/declined.htm";
			sendHtmlMessage(ptarget, filename, replace);
			return;
		}
		else if(player.isMaryAccepted())
		{
			filename = "wedding/waitforpartner.htm";
			sendHtmlMessage(player, filename, replace);
			return;
		}
		sendHtmlMessage(player, filename, replace);
	}

	private static boolean isWearingFormalWear(Player player)
	{
		if(player != null && player.getInventory() != null && player.getInventory().getPaperdollItemId(Inventory.PAPERDOLL_CHEST) == ItemTemplate.ITEM_ID_FORMAL_WEAR)
			return true;
		return false;
	}

	private void sendHtmlMessage(Player player, String filename, String replace)
	{
		NpcHtmlMessage html = new NpcHtmlMessage(player, this);
		html.setFile(filename);
		html.replace("%replace%", replace);
		html.replace("%npcname%", getName());
		player.sendPacket(html);
	}
}