package fs.fibu2.data.model;

import java.text.NumberFormat;
import java.util.Currency;
import java.util.Locale;

import org.dom4j.Element;
import org.dom4j.Node;
import org.dom4j.tree.DefaultElement;

import fs.xml.XMLConfigurable;
import fs.xml.XMLReadConfigurationException;
import fs.xml.XMLWriteConfigurationException;

/**
 * This class represent a value of an fsfibu2 journal entry together with a currency specification.
 * @author Simon Hampe
 *
 */
//TODO: I think this class is useless...

public class EntryValue implements XMLConfigurable, Comparable<EntryValue>{

	//Data
	private Currency currency;
	private float value;
	
	// CONSTRUCTOR *******************************************
	// *******************************************************
	
	public EntryValue(float value, Currency currency) throws NullPointerException {
		this.setValue(value);
		this.setCurrency(currency);
	}
	
	// GETTERS AND SETTERS ************************************
	// ********************************************************

	/**
	 * Sets the value
	 */
	public void setValue(float value) {
		this.value = value;
	}

	/**
	 * @return the value
	 */
	public float getValue() {
		return value;
	}

	/**
	 * Sets the currency
	 * @throws NullPointerException - if currency == null
	 */
	public void setCurrency(Currency currency) throws NullPointerException {
		if(currency == null) throw new NullPointerException("Null currency is not a valid currency");
		this.currency = currency;
	}

	/**
	 * @return The currency
	 */
	public Currency getCurrency() {
		return currency;
	}
	
	// XMLCONFIGURABLE ****************************************
	// ********************************************************
	
	/**
	 * This expects a root node named 'entryvalue' with subnodes 'value' and 'currency', which contain the
	 * float value and the ISO 4217 code of the currency
	 * @throws XMLWriteConfigurationException - if the expected nodes are not present or if the value is not a float value or if the currency code is not a valid ISO 4217 code
	 */
	@Override
	public void configure(Node arg0) throws XMLWriteConfigurationException {
		if(arg0 == null) throw new XMLWriteConfigurationException("Cannot configure EntryValue from null node");
		Node v = arg0.selectSingleNode("./value");
		if(v == null) throw new XMLWriteConfigurationException("Cannot configure EntryValue: value field missing.");
		try{
			value = Float.parseFloat(v.getText());
		}
		catch(NumberFormatException e) {
			throw new XMLWriteConfigurationException(e.getMessage());
		}
		Node c = arg0.selectSingleNode("./currency");
		if(c == null) throw new XMLWriteConfigurationException("Cannot configure Entry Value: currency field missing");
		try {
			currency = Currency.getInstance(c.getText());
		}
		catch(IllegalArgumentException e) {
			throw new XMLWriteConfigurationException(e.getMessage());
		}
	}

	/**
	 * Returns a node as it would be expected by configure(Node arg0)
	 */
	@Override
	public Element getConfiguration() throws XMLReadConfigurationException {
		DefaultElement root = new DefaultElement("entryvalue");
		DefaultElement v = new DefaultElement("value");
		DefaultElement c = new DefaultElement("currency");
		v.setText(value+"");
		c.setText(currency.toString());
		root.add(v);
		root.add(c);
		return root;
	}

	/**
	 * Returns 'entryvalue'
	 */
	@Override
	public String getIdentifier() {
		return "entryvalue";
	}

	/**
	 * Returns true
	 */
	@Override
	public boolean isConfigured() {
		return true;
	}
	
	// EQUALS, HASHCODE AND SUCH **************************************
	// ****************************************************************

	/**
	 * Compares the values of the two entry values
	 */
	@Override
	public int compareTo(EntryValue o) {
		if(o == null) return 1;
		return (new Float(value)).compareTo(o.getValue());
	}

	/**
	 * Returns true, if and only if the values of the two entry values agree. 
	 * The currency does not play any role.
	 */
	@Override
	public boolean equals(Object obj) {
		if(obj == this) return true;
		if(obj == null | obj.getClass() != this.getClass()) return false;
		return(value == ((EntryValue)obj).getValue());
	}

	/**
	 * @return Float.floatToIntBits(value)
	 */
	@Override
	public int hashCode() {
		return Float.floatToIntBits(value);
	}

	/**
	 * @return The value together with the currency symbol, formatted according to the default
	 * locale
	 */
	@Override
	public String toString() {
		NumberFormat nf = NumberFormat.getCurrencyInstance(Locale.getDefault());
		nf.setCurrency(currency);
		return nf.format(value);
	}

	


}
