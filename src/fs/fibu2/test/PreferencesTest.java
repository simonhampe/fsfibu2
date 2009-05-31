package fs.fibu2.test;

import java.util.GregorianCalendar;

import fs.fibu2.data.format.Fsfibu2DateFormats;

public class PreferencesTest {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		
		System.out.println(Fsfibu2DateFormats.getEntryDateFormat().format(new GregorianCalendar(2001,11,31).getTime()));
		
	}

}
