package fs.fibu2.view.render;

import java.awt.Component;

import javax.swing.DefaultListCellRenderer;
import javax.swing.JLabel;
import javax.swing.JList;

import fs.fibu2.data.model.Category;

/**
 * Renders cells of a category list. It does essentially the same as the {@link DefaultListCellRenderer}. For a given category, 
 * only the last string will be displayed as text. However, hierarchically lower strings will be preceded by n times a certain indicator symbol, usually
 * a blank space, where n is the order of the category 
 * @author Simon Hampe
 *
 */
public class CategoryListRenderer extends DefaultListCellRenderer {

	/**
	 * compiler-generated serial version uid
	 */
	private static final long serialVersionUID = -2253937189394942135L;
	private String levelIndicator = "  ";

	/**
	 * Constructs a CategoryListRenderer
	 * @param levelIndicator A string which indicate hierarchical order of category strings by preceding a certain number of times lower level strings.
	 * If null, the double blank space is used.
	 */
	public CategoryListRenderer(String levelIndicator) { 
		if(levelIndicator != null) this.levelIndicator = levelIndicator;
	}
	
	@Override
	public Component getListCellRendererComponent(JList list, Object value,
			int index, boolean isSelected, boolean cellHasFocus) {
		JLabel label = (JLabel)super.getListCellRendererComponent(list, value, index, isSelected,
				cellHasFocus);
		StringBuilder text = new StringBuilder();
		if(value instanceof Category) {
			for(int i = 0; i < ((Category)value).getOrder() - 1; i++) text.append(levelIndicator);
			text.append(((Category) value).tail);
		}
		label.setText(text.toString());
		
		return label;
	}
	
	
	
	
}
