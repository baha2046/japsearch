package org.nagoya.preferences;

import org.apache.commons.lang3.SystemUtils;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.nio.file.Files;
import java.nio.file.InvalidPathException;
import java.nio.file.Path;
import java.nio.file.Paths;

public class GuiSettings extends Settings {

	protected final static GuiSettings INSTANCE = new GuiSettings();

	public enum Key implements Settings.Key {
		lastUsedDirectory, avDirectory, doujinshiDirectory, mangaDirectory, cosplayDirectory, musicDirectory,
		lastUsedScraper, showOutputPanel, showToolbar, useContentBasedTypeIcons, pathToExternalMediaPlayer, fileListDividerLocation, width, height;

		@NotNull
        @Override
		public String getKey() {
			// prefix setting key to avoid clashing
			return "Gui:" + toString();
		}
	}

	private GuiSettings() {
		//prevent people from using this
	}

	@Contract(pure = true)
    public static synchronized GuiSettings getInstance() {
		return INSTANCE;
	}

	public boolean getShowToolbar() {
		return getBooleanValue(Key.showToolbar, Boolean.TRUE).booleanValue();
	}

	public void setShowToolbar(boolean preferenceValue) {
		setBooleanValue(Key.showToolbar, Boolean.valueOf(preferenceValue));
	}

	public boolean getShowOutputPanel() {
		return getBooleanValue(Key.showOutputPanel, false);
	}

	public void setShowOutputPanel(boolean preferenceValue) {
		setBooleanValue(Key.showOutputPanel, preferenceValue);
	}

	public Path getDirectory(Key key)
    {
        if(key == Key.doujinshiDirectory) return Paths.get("X:\\[DOUJINSHI]");
        if(key == Key.mangaDirectory) return Paths.get("X:\\[COMIC]");
        if(key == Key.cosplayDirectory) return Paths.get("X:\\Cosplay");
        return Paths.get("X:\\Movies\\AV");
    }

	public Path getLastUsedDirectory() 
	{
		String lastUsedDir = getStringValue(Key.lastUsedDirectory, null);

		if(lastUsedDir != null) 
		{
			try
			{
				Path lastPath = Paths.get(lastUsedDir);
				
				if(Files.exists(lastPath))
				{
					return lastPath;
				}
			}
			catch(InvalidPathException e1)
			{
				e1.printStackTrace();
			}
		}
		return Paths.get(System.getProperty("user.home"));
	}

	public void setLastUsedDirectory(Path lastUsedDirectoryFile) 
	{
		setStringValue(Key.lastUsedDirectory, lastUsedDirectoryFile.toString());
	}

	public boolean getUseContentBasedTypeIcons() {
		/*    
		 * Use icons in res/mime instead of system icons. 
		 * Needed for linux as system icons only show two types of icons otherwise (files and folders)
		 * There's no menu option for this preference, but you can manually modify the settings file yourself to enable it
		 * this option is also automatically enabled on linux
		 */

		// if we're on linux we want the content based icons as default        
		boolean defaultValue = SystemUtils.IS_OS_LINUX;

		return getBooleanValue(Key.useContentBasedTypeIcons, defaultValue);
	}

	public void setUseContentBasedTypeIcons(boolean preferenceValue) {
		setBooleanValue(Key.useContentBasedTypeIcons, preferenceValue);
	}

	public String getLastUsedScraper() {
		return getStringValue(Key.lastUsedScraper, null);
	}

	public void setLastUsedScraper(String preferenceValue) {
		setStringValue(Key.lastUsedScraper, preferenceValue);
	}

	public String getPathToExternalMediaPlayer() {
		return getStringValue(Key.pathToExternalMediaPlayer, null);
	}

	public void setPathToExternalMediaPlayer(String externalMediaPlayer) {
		setStringValue(Key.pathToExternalMediaPlayer, externalMediaPlayer);
	}

	public Integer getWidth() {
		return getIntegerValue(Key.width, 1045);
	}

	public void setWidth(Integer value) {
		setIntegerValue(Key.width, value);
	}

	public Integer getHeight() {
		return getIntegerValue(Key.height, 850);
	}

	public void setHeight(Integer value) {
		setIntegerValue(Key.height, value);
	}

	public Integer getFileListDividerLocation() {
		return getIntegerValue(Key.fileListDividerLocation, 850);
	}

	public void setFileListDividerLocation(Integer value) {
		setIntegerValue(Key.fileListDividerLocation, value);
	}
}
