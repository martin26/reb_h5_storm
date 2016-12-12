package l2r.gameserver.network.telnet;

import java.util.Set;

public interface TelnetCommandHolder
{
	/**
	 * Get handler commands
	 * @return
	 */
	public Set<TelnetCommand> getCommands();

}
