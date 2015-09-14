package com.gkxim.android.xtify;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

public class ISO8601 {
	public static final String DATE_PATTERN = "yyyy-MM-dd'T'HH:mm:ssZ";

	static public String toString(java.util.Calendar c) {
		return toString(c.getTime());
	}

	static public String toString(java.util.Date d) {
		SimpleDateFormat sdf = new SimpleDateFormat(DATE_PATTERN);
		return sdf.format(d);
	}

	static public Date toDate(String timeString) throws ParseException {
		if (timeString == null) {
			return null;
		}
		return toCalendar(timeString).getTime();
	}

	static public Calendar toCalendar(String timeString) throws ParseException {
		if (timeString == null) {
			return null;
		}
		SimpleDateFormat fmt = new SimpleDateFormat(DATE_PATTERN);
		Date d = fmt.parse(timeString);
		Calendar c = Calendar.getInstance();
		c.setTime(d);
		return c;
	}
}