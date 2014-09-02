package com.pqqqqq.directsupport.concurrent;

import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

import com.pqqqqq.directsupport.DirectSupport;
import com.pqqqqq.directsupport.Ticket;

public class TicketReminder implements Runnable {
	private DirectSupport	ds;

	public TicketReminder(DirectSupport ds) {
		this.ds = ds;
	}

	@Override
	public void run() {
		if (!ds.isReminderEnabled())
			return;

		int at = 0;
		for (Ticket ticket : ds.getActiveTickets()) {
			if (!ticket.isCompleted() && ticket.getHelper() == null)
				at++;
		}

		if (at <= 0)
			return;

		for (Player player : ds.getPlugin().getServer().getOnlinePlayers()) {
			if (!ds.getUserWithBlockReqMsgs().contains(player.getName())
					&& (player.hasPermission("ds.accept") || player.hasPermission("ds.mod") || player.hasPermission("ds.admin") || player.isOp())) {
				player.sendMessage(ChatColor.DARK_AQUA + "[DirectSupport] There are " + ChatColor.AQUA + at + ChatColor.DARK_AQUA
						+ " active ticket(s)");
				player.sendMessage(ChatColor.DARK_AQUA + "Use " + ChatColor.AQUA + "/ds list" + ChatColor.DARK_AQUA + " for a list of them and "
						+ ChatColor.AQUA + "/ds accept" + ChatColor.DARK_AQUA + " to accept one.");
			}
		}
	}
}
