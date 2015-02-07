package com.pqqqqq.directsupport.bukkit.events;

import java.util.ArrayList;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerChatEvent;

import com.pqqqqq.directsupport.ChatLine;
import com.pqqqqq.directsupport.DirectSupport;
import com.pqqqqq.directsupport.Ticket;

@SuppressWarnings("deprecation")
public class ChatEventOld implements Listener {
	private DirectSupport	ds;

	public ChatEventOld(DirectSupport ds) {
		this.ds = ds;
	}

	@EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
	public void chat(PlayerChatEvent event) {
		Player player = event.getPlayer();
		ArrayList<Player> recipients = new ArrayList<Player>(event.getRecipients());

		for (Ticket ticket : ds.getActiveTickets()) {
			Player helper = ticket.getHelper();
			Player creator = ticket.getCreator();

			if (helper == null)
				continue;

			for (Player recip : recipients) {
				if (recip.equals(player)) {
					if ((creator.equals(player) || helper.equals(player)) && !ticket.isCompleted()) {
						boolean creatorB = creator.equals(player);
						event.getRecipients().clear();

						String format = ds.addColour(ds.getAdminChatFormat());
						format = format.replace("{PLAYER}", player.getName());
						format = format.replace("{MESSAGE}", event.getMessage());
						format = format.replace("{OTHER}", (creatorB ? helper.getName() : creator.getName()));

						creator.sendMessage(format);
						helper.sendMessage(format);

						for (String s : ds.getSpies()) {
							Player spy = ds.getPlugin().getServer().getPlayerExact(s);
							
							if (spy == null)
								continue;
							
							if (!spy.equals(creator) && !spy.equals(helper))
								spy.sendMessage(format);
						}

						ticket.getChatLines().add(new ChatLine(player, event.getMessage()));
						event.setCancelled(true);
						return;
					}
				}
			}
		}

		if (ds.getAdminChatters().contains(player.getName())) {
			String format = ds.addColour(ds.getTicketFormat());
			format = format.replace("{PLAYER}", player.getName());
			format = format.replace("{MESSAGE}", event.getMessage());

			for (Player p : ds.getPlugin().getServer().getOnlinePlayers()) {
				if (p.isOp() || p.hasPermission("ds.mod") || p.hasPermission("ds.admin"))
					p.sendMessage(format);
			}

			event.setCancelled(true);
			return;
		}

		if (ds.doesBlockOtherChats())
		    for (Ticket ticket : ds.getActiveTickets()) {
		        Player helper = ticket.getHelper();
		        Player creator = ticket.getCreator();

		        if (helper == null || ticket.isCompleted())
		            continue;

		        event.getRecipients().remove(creator);
		        event.getRecipients().remove(helper);
		}
	}
}
