package fs.fibu2.module;

import java.util.prefs.Preferences;

import javax.swing.Icon;
import javax.swing.JPanel;

import fs.fibu2.data.model.Journal;
import fs.fibu2.view.model.JournalModule;

/**
 * This module contains a journal table with an editing toolbar and combo boxes for basic year and category filters. By default, the current
 * year is automatically selected.
 * @author Simon Hampe
 *
 */
public class OverviewModule implements JournalModule {

	@Override
	public JPanel getComponent(Preferences node, Journal j) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getID() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Icon getTabViewIcon() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getTabViewName() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getTabViewTooltip() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void insertPreferences(Preferences node) {
		// TODO Auto-generated method stub

	}

}
