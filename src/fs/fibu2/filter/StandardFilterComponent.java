package fs.fibu2.filter;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.text.Format;
import java.text.ParseException;
import java.util.Comparator;
import java.util.HashSet;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.ImageIcon;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JTextField;
import javax.swing.event.ChangeEvent;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import org.dom4j.Document;

import fs.fibu2.filter.event.StandardComponentListener;
import fs.fibu2.lang.Fsfibu2StringTableMgr;
import fs.fibu2.resource.Fsfibu2DefaultReference;
import fs.gui.SwitchIconLabel;
import fs.validate.LabelIndicValidator;
import fs.validate.ValidationResult.Result;
import fs.xml.ResourceDependent;
import fs.xml.ResourceReference;
import fs.xml.XMLDirectoryTree;

/**
 * This class implements the standard component, which is used by all standard fsfibu2
 * {@link EntryFilter}s. It consists of one or two entry fields and three radio buttons,
 * where the entry field number depends on the choice of the radio button. The radio
 * buttons read 'Equality', 'Regular expression', 'Range' (only the last one needs
 * two entry fields). The two entry fields are labelled 'Min:' and 'Max:' and the
 * single entry field according to a constructor parameter. The component also does some
 * basic validation: <br>
 * - If REGEX is selected, the single entry fields must contain a valid regular expression <br>
 * - If a format is specified upon creation, each entry field must conform to that format<br>
 * - If a comparator is specified upon creation, min <= max must be fulfilled according to that comparator (But this only
 * issues a warning, if it is not fulfilled)<br>
 * Listeners are notified of selection changes in the radio buttons and content changes
 * in the entry fields 
 * @author Simon Hampe
 *
 */
public class StandardFilterComponent extends JPanel implements ResourceDependent{

	// FIELDS ************************************
	// *******************************************
	
	/**
	 * compiler-generated serial version uid
	 */
	private static final long serialVersionUID = -1648777743795267462L;

	protected final static String sgroup = "fs.fibu2.filter.StandardFilterComponent";
	
	private Format format;
	private Comparator<String> comparator;
	
	// COMPONENTS ********************************
	// *******************************************
	
	protected JTextField singleEntry = new JTextField();
	protected JTextField minEntry = new JTextField();
	protected JTextField maxEntry = new JTextField();
	
	protected SwitchIconLabel singleLabel = new SwitchIconLabel();
	protected SwitchIconLabel minLabel = new SwitchIconLabel();
	protected SwitchIconLabel maxLabel = new SwitchIconLabel();
	
	protected Box singleEntryBox = new Box(BoxLayout.X_AXIS);
	protected Box rangeEntryBox = new Box(BoxLayout.Y_AXIS);
	
	protected JRadioButton equalityButton = new JRadioButton();
	protected JRadioButton regexButton = new JRadioButton();
	protected JRadioButton rangeButton = new JRadioButton();
	
	protected ImageIcon icon = new ImageIcon(Fsfibu2DefaultReference.getDefaultReference().getFullResourcePath(this, "graphics/share/warn.png"));
	
	// LISTENERS & VALIDATORS ************************
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
			validator.stateChanged(new ChangeEvent(e.getSource()));
			fireSelectionChanged();
		}
	};
	
	private DocumentListener contentListener = new DocumentListener() {
		@Override
		public void changedUpdate(DocumentEvent e) { validator.changedUpdate(e);fireContentChanged();}
		@Override
		public void insertUpdate(DocumentEvent e) { validator.changedUpdate(e);fireContentChanged();}
		@Override
		public void removeUpdate(DocumentEvent e) { validator.removeUpdate(e);fireContentChanged();}
	};
	
	protected LabelIndicValidator<JTextField> validator;
	
	// CONSTRUCTOR ***********************************
	// ***********************************************
	
	/**
	 * Creates a standard editor component
	 * @param singleLabelText The label text for the single entry field. If null, the empty
	 * 			string is used
	 * @param entryFormat The format to which all content must conform. If null, every format is valid
	 * @param entryComparator Compares min and max to ensure min <= max. If null, we always have min <= max. The comparator should not
	 * throw any exception, if the format of one string is not valid.
	 * @param singleContent The initial content of the single entry field. Only to be specified, if
	 * the initial selection is not RANGE
	 * @param minContent The initial content of the min entry field. Only to be specified, if the
	 * initial selection is RANGE
	 * @param maxContent The initial content of the max entry field. Only to be specified, if the 
	 * initial selection is RANGE
	 * @param initialSelection The radio button which is initially selected
	 */
	public StandardFilterComponent(String singleLabelText, 
			Format entryFormat, Comparator<String> entryComparator,
			String singleContent, String minContent, String maxContent, 
			Selection initialSelection) {
		
		//Copy fields
		this.format = entryFormat;
		this.comparator = entryComparator;
		
		//Init components
		singleLabel.setText(singleLabelText == null? "" : singleLabelText);
			singleLabel.setIconReference(icon);
		minLabel.setText(Fsfibu2StringTableMgr.getString(sgroup + ".min") + ": ");
			minLabel.setIconReference(icon);
		maxLabel.setText(Fsfibu2StringTableMgr.getString(sgroup + ".max") + ": ");
			maxLabel.setIconReference(icon);
		
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
		validator = new LabelIndicValidator<JTextField>(null, icon, icon) {			
			@Override
			protected void registerToComponent(JTextField arg0) {/*Ignore*/}
			@Override
			protected void unregisterFromComponent(JTextField arg0) {/*Ignore*/}

			@Override
			public Result validate(JTextField component) {
				Result result = Result.CORRECT;
				String tooltip = null;
				switch(getSelection()) {
				case EQUALITY:
					if(component != singleEntry) break;
					if(format == null) break;
					else {
						try {
							format.parseObject(singleEntry.getText());
						}
						catch(ParseException e) {
							result = Result.INCORRECT;
							tooltip = Fsfibu2StringTableMgr.getString(sgroup + ".invalidformat",e.getMessage());
						}
					}
					break;
				case REGEX:
					if(component != singleEntry) break;
					else { 
						try {
							Pattern.compile(singleEntry.getText());
						}
						catch(PatternSyntaxException e) {
							result = Result.INCORRECT;
							tooltip = Fsfibu2StringTableMgr.getString(sgroup + ".invalidregex");
						}
					}
					break;
				case RANGE:
					if(component == singleEntry) break;
					//If the text is empty, it is valid
					if(component.getText().length() == 0) break;
					//Check for format integrity
					try {
						if(format != null) format.parseObject(component.getText());
					}
					catch(ParseException e) {
						result = Result.INCORRECT;
						tooltip = Fsfibu2StringTableMgr.getString(sgroup + ".invalidformat",e.getMessage());
						break;
					}
					//Only check for order integrity, if the other field is also formatted properly
					try {
						if(format != null) format.parseObject((component == minEntry? maxEntry : minEntry).getText());
					}
					catch(ParseException e) {
						break;
					}
					if(!(comparator.compare(getMinEntry(),getMaxEntry()) <= 0)) {
						result = Result.WARNING;
						tooltip = Fsfibu2StringTableMgr.getString(sgroup + ".minmaxorder",minEntry.getText(), maxEntry.getText());
					}
					break;
				}
				setToolTipText(component, tooltip);
				return result;
			}
			
		};
		validator.addComponent(singleEntry, singleLabel);
		validator.addComponent(minEntry, minLabel);
		validator.addComponent(maxEntry, maxLabel);
		validator.validate();
		
		//Listeners
		equalityButton.addActionListener(selectionListener);
		regexButton.addActionListener(selectionListener);
		rangeButton.addActionListener(selectionListener);
		switch(initialSelection) {
		case EQUALITY: equalityButton.doClick(); break;
		case RANGE: rangeButton.doClick(); break;
		case REGEX: regexButton.doClick(); break;
		}
		
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
	 * @return The content of the min entry box. If the content is the empty string, it returns null
	 */
	public String getMinEntry() {
		return minEntry.getText().length() == 0 ? null : minEntry.getText();
	}
	
	/**
	 * @return The content of the max entry box. If the content is the empty string, it returns null
	 */
	public String getMaxEntry() {
		return maxEntry.getText().length() == 0 ? null : maxEntry.getText();
	}
	
	/**
	 * @return The Selection value associated to the currently selected radio box.
	 */
	public Selection getSelection() {
		return equalityButton.isSelected()? Selection.EQUALITY :
			rangeButton.isSelected()? Selection.RANGE : Selection.REGEX;
	}
	
	/**
	 * @return Whether the current content of this editor represents a valid filter
	 */
	public Result validateFilter() {
		return validator.validate().getOverallResult();
	}

	//RESOURCE-DEPENDENT *********************************
	// ***************************************************
	
	/**
	 * Ignored. the default reference is used
	 */
	@Override
	public void assignReference(ResourceReference r) {
		//Ignored		
	}

	/**
	 * Expects one icon (basedir)/graphics/share/warn.png
	 */
	@Override
	public Document getExpectedResourceStructure() {
		XMLDirectoryTree tree = new XMLDirectoryTree();
		tree.addPath("graphics/share/warn.png");
		return tree;
	}
}
