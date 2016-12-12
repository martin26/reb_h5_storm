package l2r.gameserver.model.items;

import l2r.gameserver.Config;

public final class TradeItem extends ItemInfo
{
	private long _price;
	private long _referencePrice;
	private long _currentValue;
	private int _lastRechargeTime;
	private int _rechargeTime;

	public TradeItem()
	{
		super();
	}

	public TradeItem(ItemInstance item)
	{
		super(item);
		setReferencePrice(item.getReferencePrice());
		
		// Custom itemLevel - nullfy its level.
		item.setItemLevel(0);
	}

	public void setOwnersPrice(long price)
	{
		_price = price;
	}

	public long getOwnersPrice()
	{
		return _price;
	}

	public void setReferencePrice(long price)
	{
		_referencePrice = price;
	}

	public long getReferencePrice()
	{
		return _referencePrice;
	}

	public long getStorePrice()
	{
		return getReferencePrice() / Config.ALT_SELL_PRICE_DIV;
	}

	public void setCurrentValue(long value)
	{
		_currentValue = value;
	}

	public long getCurrentValue()
	{
		return _currentValue;
	}

	/**
	 * Устанавливает время респауна предмета, используется в NPC магазинах с ограниченным количеством.
	 * @param rechargeTime : unixtime в минутах
	 */
	public void setRechargeTime(int rechargeTime)
	{
		_rechargeTime = rechargeTime;
	}

	/**
	 * Возвращает время респауна предмета, используется в NPC магазинах с ограниченным количеством.
	 * @return unixtime в минутах
	 */
	public int getRechargeTime()
	{
		return _rechargeTime;
	}

	/**
	 * Возвращает ограничен ли этот предмет в количестве, используется в NPC магазинах с ограниченным количеством.
	 * @return true, если ограничен
	 */
	public boolean isCountLimited()
	{
		return getCount() > 0;
	}

	/**
	 * Устанавливает время последнего респауна предмета, используется в NPC магазинах с ограниченным количеством.
	 * @param lastRechargeTime : unixtime в минутах
	 */
	public void setLastRechargeTime(int lastRechargeTime)
	{
		_lastRechargeTime = lastRechargeTime;
	}

	/**
	 * Возвращает время последнего респауна предмета, используется в NPC магазинах с ограниченным количеством.
	 * @return unixtime в минутах
	 */
	public int getLastRechargeTime()
	{
		return _lastRechargeTime;
	}
}