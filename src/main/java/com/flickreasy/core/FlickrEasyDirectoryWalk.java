package com.flickreasy.core;

import static com.flickreasy.core.Util.isCompatible;

import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.List;
import java.util.Vector;

import org.apache.commons.io.DirectoryWalker;
import org.apache.log4j.Logger;

public class FlickrEasyDirectoryWalk extends DirectoryWalker<File> {
	
	private static final Logger logger = Logger.getLogger(FlickrEasyDirectoryWalk.class);

	@Override
	protected void handleFile(File file, int depth, Collection<File> results) {
		handleFile(file, results);
	}

	@Override
	protected void handleDirectoryStart(File directory, int depth, Collection<File> results) throws IOException {
		logger.info("Start process folder " + directory.getPath());
	}
	
	@Override
	protected void handleDirectoryEnd(File directory, int depth, Collection<File> results) throws IOException {
		// Cria a collection e adiciona todos os arquivos na collection
		logger.info("End process folder " + directory.getPath());
	}
	
	@Override
	protected void handleEnd(Collection<File> results) throws IOException {
	}

	public List<File> walk(File startFile) throws IOException {
		List<File> results = new Vector<File>();
		
		if (startFile.isDirectory()) {
			walk(startFile, results);
		} else {
			handleStart(startFile, results);
			handleFile(startFile, results);
            handleEnd(results);
		}
		
		return results;
	}

	protected void handleFile(File file, Collection<File> results) {
		if (isCompatible(file)) {
			results.add(file);
		}
	}
	
}