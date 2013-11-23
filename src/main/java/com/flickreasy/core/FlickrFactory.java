package com.flickreasy.core;

import java.io.IOException;

import org.apache.log4j.Logger;

import com.flickr4java.flickr.Flickr;
import com.flickr4java.flickr.FlickrException;
import com.flickr4java.flickr.REST;
import com.flickr4java.flickr.auth.Auth;
import com.flickr4java.flickr.photosets.PhotosetsInterface;
import com.flickr4java.flickr.uploader.Uploader;
import com.flickreasy.core.exception.AuthenticationFailureException;

public class FlickrFactory {
	
	private static final Logger logger = Logger.getLogger(FlickrFactory.class);
	
	private static final String key = "544d0267d96c5a721e40589811de2436";
	private static final String secret = "c49693388890198c";
	
	private static Flickr flickr = null;
	private static Uploader uploader = null;
	private static PhotosetsInterface photosetsInterface = null;
	
	private static GetAuth getAuth = null;
	private static Auth auth = null;
	
	public synchronized static void setGetAuth(GetAuth getAuth) {
		FlickrFactory.getAuth = getAuth;
	}

	public synchronized static Flickr getInstance() throws FlickrException, AuthenticationFailureException, IOException {
		if (flickr == null) {
			flickr = new Flickr(key, secret, new REST());
			
			if (flickr.getAuth() == null) {
				Auth auth = getAuth();
				
				flickr.setAuth(auth);
			}
		}
		
		return flickr;
	}

	public synchronized static Auth getAuth() throws FlickrException, AuthenticationFailureException, IOException {
		if (auth == null) {
			if (getAuth == null) {
				getAuth = new ConsoleGetAuth();
			} 
			
			auth = getAuth.execute(flickr);
		}

		return auth;
	}

	public synchronized static Uploader getUploader() throws FlickrException, AuthenticationFailureException, IOException {
		if (uploader == null) {
			uploader = getInstance().getUploader();
		}
		
		return uploader;
	}
	
	public synchronized static PhotosetsInterface getPhotosetsInterface() throws FlickrException, AuthenticationFailureException, IOException {
		if (photosetsInterface == null) {
			photosetsInterface = getInstance().getPhotosetsInterface();
		}
		
		return photosetsInterface;
	}
	
}
