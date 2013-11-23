package com.flickreasy.ui;

import java.awt.Component;
import java.awt.Desktop;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;

import javax.swing.JOptionPane;

import org.apache.commons.io.FileUtils;
import org.apache.log4j.Logger;
import org.scribe.model.Token;
import org.scribe.model.Verifier;

import com.flickr4java.flickr.Flickr;
import com.flickr4java.flickr.auth.Auth;
import com.flickr4java.flickr.auth.AuthInterface;
import com.flickr4java.flickr.auth.Permission;
import com.flickr4java.flickr.util.FileAuthStore;
import com.flickreasy.core.ConsoleGetAuth;
import com.flickreasy.core.GetAuth;
import com.flickreasy.core.exception.AuthenticationFailureException;

public class SwingGetAuth implements GetAuth {
	
	private static final Logger logger = Logger.getLogger(ConsoleGetAuth.class);
	
	private Component cmp;
	
	public SwingGetAuth(Component cmp) {
		this.cmp = cmp;
	}

	public Auth execute(Flickr flickr) {
		try {
			FileAuthStore authStore = new FileAuthStore(FileUtils.getUserDirectory());
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
	        
	        String tokenKey = JOptionPane.showInputDialog(cmp, "Type your code", "Code", JOptionPane.QUESTION_MESSAGE);
	        
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
			JOptionPane.showMessageDialog(cmp, ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
			return null;
		}
	}

}
