package com.dakusuta.tools.anime.util;

/**
 * @author Rahul S<br>
 *         Constants class store constant values
 */
public class Constants {
	public static final String CONFIG_FILE = "settings\\anime.properties";
	public static final String BACKUP_FILE = "settings\\anime.json";
	public static final String EXIT_QUESTION = "Are you sure that you want to exit?";
	public static String downloadFolder = (System.getProperty("user.home") + "\\Downloads").replace("\\", "/");
}