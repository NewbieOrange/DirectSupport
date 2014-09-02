package com.pqqqqq.directsupport;

import java.util.ArrayList;
import java.util.Date;

import org.bukkit.Location;
import org.bukkit.entity.Player;

public class Ticket {
	private int							id;
	private Player						creator;
	private Date						creationDate;
	private Location					where;
	private Player						helper			= null;
	private String						requestString	= null;
	private boolean						completed		= false;

	private final ArrayList<ChatLine>	lines			= new ArrayList<ChatLine>();

	private static int					curId			= 1;

	public Ticket(Player creator, Date creationDate, Location where) {
		this.creator = creator;
		this.creationDate = creationDate;
		this.where = where;
		id = curId++;
	}

	public Player getCreator() {
		return creator;
	}

	public void setCreator(Player creator) {
		this.creator = creator;
	}

	public Date getCreationDate() {
		return creationDate;
	}

	public void setCreationDate(Date creationDate) {
		this.creationDate = creationDate;
	}

	public Location getWhere() {
		return where;
	}

	public void setWhere(Location where) {
		this.where = where;
	}

	public Player getHelper() {
		return helper;
	}

	public void setHelper(Player helper) {
		this.helper = helper;
	}

	public String getRequestString() {
		return requestString;
	}

	public void setRequestString(String requestString) {
		this.requestString = requestString;
	}

	public boolean isCompleted() {
		return completed;
	}

	public void setCompleted(boolean completed) {
		this.completed = completed;
	}

	public int getId() {
		return id;
	}

	public ArrayList<ChatLine> getChatLines() {
		return lines;
	}

	public static void setCurId(int id) {
		curId = id;
	}

	@Override
	public boolean equals(Object other) {
		if (other instanceof Ticket) {
			Ticket ticket = (Ticket) other;
			return getId() == ticket.getId();
		}
		return false;
	}

	@Override
	public int hashCode() {
		return getId();
	}
}
