package com.flickreasy.core;

import java.io.File;
import java.io.Serializable;
import java.util.List;
import java.util.Vector;

public class SyncControl implements Serializable {

	private static final long serialVersionUID = -7530873270116464959L;
	
	private List<String> fileSendedList = new Vector<String>();
	
	public synchronized void add(String file) {
		fileSendedList.add(file);
	}
	
	public synchronized boolean isSended(String file) {
		return fileSendedList.contains(file);
	}
	
	public synchronized boolean isSended(File file) {
		return isSended(file.getAbsolutePath());
	}

}
