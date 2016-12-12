package l2r.gameserver;

import l2r.commons.threading.RunnableImpl;
import l2r.gameserver.model.Creature;
import l2r.gameserver.model.GameObjectsStorage;
import l2r.gameserver.model.Player;
import l2r.gameserver.model.World;
import l2r.gameserver.network.serverpackets.ExShowScreenMessage;
import l2r.gameserver.network.serverpackets.ExShowScreenMessage.ScreenMessageAlign;
import l2r.gameserver.network.serverpackets.Say2;
import l2r.gameserver.network.serverpackets.SystemMessage2;
import l2r.gameserver.network.serverpackets.components.ChatType;
import l2r.gameserver.network.serverpackets.components.CustomMessage;

import java.io.File;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.StringTokenizer;
import java.util.concurrent.Future;

import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Announcements
{
	public class Announce extends RunnableImpl
	{
		private Future<?> _task;
		private final int _time;
		private final String _announce;

		public Announce(int t, String announce)
		{
			_time = t;
			_announce = announce;
		}

		@Override
		public void runImpl() throws Exception
		{
			announceToAll(_announce);
		}

		public void showAnnounce(Player player)
		{
			Say2 cs = new Say2(0, ChatType.ANNOUNCEMENT, player.getName(), _announce);
			player.sendPacket(cs);
		}

		public void start()
		{
			if(_time > 0)
				_task = ThreadPoolManager.getInstance().scheduleAtFixedRate(this, _time * 1000L, _time * 1000L);
		}

		public void stop()
		{
			if(_task != null)
			{
				_task.cancel(false);
				_task = null;
			}
		}

		public int getTime()
		{
			return _time;
		}

		public String getAnnounce()
		{
			return _announce;
		}
	}

	private static final Logger _log = LoggerFactory.getLogger(Announcements.class);

	private static final Announcements _instance = new Announcements();

	public static final Announcements getInstance()
	{
		return _instance;
	}

	private List<Announce> _announcements = new ArrayList<Announce>();

	private Announcements()
	{
		loadAnnouncements();
	}

	public List<Announce> getAnnouncements()
	{
		return _announcements;
	}

	public void loadAnnouncements()
	{
		_announcements.clear();
		File file = Config.getFile("config/announcements.txt");
		try
		{
			List<String> lines =  Arrays.asList(FileUtils.readFileToString(file, "UTF-8").split("\n"));
			for(String line : lines)
			{
				StringTokenizer token = new StringTokenizer(line, "\t");
				if(token.countTokens() > 1)
					addAnnouncement(Integer.parseInt(token.nextToken()), token.nextToken(), false);
				else
					addAnnouncement(0, line, false);
			}
		}
		catch(Exception e)
		{
			_log.error("Error while loading Announcements", e);
		}
	}

	public void showAnnouncements(Player activeChar)
	{
		for(Announce announce : _announcements)
			announce.showAnnounce(activeChar);
	}

	public void addAnnouncement(int val, String text, boolean save)
	{
		Announce announce = new Announce(val, text);
		announce.start();

		_announcements.add(announce);
		if(save)
			saveToDisk();
	}

	public void delAnnouncement(int line)
	{
		Announce announce = _announcements.remove(line);
		if(announce != null)
			announce.stop();

		saveToDisk();
	}

	private void saveToDisk()
	{
		File file = Config.getFile("config/announcements.txt");
		try
		{
			FileWriter writer = new FileWriter(file, false);
			for(Announce announce : _announcements)
				writer.write(announce.getTime() + "\t" + announce.getAnnounce() + "\n");
			writer.close();
		}
		catch(Exception e)
		{
			_log.error("Error while saving Announcements", e);
		}
	}

	public void announceToAll(String text)
	{
		announceToAll(text, ChatType.ANNOUNCEMENT);
	}

	public static void shout(Creature activeChar, String text, ChatType type)
	{
		Say2 cs = new Say2(activeChar.getObjectId(), type, activeChar.getName(), text);

		int rx = World.regionX(activeChar);
		int ry = World.regionY(activeChar);
		int offset = Config.SHOUT_OFFSET;

		for(Player player : GameObjectsStorage.getAllPlayersForIterate())
		{
			if(player == activeChar || activeChar.getReflection() != player.getReflection())
				continue;

			int tx = World.regionX(player);
			int ty = World.regionY(player);

			if(tx >= rx - offset && tx <= rx + offset && ty >= ry - offset && ty <= ry + offset || activeChar.isInRangeZ(player, Config.CHAT_RANGE))
				player.sendPacket(cs);
		}

		activeChar.sendPacket(cs);
	}

	public void announceToAll(String text, ChatType type)
	{
		Say2 cs = new Say2(0, type, "", text);
		for(Player player : GameObjectsStorage.getAllPlayersForIterate())
			player.sendPacket(cs);
	}

	public void announceToAll(String[] texts, ChatType type)
	{
		Say2 csEng = new Say2(0, type, "", texts[0]);
		Say2 csRu = new Say2(0, type, "", texts[1]);
		for (Player player : GameObjectsStorage.getAllPlayersForIterate())
		{
			if (player.isLangEng())
				player.sendPacket(csEng);
			else
				player.sendPacket(csRu);
		}
	}
	
	/**
	 * Отправляет анонсом CustomMessage, приминимо к примеру в шатдауне.
	 * @param address адрес в {@link l2r.gameserver.network.serverpackets.components.CustomMessage}
	 * @param replacements массив String-ов которые атоматически добавятся в сообщения
	 */
	public void announceByCustomMessage(String address, String[] replacements)
	{
		for(Player player : GameObjectsStorage.getAllPlayersForIterate())
			announceToPlayerByCustomMessage(player, address, replacements);
	}

	public void announceByCustomMessage(String address, String[] replacements, ChatType type)
	{
		for(Player player : GameObjectsStorage.getAllPlayersForIterate())
			announceToPlayerByCustomMessage(player, address, replacements, type);
	}

	public void announceByCustomMessage(String address, String[] replacements, ScreenMessageAlign align, int time, boolean bigfont)
	{
		for(Player player : GameObjectsStorage.getAllPlayersForIterate())
			announceToPlayerByCustomMessage(player, address, replacements, align, time, bigfont);
	}
	
	public void announceToPlayerByCustomMessage(Player player, String address, String[] replacements)
	{
		CustomMessage cm = new CustomMessage(address, player);
		if(replacements != null)
			for(String s : replacements)
				cm.addString(s);
		player.sendPacket(new Say2(0, ChatType.ANNOUNCEMENT, "", cm.toString()));
	}

	public void announceToPlayerByCustomMessage(Player player, String address, String[] replacements, ChatType type)
	{
		CustomMessage cm = new CustomMessage(address, player);
		if(replacements != null)
			for(String s : replacements)
				cm.addString(s);
		player.sendPacket(new Say2(0, type, "", cm.toString()));
	}
	
	public void announceToPlayerByCustomMessage(Player player, String address, String[] replacements, ScreenMessageAlign align, int time, boolean bigfont)
	{
		CustomMessage cm = new CustomMessage(address, player);
		if(replacements != null)
			for(String s : replacements)
				cm.addString(s);
		
		ExShowScreenMessage sm = new ExShowScreenMessage(cm.toString(), time, align, bigfont);
		
		player.sendPacket(sm);
	}

	public void announceToAll(SystemMessage2 sm)
	{
		for(Player player : GameObjectsStorage.getAllPlayersForIterate())
			player.sendPacket(sm);
	}
	
	public void announceToAll(ChatType type, String message)
	{
		for(Player player : GameObjectsStorage.getAllPlayersForIterate())
			player.sendChatMessage(0, type.ordinal(), "", message);
	}
	
	public void announceToAll(ChatType type, String name, String message)
	{
		for(Player player : GameObjectsStorage.getAllPlayersForIterate())
			player.sendChatMessage(0, type.ordinal(), name, message);
	}
	
	public void announceToAllSysMsg(String message)
	{
		for(Player player : GameObjectsStorage.getAllPlayersForIterate())
			player.sendMessage(message);
	}
}