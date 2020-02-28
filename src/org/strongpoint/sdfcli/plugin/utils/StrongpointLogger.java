package org.strongpoint.sdfcli.plugin.utils;

import java.io.IOException;
import java.util.logging.FileHandler;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

public class StrongpointLogger {
	
	public static void logger(String className, String logType, String logMessage) {
		Logger log = Logger.getLogger("Strongpoint");
//		FileHandler fileHandler;
		
		try {
//			fileHandler = new FileHandler(System.getProperty("user.home") + "/strongpoint_logs.log", true);
//			log.addHandler(fileHandler);
//			SimpleFormatter simpleFormatter = new SimpleFormatter();
//			fileHandler.setFormatter(simpleFormatter);		
			if(logType.equalsIgnoreCase("info")) {
				log.info(className +" :: "+ logMessage);
			} else if(logType.equalsIgnoreCase("warn")) {
				log.warning(className +" :: "+ logMessage);
			} else if(logType.equalsIgnoreCase("error")) {
				log.severe(className +" :: "+ logMessage);
			}
		} catch (SecurityException e) {
			e.printStackTrace();
		}
		
	}

}
