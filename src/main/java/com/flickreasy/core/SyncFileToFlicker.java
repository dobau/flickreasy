package com.flickreasy.core;

import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.List;
import java.util.Observer;
import java.util.Vector;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.log4j.Logger;

import com.flickr4java.flickr.Flickr;
import com.flickr4java.flickr.FlickrException;
import com.flickreasy.core.exception.AuthenticationFailureException;

public class SyncFileToFlicker {
	
	private static final Logger logger = Logger.getLogger(SyncFileToFlicker.class);
	
	private UploaderFile flickrUploader;
	
	private AtomicInteger fileTotal = new AtomicInteger(0);
	private AtomicInteger fileSended = new AtomicInteger(0);
	private AtomicInteger fileSuccessSended = new AtomicInteger(0);
	private AtomicInteger fileFailureSended = new AtomicInteger(0);
	
	private File startFile = null;
	
	private UploadListener uploadListener = new UploadListener(); 
	
	private FireObservable onEnd = new FireObservable();
	
	private FlickrEasyDirectoryWalk directoryWalk = new FlickrEasyDirectoryWalk();
	
	private List<File> fileSendedList = new Vector<File>();
	
	public SyncFileToFlicker() throws FlickrException, AuthenticationFailureException, IOException {
		Flickr flickr = FlickrFactory.getInstance();
		flickrUploader = UploaderFile.create(flickr.getAuth());
	}

	public void start(String filePath) throws IOException {
		reset();
		
		startFile = new File(filePath);
		if (startFile == null) {
			return;
		}
		
		flickrUploader.start();
		
		List<File> resultList = directoryWalk.walk(startFile);
		
		removeFilesAlreadySyncs(resultList);
		
		logger.info("Sending "+resultList.size() + " files");
		
		if (!resultList.isEmpty()) {
			for (File file : resultList) {
				Future<Boolean> isSendedFile = flickrUploader.upload(file);
				fileTotal.incrementAndGet();
				process(isSendedFile, file, fileSendedList);			
			}
		} else {
			doEnd();
		}
		
		flickrUploader.end();
	}
	
	private void removeFilesAlreadySyncs(List<File> resultList) {
		for (int i = 0; i < resultList.size(); i++) {
			File file = resultList.get(i);
			
			SyncControl syncControl = SyncControlManager.getInstance().get(file);
			if (syncControl != null) {
				if (syncControl.isSended(file)) {
					resultList.remove(i);
					i--;
				}
			}
		}
	}

	public void stop() throws IOException {
		flickrUploader.stopNow();
	}
	
	private void process(final Future<Boolean> isSendedFile, final File file, final Collection<File> results) {
		new Thread(new Runnable() {
			public void run() {
				try {
					if (isSendedFile.get()) {
						onSuccess(file);
					} else {
						onFailure(file);
					}
				} catch (Exception e) {
					onFailure(file);
				}
				
				onComplete(file);
				
				if (getFileTotal() == getFileSended()) {
					doEnd();
				}
			}
		}).start();
	}

	protected void doEnd() {
		onEnd.fire(null);
	}

	public int getFileTotal() {
		return fileTotal.get();
	}
	
	public int getFileSended() {
		return fileSended.get();
	}
	
	public int getFileFailureSended() {
		return fileFailureSended.get();
	}
	
	public int getFileSuccessSended() {
		return fileSuccessSended.get();
	}
	
	public UploadListener getUploadListener() {
		return uploadListener;
	}
	
	protected void onSuccess(File file) {
		fileSendedList.add((File)file);
		fileSuccessSended.incrementAndGet();
		
		SyncControlManager.getInstance().add(file);
		
		uploadListener.fireOnSuccess(file);
	}
	
	protected void onFailure(File file) {
		fileFailureSended.incrementAndGet();
		
		uploadListener.fireOnFailure(file);
	}
	
	protected void onComplete(File file) {
		fileSended.incrementAndGet();
		
		logger.info("End process file " + file.getPath());
		
		uploadListener.fireOnComplete(file);
	}
	
	private void reset() {
		fileTotal = new AtomicInteger(0);
		fileSended = new AtomicInteger(0);
		fileSuccessSended = new AtomicInteger(0);
		fileFailureSended = new AtomicInteger(0);
		
		fileSendedList = new Vector<File>();
	}

	public void onEnd(Observer observer) {
		onEnd.addObserver(observer);
	}
	
}