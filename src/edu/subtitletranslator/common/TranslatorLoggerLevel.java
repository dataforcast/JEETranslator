package edu.subtitletranslator.common;

import java.io.IOException;

import org.apache.log4j.Level;

public class TranslatorLoggerLevel {
	private static Level oLevel =null;
	public static Level get(){
		if( null == oLevel ){
			TranslatorProperties oTranslatorProperties = null;
			try {
				oTranslatorProperties = TranslatorProperties.getTranslatorProperties();
			} catch ( IOException oIOException ) {
				oIOException.printStackTrace();
		    	String errorMessage = "*** ERROR : TranslatorServlet() : Loading properties for configuration failed! IOException = "+oIOException.getMessage();
		    	System.out.println(errorMessage);				
			}

			/* Get level from translator.properties file*/
			try {
				String strLoggerLevel = oTranslatorProperties.getPropertyValue("log4j.level");
				oLevel = Level.toLevel(strLoggerLevel);
				/*
				if(! strLoggerLevel.equals("OFF") ){
				}*/
			} catch (TranslatorPropertyException oTranslatorPropertyException) {
		    	String errorMessage = "*** ERROR : TranslatorServlet() : NULL Pointer Exception  = "+oTranslatorPropertyException.getMessage();
		    	System.out.println(errorMessage);				
			}
		}
		return oLevel;
	}
}
