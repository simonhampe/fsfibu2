package fs.fibu2.view.render;

import java.awt.Color;

import javax.swing.BorderFactory;
import javax.swing.JLabel;

import fs.fibu2.data.model.EntrySeparator;

/**
 * This class provides a label with default colors and borders for a {@link JournalTableRenderer}.
 * @author Simon Hampe
 *
 */
public class JournalTableLabel extends JLabel {

	// COLOR VALUES **********************************************
	// ***********************************************************
	
	/**
	 * compiler-generated serial version uid
	 */
	private static final long serialVersionUID = 6924486351932614378L;

	//A normal, white background
	private final static Color color_background_normal = new Color(255,255,255);
	
	//A light blue background for selected cells
	private final static Color color_background_selected = new Color(211,220,229);
	
	//A light green background for EntrySeparators
	private final static Color color_separator_normal = new Color(157,249,145);
	
	//A slightly darker green for selected EntrySeparator cells
	private final static Color color_separator_selected = new Color(134,213,124);
	
//	//The default border color is a dark grey
//	private final static Color color_border_normal = new Color(111,111,111);
//	
//	//A darker blue for borders of selected cells
//	private final static Color color_border_selected = new Color(60,35,215);
	
	/**
	 * Creates a basic label with colors and borders set to appropriate default values
	 * @param isSelected Whether the corresponding table cell is selected
	 * @param isSeparator Whether the corresponding table cell represents part of an {@link EntrySeparator}. In this
	 * case the label will have different colors
	 */
	public JournalTableLabel(boolean isSelected, boolean isSeparator) {
		super();
		setOpaque(true);
		setBackground(isSelected? (isSeparator? color_separator_selected : color_background_selected) : 
								(isSeparator? color_separator_normal : color_background_normal));
		if(!isSeparator) setFont(getFont().deriveFont(10));
	}
}
