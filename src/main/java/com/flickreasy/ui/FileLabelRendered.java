package com.flickreasy.ui;

import java.awt.Color;
import java.awt.Component;

import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.ListCellRenderer;

class FileLabelRendered extends JLabel implements ListCellRenderer {

	private static final long serialVersionUID = 845280685461503029L;

	public FileLabelRendered() {
		setOpaque(true);
	}

	public Component getListCellRendererComponent(JList list, Object value,
			int index, boolean isSelected, boolean cellHasFocus) {
		
		FileSended sended = (FileSended) value;

		setText(sended.getText());

		Color background = Color.WHITE;
		Color foreground;

		if (sended.isSuccess()) {
			foreground = Color.GREEN;
		} else {
			foreground = Color.RED;
		}
		
		
		setBackground(background);
		setForeground(foreground);

		return this;
	}

}