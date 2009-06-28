package fs.fibu2.test.application;

import org.apache.log4j.BasicConfigurator;

import fs.fibu2.application.FrameworkLoader;

public class FrameworkLoaderTest {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		BasicConfigurator.configure();
		FrameworkLoader.loadFramework();
	}

}
