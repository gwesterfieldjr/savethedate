package com.xnihilosoft.savethedate.helper;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

public class WeddingDate  {
	
	private Calendar calendar;
	final SimpleDateFormat dateFormat = new SimpleDateFormat("MMMM d, yyyy", Locale.US);
	
	public WeddingDate() {
		this.calendar = Calendar.getInstance();
	}
	
	public WeddingDate(int year, int month, int dayOfMonth) {
		this.calendar = Calendar.getInstance();
		calendar.set(year, month, dayOfMonth);
	}

	public int getYear() {
		return calendar.get(Calendar.YEAR);
	}

	public void setYear(int year) {
		calendar.set(Calendar.YEAR, year);
	}

	public int getMonth() {
		return calendar.get(Calendar.MONTH);
	}

	public void setMonth(int month) {
		calendar.set(Calendar.MONTH, month);
	}

	public int getDayOfMonth() {
		return calendar.get(Calendar.DAY_OF_MONTH);
	}

	public void setDayOfMonth(int dayOfMonth) {
		calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);
	}
	
	public String getWeddingDate() {
		return dateFormat.format(calendar.getTime());
	}
	
	public String getWeddingDate(SimpleDateFormat format) {
		return format.format(calendar.getTime());
	}
	
}
