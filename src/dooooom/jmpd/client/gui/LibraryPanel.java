package dooooom.jmpd.client.gui;

import dooooom.jmpd.data.Track;
import dooooom.jmpd.data.TrackList;

import javax.swing.*;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import java.awt.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class LibraryPanel extends JPanel {
	private static final long serialVersionUID = -8684560904793974034L;
	
	/*
	 * The actual library of tracks to choose from
	 */
	private TrackList library = new TrackList();
	
	/*
	 *  GUI Elements
	 */
	private JList<String> artistSelection;
	private JList<String> albumSelection;
	private JList<TrackJListItem> songSelection;
	private JLabel statusBar;
	
	/*
	 * ListModels for the GUI Elements
	 */
	private DefaultListModel<String> artistList = new DefaultListModel<String>();
	private DefaultListModel<String> albumList = new DefaultListModel<String>();
	private DefaultListModel<TrackJListItem> songList = new DefaultListModel<TrackJListItem>();
	
	/*
	 * Current Selections (for filtering purposes)
	 */
	private String selectedArtist;
	private String selectedAlbum;
	
	/*
	 * HashMaps to track which artists created which albums, and which albums contain which tracks
	 */
	private Map<String, ArrayList<String>> artistAlbums;
	private Map<String, ArrayList<Track>> albumTracks;
	
	/*
	 * Track ID of the currently selected song
	 */
	private int currentTrackID;
	
	/*
	 * MainView that contains this LibraryPanel
	 */
	private MainView parentView;
	
	public LibraryPanel(MainView parent) {
		parentView = parent;
		
		this.setLayout(new GridBagLayout());
		
		addGUIElements();
		
		//set the listmodels of the JList objects
		artistSelection.setModel(artistList);
		albumSelection.setModel(albumList);
		songSelection.setModel(songList);
		
		addJListFilterListeners();
	}
	
	/*
	 * Replaces the library and updates all lists
	 */
	public void setLibrary(TrackList tl) {
		library = tl;
		
		artistAlbums = new HashMap<String, ArrayList<String>>();
		albumTracks = new HashMap<String, ArrayList<Track>>();
		
		for (Track t : tl) {
			//add new arraylist if this artist has not been encountered before
			if (!artistAlbums.containsKey(t.get("artist")))
				artistAlbums.put(t.get("artist"), new ArrayList<String>());
			
			ArrayList<String> albums = artistAlbums.get(t.get("artist"));
			if(!albums.contains(t.get("album")))
				albums.add(t.get("album"));
			
			//repeat above code for album/titles
			if (!albumTracks.containsKey(t.get("album")))
				albumTracks.put(t.get("album"), new ArrayList<Track>());
			
			//add track to album listing
			albumTracks.get(t.get("album")).add(t);
		}
		
		updateAllJLists();
	}
	
	/*
	 * After changes have been made to the selection, update the JLists with the new filters
	 */
	private void updateArtistJList() {
		artistList.clear();
		artistList.addElement("[any]");

		for (String s : artistAlbums.keySet()) {
			artistList.addElement(s);
		}
	}
	
	private void updateAlbumJList() {
		albumList.clear();
		
		albumList.addElement("[any]");
		
		if(selectedArtist == null || selectedArtist.isEmpty()) {
			//if no selected artist, add all albums
			for (String s : albumTracks.keySet())
				albumList.addElement(s);
		} else {
			//if there is a selected artist, filter albums
			for (String s : artistAlbums.get(selectedArtist))
				albumList.addElement(s);
		}
	}
	
	private void updateTrackJList() {
		songList.clear();

		TrackList availableTracks = (TrackList) (library.clone());
		
		//if there is an artist selected, filter the results
		if(selectedArtist != null && !selectedArtist.isEmpty()) {
			availableTracks = availableTracks.search("artist", selectedArtist);
		}
		
		//likewise for albums
		if(selectedAlbum != null && !selectedAlbum.isEmpty()) {
			availableTracks = availableTracks.search("album", selectedAlbum);
		}
		
		//add selected tracks to listmodel, wrapping in TrackJListItem objects
		for(Track t : availableTracks) {
			songList.addElement(new TrackJListItem(t));
		}
	}
	
	private void updateAllJLists() {
		updateArtistJList();
		updateAlbumJList();
		updateTrackJList();
	}
	
	private void addElementFromTrack(DefaultListModel<String> d, Track t, String key, String defaultValue) {
		String value = t.get(key);
		if(value == null)
			value = defaultValue;
		
		d.addElement(value);
	}
	
	private void addElementFromTrack(DefaultListModel<String> d, Track t, String key) {
		addElementFromTrack(d, t, key, "Unknown");
	}
	
	/*
	 * This method is separated to help with clutter in the main constructor
	 */
	private void addGUIElements() {
		GridBagConstraints c;
		
		c = new GridBagConstraints();
		artistSelection = new JList();
		artistSelection.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		artistSelection.setLayoutOrientation(JList.VERTICAL);
		c.gridx = 0;
		c.gridy = 0;
		c.weightx = 1;
		c.weighty = 1;
		c.fill = GridBagConstraints.BOTH;
		this.add(new JScrollPane(artistSelection), c);
		
		c = new GridBagConstraints();
		albumSelection = new JList();
		c.gridx = 1;
		c.gridy = 0;
		c.weightx = 1;
		c.weighty = 1;
		c.fill = GridBagConstraints.BOTH;
		this.add(new JScrollPane(albumSelection), c);
		
		c = new GridBagConstraints();
		songSelection = new JList();
		c.gridx = 2;
		c.gridy = 0;
		c.weightx = 1;
		c.weighty = 1;
		c.fill = GridBagConstraints.BOTH;
		this.add(new JScrollPane(songSelection), c);
		
		c = new GridBagConstraints();
		statusBar = new JLabel("Ready");
		c.gridx = 0;
		c.gridy = 1;
		c.weightx = 1;
		c.weighty = 0;
		c.fill = GridBagConstraints.BOTH;
		this.add(statusBar, c);
	}
	
	private void addJListFilterListeners() {
		ListSelectionListener artistJListListener = new ListSelectionListener() {
			
			@Override
			public void valueChanged(ListSelectionEvent e) {
				statusBar.setText(Integer.toString(artistSelection.getSelectedIndex()) + " " + (String)artistSelection.getSelectedValue());
				
				//if [any] selected
				if(artistSelection.getSelectedIndex() == 0)
					selectedArtist = "";
				else
					selectedArtist = (String) artistSelection.getSelectedValue();
				
				updateAlbumJList();
				updateTrackJList();
			}
		};
		
		ListSelectionListener albumJListListener = new ListSelectionListener() {
			
			@Override
			public void valueChanged(ListSelectionEvent e) {
				statusBar.setText(Integer.toString(albumSelection.getSelectedIndex()) + " " + (String)albumSelection.getSelectedValue());
				
				//if [any] selected
				if(albumSelection.getSelectedIndex() == 0)
					selectedAlbum = "";
				else
					selectedAlbum = (String) albumSelection.getSelectedValue();
				
				//updateAlbumJList();
				updateTrackJList();
			}
		};
		
		artistSelection.addListSelectionListener(artistJListListener);
		albumSelection.addListSelectionListener(albumJListListener);
	}
	
	/*
	 * This class is used as a wrapper for a track, to be held in the JList.
	 * The reason for this is that the JList will display a toString of each item, and
	 * using this wrapper it will display only the track title.
	 */
	private class TrackJListItem {
		Track t;
		
		TrackJListItem(Track t) {
			this.t = t;
		}
		
		@Override
		public String toString() {
			return t.get("title");
		}
	}
}
