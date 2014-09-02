package com.pqqqqq.directsupport;

import org.bukkit.entity.Player;

public class ChatLine {
	private Player	writer;
	private String	message;

	public ChatLine(Player writer, String message) {
		this.writer = writer;
		this.message = message;
	}

	public Player getWriter() {
		return writer;
	}

	public void setWriter(Player writer) {
		this.writer = writer;
	}

	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = message;
	}
}
