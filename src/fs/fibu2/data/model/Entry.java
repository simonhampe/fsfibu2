package fs.fibu2.data.model;

import java.text.ParseException;
import java.util.Currency;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.List;

import org.dom4j.Element;
import org.dom4j.Node;
import org.dom4j.tree.DefaultElement;

import fs.fibu2.data.format.Fsfibu2DateFormats;
import fs.fibu2.lang.Fsfibu2StringTableMgr;
import fs.xml.XMLConfigurable;
import fs.xml.XMLReadConfigurationException;
import fs.xml.XMLWriteConfigurationException;

/**
 * This class represents an fsfibu2 journal entry. It is immutable, at least as far as its account object is immutable (Since Account is an
 * interface, no guarantee can be made as to whether the object is immutable). 
 * @author Simon Hampe
 *
 */
public final class Entry implements XMLConfigurable {

	// MEMBERS ******************************
	
	private String 					name;
	private float 					value;
	private Currency 				currency;
	private GregorianCalendar		date;
	private Category				category;
	private Account					account;
	private HashMap<String,String> 	accountInformation;
	private String					additionalInformation;
	
	private static final String sgroup = "fs.fibu2.Entry";
	
	// CONSTRUCTOR *******************************
	// *******************************************
	
	/**
	 * Creates an entry with values set to the given parameters
	 * @param name The name of this entry, e.g. 'Getränkeeinnahmen'. A null string is interpreted as empty string.
	 * @param value The value of this entry, e.g. '140,00'.
	 * @param currency The currency of this entry, e.g. EUR. Must not be null.
	 * @param date The date of this entry, e.g. 13.4.2009. Must not be null.
	 * @param category The category of this entry, e.g. 'Getränke / Süßigkeiten - Einnahmen'. 
	 * 			The null category is interpreted as the root category
	 * @param account The account of this entry, e.g. 'bank_account'. Must not be null.
	 * @param accountInformation A list of information specific for the account type, e.g. account sheet number. 
	 * 			Null is interpreted as the empty list.
	 * @param additionalInformation Some additional information for this entry, e.g. 'Beinhaltet 5 Euro Bestechung von Fex, damit er seinen Fuffziger kleinmachen durfte.'
	 * 			A null string is interpreted as the empty string
	 * @throws NullPointerException - If any of the values where 'Must not be null' is written, is null.
	 */
	public Entry(String name, float value, Currency currency,
			GregorianCalendar date, Category category, Account account,
			HashMap<String,String> accountInformation, String additionalInformation) 
			throws NullPointerException 									{
		setName(name);
		setValue(value);
		setCurrency(currency);
		setDate(date);
		setCategory(category);
		setAccount(account);
		setAccountInformation(accountInformation);
		setAdditionalInformation(additionalInformation);
	}
	
	/**
	 * Expects a node containing the following subnodes (with appropriately formatted content): <br>
	 * - name (String)* <br>
	 * - value (float)* <br>
	 * - currency (A String containing the ISO 4217 code)* <br>
	 * - date (A date formatted as dd.MM.yyyy)* <br>
	 * - category (A node as described in {@link Category})
	 * - account (String)*<br>
	 * - accountinformation (A node containing subnodes for each field id - named after that id - which contain the associated
	 * data as a string) <br>
	 * - additionalinformation (String)
	 * @throws XMLWriteConfigurationException - If any of the nodes marked * is missing or contains invalid data.
	 */
	@SuppressWarnings("unchecked")
	public Entry(Node n) throws XMLWriteConfigurationException{
		if(n == null) throw new XMLWriteConfigurationException("Invalid entry configuration: Null node");
		
		Node nameNode = n.selectSingleNode("./name");
		if(nameNode == null) throw new XMLWriteConfigurationException("Invalid entry configuration: Name node missing");
		else setName(nameNode.getText());
		
		Node valueNode = n.selectSingleNode("./value"); 
		if(valueNode == null) throw new XMLWriteConfigurationException("Invalid entry configuration: Value node missing");
		try{
			setValue(Float.parseFloat(valueNode.getText()));
		}
		catch(NumberFormatException ne) {
			throw new XMLWriteConfigurationException("Invalid entry configuration: Value node contains nonnumerical data '" + valueNode.getText() + "'");
		}
		
		Node currencyNode = n.selectSingleNode("./currency");
		if(currencyNode == null) throw new XMLWriteConfigurationException("Invalid entry configuration: Currency node missing");
		try {
			setCurrency(Currency.getInstance(currencyNode.getText()));
		}
		catch(IllegalArgumentException ie) {
			throw new XMLWriteConfigurationException("Invalid entry configuration: Currency code '" + currencyNode.getText() + "' unknown");
		}
		
		Node dateNode = n.selectSingleNode("./date");
		if(dateNode == null) throw new XMLWriteConfigurationException("Invalid entry configuration: Date node missing");
		try {
			GregorianCalendar cal = new GregorianCalendar();
			cal.setTime(Fsfibu2DateFormats.getEntryDateFormat().parse(dateNode.getText()));
			setDate(cal);
		}
		catch(ParseException pe) {
			throw new XMLWriteConfigurationException("Invalid entry configuration: Date format '" + dateNode.getText() + "' not conforming to " + 
			Fsfibu2DateFormats.entryDateFormat);
		}
		
		Node categoryNode = n.selectSingleNode("./category");
		if(categoryNode == null) setCategory(Category.getRootCategory());
		else {
			setCategory(Category.getCategory(categoryNode));
		}
		
		Node accountNode = n.selectSingleNode("./account");
		if(accountNode == null) throw new XMLWriteConfigurationException("Invalid entry configuration: Account node missing");
		try {
			setAccount(AccountLoader.getAccount(accountNode.getText()));
		}
		catch(IllegalArgumentException ie) {
			throw new XMLWriteConfigurationException("Invalid entry configuration: Account id '" + accountNode.getText() + "' unknown");
		}
		
		Node accountInfoNode = n.selectSingleNode("./accountinformation");
		if(accountInfoNode == null) setAccountInformation(null);
		else {
			try {
				HashMap<String,String> accInf = new HashMap<String, String>();
				List l = accountInfoNode.selectNodes("./*");
				for(Object f : l) {
					accInf.put(((Node)f).getName(), ((Node)f).getText());
				}
				setAccountInformation(accInf);
			}
			catch(Exception e) {
				throw new XMLWriteConfigurationException("Invalid entry configuration: Account information node contains invalid data");
			}
		}
		
		Node additionalInfoNode = n.selectSingleNode("./additionalinformation");
		if(additionalInfoNode == null) setAdditionalInformation(null);
		else setAdditionalInformation(additionalInfoNode.getText());
	}

	// GETTERS / SETTERS *************************
	// *******************************************
	
	/**
	 * @return the name of this entry 
	 */
	public String getName() {
		return name;
	}

	/**
	 * Sets the name of this entry (null is transformed to the empty string)
	 */
	private void setName(String name) {
		this.name = name == null? "" : name;
	}

	/**
	 * @return The value of this entry
	 */
	public float getValue() {
		return value;
	}

	/**
	 * Sets the value of this entry. The number of fraction digits actually displayed is determined by the currency.
	 */
	private void setValue(float value) {
		this.value = value;
	}

	/**
	 * @return The currency of this entry
	 */
	public Currency getCurrency() {
		return currency;
	}

	/**
	 * Sets the currency of this entry. 
	 * @throws NullPointerException - If currency == null
	 */
	private void setCurrency(Currency currency) throws NullPointerException {
		if(currency == null) throw new NullPointerException("Null currency invalid.");
		this.currency = currency;
	}

	/**
	 * The date of this entry (usually the invoice date)
	 */
	public GregorianCalendar getDate() {
		return (GregorianCalendar)date.clone();
	}

	/**
	 * Sets the date of this entry. 
	 * @throws NullPointerException - If date == null
	 */
	private void setDate(GregorianCalendar date) throws NullPointerException {
		if(date == null) throw new NullPointerException("Null date invalid");
		this.date = (GregorianCalendar)date.clone();
	}

	/**
	 * @return the category of this entry
	 */
	public Category getCategory() {
		return category;
	}

	/**
	 * Sets the category of this entry. The null category is replaced by the root category
	 */
	private void setCategory(Category category) {
		this.category = category == null? Category.getRootCategory() : category;
	}

	/**
	 * @return the account of this entry
	 */
	public Account getAccount() {
		return account;
	}

	/**
	 * Sets the account of this entry. 
	 * @throws NullPointerException - If account == null
	 */
	private void setAccount(Account account) throws NullPointerException {
		if(account == null) throw new NullPointerException("Null account invalid.");
		this.account = account;
	}

	/**
	 * @return A list of information fields specific for the account type of this entry
	 */
	public HashMap<String,String> getAccountInformation() {
		return new HashMap<String,String>(accountInformation);
	}

	/**
	 * Sets the list of information for the account type of this entry. A null list
	 * is replaced by the empty list
	 */
	private void setAccountInformation(HashMap<String, String> accountInformation) {
		this.accountInformation = accountInformation == null? new HashMap<String,String>() : accountInformation;
	}

	/**
	 * @return A string containing additional information about this entry
	 */
	public String getAdditionalInformation() {
		return additionalInformation;
	}

	/**
	 * Sets the additional information about this entry. A null string is replaced by the empty string
	 */
	private void setAdditionalInformation(String additionalInformation) {
		this.additionalInformation = additionalInformation == null? "" : additionalInformation;
	}
	
	// TOSTRING, CLONE *************************************
	// *****************************************************
	
	/**
	 * Returns a multi-line representation of this entry
	 */
	@Override
	public String toString() {
		StringBuilder b = new StringBuilder();
		
		b.append(Fsfibu2StringTableMgr.getString("fs.fibu2.global.name"));
		b.append(": " + name + "\n");
		b.append(Fsfibu2StringTableMgr.getString(sgroup + ".value"));
		b.append(": " + value + "\n");
		b.append(Fsfibu2StringTableMgr.getString(sgroup + ".currency"));
		b.append(": " + currency.getSymbol() + "\n");
		b.append(Fsfibu2StringTableMgr.getString(sgroup + ".date"));
		b.append(": " + Fsfibu2DateFormats.getEntryDateFormat().format(date.getTime()) + "\n");
		b.append(Fsfibu2StringTableMgr.getString(sgroup + ".category"));
		b.append(": " + category.toString() + "\n");
		b.append(Fsfibu2StringTableMgr.getString(sgroup + ".account"));
		b.append(": " + account.getName() + "\n");
		for(String field : accountInformation.keySet()) {
			b.append("  " + account.getFieldNames().get(field) + ": " + accountInformation.get(field) + "\n");
		}
		b.append(Fsfibu2StringTableMgr.getString(sgroup + ".info"));
		b.append(": " + additionalInformation);
		return b.toString();
	}
	
	/**
	 * @return A copy of this entry
	 */
	public Entry clone() {
		return new Entry(name,value,currency,getDate(),category,account,new HashMap<String, String>(accountInformation),additionalInformation);
	}
	
	// XMLCONFIFGURABLE *********************************
	// **************************************************
	
	
	/**
	 * @return 'entry'
	 */
	@Override
	public String getIdentifier() {
		return "entry"; 
	}

	/**
	 * @return true
	 */
	@Override
	public boolean isConfigured() {
		return true;
	}

	/**
	 * @throws XMLWriteConfigurationException - Always, since instances of this class should be immutable
	 */
	@Override
	public void configure(Node n) throws XMLWriteConfigurationException {
		throw new XMLWriteConfigurationException("Cannot modify immutable entry.");
	}

	/**
	 * @return A node formatted as specified by configure(Node n)
	 */
	@Override
	public Element getConfiguration() throws XMLReadConfigurationException {
		DefaultElement root = new DefaultElement("entry");
		
		DefaultElement nameNode = new DefaultElement("name");
		nameNode.setText(name);
		root.add(nameNode);
		
		DefaultElement valueNode = new DefaultElement("value");
		valueNode.setText(value + "");
		root.add(valueNode);
		
		DefaultElement currencyNode = new DefaultElement("currency");
		currencyNode.setText(currency.getCurrencyCode());
		root.add(currencyNode);
		
		DefaultElement dateNode = new DefaultElement("date");
		dateNode.setText(Fsfibu2DateFormats.getEntryDateFormat().format(date.getTime()));
		root.add(dateNode);
		
		Node categoryNode = category.getConfiguration();
		root.add(categoryNode);
		
		DefaultElement accountNode = new DefaultElement("account");
		accountNode.setText(account.getID());
		root.add(accountNode);
		
		DefaultElement accountInfoNode = new DefaultElement("accountinformation");
		for(String key : accountInformation.keySet()) {
			DefaultElement k = new DefaultElement(key);
			k.setText(accountInformation.get(key));
			accountInfoNode.add(k);
		}
		root.add(accountInfoNode);
		
		DefaultElement additionalInfoNode = new DefaultElement("additionalinformation");
		additionalInfoNode.setText(additionalInformation);
		root.add(additionalInfoNode);
		
		return root;
	}

}
