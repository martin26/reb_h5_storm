/**
 * 
 */
package l2r.gameserver.nexus_engine.events.engine;

import l2r.commons.dbutils.DbUtils;
import l2r.gameserver.nexus_engine.events.NexusLoader;
import l2r.gameserver.nexus_engine.l2r.CallBack;
import l2r.gameserver.nexus_interface.PlayerEventInfo;
import l2r.gameserver.nexus_interface.delegate.SkillData;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.logging.Level;

import javolution.text.TextBuilder;
import javolution.util.FastTable;
import javolution.util.FastMap;
import javolution.util.FastSet;

/**
 * @author hNoke
 *
 */
public class EventBuffer
{
	private Map<String, Map<Integer, Integer>> _availableBuffs;
	
	private FastMap<Integer, Map<String, List<Integer>>> _buffs;
	private Map<Integer, String> _activeSchemes;
	private Map<Integer, String> _activePetSchemes;
	
	private Map<Integer, List<String>> _modified;
	
	@SuppressWarnings("unused")
	private DataUpdater _dataUpdater;
	
	public EventBuffer()
	{
		_availableBuffs = new FastMap<String, Map<Integer, Integer>>();
		_buffs = new FastMap<Integer,  Map<String, List<Integer>>>();
		_modified = new FastMap<Integer, List<String>>();
		_activeSchemes = new FastMap<Integer, String>();
		_activePetSchemes = new FastMap<Integer, String>();
		
		_dataUpdater = new DataUpdater();
		
		loadAvailableBuffs(true);
	}
	
	public void reloadBuffer()
	{
		loadAvailableBuffs(false);
	}
	
	public void loadPlayer(PlayerEventInfo player)
	{
		loadData(player.getPlayersId());
	}
	
	public void buffPlayer(PlayerEventInfo player, boolean heal)
	{
		for(int buffId : getBuffs(player))
		{
			player.getSkillEffects(buffId, getLevelFor(buffId));
		}
		
		if(heal)
		{
			player.setCurrentHp(player.getMaxHp());
			player.setCurrentMp(player.getMaxMp());
			player.setCurrentCp(player.getMaxCp());
		}
	}
	
	public void buffPlayer(PlayerEventInfo player)
	{
		for(int buffId : getBuffs(player))
		{
			player.getSkillEffects(buffId, getLevelFor(buffId));
		}
		
		if(EventConfig.getInstance().getGlobalConfigBoolean("bufferHealsPlayer"))
		{
			player.setCurrentHp(player.getMaxHp());
			player.setCurrentMp(player.getMaxMp());
			player.setCurrentCp(player.getMaxCp());
		}
	}
	
	public void buffPet(PlayerEventInfo player)
	{
		if (player.hasPet())
		{
			if (getPlayersCurrentPetScheme(player.getPlayersId()) == null)
				return;
			
			for (int buffId : getBuffs(player, getPlayersCurrentPetScheme(player.getPlayersId())))
			{
				player.getPetSkillEffects(buffId, getLevelFor(buffId));
			}
		}
	}
	
	public void addModifiedBuffs(PlayerEventInfo player, String schemeName)
	{
		if (player == null || schemeName == null)
			return;
		
		if(!_modified.containsKey(player.getPlayersId()))
			_modified.put(player.getPlayersId(), new FastTable<String>());
		
		if(!_modified.get(player.getPlayersId()).contains(schemeName))
			_modified.get(player.getPlayersId()).add(schemeName);
	}
	
	public void addModifiedBuffs(int player, String schemeName)
	{
		if (schemeName == null)
			return;
		
		if(!_modified.containsKey(player))
			_modified.put(player, new FastTable<String>());
		
		if(!_modified.get(player).contains(schemeName))
			_modified.get(player).add(schemeName);
	}
	
	public boolean hasBuffs(PlayerEventInfo player)
	{
		try
		{
			return !_buffs.get(player.getPlayersId()).isEmpty();
		}
		catch (Exception e)
		{
			return false;
		}
	}
	
	public boolean addScheme(PlayerEventInfo player, String schemeName)
	{
		if(_buffs.get(player.getPlayersId()).containsKey(schemeName))
			return false;
		
		if(_buffs.get(player.getPlayersId()).size() >= 6)
		{
			if(player != null) player.sendMessage("You can't have more than 6 schemes.");
			return false;
		}
		
		_buffs.get(player.getPlayersId()).put(schemeName, new FastTable<Integer>());
		setPlayersCurrentScheme(player.getPlayersId(), schemeName);
		
		addModifiedBuffs(player, schemeName);
		return true;
	}
	
	public boolean removeScheme(PlayerEventInfo player, String schemeName)
	{
		if(!_buffs.get(player.getPlayersId()).containsKey(schemeName))
			return false;
		
		_buffs.get(player.getPlayersId()).remove(schemeName);
		
		if(schemeName.equals(getPlayersCurrentScheme(player.getPlayersId())))
			setPlayersCurrentScheme(player.getPlayersId(), getFirstScheme(player.getPlayersId()));
		
		addModifiedBuffs(player, schemeName);
		return true;
	}
	
	public String getPlayersCurrentScheme(int player)
	{
		String current = _activeSchemes.get(player);
		if(current == null)
			current = setPlayersCurrentScheme(player, getFirstScheme(player));
		
		return current;
	}
	
	public String getPlayersCurrentPetScheme(int player)
	{
		return _activePetSchemes.get(player);
	}
	
	public String setPlayersCurrentScheme(int player, String schemeName)
	{
		return setPlayersCurrentScheme(player, schemeName, true);
	}
	
	public String setPlayersCurrentScheme(int player, String schemeName, boolean updateInDb)
	{
		if(schemeName == null)
		{
			_activeSchemes.remove(player);
			return null;
		}
		
		if(!_buffs.get(player).containsKey(schemeName))
			_buffs.get(player).put(schemeName, new FastTable<Integer>());
		
		if(updateInDb)
		{
			if(_activeSchemes.containsKey(player))
				addModifiedBuffs(player, _activeSchemes.get(player));
			addModifiedBuffs(player, schemeName);
		}
		
		_activeSchemes.put(player, schemeName);
		
		return schemeName;
	}
	
	public String setPlayersCurrentPetScheme(int player, String schemeName)
	{
		if (schemeName == null)
		{
			_activePetSchemes.remove(player);
			return null;
		}
		
		if (!_buffs.get(player).containsKey(schemeName))
			_buffs.get(player).put(schemeName, new FastTable<Integer>());
		
		_activePetSchemes.put(player, schemeName);
		
		return schemeName;
	}
	
	public String getFirstScheme(int player)
	{
		if(_buffs.containsKey(player))
		{
			for(Entry<String, List<Integer>> e : _buffs.get(player).entrySet())
			{
				return e.getKey();
			}
		}
		
		return null;
	}
	
	public Set<Entry<String, List<Integer>>> getSchemes(PlayerEventInfo player)
	{
		if(_buffs.containsKey(player.getPlayersId()))
		{
			return _buffs.get(player.getPlayersId()).entrySet();
		}
		
		return new FastSet<Entry<String, List<Integer>>>();
	}
	
	public boolean addBuff(int buffId, PlayerEventInfo player)
	{
		String scheme = getPlayersCurrentScheme(player.getPlayersId());
		if(scheme == null)
			return false;
		
		if(!_buffs.get(player.getPlayersId()).containsKey(scheme))
			return false;
		
		if(_buffs.get(player.getPlayersId()).get(scheme).contains(buffId))
			return false;
		
		_buffs.get(player.getPlayersId()).get(scheme).add(buffId);
		addModifiedBuffs(player, scheme);
		return true;
	}
	
	public void removeBuff(int buffId, PlayerEventInfo player)
	{
		String scheme = getPlayersCurrentScheme(player.getPlayersId());
		if(scheme == null)
			return;
		
		_buffs.get(player.getPlayersId()).get(scheme).remove(new Integer(buffId));
		addModifiedBuffs(player, scheme);
	}
	
	public boolean containsSkill(int buffId, PlayerEventInfo player)
	{
		String scheme = getPlayersCurrentScheme(player.getPlayersId());
		if(scheme == null)
			return false;
		
		return _buffs.get(player.getPlayersId()).get(scheme).contains(buffId);
	}
	
	public List<Integer> getBuffs(PlayerEventInfo player)
	{
		String scheme = getPlayersCurrentScheme(player.getPlayersId());
		if(scheme == null)
			return new FastTable<Integer>();
		
		return _buffs.get(player.getPlayersId()).get(scheme);
	}
	
	public List<Integer> getBuffs(PlayerEventInfo player, String scheme)
	{
		return _buffs.get(player.getPlayersId()).get(scheme);
	}
	
	private void loadData(int playerId)
	{
		synchronized(_buffs)
		{
			_buffs.put(playerId, new FastMap<String, List<Integer>>());
			
			Connection con = null;
			PreparedStatement statement = null;
			ResultSet rset = null;
			try
			{
				con = CallBack.getInstance().getOut().getConnection();
				statement = con.prepareStatement("SELECT * FROM nexus_playerbuffs WHERE playerId = " + playerId);
				rset = statement.executeQuery();
				while (rset.next())
				{
					String scheme = rset.getString("scheme");
					int active = rset.getInt("active");
					
					_buffs.get(playerId).put(scheme, new FastTable<Integer>());
					
					for(String buffId : rset.getString("buffs").split(","))
					{
						try
						{
							_buffs.get(playerId).get(scheme).add(Integer.parseInt(buffId));
						}
						catch (Exception e) { continue; }
					}
					
					if(active == 1)
						setPlayersCurrentScheme(playerId, scheme, false);
				}
			}
			catch (Exception e)
			{
				e.printStackTrace();
			}
			finally
			{
				DbUtils.closeQuietly(con, statement, rset);
			}
		}
	}
	
	private synchronized void storeData()
	{
		if(_modified.isEmpty())
			return;
		
		Connection con = null;
		PreparedStatement statement = null;
		PreparedStatement statement2 = null;
		
		String buffs;

		try
		{
			TextBuilder tb;
			con = CallBack.getInstance().getOut().getConnection();

			for (Entry<Integer, List<String>> modified : _modified.entrySet())
			{
				
				for(String modifiedScheme : modified.getValue())
				{
					statement = con.prepareStatement("DELETE FROM nexus_playerbuffs WHERE playerId = " + modified.getKey() + " AND scheme = '" + modifiedScheme + "'");
					statement.execute();
					
					if(_buffs.get(modified.getKey()).containsKey(modifiedScheme))
					{
						tb = new TextBuilder();
						
						for(int buffId : _buffs.get(modified.getKey()).get(modifiedScheme))
						{
							tb.append(buffId + ",");
						}
						
						buffs = tb.toString();
						if(buffs.length() > 0)
							buffs = buffs.substring(0, buffs.length() - 1);
						
						statement2 = con.prepareStatement("REPLACE INTO nexus_playerbuffs VALUES (?,?,?,?)");
						statement2.setInt(1, modified.getKey());
						statement2.setString(2, modifiedScheme);
						statement2.setString(3, buffs);
						statement2.setInt(4, modifiedScheme.equals(getPlayersCurrentScheme(modified.getKey())) ? 1 : 0);

						statement2.executeUpdate();
					}
				}
			}
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
		finally
		{
			DbUtils.closeQuietly(con, statement);
			DbUtils.closeQuietly(statement2);
		}
		
		_modified.clear();
	}
	
	public Map<String, Map<Integer, Integer>> getAvailableBuffs()
	{
		return _availableBuffs;
	}
	
	public int getLevelFor(int skillId)
	{
		for(Map<Integer, Integer> e : _availableBuffs.values())
		{
			for(Entry<Integer, Integer> entry : e.entrySet())
			{
				if(entry.getKey() == skillId)
					return entry.getValue();
			}
		}
		return -1;
	}
	
	private void loadAvailableBuffs(boolean test)
	{
		if(!_availableBuffs.isEmpty())
			_availableBuffs.clear();
		
		String category;
		int buffId, level;
		int count = 0;
		String name;
		
		Connection con = null;
		PreparedStatement statement = null;
		PreparedStatement statement2 = null;
		ResultSet rset = null;
		try
		{
			con = CallBack.getInstance().getOut().getConnection();
			statement = con.prepareStatement("SELECT * FROM nexus_buffs");
			rset = statement.executeQuery();
			
			while (rset.next())
			{
				category = rset.getString("category");
				buffId = rset.getInt("buffId");
				level = rset.getInt("level");
				
				if(test)
				{
					name = rset.getString("name");
					if(name == null || name.length() == 0)
					{
						try
						{
							name = new SkillData(buffId, level).getName();
							if(name != null)
							{
								statement2 = con.prepareStatement("UPDATE nexus_buffs SET name = '" + name + "' WHERE buffId = " + buffId + " AND level = " + level + "");
								statement2.execute();
							}
						}
						catch (Exception e)
						{
						}
					}
				}
				
				if(!_availableBuffs.containsKey(category))
					_availableBuffs.put(category, new FastMap<Integer, Integer>());
				
				_availableBuffs.get(category).put(buffId, level);
				
				count ++;
			}
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
		finally
		{
			DbUtils.closeQuietly(con, statement, rset);
			DbUtils.closeQuietly(statement2);
		}
		
		NexusLoader.debug("Loaded " + count + " buffs for Event Buffer.", Level.INFO);
	}
	
	private class DataUpdater implements Runnable
	{
		private DataUpdater()
		{
//			CallBack.getInstance().getOut().scheduleGeneralAtFixedRate(this, 600000, 600000);
			CallBack.getInstance().getOut().scheduleGeneralAtFixedRate(this, 10000, 10000);
		}
		
		@Override
		public void run()
		{
			storeData();
		}
	}
	
	public static final EventBuffer getInstance()
	{
		return SingletonHolder._instance;
	}
	
	@SuppressWarnings("synthetic-access")
	private static class SingletonHolder
	{
		protected static final EventBuffer _instance = new EventBuffer();
	}
}
