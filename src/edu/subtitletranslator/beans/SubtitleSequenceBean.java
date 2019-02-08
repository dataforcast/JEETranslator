package edu.subtitletranslator.beans;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;

import edu.subtitletranslator.common.TranslatorLoggerLevel;

/*-----------------------------------------------------------------------*/
/**
 * This class is a container for a translation.
 * A translation is a set of followings records :
 * 	A number
 * 	A time interval
 *  One or more original sentences handled into an array list. 
 *  One or more translated sentences handled into an array list. 
 * */
/*-----------------------------------------------------------------------*/
public class SubtitleSequenceBean {
	public static final Integer SUBTITLE_ACTION_NONE   = 0;
	public static final Integer SUBTITLE_ACTION_CLEAR  = 1;
	public static final Integer SUBTITLE_ACTION_STORE  = 2;
	public static final Integer SUBTITLE_ACTION_INSERT = 3;
	public static final Integer SUBTITLE_ACTION_UPDATE = 4;
	Pattern oPatternTime = null;
	
	private   Integer sequenceNumber;
	private   String    startTime;
	private   String    endTime;
	private   Integer file_id;
	
	protected ArrayList<String>  hashCodeArrayList = new ArrayList<String>();
	
	/** This hash code allows address a line into a subtitle sequence. */	
	public  ArrayList<String>  originalLinesArrayList   = new ArrayList<String>();
	public  Hashtable<Integer, String> translatedLines = new Hashtable<Integer, String>();
		
	private static Logger logger = Logger.getLogger(SubtitleSequenceBean.class);

	/*-----------------------------------------------------------------------*/
	/**
	 * 
	 */
	/*-----------------------------------------------------------------------*/
	public  SubtitleSequenceBean(){
    	Level oLevel = TranslatorLoggerLevel.get();
    	logger.setLevel(oLevel);

		oPatternTime = Pattern.compile(SubtitleDataManagerBean.REGEXP_PATTERN_TIME_INTERVAL);		
	}
	/*-----------------------------------------------------------------------*/

	/*-----------------------------------------------------------------------*/
	/**
	 * Getter and setters 
	 */
	/*-----------------------------------------------------------------------*/
	public Hashtable<Integer, String> getTranslatedLines() {
		return translatedLines;
	}
	/** sequenceNumber getter and setter */
	public Integer getSequenceNumber() {
		return sequenceNumber;
	}
	public void setSequenceNumber(Integer sequenceNumber) {
		this.sequenceNumber = sequenceNumber;
	}
	/** startTime getter and setter */
	public String getStartTime() {
		return startTime;
	}
	public Integer getFile_id() {
		return file_id;
	}
	public void setFile_id(Integer file_id) {
		this.file_id = file_id;
	}
	public void setStartTime(String strStartDateTime) {
		this.startTime = formatTimetoSrt(strStartDateTime);
	}
	public String getEndTime() {
		return endTime;
	}
	public void setEndTime(String strEndDateTime) {
		this.endTime = formatTimetoSrt(strEndDateTime);
	}
	public ArrayList<String> getoriginalLinesArrayList() {
		return originalLinesArrayList;
	}
	public void setoriginalLinesArrayList(ArrayList<String> originalLinesArrayList) {
		this.originalLinesArrayList = originalLinesArrayList;
	}
	/*-----------------------------------------------------------------------*/

	/*-----------------------------------------------------------------------*/
	/**
	 * This method allows to format time retrieved from database to time formated for SRT file.
	 * @param timeToBeFormated
	 * @return SRT formated time.
	 */
	/*-----------------------------------------------------------------------*/
	private String formatTimetoSrt(String timeToBeFormated){
		
    	/** Milliseconds processing: replace occurrence of character '.' with character ',' */
		String formatedTime = timeToBeFormated.replace(".", ",");
		Matcher oMatcherTime = oPatternTime.matcher( formatedTime );
		while (oMatcherTime.find()) {
		    logger.debug("*** INFO : setStartTime() : Start time group = " + oMatcherTime.group()) ;
			formatedTime = oMatcherTime.group();
		}
		return formatedTime; 
	}
	/*-----------------------------------------------------------------------*/
	
	/** ---------------------------------------------------------------------*/
	/** This method is invoked when a line read from "srt" file is added 
	 * to a subtitle sequence.*/
	/** ---------------------------------------------------------------------*/
	public void addOriginalLine(Integer lineNumber, String originalLine){
		this.originalLinesArrayList.add(lineNumber, originalLine);
	}
	/** ---------------------------------------------------------------------*/

	/** ---------------------------------------------------------------------*/
	/**
	 * 
	 */
	/** ---------------------------------------------------------------------*/
	public Integer clearTranslatedLine(Integer lineNumber, String translatedLine) {
		Integer action = SUBTITLE_ACTION_NONE;
		if( null != translatedLine ) 
		{
			this.translatedLines.remove(lineNumber);
			action = SUBTITLE_ACTION_CLEAR;
		}
		else{
			action = SUBTITLE_ACTION_NONE;
		}
		
		return action;

	}
	/** ---------------------------------------------------------------------*/
	
	/** ---------------------------------------------------------------------*/
	/**
	 * This method allows to add into a hach table a translated line belonging a section.
	 * key for storage is the line index into the section.
	 * 
	 * @param index : line number of translated line into the section 
	 * @param translatedLine
	 * Returns action value to be performed : 
	 * 	SUBTITLE_STORE_ACTION_NONE
	 * 	SUBTITLE_STORE_ACTION_INSERT
	 * 	SUBTITLE_STORE_ACTION_UPDATE
	 * 
	 */
	/** ---------------------------------------------------------------------*/
	public Integer addTranslatedLine(Integer index, String translatedLine){
		/** Check if translated line exists or has changed */
		Integer action = SUBTITLE_ACTION_INSERT;
		if( null == translatedLine ) 
		{
			action = SUBTITLE_ACTION_NONE;
		}
		else
		{
			String storedTranslatedLine = this.translatedLines.get(index);
			if( null == storedTranslatedLine ){
				/** No translated line for this index; translated line given as parameter will be stored */
				this.translatedLines.put(index, translatedLine);
				action = SUBTITLE_ACTION_INSERT;
			}
			else{
				if( translatedLine.equals(storedTranslatedLine) ){
					/** No change need to be performed */
					logger.debug("*** INFO : addTranslatedLine() : Index= "+index+" Stored translation= "+storedTranslatedLine+" / Page translation= "+translatedLine);
					action = SUBTITLE_ACTION_NONE;
				}
				else
				{
					/** This is a translation update or correction */
					this.translatedLines.put(index, translatedLine);
					action = SUBTITLE_ACTION_UPDATE;
				}
			}
		}
		return action;
	}
	/** ---------------------------------------------------------------------*/	
}
