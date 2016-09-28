package com.socurites.jive;


public class AbstractJiveTestCase {
	/**
	 * get absoulte path of script directory from resource path.
	 * 
	 * @param resourcePath
	 * @return
	 */
	protected String getPathFromResource(String resourcePath) {
		return this.getClass().getClassLoader().getResource(resourcePath).getPath();
	}
}
