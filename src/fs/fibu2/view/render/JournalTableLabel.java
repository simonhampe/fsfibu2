package fs.fibu2.view.render;

import java.awt.Color;

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

	//Black is the standard foreground color
	private final static Color color_foreground_normal = new Color(0,0,0);
	
	//A normal, white background
	private final static Color color_background_normal = new Color(255,255,255);
	
	//A light blue background for selected cells
	private final static Color color_background_selected = new Color(211,220,229);
	
	//A light green background for EntrySeparators
	private final static Color color_separator_normal = new Color(177,237,241);
	
	//A slightly darker green for selected EntrySeparator cells
	private final static Color color_separator_selected = //new Color(250,197,247);
															new Color(151,202,206);
	
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
		setValues(isSelected, isSeparator);
	}
	
	/**
	 * Adjusts color and font values according to the given parameters and resets the following parameters:<br>
	 * - Icon = null<br>
	 * - Foreground= black <br>
	 * - Alignment = left <br>
	 * - Text = ""
	 */
	public void setValues(boolean isSelected, boolean isSeparator) {
		setOpaque(true);
		setBackground(isSelected? (isSeparator? color_separator_selected : color_background_selected) : 
								(isSeparator? color_separator_normal : color_background_normal));
		if(!isSeparator) setFont(getFont().deriveFont(10));
		setIcon(null);
		setForeground(color_foreground_normal);
		setHorizontalAlignment(LEFT);
		setText("");
	}
	
}
