package l2r.gameserver.network.clientpackets;

public class RequestExCleftEnter extends L2GameClientPacket
{
	int unk;

	/**
	 * format: d
	 */
	@Override
	protected void readImpl()
	{
		unk = readD();
	}

	@Override
	protected void runImpl()
	{
		//TODO not implemented
	}
}