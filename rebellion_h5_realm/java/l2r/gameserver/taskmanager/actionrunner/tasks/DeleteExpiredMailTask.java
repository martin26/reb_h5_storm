package l2r.gameserver.taskmanager.actionrunner.tasks;

import l2r.commons.dao.JdbcEntityState;
import l2r.gameserver.dao.MailDAO;
import l2r.gameserver.model.Player;
import l2r.gameserver.model.World;
import l2r.gameserver.model.mail.Mail;
import l2r.gameserver.network.serverpackets.ExNoticePostArrived;
import l2r.gameserver.network.serverpackets.components.SystemMsg;

import java.util.List;

public class DeleteExpiredMailTask extends AutomaticTask
{
	public DeleteExpiredMailTask()
	{
		super();
	}

	@Override
	public void doTask() throws Exception
	{
		int expireTime = (int) (System.currentTimeMillis() / 1000L);

		List<Mail> mails = MailDAO.getInstance().getExpiredMail(expireTime);

		for(Mail mail : mails)
		{
			if(!mail.getAttachments().isEmpty())
			{
				if(mail.getType() == Mail.SenderType.NORMAL)
				{
					Player player = World.getPlayer(mail.getSenderId());

					Mail reject = mail.reject();
					mail.delete();
					reject.setExpireTime(expireTime + (360 * 3600));
					reject.save();

					if(player != null)
					{
						player.sendPacket(ExNoticePostArrived.STATIC_TRUE);
						player.sendPacket(SystemMsg.THE_MAIL_HAS_ARRIVED);
					}
				}
				else
				{
					//TODO [G1ta0] возврат вещей в инвентарь игрока
					mail.setExpireTime(expireTime + 86400);
					mail.setJdbcState(JdbcEntityState.UPDATED);
					mail.update();
				}
			}
			else
			{
				mail.delete();
			}
		}
	}

	@Override
	public long reCalcTime(boolean start)
	{
		return System.currentTimeMillis() + 600000L;
	}
}
