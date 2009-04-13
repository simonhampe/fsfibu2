package fs.fibu2.data.model;

import java.util.HashMap;
import java.util.HashSet;

import javax.swing.undo.UndoManager;

import org.dom4j.Element;
import org.dom4j.Node;
import org.dom4j.tree.DefaultElement;

import com.sun.swing.internal.plaf.synth.resources.synth;

import fs.fibu2.undo.JournalUndoManager;
import fs.xml.XMLConfigurable;
import fs.xml.XMLReadConfigurationException;
import fs.xml.XMLWriteConfigurationException;

/**
 * This class implements an fsfibu2 journal. A journal is essentially an (unsorted) collection of entries together
 * with some additional data: <br>
 * - ReadingPoints: A reading point is a certain day of the year at which the user whishes to see a bilancial overview and potentially reset
 * all sums. <br>
 * - Start values: A journal contains a start value for each account it uses.<br>
 * <br> 
 * @see 
 * @author Simon Hampe
 *
 */
public class Journal implements XMLConfigurable {

	// FIELDS ****************************************
	// ***********************************************
	
	private HashSet<Entry> listOfEntries = new HashSet<Entry>();
	private HashSet<ReadingPoint> listOfReadingPoints = new HashSet<ReadingPoint>();
	private HashMap<Account, Float> startValues = new HashMap<Account, Float>();
	
	private JournalUndoManager manager = JournalUndoManager.getInstance(this);
	
	// CONSTRUCTOR ***********************************
	// ***********************************************
	
	/**
	 * Creates an empty journal.
	 */
	public Journal() {
	}
	
	/**
	 * Creates a journal from the given node 
	 * @throws XMLWriteConfigurationException - If the node is null or invalid
	 */
	public Journal(Node n) throws XMLWriteConfigurationException  {
		configure(n);
	}
	
	// GETTERS AND SETTERS ***************************
	// ***********************************************
	
	
	
	// XMLCONFIGURABLE *******************************
	// ***********************************************
	
	/**
	 * Expects the following node structure:<br>
	 * - An optional node called startvalues with subnodes for each account. The subnodes have
	 * as name the account id (which must be known to AccountLoader) and as text a valid
	 * float value indicating the start value for this account. All accounts for which
	 * no start value has been given will be initialized to zero <br> 
	 * - An arbitrary number of valid {@link ReadingPoint} nodes <br>
	 * - An arbitrary number of valid {@link Entry} nodes <br>
	 * This operation is not undoable, however it will reset the associated undomanager. The journal
	 * and its {@link UndoManager} will remain completely unchanged, if any {@link XMLWriteConfigurationException} occurs. 
	 * @throws XMLWriteConfigurationException - If any of the following cases holds:<br>
	 * - There is an account ID node with an unknown ID <br>
	 * - Any of the {@link ReadingPoint} or {@link Entry} nodes is faulty <br>
	 * - n == null
	 */
	@Override
	public synchronized void configure(Node n) throws XMLWriteConfigurationException {
		if(n == null) throw new XMLWriteConfigurationException("Can't create journal from null node.");
		
		HashMap<Account, Float> newstartvalues = new HashMap<Account, Float>();
		HashSet<Entry> newentries = new HashSet<Entry>();
		HashSet<ReadingPoint> newreadings = new HashSet<ReadingPoint>();
		
		Node startNode = n.selectSingleNode("./startvalues");
		if(startNode != null) {
			for(Object accountNode : startNode.selectNodes("./*")) {
				try {
					Account a = AccountLoader.getAccount(((Node)accountNode).getName());
					float v = Float.parseFloat(((Node)accountNode).getText());
					newstartvalues.put(a, v);
				}
				catch(ClassCastException ce) {
					//Skip
				}
				catch(NumberFormatException ne) {
					throw new XMLWriteConfigurationException("Invalid journal configuration: " + 
							"The value " + ((Node)accountNode).getText() + " for account " + 
							((Node)accountNode).getName() + " is not a valid float value");
				}
				catch(IllegalArgumentException ie) {
					throw new XMLWriteConfigurationException("Invalid journal configuration: Account ID " + ((Node)accountNode).getName() + " unknown.");
				}
			}
		}
		
		for(Object pointNode : n.selectNodes("./readingpoint")) {
			try {
				ReadingPoint rp = new ReadingPoint((Node)pointNode);
				newreadings.add(rp);
			}
			catch(ClassCastException ce) {
				throw new XMLWriteConfigurationException("Invalid journal configuration: Reading point node is not a node");
			}
		}
		
		for(Object entryNode : n.selectNodes("./entry")) {
			try {
				Entry e = new Entry((Node) entryNode);
				newentries.add(e);
			}
			catch(ClassCastException ce) {
				throw new XMLWriteConfigurationException("Invalid journal configuration: Entry node is not a node");
			}
		}
		
		//Copy values and reset manager
		listOfEntries = newentries;
		listOfReadingPoints = newreadings;
		startValues = newstartvalues;
		manager.discardAllEdits();
		//TODO: fire journalchanged or so...
	}

	/**
	 * @return A node named 'journal' with subnodes as specified by configure(Node n)
	 */
	@Override
	public synchronized Element getConfiguration() throws XMLReadConfigurationException {
		DefaultElement root = new DefaultElement("journal");
		
		DefaultElement startNode = new DefaultElement("startvalues");
		for(Account a : startValues.keySet()) {
			DefaultElement accountNode = new DefaultElement(a.getID());
			accountNode.setText(startValues.get(a).toString());
			startNode.add(accountNode);
		}
		root.add(startNode);
		
		for(ReadingPoint rp : listOfReadingPoints) {
			root.add(rp.getConfiguration());
		}
		
		for(Entry e : listOfEntries) {
			root.add(e.getConfiguration());
		}
		
		return root;
	}

	/**
	 * @return 'journal'
	 */
	@Override
	public String getIdentifier() {
		return "journal";
	}

	/**
	 * @return true
	 */
	@Override
	public boolean isConfigured() {
		return true;
	}

}
