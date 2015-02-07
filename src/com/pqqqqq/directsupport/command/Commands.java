package com.pqqqqq.directsupport.command;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import com.pqqqqq.directsupport.ChatLine;
import com.pqqqqq.directsupport.DirectSupport;
import com.pqqqqq.directsupport.Ticket;

public class Commands {
	private DirectSupport			ds;
	private final ArrayList<Method>	commands	= new ArrayList<Method>();

	/* Commands */

	@Command(
			permissions = { "ds.create-ticket" },
			aliases = { "create", "make", "request", "req", "new" },
			description = "Creates a new ticket",
			usage = "/ds create <message message...>",
			example = "/ds create I need help")
	public boolean createTicket(CommandSender sender, String... args) {
		if (args.length <= 0) {
			sender.sendMessage(ChatColor.DARK_AQUA + "[DirectSupport] " + ChatColor.AQUA + "Usage: /ds create <message message...>.");
			return true;
		}

		if (!(sender instanceof Player)) {
			sender.sendMessage(ChatColor.DARK_RED + "[DirectSupport] " + ChatColor.RED + "Player-only command.");
			return true;
		}

		Player player = (Player) sender;

		if (ds.getDelayTimes().containsKey(player.getName())) {
			long start = ds.getDelayTimes().get(player.getName());
			long diff = (start + (ds.getCreationDelay() * 1000)) - System.currentTimeMillis();

			if (diff <= 0) {
				ds.getDelayTimes().remove(player.getName());
			} else {
				int sec = (int) Math.ceil(diff / 1000D);
				player.sendMessage(ChatColor.DARK_RED + "[DirectSupport] " + ChatColor.RED + "Please wait " + ChatColor.DARK_RED + sec
						+ ChatColor.RED + " more second(s) to send another request.");
				return true;
			}
		}

		for (Ticket ticket : ds.getActiveTickets()) {
			Player helper = ticket.getHelper();

			if ((ticket.getCreator().equals(player) || helper != null && helper.equals(player)) && !ticket.isCompleted()) {
				player.sendMessage(ChatColor.DARK_RED + "[DirectSupport] " + ChatColor.RED + "You are already participating in a ticket!");
				return true;
			}
		}

		easybreak: if (ds.doesDisallowRequests()) {
			for (Player on : ds.getPlugin().getServer().getOnlinePlayers()) {
				if (!on.equals(player) && (on.hasPermission("ds.accept") || on.hasPermission("ds.mod") || on.hasPermission("ds.admin") || on.isOp())) {
					break easybreak;
				}
			}

			player.sendMessage(ChatColor.DARK_RED + "[DirectSupport] " + ChatColor.RED
					+ "You can't make a ticket because there would be no one to help you!");
			return true;
		}

		String message = implode(args, " ").trim();

		if (message.length() > ds.getTicketMaxNameSize()) {
			player.sendMessage(ChatColor.DARK_RED + "[DirectSupport] " + ChatColor.RED + "The ticket request must be less than " + ChatColor.DARK_RED
					+ ds.getTicketMaxNameSize() + ChatColor.RED + " characters.");
			return true;
		}

		Ticket ticket = new Ticket(player, new Date(), player.getLocation());
		ticket.setRequestString(message);
		ds.getActiveTickets().add(ticket);

		for (Player on : ds.getPlugin().getServer().getOnlinePlayers()) {
			if (!ds.getUserWithBlockReqMsgs().contains(on.getName()) && !on.equals(player)
					&& (on.hasPermission("ds.accept") || on.hasPermission("ds.mod") || on.hasPermission("ds.admin") || on.isOp())) {
				on.sendMessage(ChatColor.DARK_AQUA + "[DirectSupport] " + ChatColor.AQUA + player.getName() + " has requested assistance (ID :"
						+ ChatColor.DARK_AQUA + "#" + ticket.getId() + ChatColor.AQUA + ").");
			}
		}

		player.sendMessage(ChatColor.DARK_AQUA + "[DirectSupport] " + ChatColor.AQUA + "Your ticket request has been sent.");
		player.sendMessage(ChatColor.AQUA + "Use /ds leave if you'd like to cancel your request.");
		return true;
	}

	@Command(
			permissions = { "ds.accept", "ds.mod", "ds.admin" },
			aliases = { "accept", "join" },
			description = "Accepts and joins a ticket",
			usage = "/ds accept [id]",
			example = "/ds accept OR /ds accept 5")
	public boolean accept(CommandSender sender, String... args) {
		if (!(sender instanceof Player)) {
			sender.sendMessage(ChatColor.DARK_RED + "[DirectSupport] " + ChatColor.RED + "Player-only command.");
			return true;
		}

		Player player = (Player) sender;

		for (Ticket ticket : ds.getActiveTickets()) {
			Player helper = ticket.getHelper();

			if ((ticket.getCreator().equals(player) || helper != null && helper.equals(player)) && !ticket.isCompleted()) {
				player.sendMessage(ChatColor.DARK_RED + "[DirectSupport] " + ChatColor.RED + "You are already participating in a ticket!");
				return true;
			}
		}

		String accFormat = ds.addColour(ds.getAcceptFormat()).replace("{HELPER}", player.getName());
		if (args.length == 0) {
			for (Ticket ticket : ds.getActiveTickets()) {
				Player helper = ticket.getHelper();

				if (helper != null || ticket.isCompleted())
					continue;

				Player creator = ticket.getCreator();
				if (creator.equals(player))
					continue;

				ticket.setHelper(player);

				player.sendMessage(ChatColor.DARK_AQUA + "[DirectSupport] " + ChatColor.AQUA + "You are now assisting " + ChatColor.DARK_AQUA
						+ creator.getName() + ChatColor.AQUA + " for: " + ChatColor.DARK_AQUA + ticket.getRequestString() + ".");
				creator.sendMessage(accFormat);

				for (Player on : ds.getPlugin().getServer().getOnlinePlayers()) {
					if (!ds.getUserWithBlockReqMsgs().contains(on.getName()) && !on.equals(creator)
							&& (on.hasPermission("ds.accept") || on.hasPermission("ds.mod") || on.hasPermission("ds.admin") || on.isOp())) {
						on.sendMessage(ChatColor.DARK_AQUA + "[DirectSupport] " + ChatColor.AQUA + player.getName() + ChatColor.DARK_AQUA
								+ " has accepted ticket request " + ChatColor.AQUA + "#" + ticket.getId() + ".");
					}
				}
				return true;
			}

			player.sendMessage(ChatColor.DARK_RED + "[DirectSupport] " + ChatColor.RED + "There are no players in need of assistance!");
		} else {
			int id;

			try {
				id = Integer.parseInt(args[0]);
			} catch (NumberFormatException e) {
				player.sendMessage(ChatColor.DARK_RED + "[DirectSupport] " + ChatColor.RED + "The id must be a number!");
				return true;
			}

			for (Ticket ticket : ds.getActiveTickets()) {
				if (ticket.getId() == id) {
					Player helper = ticket.getHelper();

					if (helper != null || ticket.isCompleted()) {
						player.sendMessage(ChatColor.DARK_RED + "[DirectSupport] " + ChatColor.RED + "That ticket is already being assisted!");
						return true;
					}

					Player creator = ticket.getCreator();
					if (creator.equals(player)) {
						player.sendMessage(ChatColor.DARK_RED + "[DirectSupport] " + ChatColor.RED + "You can't assist your own ticket!");
						return true;
					}

					ticket.setHelper(player);

					player.sendMessage(ChatColor.DARK_AQUA + "[DirectSupport] " + ChatColor.AQUA + "You are now assisting " + ChatColor.DARK_AQUA
							+ creator.getName() + ChatColor.AQUA + " for: " + ChatColor.DARK_AQUA + ticket.getRequestString() + ".");
					creator.sendMessage(accFormat);

					for (Player on : ds.getPlugin().getServer().getOnlinePlayers()) {
						if (!ds.getUserWithBlockReqMsgs().contains(on.getName()) && !on.equals(creator)
								&& (on.hasPermission("ds.accept") || on.hasPermission("ds.mod") || on.hasPermission("ds.admin") || on.isOp())) {
							on.sendMessage(ChatColor.DARK_AQUA + "[DirectSupport] " + ChatColor.AQUA + player.getName() + ChatColor.DARK_AQUA
									+ " has accepted ticket request " + ChatColor.AQUA + "#" + ticket.getId() + ".");
						}
					}
					return true;
				}
			}

			player.sendMessage(ChatColor.DARK_RED + "[DirectSupport] " + ChatColor.RED + "There is no such ticket with that id!");
		}
		return true;
	}

	@Command(permissions = {}, aliases = { "leave", "l", "quit" }, description = "Leave your current ticket", usage = "/ds leave")
	public boolean leave(CommandSender sender, String... args) {
		if (!(sender instanceof Player)) {
			sender.sendMessage(ChatColor.DARK_RED + "[DirectSupport] " + ChatColor.RED + "Player-only command.");
			return true;
		}

		Player player = (Player) sender;

		ArrayList<Ticket> tcks = new ArrayList<Ticket>();
		tcks.addAll(ds.getActiveTickets());

		for (Ticket ticket : tcks) {
			final Player creator = ticket.getCreator();
			Player helper = ticket.getHelper();

			if ((creator.equals(player) || helper != null && helper.equals(player)) && !ticket.isCompleted()) {
				boolean creatorDelete = creator.equals(player);

				ticket.setCompleted(true);
				ds.getCompletedTickets().add(ticket);
				ds.getActiveTickets().remove(ticket);

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
					player.sendMessage(ChatColor.DARK_AQUA + "[DirectSupport] " + ChatColor.AQUA + "You closed your assistance request.");

					if (helper != null)
						helper.sendMessage(ChatColor.DARK_AQUA + "[DirectSupport] " + ChatColor.AQUA + player.getName() + ChatColor.DARK_AQUA
								+ " does not need your assistance anymore.");
				} else {
					helper.sendMessage(ChatColor.DARK_AQUA + "[DirectSupport] " + ChatColor.AQUA + "You are no longer helping " + ChatColor.DARK_AQUA
							+ creator.getName() + ".");
					creator.sendMessage(ChatColor.DARK_AQUA + "[DirectSupport] " + ChatColor.AQUA + player.getName() + ChatColor.DARK_AQUA
							+ " is not assisting you anymore.");
				}

				if (helper != null) {
					for (String s : ds.getSpies()) {
						Player spy = ds.getPlugin().getServer().getPlayerExact(s);

						if (spy == null)
							continue;

						if (!spy.equals(creator) && !spy.equals(helper))
							spy.sendMessage(ChatColor.DARK_AQUA + "[DirectSupport] Ticket " + ChatColor.AQUA + "#" + ticket.getId()
									+ ChatColor.DARK_AQUA + " was closed.");
					}
				}

				return true;
			}
		}

		player.sendMessage(ChatColor.DARK_RED + "[DirectSupport] " + ChatColor.RED + "You are not participating in a ticket!");
		return true;
	}

	@Command(permissions = { "ds.spy", "ds.admin" }, aliases = { "spy" }, description = "Spy on others' tickets", usage = "/ds spy")
	public boolean spy(CommandSender sender, String... args) {
		if (!(sender instanceof Player)) {
			sender.sendMessage(ChatColor.DARK_RED + "[DirectSupport] " + ChatColor.RED + "Player-only command.");
			return true;
		}

		Player player = (Player) sender;

		if (ds.getSpies().contains(player.getName())) {
			ds.getSpies().remove(player.getName());
			player.sendMessage(ChatColor.DARK_AQUA + "[DirectSupport] " + ChatColor.AQUA + "Spy mode: " + ChatColor.DARK_AQUA + "OFF");
		} else {
			ds.getSpies().add(player.getName());
			player.sendMessage(ChatColor.DARK_AQUA + "[DirectSupport] " + ChatColor.AQUA + "Spy mode: " + ChatColor.DARK_AQUA + "ON");
		}
		return true;
	}

	@Command(
			permissions = { "ds.block-messages", "ds.admin" },
			aliases = { "block", "breq" },
			description = "Block user request messages",
			usage = "/ds block")
	public boolean blockRequests(CommandSender sender, String... args) {
		if (!(sender instanceof Player)) {
			sender.sendMessage(ChatColor.DARK_RED + "[DirectSupport] " + ChatColor.RED + "Player-only command");
			return true;
		}

		Player player = (Player) sender;

		if (ds.getUserWithBlockReqMsgs().contains(player.getName())) {
			ds.getUserWithBlockReqMsgs().remove(player.getName());
			player.sendMessage(ChatColor.DARK_AQUA + "[DirectSupport] " + ChatColor.AQUA + "Block request messages: " + ChatColor.DARK_AQUA + "OFF");
		} else {
			ds.getUserWithBlockReqMsgs().add(player.getName());
			player.sendMessage(ChatColor.DARK_AQUA + "[DirectSupport] " + ChatColor.AQUA + "Block request messages: " + ChatColor.DARK_AQUA + "ON");
		}
		return true;
	}

	@Command(
			permissions = { "ds.say", "ds.admin" },
			aliases = { "say", "broadcast" },
			description = "Say a message into others' tickets",
			usage = "/ds say <id> <message message...>",
			example = "/ds say 5 Hi, how are you?")
	public boolean say(CommandSender sender, String... args) {
		if (args.length <= 1) {
			sender.sendMessage(ChatColor.DARK_AQUA + "[DirectSupport] " + ChatColor.AQUA + "Usage: /ds say <id> <message message...>.");
			return true;
		}

		String message = implode(args, " ", 1).trim();

		int id;

		try {
			id = Integer.parseInt(args[0]);
		} catch (NumberFormatException e) {
			sender.sendMessage(ChatColor.DARK_RED + "[DirectSupport] " + ChatColor.RED + "The id must be a number!");
			return true;
		}

		for (Ticket ticket : ds.getActiveTickets()) {
			if (ticket.getId() == id) {
				Player creator = ticket.getCreator();
				Player helper = ticket.getHelper();

				if (helper == null || ticket.isCompleted())
					continue;

				String m = ChatColor.DARK_AQUA + "[DirectSupport -> " + ChatColor.AQUA + "#" + ticket.getId() + ChatColor.DARK_AQUA + "] "
						+ ChatColor.AQUA + sender.getName() + ChatColor.WHITE + ": " + message;

				creator.sendMessage(m);
				helper.sendMessage(m);
				sender.sendMessage(m);
				return true;
			}
		}

		sender.sendMessage(ChatColor.DARK_RED + "[DirectSupport] " + ChatColor.RED + "There is no active ticket with that id!");
		return true;
	}

	@Command(
			permissions = {},
			aliases = { "help" },
			description = "This help menu",
			usage = "/ds help [command|page]",
			example = "/ds help 2 OR /ds help create")
	public boolean help(CommandSender sender, String... args) {
		int cpp = ds.getCommandsPerPage();
		int page = 1;
		int pges = (int) Math.ceil((double) (commands.size()) / cpp);

		try {
			page = Math.max(1, Integer.parseInt(args[0]));
		} catch (NumberFormatException e) {
			for (Method method : commands) {
				Command commandInfo = method.getAnnotation(Command.class);
				String[] aliases = commandInfo.aliases();
				String[] permissions = commandInfo.permissions();

				String aFormatted = Arrays.asList(aliases).toString().replaceAll("\\[|\\]", "");

				boolean authorized = sender.isOp() || permissions == null || permissions.length <= 0;
				if (!authorized) {
					for (String perm : permissions) {
						if (sender.hasPermission(perm)) {
							authorized = true;
							break;
						}
					}
				}

				String description = commandInfo.description();
				String usage = commandInfo.usage();
				String examples = commandInfo.example();

				for (String alias : aliases) {
					if (alias.equalsIgnoreCase(args[0])) {
						sender.sendMessage(ChatColor.DARK_AQUA + "Command: " + ChatColor.AQUA + aliases[0] + ".");
						sender.sendMessage(ChatColor.DARK_AQUA + "Aliases: " + ChatColor.AQUA + aFormatted + ".");
						sender.sendMessage(ChatColor.DARK_AQUA + "Usage: " + ChatColor.AQUA + usage + ".");
						sender.sendMessage(ChatColor.DARK_AQUA + "Description: " + ChatColor.AQUA + description + ".");
						sender.sendMessage(ChatColor.DARK_AQUA + "Authorized: " + ChatColor.AQUA + authorized + ".");

						if (!examples.isEmpty())
							sender.sendMessage(ChatColor.DARK_AQUA + "Examples: " + ChatColor.AQUA + examples + ".");
						return true;
					}
				}
			}
		} catch (Throwable e) {
		}

		if (page > pges) {
			sender.sendMessage(ChatColor.DARK_RED + "[DirectSupport] " + ChatColor.RED + "There are only " + pges + " pages!");
			return true;
		}

		int start = (page - 1) * cpp;
		int end = (page * cpp) - 1;

		sender.sendMessage(ChatColor.DARK_AQUA + "Use " + ChatColor.AQUA + "/ds help [command] " + ChatColor.DARK_AQUA
				+ " to get information about a command");
		sender.sendMessage(ChatColor.AQUA + "=====------~ " + ChatColor.DARK_AQUA + "Help PG " + page + "/" + pges + ChatColor.AQUA + " ~-----=====");
		for (int i = 0; i < commands.size(); i++) {
			if (i >= start && i <= end) {
				Method method = commands.get(i);
				Command commandInfo = method.getAnnotation(Command.class);
				String[] aliases = commandInfo.aliases();
				String description = commandInfo.description();
				String[] permission = commandInfo.permissions();

				check: if (!sender.isOp() && permission != null && permission.length > 0) {
					for (String p : permission) {
						if (sender.hasPermission(p))
							break check;
					}

					// end++;
					continue;
				}

				sender.sendMessage(ChatColor.AQUA + "/ds " + aliases[0] + ChatColor.DARK_AQUA + " -> " + ChatColor.AQUA + description);
			}
		}
		return true;
	}

	@Command(
			permissions = { "ds.list", "ds.mod", "ds.admin" },
			aliases = { "list", "tickets" },
			description = "Shows a list of tickets",
			usage = "/ds list [page]",
			example = "/ds list 5")
	public boolean list(CommandSender sender, String... args) {
		int cpp = ds.getActiveTicketsPerPage();
		int page = 1;
		int pges = (int) Math.ceil((double) (ds.getActiveTickets().size()) / cpp);

		try {
			page = Math.max(1, Integer.parseInt(args[0]));
		} catch (Throwable e) {
		}

		if (pges <= 0) {
			sender.sendMessage(ChatColor.DARK_RED + "[DirectSupport] " + ChatColor.RED + "There are no pending requests.");
			return true;
		}

		if (page > pges) {
			sender.sendMessage(ChatColor.DARK_RED + "[DirectSupport] " + ChatColor.RED + "There are only " + pges + " pages!");
			return true;
		}

		int start = (page - 1) * cpp;
		int end = (page * cpp) - 1;

		sender.sendMessage(ChatColor.AQUA + "=====------~ " + ChatColor.DARK_AQUA + "Active Tickets PG " + page + "/" + pges + ChatColor.AQUA
				+ " ~-----=====");
		Ticket[] tickets = ds.getActiveTickets().toArray(new Ticket[ds.getActiveTickets().size()]);
		for (int i = 0; i < tickets.length; i++) {
			if (i >= start && i <= end) {
				Ticket ticket = tickets[i];
				Player creator = ticket.getCreator();
				Player helper = ticket.getHelper();

				if (!ticket.isCompleted()) {
					sender.sendMessage(ChatColor.AQUA.toString() + ticket.getId() + ChatColor.DARK_AQUA + ": \"" + ChatColor.AQUA
							+ ticket.getRequestString() + ChatColor.DARK_AQUA + "\" by: " + ChatColor.AQUA + creator.getName() + ChatColor.DARK_AQUA
							+ (helper != null ? " helper: " + ChatColor.AQUA + helper.getName() : ""));
				}
			}
		}
		return true;
	}

	@Command(
			permissions = { "ds.completed", "ds.mod", "ds.admin" },
			aliases = { "done", "completed" },
			description = "Shows a list of completed tickets",
			usage = "/ds done [page]",
			example = "/ds done 5")
	public boolean completed(CommandSender sender, String... args) {
		int cpp = ds.getCompletedTicketsPerPage();
		int page = 1;
		int pges = (int) Math.ceil((double) (ds.getCompletedTickets().size()) / cpp);

		try {
			page = Math.max(1, Integer.parseInt(args[0]));
		} catch (Throwable e) {
		}

		if (pges <= 0) {
			sender.sendMessage(ChatColor.DARK_RED + "[DirectSupport] " + ChatColor.RED + "There are no completed requests.");
			return true;
		}

		if (page > pges) {
			sender.sendMessage(ChatColor.DARK_RED + "[DirectSupport] " + ChatColor.RED + "There are only " + pges + " page(s)!");
			return true;
		}

		int start = (page - 1) * cpp;
		int end = (page * cpp) - 1;

		sender.sendMessage(ChatColor.AQUA + "=====------~ " + ChatColor.DARK_AQUA + "Completed Tickets PG " + page + "/" + pges + ChatColor.AQUA
				+ " ~-----=====");
		Ticket[] tickets = ds.getCompletedTickets().toArray(new Ticket[ds.getCompletedTickets().size()]);
		for (int i = 0; i < tickets.length; i++) {
			if (i >= start && i <= end) {
				Ticket ticket = tickets[i];
				Player creator = ticket.getCreator();
				Player helper = ticket.getHelper();

				if (ticket.isCompleted()) {
					sender.sendMessage(ChatColor.AQUA + "" + ticket.getId() + ChatColor.DARK_AQUA + ": \"" + ChatColor.AQUA
							+ ticket.getRequestString() + ChatColor.DARK_AQUA + "\" by: " + ChatColor.AQUA + creator.getName() + ChatColor.DARK_AQUA
							+ (helper != null ? " completed by: " + ChatColor.AQUA + helper.getName() : ""));
				}
			}
		}
		return true;
	}

	@Command(permissions = { "ds.reload", "ds.admin" }, aliases = { "reload" }, description = "Reloads plugin config", usage = "/ds reload")
	public boolean reload(CommandSender sender, String... args) {
		ds.getConfig().load();
		ds.getActiveTickets().clear();
		ds.getCompletedTickets().clear();
		Ticket.setCurId(1);

		sender.sendMessage(ChatColor.DARK_AQUA + "[DirectSupport] " + ChatColor.AQUA + "Reloaded.");
		return true;
	}
	
	@Command(
            permissions = { "ds.transfer", "ds.admin" },
            aliases = { "transfer" },
            description = "Transfers the active ticket to another admin",
            usage = "/ds transfer <id> <to>",
            example = "/ds transfer 5 chengzi")
	public boolean transfer(CommandSender sender, String... args)
	{
	    if (args.length <= 0) {
            sender.sendMessage(ChatColor.DARK_AQUA + "[DirectSupport] " + ChatColor.AQUA + "Usage: /ds transfer <id> <to>.");
            return true;
        }

        int id;

        try {
            id = Integer.parseInt(args[0]);
        } catch (NumberFormatException e) {
            sender.sendMessage(ChatColor.DARK_RED + "[DirectSupport] " + ChatColor.RED + "The id must be a number!");
            return true;
        }
        
        Player helper = Bukkit.getPlayer(args[1]);
        
        if (helper == null) {
            sender.sendMessage(ChatColor.DARK_RED + "[DirectSupport] " + ChatColor.RED + "The target helper not found or isn't online.");
            return true;
        }
        else if (!helper.hasPermission("ds.accept")) {
            sender.sendMessage(ChatColor.DARK_RED + "[DirectSupport] " + ChatColor.RED + "The target helper can't accept any tickets.");
            return true;
        }

        ArrayList<Ticket> tcks = new ArrayList<Ticket>();
        tcks.addAll(ds.getActiveTickets());

        for (Ticket ticket : tcks) {
            if (ticket.getId() == id){
                final Player creator = ticket.getCreator();
                final Player oldHelper = ticket.getHelper();
                ticket.setHelper(helper);
                creator.sendMessage(ChatColor.DARK_AQUA + "[DirectSupport] " + ChatColor.AQUA + "Your ticket has been transfered to " + helper.getName() +  ".");
                sender.sendMessage(ChatColor.DARK_AQUA + "[DirectSupport] " + ChatColor.AQUA + "You transfered the ticket to " + helper.getName() +  ".");
                helper.sendMessage(ChatColor.DARK_AQUA + "[DirectSupport] " + ChatColor.AQUA + "You are now assigning ticket #" + id +  ".");
                oldHelper.sendMessage(ChatColor.DARK_AQUA + "[DirectSupport] " + ChatColor.AQUA + "The ticket you were helping has been transfered to " + helper.getName() +  ".");
                
                for (String s : ds.getSpies()) {
                    Player spy = ds.getPlugin().getServer().getPlayerExact(s);

                    if (spy == null)
                        continue;

                    if (!spy.equals(creator) && !spy.equals(helper) && !spy.equals(sender))
                        spy.sendMessage(ChatColor.DARK_AQUA + "[DirectSupport] Ticket " + ChatColor.AQUA + "#" + ticket.getId()
                                + ChatColor.DARK_AQUA + " was transfered from " + oldHelper.getName() + " to " + helper.getName() + ".");
                }
                break;
            }
        }
	    return true;
	}

	@Command(
			permissions = { "ds.delete", "ds.admin" },
			aliases = { "delete", "remove", "close" },
			description = "Deletes the active ticket",
			usage = "/ds delete <id>",
			example = "/ds delete 5")
	public boolean delete(CommandSender sender, String... args) {
		if (args.length <= 0) {
			sender.sendMessage(ChatColor.DARK_AQUA + "[DirectSupport] " + ChatColor.AQUA + "Usage: /ds delete <id>.");
			return true;
		}

		int id;

		try {
			id = Integer.parseInt(args[0]);
		} catch (NumberFormatException e) {
			sender.sendMessage(ChatColor.DARK_RED + "[DirectSupport] " + ChatColor.RED + "The id must be a number!");
			return true;
		}

		ArrayList<Ticket> tcks = new ArrayList<Ticket>();
		tcks.addAll(ds.getActiveTickets());

		for (Ticket ticket : tcks) {
			if (ticket.getId() == id) {
				final Player creator = ticket.getCreator();
				Player helper = ticket.getHelper();

				if (ticket.isCompleted())
					continue;

				ticket.setCompleted(true);
				ds.getActiveTickets().remove(ticket);

				if (ds.getCreationDelay() > 0) {
					ds.getDelayTimes().put(creator.getName(), System.currentTimeMillis());
					ds.getPlugin().getServer().getScheduler().scheduleSyncDelayedTask(ds.getPlugin(), new Runnable() {

						@Override
						public void run() {
							ds.getDelayTimes().remove(creator.getName());
						}
					}, 20 * ds.getCreationDelay());
				}

				creator.sendMessage(ChatColor.DARK_AQUA + "[DirectSupport] " + ChatColor.AQUA + "Your ticket has been deleted.");
				if (helper != null) {
					helper.sendMessage(ChatColor.DARK_AQUA + "[DirectSupport] " + ChatColor.AQUA + "The ticket you were helping has been deleted.");

					for (String s : ds.getSpies()) {
						Player spy = ds.getPlugin().getServer().getPlayerExact(s);

						if (spy == null)
							continue;

						if (!spy.equals(creator) && !spy.equals(helper))
							spy.sendMessage(ChatColor.DARK_AQUA + "[DirectSupport] Ticket " + ChatColor.AQUA + "#" + ticket.getId()
									+ ChatColor.DARK_AQUA + " was closed.");
					}
				}
				sender.sendMessage(ChatColor.DARK_AQUA + "[DirectSupport] " + ChatColor.AQUA + "You deleted " + ChatColor.DARK_AQUA
						+ creator.getName() + ChatColor.AQUA + "'s ticket");
				return true;
			}
		}

		sender.sendMessage(ChatColor.DARK_RED + "[DirectSupport] " + ChatColor.RED + "There is no active ticket with that id!");
		return true;
	}

	@Command(permissions = {}, aliases = { "helpers", "mods", "ops" }, description = "Shows availible helpers", usage = "/ds helpers")
	public boolean helpers(CommandSender sender, String... args) {
		String msg = ChatColor.DARK_AQUA + "[DirectSupport] " + ChatColor.AQUA + "Helpers (" + ChatColor.DARK_AQUA + "{AMT}" + ChatColor.AQUA + "): "
				+ ChatColor.DARK_AQUA;
		int amt = 0;
		for (Player on : ds.getPlugin().getServer().getOnlinePlayers()) {
			if (on.hasPermission("ds.accept") || on.hasPermission("ds.mod") || on.hasPermission("ds.admin") || on.isOp()) {
				msg += on.getName() + ", ";
				amt++;
			}
		}
		if (amt == 0)
			sender.sendMessage(ChatColor.DARK_AQUA + "[DirectSupport] " + ChatColor.AQUA + "No helpers online");
		else
			sender.sendMessage(msg.substring(0, msg.length() - 2).replaceFirst("\\{AMT\\}", Integer.toString(amt)));
		return true;
	}

	@SuppressWarnings("deprecation")
	@Command(
			permissions = { "ds.mod" },
			aliases = { "a", "ac" },
			description = "Talks to the mods with the permission",
			usage = "/ds a [message]",
			example = "/ds a Hey mods OR /ds a")
	public boolean adminChat(CommandSender sender, String... args) {
		if (!(sender instanceof Player)) {
			sender.sendMessage(ChatColor.DARK_RED + "[DirectSupport] " + ChatColor.RED + "Player-only command.");
			return true;
		}

		Player player = (Player) sender;

		if (args.length == 0) {
			if (ds.getAdminChatters().contains(player.getName())) {
				ds.getAdminChatters().remove(player.getName());
				player.sendMessage(ChatColor.DARK_AQUA + "[DirectSupport] " + ChatColor.AQUA + "Admin chat toggled: " + ChatColor.DARK_AQUA + "OFF");
			} else {
				ds.getAdminChatters().add(player.getName());
				player.sendMessage(ChatColor.DARK_AQUA + "[DirectSupport] " + ChatColor.AQUA + "Admin chat toggled: " + ChatColor.DARK_AQUA + "ON");
			}
		} else {
			String message = implode(args, " ").trim();

			boolean ac = ds.getAdminChatters().contains(player.getName());

			if (!ac)
				ds.getAdminChatters().add(player.getName());

			if (ds.isOldEvents()) {
				org.bukkit.event.player.PlayerChatEvent event = new org.bukkit.event.player.PlayerChatEvent(player, message, message,
						new HashSet<Player>(ds.getPlugin().getServer().getOnlinePlayers()));
				ds.getPlugin().getServer().getPluginManager().callEvent(event);
			} else {
				org.bukkit.event.player.AsyncPlayerChatEvent event = new org.bukkit.event.player.AsyncPlayerChatEvent(false, player, message,
						new HashSet<Player>(ds.getPlugin().getServer().getOnlinePlayers()));
				ds.getPlugin().getServer().getPluginManager().callEvent(event);
			}

			if (!ac)
				ds.getAdminChatters().remove(player.getName());
		}
		return true;
	}

	@Command(
			permissions = { "ds.view", "ds.admin" },
			aliases = { "view", "check" },
			description = "Views the conversation of a ticket",
			usage = "/ds view <id> [page]",
			example = "/ds view 2 OR /ds view 2 5")
	public boolean view(CommandSender sender, String... args) {
		if (args.length <= 0) {
			sender.sendMessage(ChatColor.DARK_AQUA + "[DirectSupport] " + ChatColor.AQUA + "Usage: /ds view <id> [page].");
			return true;
		}

		int id;
		try {
			id = Integer.parseInt(args[0]);
		} catch (NumberFormatException e) {
			sender.sendMessage(ChatColor.DARK_RED + "[DirectSupport] " + ChatColor.RED + "The id must be a number!");
			return true;
		}

		Ticket ticket = null;

		ArrayList<Ticket> allTickets = new ArrayList<Ticket>();
		allTickets.addAll(ds.getActiveTickets());
		allTickets.addAll(ds.getCompletedTickets());

		for (Ticket t : allTickets) {
			if (t.getId() == id) {
				ticket = t;
				break;
			}
		}

		if (ticket == null) {
			sender.sendMessage(ChatColor.DARK_RED + "[DirectSupport] " + ChatColor.RED + "There is no ticket with that id!");
			return true;
		}

		int cpp = ds.getConversationMessagesPerPage();
		int page = 1;
		int pges = (int) Math.ceil((double) (ticket.getChatLines().size()) / cpp);

		try {
			page = Math.max(1, Integer.parseInt(args[1]));
		} catch (Throwable e) {
		}

		if (pges <= 0) {
			sender.sendMessage(ChatColor.DARK_RED + "[DirectSupport] " + ChatColor.RED + "There is no conversation in this ticket.");
			return true;
		}

		if (page > pges) {
			sender.sendMessage(ChatColor.DARK_RED + "[DirectSupport] " + ChatColor.RED + "There are only " + pges + " page(s)!");
			return true;
		}

		int start = (page - 1) * cpp;
		int end = (page * cpp) - 1;

		sender.sendMessage(ChatColor.AQUA + "=====------~ " + ChatColor.DARK_AQUA + "Conversation (" + ChatColor.AQUA + "#" + ticket.getId()
				+ ChatColor.DARK_AQUA + ") PG " + page + "/" + pges + ChatColor.AQUA + " ~-----=====");
		for (int i = 0; i < ticket.getChatLines().size(); i++) {
			if (i >= start && i <= end) {
				ChatLine line = ticket.getChatLines().get(i);

				sender.sendMessage(ChatColor.AQUA + line.getWriter().getName() + ChatColor.WHITE + ": " + line.getMessage());
			}
		}
		return true;
	}

	@Command(
			permissions = { "ds.info", "ds.mod", "ds.admin" },
			aliases = { "info", "information" },
			description = "Retrieves information about a ticket",
			usage = "/ds info <id>",
			example = "/ds info 5")
	public boolean info(CommandSender sender, String... args) {
		if (args.length <= 0) {
			sender.sendMessage(ChatColor.DARK_AQUA + "[DirectSupport] " + ChatColor.AQUA + "Usage: /ds info <id>.");
			return true;
		}

		int id;
		try {
			id = Integer.parseInt(args[0]);
		} catch (NumberFormatException e) {
			sender.sendMessage(ChatColor.DARK_RED + "[DirectSupport] " + ChatColor.RED + "The id must be a number!");
			return true;
		}

		Ticket ticket = null;

		ArrayList<Ticket> allTickets = new ArrayList<Ticket>();
		allTickets.addAll(ds.getActiveTickets());
		allTickets.addAll(ds.getCompletedTickets());

		for (Ticket t : allTickets) {
			if (t.getId() == id) {
				ticket = t;
				break;
			}
		}

		if (ticket == null) {
			sender.sendMessage(ChatColor.DARK_RED + "[DirectSupport] " + ChatColor.RED + "There is no ticket with that id!");
			return true;
		}

		sender.sendMessage(ChatColor.DARK_AQUA + "ID: " + ChatColor.AQUA + ticket.getId());
		sender.sendMessage(ChatColor.DARK_AQUA + "Creator: " + ChatColor.AQUA + ticket.getCreator().getName());
		sender.sendMessage(ChatColor.DARK_AQUA + "Reason: " + ChatColor.AQUA + ticket.getRequestString());
		sender.sendMessage(ChatColor.DARK_AQUA + "Completed: " + ChatColor.AQUA + ticket.isCompleted());
		if (ticket.getHelper() != null)
			sender.sendMessage(ChatColor.DARK_AQUA + "Helper: " + ChatColor.AQUA + ticket.getHelper().getName());
		sender.sendMessage(ChatColor.DARK_AQUA + "Creation: " + ChatColor.AQUA + ds.getDateFormat().format(ticket.getCreationDate()));
		return true;
	}

	@Command(
			permissions = { "ds.goto", "ds.mod", "ds.admin" },
			aliases = { "goto", "go", "teleport", "tp" },
			description = "Teleports to a ticket",
			usage = "/ds goto [ticket]",
			example = "/ds goto OR /ds goto 2")
	public boolean tp(CommandSender sender, String... args) {
		if (!(sender instanceof Player)) {
			sender.sendMessage(ChatColor.DARK_RED + "[DirectSupport] " + ChatColor.RED + "Player-only command.");
			return true;
		}
		Player player = (Player) sender;

		Ticket ticket = null;

		if_break: if (args.length == 0) {
			Set<Ticket> tickets = new HashSet<Ticket>();
			tickets.addAll(ds.getActiveTickets());

			for (Ticket t : tickets) {
				if (t.getHelper() != null && t.getHelper().equals(player)) {
					ticket = t;
					break if_break;
				}
			}

			sender.sendMessage(ChatColor.DARK_RED + "[DirectSupport] " + ChatColor.RED + "You are not a helper in a ticket!");
			return true;
		} else {
			try {
				Set<Ticket> tickets = new HashSet<Ticket>();
				tickets.addAll(ds.getActiveTickets());
				tickets.addAll(ds.getCompletedTickets());

				int id = Integer.parseInt(args[0]);

				for (Ticket t : tickets) {
					if (t.getId() == id) {
						ticket = t;
						break if_break;
					}
				}

				sender.sendMessage(ChatColor.DARK_RED + "[DirectSupport] " + ChatColor.RED + "There is no ticket with that id!");
				return true;
			} catch (NumberFormatException e) {
				sender.sendMessage(ChatColor.DARK_RED + "[DirectSupport] " + ChatColor.RED + "The id must be a number!");
				return true;
			}
		}

		player.teleport(ticket.getWhere());
		player.sendMessage(ChatColor.DARK_AQUA + "[DirectSupport] " + ChatColor.AQUA + "Teleported to the creation of ticket: " + ChatColor.DARK_AQUA
				+ "#" + ticket.getId());
		return true;
	}

	/* End of commands */

	public Commands(DirectSupport ds) {
		this.ds = ds;
	}

	public void populateCommands() {
		commands.clear();

		for (Method method : getClass().getDeclaredMethods()) {
			if (method.isAnnotationPresent(Command.class) && method.getReturnType() == boolean.class) {
				commands.add(method);
			}
		}

		Collections.sort(commands, new SortMethods());
	}

	public boolean executeCommand(CommandSender sender, String[] args) {
		if (args.length <= 0) {
			sender.sendMessage(ChatColor.DARK_AQUA + "[DirectSupport] " + ChatColor.AQUA + "V" + ds.getVersion() + ChatColor.DARK_AQUA
					+ " created by " + ChatColor.AQUA + "Pqqqqq");
			sender.sendMessage(ChatColor.DARK_AQUA + "Use " + ChatColor.AQUA + "/ds help");
			return true;
		}

		for (Method method : commands) {
			try {
				Command commandInfo = method.getAnnotation(Command.class);
				String[] aliases = commandInfo.aliases();
				String[] perms = commandInfo.permissions();

				for (String alias : aliases) {
					if (alias.equalsIgnoreCase(args[0])) {
						check: if (!sender.isOp() && perms != null && perms.length > 0) {
							for (String p : perms) {
								if (sender.hasPermission(p))
									break check;
							}

							sender.sendMessage(ChatColor.DARK_RED + "[DirectSupport] " + ChatColor.RED + "Insufficient permissions!");
							return true;
						}

						try {
							return (Boolean) method.invoke(this, sender, trimFirst(args));
						} catch (Throwable e) {
							e.printStackTrace();
						}
					}
				}
			} catch (Throwable e) {
				continue;
			}
		}

		sender.sendMessage(ChatColor.DARK_AQUA + "[DirectSupport] " + ChatColor.AQUA + "V" + ds.getVersion() + ChatColor.DARK_AQUA + " created by "
				+ ChatColor.AQUA + "Pqqqqq");
		sender.sendMessage(ChatColor.DARK_AQUA + "Use " + ChatColor.AQUA + "/ds help");
		return true;
	}

	private String[] trimFirst(String[] a) {
		if (a.length <= 0)
			return a;

		String[] ret = new String[a.length - 1];

		for (int i = 1; i < a.length; i++) {
			ret[i - 1] = a[i];
		}

		return ret;
	}

	private String implode(String[] args, String connect) {
		return implode(args, connect, 0);
	}

	private String implode(String[] args, String connect, int start) {
		if (args == null || args.length <= 0)
			return "";

		String ret = "";
		for (int i = start; i < args.length; i++) {
			ret += args[i] + connect;
		}

		return ret.substring(0, ret.length() - connect.length());
	}
}
