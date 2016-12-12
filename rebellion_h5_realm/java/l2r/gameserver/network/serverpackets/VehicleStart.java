package l2r.gameserver.network.serverpackets;

import l2r.gameserver.model.entity.boat.Boat;

public class VehicleStart extends L2GameServerPacket
{
	private int _objectId, _state;

	public VehicleStart(Boat boat)
	{
		_objectId = boat.getObjectId();
		_state = boat.getRunState();
	}

	@Override
	protected void writeImpl()
	{
		writeC(0xC0);
		writeD(_objectId);
		writeD(_state);
	}
}