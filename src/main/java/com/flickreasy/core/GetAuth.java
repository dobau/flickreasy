package com.flickreasy.core;

import com.flickr4java.flickr.Flickr;
import com.flickr4java.flickr.auth.Auth;

public interface GetAuth {

	public Auth execute(Flickr flickr);
	
}
