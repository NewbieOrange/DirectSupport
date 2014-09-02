package com.pqqqqq.directsupport.bukkit;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.java.JavaPlugin;

import com.pqqqqq.directsupport.DirectSupport;

public class DirectSupportPlugin extends JavaPlugin {
	private DirectSupport	ds;

	@Override
	public void onDisable() {
		ds.onDisable();
	}

	@Override
	public void onEnable() {
		ds = new DirectSupport(this);

		ds.onEnable();
	}

	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String lbl, String[] args) {
		return ds.parseCommand(sender, cmd, lbl, args);
	}
}
