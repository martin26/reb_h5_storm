package l2r.gameserver.network.serverpackets;

import l2r.gameserver.model.Player;
import l2r.gameserver.model.base.Element;
import l2r.gameserver.model.items.ItemInstance;
import l2r.gameserver.templates.item.ItemTemplate;

import java.util.ArrayList;
import java.util.List;


/**
 * @author SYS
 */
public class ExShowBaseAttributeCancelWindow extends L2GameServerPacket
{
	private final List<ItemInstance> _items = new ArrayList<ItemInstance>();

	public ExShowBaseAttributeCancelWindow(Player activeChar)
	{
		for(ItemInstance item : activeChar.getInventory().getItems())
		{
			if(item.getAttributeElement() == Element.NONE || !item.canBeEnchanted(true) || getAttributeRemovePrice(item) == 0)
				continue;
			_items.add(item);
		}
	}

	@Override
	protected final void writeImpl()
	{
		writeEx(0x74);
		writeD(_items.size());
		for(ItemInstance item : _items)
		{
			writeD(item.getObjectId());
			writeQ(getAttributeRemovePrice(item));
		}
	}

	public static long getAttributeRemovePrice(ItemInstance item)
	{
		switch(item.getCrystalType())
		{
			case S:
				return item.getTemplate().getType2() == ItemTemplate.TYPE2_WEAPON ? 50000 : 40000;
			case S80:
				return item.getTemplate().getType2() == ItemTemplate.TYPE2_WEAPON ? 100000 : 80000;
			case S84:
				return item.getTemplate().getType2() == ItemTemplate.TYPE2_WEAPON ? 200000 : 160000;
		}
		return 0;
	}
}