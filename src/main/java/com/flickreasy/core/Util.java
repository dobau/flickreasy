package com.flickreasy.core;

import java.io.File;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.io.FilenameUtils;

public class Util {
	
	public static final List<String> EXTENSIONS = Arrays.asList("jpg");
	
	public static boolean isCompatible(String path) {
		return FilenameUtils.isExtension(path.toLowerCase(), EXTENSIONS);
	} 
	
	public static boolean isCompatible(File file) {
		return FilenameUtils.isExtension(file.getName().toLowerCase(), EXTENSIONS);
	} 

}
