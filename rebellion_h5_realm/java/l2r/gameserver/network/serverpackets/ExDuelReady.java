package l2r.gameserver.network.serverpackets;

import l2r.gameserver.model.entity.events.impl.DuelEvent;

public class ExDuelReady extends L2GameServerPacket
{
	private int _duelType;

	public ExDuelReady(DuelEvent event)
	{
		_duelType = event.getDuelType();
	}

	@Override
	protected final void writeImpl()
	{
		writeEx(0x4d);
		writeD(_duelType);
	}
}