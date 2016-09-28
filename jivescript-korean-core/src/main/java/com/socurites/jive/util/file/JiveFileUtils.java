package com.socurites.jive.util.file;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * File Utils
 * 
 * @author socurites
 *
 */
public class JiveFileUtils {
	/**
	 * return directory file accordint to path
	 * 
	 * @param dirPath	absolute path or relative path
	 * @return
	 */
	public static File getDir(String dirPath) {
		Path path = Paths.get(dirPath);
		File templateDir = null;
		if ( path.isAbsolute() ) {
			templateDir = new File(dirPath);
		} else {
			templateDir = new File(JiveFileUtils.class.getClassLoader().getResource(dirPath).getFile());
		}
		
		return templateDir;
	}
}
