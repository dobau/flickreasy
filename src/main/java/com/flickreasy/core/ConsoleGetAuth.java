package com.flickreasy.core;

import java.awt.Desktop;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Scanner;

import org.apache.log4j.Logger;
import org.scribe.model.Token;
import org.scribe.model.Verifier;

import com.flickr4java.flickr.Flickr;
import com.flickr4java.flickr.auth.Auth;
import com.flickr4java.flickr.auth.AuthInterface;
import com.flickr4java.flickr.auth.Permission;
import com.flickr4java.flickr.util.FileAuthStore;
import com.flickreasy.core.exception.AuthenticationFailureException;

public class ConsoleGetAuth implements GetAuth {
	
	private static final Logger logger = Logger.getLogger(ConsoleGetAuth.class);

	public Auth execute(Flickr flickr) {
		try {
			FileAuthStore authStore = new FileAuthStore(new File("."));
			for (Auth auth : authStore.retrieveAll()) {
				if (auth.getPermission().getType() >= Permission.WRITE.getType()) {
					return auth;
				}
			}
			
			final AuthInterface authInterface = flickr.getAuthInterface();
	        final Token token = authInterface.getRequestToken();
	        logger.info("Token used " + token);
	        
	        String url = authInterface.getAuthorizationUrl(token, Permission.WRITE);
	        
	        if (Desktop.isDesktopSupported()) {
	        	try {
					Desktop.getDesktop().browse(new URI(url));
				} catch (IOException e) {
					e.printStackTrace();
				} catch (URISyntaxException e) {
					e.printStackTrace();
				}
	        }
	        
	        Scanner scanner = new Scanner(System.in);
	        String tokenKey = scanner.nextLine();
	        
	        Token accessToken = authInterface.getAccessToken(token, new Verifier(tokenKey));
	        
	        if (accessToken != null && !accessToken.isEmpty()) {
	        	logger.info("Authentication success");
	
	            Auth auth = authInterface.checkToken(accessToken);
	            authStore.store(auth);
	
	            return auth;
	        } else {
	        	throw new AuthenticationFailureException();
	        }
		} catch(Exception ex) {
			throw new RuntimeException(ex);
		}
	}

}
