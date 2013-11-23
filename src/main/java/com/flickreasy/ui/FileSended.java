package com.flickreasy.ui;

import javax.swing.JLabel;

public class FileSended extends JLabel {

	private Boolean success;

	public FileSended(String path, Boolean success) {
		super(path);
		
		this.success = success;
	}

	public Boolean isSuccess() {
		return success;
	}
	
}
