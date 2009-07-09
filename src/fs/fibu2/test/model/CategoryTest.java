package fs.fibu2.test.model;

import java.util.Arrays;
import java.util.TreeSet;
import java.util.Vector;

import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.dom4j.tree.DefaultDocument;

import fs.fibu2.data.model.Category;
import fs.xml.XMLToolbox;

/**
 * Tests the class Category
 * @author Simon Hampe
 *
 */
public class CategoryTest {

	public static void main(String[] args) {
		BasicConfigurator.configure();
		Logger.getRootLogger().setLevel(Level.TRACE);
		
		//Testing basic generation and equality
		Category c = Category.getCategory(null, "bla");
		Category.getExistingCategories();
		Category c2 = Category.getCategory(new Vector<String>(Arrays.asList("bla","bli","blu")));
		Category.getExistingCategories();
		Category c3 = Category.getCategory(c, "bli");
		Category.getExistingCategories();
		Category c4 = Category.getCategory(c3, "blu");
		Category.getExistingCategories();
		System.out.println(c);
		System.out.println(c2);
		System.out.println(c2.parent.parent.equals(c));
		System.out.println(c4.equals(c2));

		
		//XML Features
		DefaultDocument doc = new DefaultDocument();
		try {
			doc.setRootElement(c4.getConfiguration());
			System.out.println(XMLToolbox.getDocumentAsPrettyString(doc));
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		//Performance test
		String testword = "abcdefghijklmnopqrstuvwxyz";
		for(int i = 100; i <= 1000; i+=100) {
			Vector<String> wordlist = new Vector<String>();
			for(int j = 0; j < 2; j++) {
				wordlist.add(testword);
			}
			System.out.println("Creating " + i + " identical categories");
			long time1 = System.currentTimeMillis();
			for(int j = 0; j < i; j++) {
				Category.getCategory(wordlist);
			}
			long time2 = System.currentTimeMillis();
			System.out.println("Time needed: " + (time2-time1));
			System.out.println("List of all categories: " + Category.getExistingCategories());
		}
		
		//Sorting and comparison
		Category c5 = Category.getCategory(c3,"baa");
		System.out.println("Greatest common parent: " + c5.getGreatestCommonParent(c2));
		TreeSet<Category>  treeset = new TreeSet<Category>();
		treeset.addAll(Category.getExistingCategories());
		System.out.println(treeset);
	}

}
