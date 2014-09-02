package com.pqqqqq.directsupport;

import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;
import java.util.logging.Logger;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.PluginManager;

import com.pqqqqq.directsupport.bukkit.DirectSupportPlugin;
import com.pqqqqq.directsupport.bukkit.events.ChatEventNew;
import com.pqqqqq.directsupport.bukkit.events.ChatEventOld;
import com.pqqqqq.directsupport.bukkit.events.LeaveEvent;
import com.pqqqqq.directsupport.command.Commands;
import com.pqqqqq.directsupport.concurrent.TicketReminder;
import com.pqqqqq.directsupport.config.Config;
import com.pqqqqq.directsupport.config.PluginConfig;
import com.pqqqqq.directsupport.metrics.Metrics;

public class DirectSupport {
	/* Internals */
	private DirectSupportPlugin			dsp;
	private final Logger				log						= Logger.getLogger("Minecraft");
	private Commands					commands;

	/* Config stuff */
	private Config						cfg;
	private String						ticketFormat;
	private String						adminChatFormat;
	private String						acceptFormat;
	private int							ticketNameSize;
	private int							creationDelay;
	private int							commandsPerPage;
	private int							listPerPage;
	private int							donePerPage;
	private int							convoPerPage;
	private int							reminderDelay;
	private boolean						disallowRequests;
	private boolean						reminderEnabled;
	private SimpleDateFormat			dateFormat;

	/* Read-only */
	private boolean						oldEvents;

	/* Sets and maps */
	// Sets
	private final Set<Ticket>			activeTickets			= new HashSet<Ticket>();
	private final Set<Ticket>			completedTickets		= new HashSet<Ticket>();
	private final Set<String>			spies					= new HashSet<String>();
	private final Set<String>			blockRequestMessages	= new HashSet<String>();
	private final Set<String>			adminChat				= new HashSet<String>();

	// Maps
	private final HashMap<String, Long>	times					= new HashMap<String, Long>();

	/* Public class commands */
	public boolean parseCommand(CommandSender sender, Command cmd, String lbl, String[] args) {
		if (cmd.getName().equalsIgnoreCase("ds")) {
			return commands.executeCommand(sender, args);
		}
		return false;
	}

	public DirectSupportPlugin getPlugin() {
		return dsp;
	}

	public String getVersion() {
		return getPlugin().getDescription().getVersion();
	}

	public void log(Object msg) {
		log.info("[DirectSupport " + getVersion() + "]: " + msg.toString());
	}

	public boolean isOldEvents() {
		return oldEvents;
	}

	public String addColour(String m) {
		for (ChatColor c : ChatColor.values()) {
			m = m.replaceAll("&" + c.getChar() + "|&" + Character.toUpperCase(c.getChar()), c.toString());
		}
		return m;
	}

	/* Superclass overrides (called) */
	public void onEnable() {
		cfg = new PluginConfig(this);
		cfg.init();
		cfg.load();

		commands = new Commands(this);
		commands.populateCommands();

		oldEvents = !hasClass("org.bukkit.event.player.AsyncPlayerChatEvent");
		registerEvents();

		try {
			Metrics metrics = new Metrics(getPlugin());
            metrics.createGraph("Number of Active Tickets").addPlotter(new Metrics.Plotter("Number of Active Tickets") {

                @Override
                public int getValue() {
                    return activeTickets.size();
                }
            });

            metrics.createGraph("Number of Completed Tickets").addPlotter(new Metrics.Plotter("Number of Completed Tickets") {

                @Override
                public int getValue() {
                    return completedTickets.size();
                }
            });
			metrics.start();
		} catch (Exception e) {
			log("Metrics could not be loaded.");
		}

		getPlugin().getServer().getScheduler().scheduleAsyncRepeatingTask(getPlugin(), new TicketReminder(this), 0, reminderDelay * 20);
		log("Successfully loaded");
	}

	public void onDisable() {
		getPlugin().getServer().getScheduler().cancelTasks(getPlugin());

		log("Disabled");
	}

	/* Private class commands */
	private void registerEvents() {
		PluginManager pm = getPlugin().getServer().getPluginManager();

		if (oldEvents)
			pm.registerEvents(new ChatEventOld(this), getPlugin());
		else
			pm.registerEvents(new ChatEventNew(this), getPlugin());

		pm.registerEvents(new LeaveEvent(this), getPlugin());
	}

	private boolean hasClass(String path) {
		try {
			Class.forName(path);
			return true;
		} catch (Throwable e) {
			return false;
		}
	}

	/* Class constructor */
	public DirectSupport(DirectSupportPlugin dsp) {
		this.dsp = dsp;
	}

	/* Getters and Setters */
	public Set<Ticket> getActiveTickets() {
		return activeTickets;
	}

	public Set<Ticket> getCompletedTickets() {
		return completedTickets;
	}

	public Set<String> getSpies() {
		return spies;
	}

	public Set<String> getUserWithBlockReqMsgs() {
		return blockRequestMessages;
	}

	public Set<String> getAdminChatters() {
		return adminChat;
	}

	public HashMap<String, Long> getDelayTimes() {
		return times;
	}

	public String getTicketFormat() {
		return ticketFormat;
	}

	public void setTicketFormat(String ticketFormat) {
		this.ticketFormat = ticketFormat;
	}

	public String getAdminChatFormat() {
		return adminChatFormat;
	}

	public void setAdminChatFormat(String adminChatFormat) {
		this.adminChatFormat = adminChatFormat;
	}

	public String getAcceptFormat() {
		return acceptFormat;
	}

	public void setAcceptFormat(String acceptFormat) {
		this.acceptFormat = acceptFormat;
	}

	public SimpleDateFormat getDateFormat() {
		return dateFormat;
	}

	public void setDateFormat(SimpleDateFormat dateFormat) {
		this.dateFormat = dateFormat;
	}

	public int getCommandsPerPage() {
		return commandsPerPage;
	}

	public void setCommandsPerPage(int commandsPerPage) {
		this.commandsPerPage = commandsPerPage;
	}

	public int getTicketMaxNameSize() {
		return ticketNameSize;
	}

	public void setTicketMaxNameSize(int ticketNameSize) {
		this.ticketNameSize = ticketNameSize;
	}

	public int getCreationDelay() {
		return creationDelay;
	}

	public void setCreationDelay(int creationDelay) {
		this.creationDelay = creationDelay;
	}

	public int getActiveTicketsPerPage() {
		return listPerPage;
	}

	public void setActiveTicketsPerPage(int listPerPage) {
		this.listPerPage = listPerPage;
	}

	public int getCompletedTicketsPerPage() {
		return donePerPage;
	}

	public void setCompletedTicketsPerPage(int donePerPage) {
		this.donePerPage = donePerPage;
	}

	public int getConversationMessagesPerPage() {
		return convoPerPage;
	}

	public void setConversationMessagesPerPage(int convoPerPage) {
		this.convoPerPage = convoPerPage;
	}

	public boolean doesDisallowRequests() {
		return disallowRequests;
	}

	public void setDisallowRequests(boolean disallowRequests) {
		this.disallowRequests = disallowRequests;
	}

	public int getReminderDelay() {
		return reminderDelay;
	}

	public void setReminderDelay(int reminderDelay) {
		this.reminderDelay = reminderDelay;
	}

	public boolean isReminderEnabled() {
		return reminderEnabled;
	}

	public void setReminderEnabled(boolean reminderEnabled) {
		this.reminderEnabled = reminderEnabled;
	}

	public Config getConfig() {
		return cfg;
	}
}
