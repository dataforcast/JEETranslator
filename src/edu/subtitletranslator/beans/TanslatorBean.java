package edu.subtitletranslator.beans;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;

import java.util.ArrayList;
import java.util.regex.*;
import java.util.Date;
import java.util.Enumeration;
import java.util.Hashtable;
import java.lang.Integer;
import java.text.ParseException;
import java.text.SimpleDateFormat;

@SuppressWarnings("unused")
public class TanslatorBean {

	private ArrayList<String> translatedSubtitles = null;
	private ArrayList<String> originalFileredSubtitles = new ArrayList<String>();
	

	/*-----------------------------------------------------------------------*/
	/**
	 * Lines from file name given as parameter are read and stored in an 
	 * array list.  
	 * @param fileName
	 */
	/*-----------------------------------------------------------------------*/
	public TanslatorBean(String fileName) {
		setTranslatedSubtitles(new ArrayList<String>());
	}
	/*-----------------------------------------------------------------------*/
	

	/*-----------------------------------------------------------------------*/
	/**
	 * 
	 * @return
	 */
	/*-----------------------------------------------------------------------*/
	public ArrayList<String> getTranslatedSubtitles() {
		return translatedSubtitles;
	}
	/*-----------------------------------------------------------------------*/

	
	public void setTranslatedSubtitles(ArrayList<String> translatedSubtitles) {
		this.translatedSubtitles = translatedSubtitles;
	}
	/*-----------------------------------------------------------------------*/	
}
