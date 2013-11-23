package com.flickreasy.core;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.Hashtable;
import java.util.Map;

import org.apache.log4j.Logger;

public class SyncControlManager {
	
	private static final Logger logger = Logger.getLogger(SyncControlManager.class);
	
	private static final String CONFIG_FILE_NAME = ".flickreasy";
	
	private static Map<File, SyncControl> syncControlCache = new Hashtable<File, SyncControl>();
	
	private static SyncControlManager ref = null;
	
	public static SyncControlManager getInstance() {
		if (ref == null) {
			ref = new SyncControlManager();
		}
		
		return ref;
	}
	
	public synchronized SyncControl getOrCreate(File file) {
		File syncControlFile = getSyncControlFile(file);
		
		if (!syncControlFile.exists()) {
			createSyncControlFile(file);
		}
		
		return get(file);
	}
	
	public synchronized void saveOrCreate(File file, SyncControl syncControl) {
		File syncControlFile = getSyncControlFile(file);
		
		if (!syncControlFile.exists()) {
			createSyncControlFile(file);
		}
		
		save(syncControlFile, syncControl);
	}

	public synchronized SyncControl get(File file) {
		File syncControlFile = getSyncControlFile(file);
		
		SyncControl syncControl = syncControlCache.get(syncControlFile);
		
		if (syncControl == null) {
			try {
				ObjectInputStream fInput = new ObjectInputStream(new FileInputStream(syncControlFile));
				syncControl = (SyncControl) fInput.readObject();
				fInput.close();
			} catch (Exception e) {
				syncControl = new SyncControl();
			}
			
			syncControlCache.put(syncControlFile, syncControl);
		}
		
		return syncControl;
	}
	
	public synchronized void save(File syncControlFile, SyncControl syncControl) {
		try {
			ObjectOutputStream fOutput = new ObjectOutputStream(new FileOutputStream(syncControlFile));
			fOutput.writeObject(syncControl);
			fOutput.close();
		} catch(Exception e) {
			logger.error(e.getMessage(), e);
		}
	}

	public synchronized void createSyncControlFile(File file) {
		File folder = file;
		
		if (!file.isDirectory()) {
			folder = file.getParentFile();
		}
		
		File syncControlFile = getSyncControlFile(file);
		
		if (folder.canRead() && folder.canWrite()) {
			try {
				save(syncControlFile, new SyncControl());
			} catch (Exception e) {
				logger.error(e.getMessage(), e);
			}
		}
	}
	
	private synchronized File getSyncControlFile(File file) {
		File folder = file;
		
		if (!file.isDirectory()) {
			folder = file.getParentFile();
		}
		
		return new File(folder, CONFIG_FILE_NAME);
	}

	public synchronized void add(File file) {
		SyncControl syncControl = getOrCreate(file);
		syncControl.add(file.getAbsolutePath());
		
		File syncControlFile = getSyncControlFile(file);
		
		save(syncControlFile, syncControl);
	}
	
}
