package fs.fibu2.print;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Rectangle;

/**
 * This class provides some static methods which simplify painting in a graphics context
 * @author Simon Hampe
 *
 */
public class PrintHelper {

	public enum XAlign {LEFT,RIGHT,CENTER};
	public enum YALign {TOP, BOTTOM, CENTER};
	
	/**
	 * Prints a string and scales it to fit the given parameters
	 * @param g The graphics context to draw on. Changes to the context will not be reversed at the end (such as e.g. color)
	 * @param s The string to print
	 * @param r The rectangle in which the string should be drawn
	 * @param c The color in which the string should be drawn
	 * @param xalign Where the string should be drawn horizontally within the rectangle
	 * @param yalign Where the string should be drawn vertically within the rectangle
	 * @param scale Whether the font size should be changed. If false is selected, the given font in the graphics context is used. If 
	 * true is chosen, the font size is adapted, such that the String is as large as possible while still fitting within the rectangle
	 */
	public static void printString(Graphics g, String s, Rectangle r, Color c, XAlign xalign, YALign yalign, boolean scale) {
		g.setColor(c);
		//Determine font size
		if(scale) {
			float fontSize = r.height;
			g.setFont(g.getFont().deriveFont(fontSize));
			while(g.getFontMetrics().getStringBounds(s, g).getWidth() > r.width && fontSize > 1) {
				fontSize--;
				g.setFont(g.getFont().deriveFont(fontSize));
			}
		}
		//Determine x-position
		double xpos = r.x;
		switch (xalign) {
		case LEFT:break;
		case RIGHT: xpos += (r.width - g.getFontMetrics().getStringBounds(s, g).getWidth()); break;
		case CENTER: xpos += ((r.width - g.getFontMetrics().getStringBounds(s, g).getWidth())/2); break;
		}
		//Determin y-position
		double ypos = r.y + r.height;
		switch(yalign) {
		case BOTTOM: break;
		case TOP: ypos -= (r.height - g.getFontMetrics().getStringBounds(s, g).getHeight()); break;
		case CENTER: ypos -= ((r.height - g.getFontMetrics().getStringBounds(s, g).getHeight())/2);
		}
		g.drawString(s, (int)xpos, (int)ypos);
	}
	
}
