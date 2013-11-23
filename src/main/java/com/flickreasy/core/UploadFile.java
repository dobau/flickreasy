package com.flickreasy.core;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;

import org.apache.commons.lang.WordUtils;
import org.apache.log4j.Logger;

import com.flickr4java.flickr.FlickrException;
import com.flickr4java.flickr.RequestContext;
import com.flickr4java.flickr.auth.Auth;
import com.flickr4java.flickr.photosets.Photoset;
import com.flickr4java.flickr.photosets.Photosets;
import com.flickr4java.flickr.photosets.PhotosetsInterface;
import com.flickr4java.flickr.uploader.UploadMetaData;
import com.flickr4java.flickr.uploader.Uploader;
import com.flickreasy.core.exception.AuthenticationFailureException;


public class UploadFile implements Callable<Boolean> {
	
	private static final Logger logger = Logger.getLogger(UploadFile.class);
	
	private final File file;
	private final Auth auth;
	
	private String photoId = null;
	
	private static Map<String, Photoset> photosetCache = new HashMap<String, Photoset>();

	public UploadFile(File file, Auth auth) {
		this.file = file;
		this.auth = auth;
	}

	public Boolean call() {
		try {
			if (!exists()) {
				RequestContext.getRequestContext().setAuth(auth);
				
				UploadMetaData metaData = new UploadMetaData();
				metaData.setTitle(createTitle());
				metaData.setTags(createTags());
				//metaData.setAsync(true);
				
				Uploader uploader = FlickrFactory.getUploader();
				
				logger.info("Sending file "+file.getPath());
				photoId = uploader.upload(file, metaData);
				
				putFileInSet(photoId);
			}
			
			return Boolean.TRUE;
		} catch(Exception e) {
			logger.warn("Failure sending file "+file.getPath(), e);
			return Boolean.FALSE;
		}
	}

	protected String createTitle() {
		return WordUtils.capitalize(file.getName());
	}
	
	protected Collection<String> createTags() {
		List<String> tagsList = new ArrayList<String>();
		
		Calendar lastModified = Calendar.getInstance();
		lastModified.setTimeInMillis(file.lastModified());
		
		// ano
		tagsList.add(Integer.toString(lastModified.get(Calendar.YEAR)));
		
		// set
		tagsList.add(getSetName());
		
		return tagsList;
	}

	private void putFileInSet(String photoId) throws FlickrException, AuthenticationFailureException, IOException {
		String photosetName = getSetName();
		
		Photoset photoset = findSet(photosetName);
		
		if (photoset == null) {
			photoset = FlickrFactory.getPhotosetsInterface().create(photosetName, "", photoId);
			photosetCache.put(photosetName, photoset);
		} else {
			FlickrFactory.getPhotosetsInterface().addPhoto(photoset.getId(), photoId);	
		}
	}
	
	private Photoset findSet(String photosetName) throws FlickrException, AuthenticationFailureException, IOException {
		Photoset photoset = photosetCache.get(photosetName);
		if (photoset != null) {
			return photoset;
		}
		
		PhotosetsInterface photosetsInterface = FlickrFactory.getPhotosetsInterface();
		Photosets photosets = photosetsInterface.getList(auth.getUser().getId());
		
		for (Photoset p : photosets.getPhotosets()) {
			if (photosetName.compareToIgnoreCase(p.getTitle()) == 0) {
				photosetCache.put(photosetName, p);
				return p;
			}
		}
		
		return photoset;
	}

	public String getSetName() {
		return WordUtils.capitalize(file.getParentFile().getName());
	}
	
	public boolean exists() throws FlickrException, AuthenticationFailureException, IOException {
		return Boolean.FALSE;
	}

}
