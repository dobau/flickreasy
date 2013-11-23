package com.flickreasy.core;

import java.io.File;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import com.flickr4java.flickr.auth.Auth;

public class UploaderFile {
	
	private ExecutorService service = null;
	
	private final Auth auth;
	
	private static final int SIZE_POOL = 10;
	
	private UploaderFile(Auth auth) {
		this.auth = auth;
	}
	
	public static UploaderFile create(Auth auth) {
		return new UploaderFile(auth);
	}

	public Future<Boolean> upload(File file) {
		if (service == null) {
			throw new RuntimeException("Uploader not started");
		}
		
		return service.submit(new UploadFile(file, auth));
	}
	
	public void start() {
		service = Executors.newFixedThreadPool(SIZE_POOL);
	}

	public void end() {
		if (service != null) {
			service.shutdown();
		}
	}

	public void stopNow() {
		if (service != null) {
			service.shutdownNow();
		}
	}

}
