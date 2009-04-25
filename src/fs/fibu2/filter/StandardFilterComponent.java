package fs.fibu2.filter;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.HashSet;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.Icon;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JTextField;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import fs.fibu2.filter.event.StandardComponentListener;
import fs.fibu2.lang.Fsfibu2StringTableMgr;
import fs.gui.SwitchIconLabel;
import fs.validate.LabelIndicValidator;

/**
 * This class implements the standard component, which is used by all standard fsfibu2
 * {@link EntryFilter}s. It consists of one or two entry fields and three radio buttons,
 * where the entry field number depends on the choice of the radio button. The radio
 * buttons read 'Equality', 'Regular expression', 'Range' (only the last one needs
 * two entry fields). The two entry fields are labelled 'Min:' and 'Max:' and the
 * single entry field according to a constructor parameter. On construction you also
 * have to specify a validator which checks if the content of this component is valid.
 * Listeners are notified of selection changes in the radio buttons and content changes
 * in the entry fields 
 * @author Simon Hampe
 *
 */
public class StandardFilterComponent extends JPanel {

	// FIELDS ************************************
	// *******************************************
	
	/**
	 * compiler-generated serial version uid
	 */
	private static final long serialVersionUID = -1648777743795267462L;

	private final static String sgroup = "fs.fibu2.filter.StandardFilterComponent";
	
	// COMPONENTS ********************************
	// *******************************************
	
	private JTextField singleEntry = new JTextField();
	private JTextField minEntry = new JTextField();
	private JTextField maxEntry = new JTextField();
	
	private SwitchIconLabel singleLabel = new SwitchIconLabel();
	private SwitchIconLabel minLabel = new SwitchIconLabel();
	private SwitchIconLabel maxLabel = new SwitchIconLabel();
	
	private Box singleEntryBox = new Box(BoxLayout.X_AXIS);
	private Box rangeEntryBox = new Box(BoxLayout.Y_AXIS);
	
	private JRadioButton equalityButton = new JRadioButton();
	private JRadioButton regexButton = new JRadioButton();
	private JRadioButton rangeButton = new JRadioButton();
	
	// LISTENERS *************************************
	// ***********************************************
	
	public enum Selection {EQUALITY, REGEX, RANGE};
	
	private HashSet<StandardComponentListener> listeners = new HashSet<StandardComponentListener>();
	
	private ActionListener selectionListener = new ActionListener() {
		@Override
		public void actionPerformed(ActionEvent e) {
			switch(getSelection()) {
			case RANGE: rangeEntryBox.setVisible(true);
						singleEntryBox.setVisible(false); break;
			default:	rangeEntryBox.setVisible(false);
						singleEntryBox.setVisible(true);				
			}
			fireSelectionChanged();
		}
	};
	
	private DocumentListener contentListener = new DocumentListener() {
		@Override
		public void changedUpdate(DocumentEvent e) { fireContentChanged();}
		@Override
		public void insertUpdate(DocumentEvent e) { fireContentChanged();}
		@Override
		public void removeUpdate(DocumentEvent e) { fireContentChanged();}
	};
	
	// CONSTRUCTOR ***********************************
	// ***********************************************
	
	/**
	 * Creates a standard editor component
	 * @param singleLabelText The label text for the single entry field. If null, the empty
	 * 			string is used
	 * @param singleValidator The validator for the single entry field. If null, no validation takes place.
	 * @param minValidator The validator for the min entry field. If null, no validation takes place.
	 * @param maxValidator The validator for the max entry field. If null, no validation takes place.
	 * @param singleIconReference The icon reference for the single entry {@link SwitchIconLabel}
	 * @param minIconReference The icon reference for the min entry {@link SwitchIconLabel}
	 * @param maxIconReference The icon reference for the max entry {@link SwitchIconLabel}
	 * @param singleContent The initial content of the single entry field. Only to be specified, if
	 * the initial selection is not RANGE
	 * @param minContent The initial content of the min entry field. Only to be specified, if the
	 * initial selection is RANGE
	 * @param maxContent The initial content of the max entry field. Only to be specified, if the 
	 * initial selection is RANGE
	 * @param initialSelection The radio button which is initially selected
	 */
	public StandardFilterComponent(String singleLabelText, LabelIndicValidator<JTextField> singleValidator,
			LabelIndicValidator<JTextField> minValidator,
			LabelIndicValidator<JTextField> maxValidator,
			Icon singleIconReference,
			Icon minIconReference, Icon maxIconReference,
			String singleContent, String minContent, String maxContent, 
			Selection initialSelection) {
		
		//Init components
		singleLabel.setText(singleLabelText == null? "" : singleLabelText);
			singleLabel.setIconReference(singleIconReference);
		minLabel.setText(Fsfibu2StringTableMgr.getString(sgroup + ".min"));
			minLabel.setIconReference(minIconReference);
		maxLabel.setText(Fsfibu2StringTableMgr.getString(sgroup + ".max"));
			maxLabel.setIconReference(maxIconReference);
		
		singleEntry.setText(singleContent);
		minEntry.setText(minContent);
			minEntry.setToolTipText(Fsfibu2StringTableMgr.getString(sgroup + ".mintooltip"));
		maxEntry.setText(maxContent);
			maxEntry.setToolTipText(Fsfibu2StringTableMgr.getString(sgroup + ".maxtooltip"));
		
		equalityButton.setText(Fsfibu2StringTableMgr.getString(sgroup + ".equality"));
		regexButton.setText(Fsfibu2StringTableMgr.getString(sgroup + ".regex"));
		rangeButton.setText(Fsfibu2StringTableMgr.getString(sgroup + ".range"));
		ButtonGroup group = new ButtonGroup();
			group.add(equalityButton);
			group.add(rangeButton);
			group.add(regexButton);
		switch(initialSelection) {
		case EQUALITY: equalityButton.setSelected(true); break;
		case RANGE: rangeButton.setSelected(true); break;
		case REGEX: regexButton.setSelected(true); break;
		}
		
		//Layout
		Box box = new Box(BoxLayout.Y_AXIS);
		box.setAlignmentX(LEFT_ALIGNMENT);
		
		singleEntryBox.setAlignmentX(LEFT_ALIGNMENT);
			singleEntryBox.add(singleLabel); singleEntryBox.add(singleEntry);
		rangeEntryBox.setAlignmentX(LEFT_ALIGNMENT);
			Box minBox = new Box(BoxLayout.X_AXIS);
			minBox.setAlignmentX(LEFT_ALIGNMENT);
				minBox.add(minLabel); minBox.add(minEntry);
			Box maxBox = new Box(BoxLayout.X_AXIS);
			maxBox.setAlignmentX(LEFT_ALIGNMENT);
				maxBox.add(maxLabel); maxBox.add(maxEntry);
			rangeEntryBox.add(minBox); rangeEntryBox.add(maxBox);
		box.add(singleEntryBox);
		box.add(rangeEntryBox);
		box.add(equalityButton);
		box.add(regexButton);
		box.add(rangeButton);
		
		add(box);
		
		//Validation
		if(singleValidator != null) {
			singleValidator.addComponent(singleEntry, singleLabel);
		}
		if(minValidator != null) {
			minValidator.addComponent(minEntry, minLabel);
		}
		if(maxValidator != null) {
			maxValidator.addComponent(maxEntry, maxLabel);
		}
		
		//Listeners
		equalityButton.addActionListener(selectionListener);
		regexButton.addActionListener(selectionListener);
		rangeButton.addActionListener(selectionListener);
		
		singleEntry.getDocument().addDocumentListener(contentListener);
		minEntry.getDocument().addDocumentListener(contentListener);
		maxEntry.getDocument().addDocumentListener(contentListener);
	}
	
	// LISTENER METHODS *****************************
	// **********************************************
	
	/**
	 * Adds l to the list of listeners, if it isn't null
	 */
	public void addStandardComponentListener(StandardComponentListener l) {
		if(l != null) listeners.add(l);
	}
	
	/**
	 * Removes l from the list of listeners
	 */
	public void removeStandardComponentListener(StandardComponentListener l) {
		listeners.remove(l);
	}
	
	protected void fireSelectionChanged() {
		for(StandardComponentListener l : listeners) l.selectionChanged(this, getSelection());
	}
	
	protected void fireContentChanged() {
		for(StandardComponentListener l : listeners) l.contentChanged(this);
	}
	
	// GETTER METHODS ***********************************
	// **************************************************
	
	/**
	 * @return The content of the single entry box
	 */
	public String getSingleEntry() {
		return singleEntry.getText();
	}
	
	/**
	 * @return The content of the min entry box
	 */
	public String getMinEntry() {
		return minEntry.getText();
	}
	
	/**
	 * @return The content of the max entry box
	 */
	public String getMaxEntry() {
		return maxEntry.getText();
	}
	
	/**
	 * @return The Selection value associated to the currently selected radio box.
	 */
	public Selection getSelection() {
		return equalityButton.isSelected()? Selection.EQUALITY :
			rangeButton.isSelected()? Selection.RANGE : Selection.REGEX;
	}
}
