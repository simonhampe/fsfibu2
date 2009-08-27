package fs.fibu2.module;

import java.awt.BorderLayout;
import java.awt.Color;
import java.util.prefs.Preferences;

import javax.swing.Icon;
import javax.swing.JLabel;
import javax.swing.JPanel;

import fs.fibu2.data.model.Journal;
import fs.fibu2.view.render.JournalModule;

/**
 * This is an example module doing... err... more or less nothing.
 * @author Simon Hampe
 *
 */
public class ExampleModule implements JournalModule {

	@Override
	public JPanel getComponent(Preferences node, Journal j) {
		JPanel panel = new JPanel();
		JLabel label = new JLabel("FUN!!");
		label.setBackground(Color.GREEN);
		label.setForeground(Color.RED);
		panel.setLayout(new BorderLayout());
		panel.add(label,BorderLayout.CENTER);
		return panel;
	}

	@Override
	public String getID() {
		return "ff2module_example";
	}

	@Override
	public Icon getTabViewIcon() {
		return null;
	}

	@Override
	public String getTabViewName() {
		return "FUN!!";
	}

	@Override
	public String getTabViewTooltip() {
		return "More FUN!!";
	}

	@Override
	public void insertPreferences(Preferences node, Journal j) {
		//Ignore
	}

}
