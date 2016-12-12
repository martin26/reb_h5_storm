package l2r.loginserver.gameservercon.gspackets;

import l2r.loginserver.accounts.SessionManager;
import l2r.loginserver.gameservercon.GameServer;
import l2r.loginserver.gameservercon.ReceivablePacket;

import org.apache.commons.lang3.ArrayUtils;

/**
 * @author VISTALL
 * @date 20:52/25.03.2011
 */
public class SetAccountInfo extends ReceivablePacket
{
	private String _account;
	private int _size;
	private int[] _deleteChars;

	@Override
	protected void readImpl()
	{
		_account = readS();
		_size = readC();
		int size = readD();
		if(size > 7 || size <= 0)
			_deleteChars = ArrayUtils.EMPTY_INT_ARRAY;
		else
		{
			_deleteChars = new int[size];
			for(int i = 0; i < _deleteChars.length; i++)
				_deleteChars[i] = readD();
		}
	}

	@Override
	protected void runImpl()
	{
		GameServer gs = getGameServer();
		if(gs.isAuthed())
		{
			SessionManager.Session session= SessionManager.getInstance().getSessionByName(_account);
			if(session == null)
				return;
			session.getAccount().addAccountInfo(gs.getIds()[0], _size, _deleteChars);
		}
	}
}
