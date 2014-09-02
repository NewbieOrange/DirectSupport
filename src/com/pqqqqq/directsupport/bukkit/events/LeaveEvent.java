package com.pqqqqq.directsupport.bukkit.events;

import java.util.ArrayList;

import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerKickEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import com.pqqqqq.directsupport.DirectSupport;
import com.pqqqqq.directsupport.Ticket;

public class LeaveEvent implements Listener {
	private DirectSupport	ds;

	public LeaveEvent(DirectSupport ds) {
		this.ds = ds;
	}

	@EventHandler
	public void quit(PlayerQuitEvent event) {
		quit(event.getPlayer());
	}

	@EventHandler(ignoreCancelled = true)
	public void kick(PlayerKickEvent event) {
		quit(event.getPlayer());
	}

	private void quit(Player player) {
		ArrayList<Ticket> tcks = new ArrayList<Ticket>();
		tcks.addAll(ds.getActiveTickets());

		for (Ticket ticket : tcks) {
			final Player creator = ticket.getCreator();
			Player helper = ticket.getHelper();

			if ((creator.equals(player) || helper != null && helper.equals(player)) && !ticket.isCompleted()) {
				boolean creatorDelete = creator.equals(player);

				ds.getActiveTickets().remove(ticket);
				ds.getCompletedTickets().add(ticket);
				ticket.setCompleted(true);

				if (ds.getCreationDelay() > 0) {
					ds.getDelayTimes().put(creator.getName(), System.currentTimeMillis());
					ds.getPlugin().getServer().getScheduler().scheduleSyncDelayedTask(ds.getPlugin(), new Runnable() {

						@Override
						public void run() {
							ds.getDelayTimes().remove(creator.getName());
						}
					}, 20 * ds.getCreationDelay());
				}

				if (creatorDelete) {
					if (helper != null)
						helper.sendMessage(ChatColor.DARK_AQUA + "[DirectSupport] " + ChatColor.AQUA + player.getName()
								+ " does not need your assistance anymore.");
				} else {
					creator.sendMessage(ChatColor.DARK_AQUA + "[DirectSupport] " + ChatColor.AQUA + player.getName()
							+ " is not assisting you anymore.");
				}
			}
		}
	}
}
