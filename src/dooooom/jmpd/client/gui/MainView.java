package dooooom.jmpd.client.gui;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;

import javax.sound.midi.Track;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JPanel;
import javax.swing.JSlider;

import dooooom.jmpd.client.ConnectionController;
import dooooom.jmpd.data.testing.TrackListGenerator;


public class MainView {
	/*
	 * GUI Elements
	 */
	private JMenuBar mainMenu;
	private JButton prevButton;
	private JButton nextButton;
	private JButton playPauseButton;
	private JLabel currentTitleLabel;
	private JSlider seekSlider;
	private JLabel currentSeekLabel;
	private JLabel songLengthLabel;
	private JComboBox paneSelectorComboBox;
	private JComboBox playlistComboBox;
	private JLabel albumArtLabel;
	private JPanel mainPanel;
	private LibraryPanel libraryPanel = new LibraryPanel(this);;
	
	/*
	 * This object will handle all daemon calls
	 */
	private ConnectionController cc;
	
	JFrame mainFrame = new JFrame();

	public MainView() {
		mainFrame.setLayout(new GridBagLayout());
		
		/*
		 * This line adds random garbage data to the library in order to test library filter panes.
		 * Comment it out if you don't want garbage data
		 */
		libraryPanel.setLibrary(TrackListGenerator.randomTracksGib(10));
		
		addGUIElements();
		
		mainFrame.pack();
		mainFrame.setVisible(true);
	}
	
	/*
	 * update everything with the info of this track:
	 * 	albumArtLabel
	 * 	currentTitleLabel
	 * 	songLengthLabel
	 */
	public void setTrack(Track track) {
		
	}
	
	public void setSeek(int seconds) {
		
	}
	
	public void play() {
		
	}
	
	public JPanel getMainPanel() {
		return mainPanel;
	}
	
	private void addGUIElements() {
		GridBagConstraints c;
		
		c = new GridBagConstraints();
		mainMenu = new JMenuBar();
		c.gridx = 0;
		c.gridy = 0;
		c.gridwidth = 4;
		c.anchor = GridBagConstraints.LINE_START;
		c.weightx = 1;
		c.weighty = 0;
		c.fill = GridBagConstraints.HORIZONTAL;
		mainFrame.add(mainMenu, c);
		
		c = new GridBagConstraints();
		prevButton = new JButton("|<");
		c.gridx = 0;
		c.gridy = 1;
		c.gridheight = 2;
		c.weightx = 0;
		c.weighty = 0;
		c.fill = GridBagConstraints.NONE;
		mainFrame.add(prevButton, c);
		
		c = new GridBagConstraints();
		playPauseButton = new JButton("|>");
		c.gridx = 1;
		c.gridy = 1;
		c.gridheight = 2;
		c.weightx = 0;
		c.weighty = 0;
		c.fill = GridBagConstraints.NONE;
		mainFrame.add(playPauseButton, c);
		
		c = new GridBagConstraints();
		nextButton = new JButton(">|");
		c.gridx = 2;
		c.gridy = 1;
		c.gridheight = 2;
		c.weightx = 0;
		c.weighty = 0;
		c.fill = GridBagConstraints.NONE;
		mainFrame.add(nextButton, c);
		
		c = new GridBagConstraints();
		currentTitleLabel = new JLabel("Title - Artist - Album (Year)");
		c.gridx = 3;
		c.gridy = 1;
		c.weightx = 1;
		c.weighty = 0;
		c.fill = GridBagConstraints.HORIZONTAL;
		mainFrame.add(currentTitleLabel, c);
		
		c = new GridBagConstraints();
		seekSlider = new JSlider();
		c.gridx = 3;
		c.gridy = 2;
		c.weightx = 1;
		c.weighty = 0;
		c.fill = GridBagConstraints.HORIZONTAL;
		mainFrame.add(seekSlider, c);
		
		c = new GridBagConstraints();
		mainPanel = libraryPanel;
		mainPanel.setPreferredSize(new Dimension(640,480));
		c.gridx = 3;
		c.gridy = 3;
		c.weightx = 1;
		c.weighty = 1;
		c.gridheight = 2;
		c.fill = GridBagConstraints.BOTH;
		mainFrame.add(mainPanel, c);
		
		mainMenu.add(new JMenu("File"));
		mainMenu.add(new JMenu("Daemon"));
		mainMenu.add(new JMenu("Client"));
		mainMenu.add(new JMenu("Help"));
	}

}
