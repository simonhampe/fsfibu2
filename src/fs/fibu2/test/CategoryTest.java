package fs.fibu2.test;

import java.util.Arrays;
import java.util.Vector;

import fs.fibu2.data.model.Category;

/**
 * Tests the class Category
 * @author Simon Hampe
 *
 */
public class CategoryTest {

	public static void main(String[] args) {
		Category c = Category.getCategory(null, "bla");
		Category c2 = Category.getCategory(new Vector<String>(Arrays.asList("bla","bli","blu")));
		System.out.println(c);
		System.out.println(c2);
		System.out.println(c2.parent.parent.equals(c));

	}

}
