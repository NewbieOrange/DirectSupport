package com.pqqqqq.directsupport.config;

import java.io.File;
import java.text.SimpleDateFormat;

import org.bukkit.configuration.file.YamlConfiguration;

import com.pqqqqq.directsupport.DirectSupport;

public class PluginConfig implements Config {
	private DirectSupport		ds;
	private YamlConfiguration	cfg;
	private final File			dir		= new File("plugins/DirectSupport/");
	private final File			file	= new File(dir + "/config.yml");

	public PluginConfig(DirectSupport ds) {
		this.ds = ds;
	}

	@Override
	public void init() {
		try {
			dir.mkdirs();
			file.createNewFile();
			cfg = new YamlConfiguration();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Override
	public void load() {
		try {
			cfg.load(file);

			cfg.options()
					.header("\r\nDirectSupport configuration\r\n"
							+ "DirectSupport(ds) was created by: Pqqqqq\r\n\r\n"
							+ "tickets: Configuration related to tickets\r\n"
							+ "  channel-format: The format for an incoming message in a DirectSupport channel\r\n"
							+ "  max-name-size: The maximum size a ticket reason can be\r\n"
							+ "  delay-after-cancellation: The delay, in seconds, a player has to wait before creating another ticket after the completion of a previous one\r\n"
							+ "  disallow-creation-when-no-ops: Disallow players to create tickets when there is no one in the server to accept them\r\n"
							+ "entires-per-page: Configuration related to the amount of entries per page in a command\r\n"
							+ "  help-menu: The amount of entries, per page, the help menu (/ds help) has\r\n"
							+ "  active-tickets: The amount of entries, per page, the active ticket list (/ds list) has\r\n"
							+ "  completed-tickets: The amount of entries, per page, the completed ticket list (/ds done) has\r\n"
							+ "  convo-messages: The amount of entries, per page, the conversation viewer (/ds view) has\r\n"
							+ "ticket-reminders: Configuration related to repeating constant ticket reminders for mods\r\n"
							+ "  enabled: Whether or not ticket reminders are enabled\r\n"
							+ "  delay: The delay, in seconds, between each reminder\r\n"
							+ "admin-chat-format: The format for the admin chatting command (/ds a)\r\n"
							+ "accept-ticket-format: The message a player receives when a moderator accepts their ticket\r\n"
							+ "date-format: The format dates are shown (see SimpleDateFormat in java):\r\n"
							+ "  European Format: EEEE, dd MMMM yyyy HH:mm:ss\r\n"
							+ "  North-American Format: EEEE, MMMM dd, yyyy hh:mm:ss a\r\n \r\n");

			ds.setTicketFormat(ConfigUtil.getString(cfg, file, "general.tickets.channel-format",
					"&3[DirectSupport -> &b{OTHER}&3] &b{PLAYER}&f: {MESSAGE}"));
			ds.setAdminChatFormat(ConfigUtil.getString(cfg, file, "general.admin-chat-format", "&3[DirectSupport &bMods&3] &b{PLAYER}&f: {MESSAGE}"));
			ds.setAcceptFormat(ConfigUtil
					.getString(cfg, file, "general.accept-ticket-format", "&3[DirectSupport] &b{HELPER} &3is now assisting you."));
			ds.setDateFormat(new SimpleDateFormat(ConfigUtil.getString(cfg, file, "general.date-format", "EEEE, MMMM dd, yyyy hh:mm:ss a")));
			ds.setCommandsPerPage(ConfigUtil.getInt(cfg, file, "general.entires-per-page.help-menu", 7));
			ds.setActiveTicketsPerPage(ConfigUtil.getInt(cfg, file, "general.entires-per-page.active-tickets", 7));
			ds.setCompletedTicketsPerPage(ConfigUtil.getInt(cfg, file, "general.entires-per-page.completed-tickets", 7));
			ds.setConversationMessagesPerPage(ConfigUtil.getInt(cfg, file, "general.entires-per-page.convo-messages", 7));
			ds.setTicketMaxNameSize(ConfigUtil.getInt(cfg, file, "general.tickets.max-name-size", 50));
			ds.setCreationDelay(ConfigUtil.getInt(cfg, file, "general.tickets.delay-after-cancellation", 10));
			ds.setDisallowRequests(ConfigUtil.getBoolean(cfg, file, "general.tickets.disallow-creation-when-no-ops", true));
			ds.setReminderEnabled(ConfigUtil.getBoolean(cfg, file, "general.ticket-reminders.enabled", true));
			ds.setReminderDelay(Math.max(1, ConfigUtil.getInt(cfg, file, "general.ticket-reminders.delay", 60)));
			cfg.save(file);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
