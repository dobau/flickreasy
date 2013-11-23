package com.flickreasy.ui;

import java.awt.BorderLayout;
import java.awt.Cursor;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.Observable;
import java.util.Observer;

import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.filechooser.FileFilter;

import org.apache.commons.io.FilenameUtils;
import org.apache.log4j.Logger;

import com.flickr4java.flickr.Flickr;
import com.flickr4java.flickr.FlickrException;
import com.flickr4java.flickr.people.PeopleInterface;
import com.flickr4java.flickr.people.User;
import com.flickreasy.core.FlickrFactory;
import com.flickreasy.core.SyncFileToFlicker;
import com.flickreasy.core.exception.AuthenticationFailureException;

public class FlickrEasyUI extends JFrame {
	
	private static final long serialVersionUID = -7467836434027318966L;

	private static final Logger logger = Logger.getLogger(FlickrEasyUI.class);
	
	private SyncFileToFlicker syncFlicker = null;
	
	private static final int HEIGHT = 20;
	
	final JFrame thisFrame = this;
	
	final JProgressBar pb;
	final DefaultListModel listModel;
	final JButton btStop;
	final JButton btStart;

	private JPanel pnUser = null;
	
	public FlickrEasyUI() {
		try {
			FlickrFactory.setGetAuth(new SwingGetAuth(thisFrame));
			
			setSize(800, 600);
			setTitle("Flickr Easy");
			setDefaultCloseOperation(EXIT_ON_CLOSE);
			
			final JPanel rootPanel = new JPanel();
			rootPanel.setLayout(new BorderLayout());
			
			final JPanel pnTop = new JPanel();
			pnTop.setLayout(new GridBagLayout());
			rootPanel.add(pnTop, BorderLayout.NORTH);
	
			GridBagConstraints topConstraint = new GridBagConstraints();
			topConstraint.weightx = 1;
			
			topConstraint.gridx = 0;
			topConstraint.gridy = 0;
			topConstraint.gridwidth = 5;
			topConstraint.fill = GridBagConstraints.HORIZONTAL;
			pnTop.add(getPnUser(), topConstraint);
			
			final JLabel lbDirectory = new JLabel("Directory:");
			topConstraint.gridwidth = 1;
			topConstraint.gridx = 0;
			topConstraint.gridy = 1;
			pnTop.add(lbDirectory, topConstraint);
	
			final JTextField txDirectory = new JTextField(20);
			topConstraint.gridx = 1;
			pnTop.add(txDirectory, topConstraint);
			
			JButton btSearch = new JButton("...");
			topConstraint.gridx = 2;
			btSearch.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					JFileChooser fc = new JFileChooser();
					fc.setVisible(Boolean.TRUE);
					fc.setMultiSelectionEnabled(Boolean.FALSE);
					fc.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);
					fc.setFileFilter(new FileFilter() {
						
						@Override
						public String getDescription() {
							return "Flickr files";
						}
						
						@Override
						public boolean accept(File f) {
							return f.isDirectory() || FilenameUtils.isExtension(f.getName().toLowerCase(), Arrays.asList("jpg"));
						}
					});
					int choose = fc.showOpenDialog(thisFrame);
					
					if (choose == JFileChooser.APPROVE_OPTION) {
			            File file = fc.getSelectedFile();
			            txDirectory.setText(file.getAbsolutePath());
			        }
				}
			});
			pnTop.add(btSearch, topConstraint);
	
			
			btStart = new JButton("Start");
			topConstraint.gridx = 3;
			btStart.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					try {
						syncFlicker.start(txDirectory.getText());
						
						exec(new Command() {
							public void execute() {
								btStart.setEnabled(Boolean.FALSE);							
								btStop.setEnabled(Boolean.TRUE);							
							}
						});
						
					} catch (Exception ex) {
						logger.error(ex.getMessage(), ex);
						JOptionPane.showMessageDialog(thisFrame, ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
					}
				}
			});
			pnTop.add(btStart, topConstraint);
			
			btStop = new JButton("Stop");
			btStop.setSize(40, HEIGHT);
			//btStop.setEnabled(Boolean.FALSE);
			topConstraint.gridx = 4;
			btStop.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					try {
						if (syncFlicker != null) {
							syncFlicker.stop();
						}
						
						exec(new Command() {
							public void execute() {
								btStart.setEnabled(Boolean.TRUE);							
								btStop.setEnabled(Boolean.FALSE);							
							}
						});
					} catch (Exception ex) {
						logger.error(ex.getMessage(), ex);
						JOptionPane.showMessageDialog(thisFrame, ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
					}
				}
			});
			pnTop.add(btStop, topConstraint);
			
			final JPanel pnCenter = new JPanel();
			pnCenter.setLayout(new BorderLayout());
			rootPanel.add(pnCenter, BorderLayout.CENTER);
			
			this.listModel = new DefaultListModel();
			JList lsLog = new JList(listModel);
			lsLog.setCellRenderer(new FileLabelRendered());
			pnCenter.add(lsLog, BorderLayout.CENTER);
			
			final JPanel pnBottom = new JPanel();
			pnBottom.setLayout(new BorderLayout());
			rootPanel.add(pnBottom, BorderLayout.SOUTH);
			
			pb = new JProgressBar(0, 100);
			pb.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
			pb.setStringPainted(Boolean.TRUE);
			pb.setValue(0);
			pnBottom.add(pb, BorderLayout.CENTER);
			
			this.add(rootPanel);
		
			syncFlicker = createNewSynFlicker();
		} catch (Exception ex) {
			JOptionPane.showMessageDialog(thisFrame, ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
			throw new RuntimeException(ex);
		}
	}
	
	private JPanel getPnUser() throws Exception {
		if (pnUser  == null) {
			pnUser = new JPanel();
			
			pnUser.setLayout(new BorderLayout());
			
			final Flickr flickr = FlickrFactory.getInstance();
			final User user = flickr.getAuth().getUser();
			
			JLabel name = new JLabel("Name: ");
			JLabel username = new JLabel(user.getRealName());
			final JLabel countPhotos = new JLabel(String.format("%d photos syncronizeds", user.getPhotosCount()));
			
			JPanel panelName = new JPanel();
			panelName.setLayout(new GridLayout(1, 2));
			
			panelName.add(name);
			panelName.add(username);
			
			pnUser.add(panelName, BorderLayout.CENTER);
			pnUser.add(countPhotos, BorderLayout.EAST);
			
			exec(new Command() {

				public void execute() {
					PeopleInterface peopleInterface = flickr.getPeopleInterface();
					try {
						User userUpdated = peopleInterface.getInfo(user.getId());
						countPhotos.setText(String.format("%d photos syncronizeds", userUpdated.getPhotosCount()));
					} catch (FlickrException e) {
						new RuntimeException(e);
					}
				}
				
			});
		}

		return pnUser;
	}

	public SyncFileToFlicker createNewSynFlicker() throws FlickrException, AuthenticationFailureException, IOException {
		final SyncFileToFlicker syncFlicker = new SyncFileToFlicker();
		
		syncFlicker.getUploadListener().addOnComplete(new Observer() {
			public void update(Observable arg0, Object file) {
				int fileSended = syncFlicker.getFileSended();
				int fileTotal = syncFlicker.getFileTotal();
				
				logger.info("File sended "+ fileSended +" of "+fileTotal);
				
				pb.setValue((100 * fileSended) / fileTotal);
			}
		});
		
		syncFlicker.getUploadListener().addOnSuccess(new Observer() {
			public void update(Observable arg0, Object arg) {
				File file = (File) arg;
				
				listModel.addElement(new FileSended(file.getAbsolutePath(), Boolean.TRUE));
			}
		});
		
		syncFlicker.getUploadListener().addOnFailure(new Observer() {
			public void update(Observable arg0, Object arg) {
				File file = (File) arg;
				
				listModel.addElement(new FileSended(file.getAbsolutePath(), Boolean.FALSE));
			}
		});
		
		syncFlicker.onEnd(new Observer() {
			public void update(Observable arg0, Object arg) {
				logger.info("Finished");
				
				pb.setValue(100);
				
				btStart.setEnabled(Boolean.TRUE);
				btStop.setEnabled(Boolean.FALSE);
			}
		});
		
		return syncFlicker;
	}
	
	public void exec(final Command command) {
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				command.execute();
			}
		});
	}

}
