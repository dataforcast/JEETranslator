package edu.subtitletranslator.beans;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.List;
import java.util.Vector;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;

import edu.subtitletranslator.dao.DAOFactory;
import edu.subtitletranslator.common.TranslatorLoggerLevel;
import edu.subtitletranslator.common.TranslatorProperties;
import edu.subtitletranslator.common.TranslatorPropertyException;
import edu.subtitletranslator.dao.DAOException;
import edu.subtitletranslator.dao.SubtitleDAO;



public class SubtitleDataManagerBean {	
	/**
	 * This class acts as a middleware between controller (from MVC model) and data sources.
	 * 
	 * Data sources are files for translation work or database.
	 * 
	 * One object from this class is uniquely related to a file to be translated. 
	 * This object is created when a new file is loaded. 
	 * When a translation is reset from user web page menu, then this object is deleted.
	 * 
	 * Object from this class captures data from files, database or view when translation is processed from user.
	 * It recored data into database or write them into a translated file.
	 * 
	 * When reading a file to be translated, it also structures data into sequences, original lines and translated lines.
	 * Original lines are never changed. Translated lines structures are altered while adding or deleting them.
	 * 
	 * Object of this class also provides services allowing to access statistics related to translation progress.
	 */
	private ArrayList<String> originalSubtitlesArrayList = null;
	private String substitleFileName = null;

	public static final String REGEXP_PATTERN_TIME_INTERVAL ="[0-9]{2}(:[0-9]{2}){2},([0-9]*)";
	public static final String REGEXP_PATTERN_SEQ_NUMBER = "^[0-9]+$";
	private static final String PREFIX_FILE = "trans-";
	//private ArrayList<SubtitleSequenceBean> oSubtitleSequenceHashTable = new ArrayList<SubtitleSequenceBean>();
	public  Hashtable<Integer, SubtitleSequenceBean> oSubtitleSequenceHashTable = new Hashtable<Integer, SubtitleSequenceBean>();

	private Integer isSubtitlesBuilt = 0;
	private SubtitleDAO oSubtitleDAO = null;

	/** Number of subtitle sequences */
	private Integer sequenceCount =0;
	private Integer file_id;
	private SubtitleStatsBean oSubtitleStats = null;
	private String translatedFileName;
	private TranslatorProperties oTranslatorProperties;
	private String fileDirectoryPathName;
	
	private static Logger logger = Logger.getLogger(SubtitleDataManagerBean.class);


	/*-----------------------------------------------------------------------*/
	/**
	 * Load resources for accessing data sources.
	 * Data resources are those objects allowing to access files and related database.
	 * 
	 * @throws SubtitleException 
	 */
	/*-----------------------------------------------------------------------*/
	public SubtitleDataManagerBean(String subtitleFileName) throws SubtitleException {
		this.substitleFileName = subtitleFileName;
		String errorMessage = null;
    	Level oLevel = TranslatorLoggerLevel.get();
    	logger.setLevel(oLevel);

		/** Load configuration database access */
		DAOFactory oDAOFactory;
		try {
			oDAOFactory = DAOFactory.getInstance();
		} catch ( DAOException oDAOException ) {
			oDAOException.printStackTrace();
			errorMessage = "*** ERROR : JDBC initialization failed! DAOException= "+oDAOException.getMessage();
			throw new SubtitleException(errorMessage);
		}	
		try {
			oSubtitleDAO = oDAOFactory.getSubtitleDAO();
		} catch (DAOException oDAOException) {
			oDAOException.printStackTrace();
			errorMessage = "*** ERROR : Database connection failed! DAOException= "+oDAOException.getMessage();
			throw new SubtitleException( errorMessage );
		}
		
		/** Load configuration file paths*/
		try {
			oTranslatorProperties = TranslatorProperties.getTranslatorProperties();
		} catch ( IOException oIOException ) {
			oIOException.printStackTrace();
        	errorMessage = "*** ERROR : SubtitleDataManagerBean() : Loading properties for configuration failed! IOException = "+oIOException.getMessage();
        	throw new SubtitleException(errorMessage);				
		}
		try {
			String rootDirectoryPath = oTranslatorProperties.getPropertyValue("translator.rootdir");
			this.fileDirectoryPathName =  rootDirectoryPath+"/"+oTranslatorProperties.getPropertyValue("translator.resources.file");
		} catch (TranslatorPropertyException oTranslatorPropertyException) {
			oTranslatorPropertyException.printStackTrace();
        	errorMessage = "*** ERROR : SubtitleDataManagerBean() : Loading directory path name FAILED! TranslatorPropertyException = "+oTranslatorPropertyException.getMessage();
        	throw new SubtitleException(errorMessage);				
		}

		/** Statistics object initialization */
		oSubtitleStats  = new SubtitleStatsBean(subtitleFileName);
		
		/** Add JDBC Engine to be displayed on view */
		oSubtitleStats.setJdbcEngine(oDAOFactory.getJdbcEngine());
		
	}
	/*-----------------------------------------------------------------------*/

	/*-----------------------------------------------------------------------*/
	/**
	 * 
	 * @return
	 */
	public SubtitleStatsBean getoSubtitleStats() {
		return oSubtitleStats;
	}
	/*-----------------------------------------------------------------------*/
	/*-----------------------------------------------------------------------*/
	/**
	 * 
	 */
	/*-----------------------------------------------------------------------*/
	public String getTranslatedFileName() {
		return translatedFileName;
	}
	/*-----------------------------------------------------------------------*/
	
	/*-----------------------------------------------------------------------*/
	/**
	 */
	/*-----------------------------------------------------------------------*/
	public Integer getSequenceCount() {
		return sequenceCount;
	}
	/*-----------------------------------------------------------------------*/

	/*-----------------------------------------------------------------------*/
	/**
	 */
	/*-----------------------------------------------------------------------*/
	public String getSubstitleFileName() {
		return substitleFileName;
	}
	/*-----------------------------------------------------------------------*/

	/*-----------------------------------------------------------------------*/
	/**
	 */
	/*-----------------------------------------------------------------------*/
	public void setSubstitleFileName(String substitleFileName) {
		this.substitleFileName = substitleFileName;
	}
	/*-----------------------------------------------------------------------*/

	/*-----------------------------------------------------------------------*/
	/**
	 */
	/*-----------------------------------------------------------------------*/
	public Hashtable<Integer,SubtitleSequenceBean> getoSubtitleSequenceHashTable() {
		return oSubtitleSequenceHashTable;
	}
	/*-----------------------------------------------------------------------*/
	

	
	/*-----------------------------------------------------------------------*/
	/**
	 * Read file and build array list of original subtitles lines.
	 */
	/*-----------------------------------------------------------------------*/
	public void readOriginalFile( String realPathName) throws SubtitleException{
		originalSubtitlesArrayList = new ArrayList<String>(); 
		
		realPathName = this.fileDirectoryPathName+"/"+this.substitleFileName;
		logger.debug("*** INFO : readOriginalFile() : File full path= "+realPathName);
		Integer originalLines = 0;
		try {
			BufferedReader oBufferedReader = new BufferedReader(new FileReader(realPathName));
			String line;
			while ((line = oBufferedReader.readLine()) != null) {
				originalSubtitlesArrayList.add(line);
				originalLines++;
				//logger.debug("*** INFO : readOriginalFile() : Line = "+line);
			}
			oBufferedReader.close();
		} catch (IOException e) {
			String errorMEssage = e.getMessage();
			e.printStackTrace();
			throw new SubtitleException(errorMEssage);
		}
		//this.oSubtitleStats.setOriginalLines(originalLines);
		logger.debug("*** INFO : readOriginalFile() : Number of processed lines = "+originalSubtitlesArrayList.size());
	}
	/*-----------------------------------------------------------------------*/

	/*-----------------------------------------------------------------------*/
	/**
	 * Clean tables related to a srt file.
	 * @throws SubtitleException 
	 */
	/*-----------------------------------------------------------------------*/
	public void cleanDatabase() throws SubtitleException{
		try {
			this.file_id = this.oSubtitleDAO.checkFileName(this.substitleFileName);
			oSubtitleDAO.clean(this.file_id);
		} catch (DAOException e) {
			e.printStackTrace();
			throw new SubtitleException(e.getMessage());
		}
	}
	/*-----------------------------------------------------------------------*/

	/*-----------------------------------------------------------------------*/
	/**
	 * Store content from any object type SubtitleSequenceBean into database.
	 * Object of type SubtitleSequenceBean are picked from array list. 
	 */
	/*-----------------------------------------------------------------------*/
	public void storeSubtitleSequence() throws SubtitleException {
		
		try {
			file_id = this.oSubtitleDAO.checkFileName(this.substitleFileName);
		} catch (DAOException e1) {
			String errorMEssage =e1.getMessage(); 
			e1.printStackTrace();
			throw new SubtitleException(errorMEssage);
		} 
		Enumeration<SubtitleSequenceBean> oEnumeration = this.oSubtitleSequenceHashTable.elements();
		while (oEnumeration.hasMoreElements()){
			SubtitleSequenceBean oSubtitleSequenceBean = oEnumeration.nextElement();
			oSubtitleSequenceBean.setFile_id(file_id);
			try {
				oSubtitleDAO.storeSubtitleSequence(oSubtitleSequenceBean);
			} catch (DAOException e) {
				String errorMEssage =e.getMessage(); 
				e.printStackTrace();
				throw new SubtitleException(errorMEssage);
			}
		}
	}
	/*-----------------------------------------------------------------------*/

	/*-----------------------------------------------------------------------*/
	/**
	 * This method  allows to store an object of type SubtitleSequenceBean into 
	 * the list of such objects.
	 * 
	 * If object is still into list, then it is replaced. Otherwise, it is added.
	 *  
	 * @param oSubtitleSequenceBean
	 */
	/*-----------------------------------------------------------------------*/
	private void storeSubtitleSequenceList(SubtitleSequenceBean oSubtitleSequenceBean){
		if (oSubtitleSequenceHashTable.contains(oSubtitleSequenceBean)){
			oSubtitleSequenceHashTable.replace(oSubtitleSequenceBean.getSequenceNumber(),oSubtitleSequenceBean );
		}else{
			oSubtitleSequenceHashTable.put(oSubtitleSequenceBean.getSequenceNumber(), oSubtitleSequenceBean);
		}
		return;
	}
	/*-----------------------------------------------------------------------*/

	/*-----------------------------------------------------------------------*/
	/** This method builds a list of original subtitles sequences read from 
	 * sourced from ".srt" file.
	 * 
	 * Once done, subtitles sequences and original lines are stored into database.
	 * Hash table containing SubtitleSequenceBean objects types is updated.
	 * */
	/*-----------------------------------------------------------------------*/
	public void buildSubtitleSequences() throws SubtitleException {
		
		String line = null;
		Pattern oPatternSeqNumber = Pattern.compile(REGEXP_PATTERN_SEQ_NUMBER);
		Pattern oPatternStartTime = Pattern.compile("^"+REGEXP_PATTERN_TIME_INTERVAL);
		Pattern oPatternEndTime   = Pattern.compile(REGEXP_PATTERN_TIME_INTERVAL+"$");
		
		SubtitleSequenceBean oSubtitleSequenceBean = null; 
		
		/** Build only once original sentences from file */
		if( 1 == isSubtitlesBuilt )
		{
			logger.debug("*** INFO : buildSubtitleSequences() : Already Built sequences!= "+line);
			return;
		}
		
		/** Subtitle sequence index */
		Integer sequenceNumber = null;
		
		/** Index of lines in a subtitle sequence*/
		Integer lineNumber     = 0;
		for(int index=0; index< this.originalSubtitlesArrayList.size(); index++){
			
			line = this.originalSubtitlesArrayList.get(index);
			logger.debug("*** INFO : buildSubtitleSequences() : Line = "+line);

			/** Empty lines are ignored */
			if ( 0 < line.length()){
				/** Sequence number is kept from line*/
				if( line.matches(REGEXP_PATTERN_SEQ_NUMBER)){
					
					/** Total number of lines to be translated from previous sequence is added */
					this.oSubtitleStats.addOriginalLines(lineNumber);
					
					/** Number of lines to be translated in this sequence is reset */					
					lineNumber = 0;
					
					/** A new sequence is created, holding sequence number, start and end time,...*/
					oSubtitleSequenceBean = new SubtitleSequenceBean();
					oSubtitleSequenceBean.setFile_id(file_id);
			
					Matcher oMatcher = oPatternSeqNumber.matcher(line);
					if(null != oMatcher){
						 while (oMatcher.find()) {
							 sequenceNumber = new Integer(oMatcher.group());			
						    //logger.debug("Sequence number = " + sequenceNumber) ;
							oSubtitleSequenceBean.setSequenceNumber(sequenceNumber);					
							}
					}
				}
				/** Lines from times intervals are filtered*/
				else if ( line.matches("^"+REGEXP_PATTERN_TIME_INTERVAL+" --> "+REGEXP_PATTERN_TIME_INTERVAL) ){
					/** Start time sequence is kept from line */
					Matcher oMatcherStartTime = oPatternStartTime.matcher(line);
					while (oMatcherStartTime.find()) {
					    logger.debug("Start time group = " + oMatcherStartTime.group()) ;
					    String strStartDateTime = oMatcherStartTime.group();
						oSubtitleSequenceBean.setStartTime(strStartDateTime);
					}

					/** End time sequence is kept from line */
					Matcher oMatcherEndTime   = oPatternEndTime.matcher(line);
					while (oMatcherEndTime.find()) {
					    logger.debug("End time groupe = " + oMatcherEndTime.group()) ;
					    String strEndDateTime = oMatcherEndTime.group();
						oSubtitleSequenceBean.setEndTime(strEndDateTime);
					}
				}
				else{
					/** This is a line to be translated; it is added into subtitle sequence*/
					if( null != line && ! line.isEmpty())
					{
						if( null != oSubtitleSequenceBean){
							oSubtitleSequenceBean.addOriginalLine( lineNumber, line);
							lineNumber++;
							logger.debug("*** INFO : Processed lines : sequence = "+this.sequenceCount+ "Lines= "+lineNumber+" Total= "+this.oSubtitleStats.getOriginalLines());								
						}
					}
				}
			}
			else{
				if( null != oSubtitleSequenceBean){
					/** Subtitle sequence is added into array list*/
					storeSubtitleSequenceList(oSubtitleSequenceBean);
					this.sequenceCount++;
					oSubtitleSequenceBean = null;
				}
			}
		}
		/** Latest sequence is added */
		if( null != oSubtitleSequenceBean){
			/** Subtitle sequence is added into array list*/
			//oSubtitleSequenceHashTable.add(this.sequenceCount, oSubtitleSequenceBean);
			storeSubtitleSequenceList(oSubtitleSequenceBean);
			this.sequenceCount++;
			oSubtitleSequenceBean = null;
		}

		/** Total number of lines to be translated from last sequence is added */
		this.oSubtitleStats.addOriginalLines(lineNumber);
		
		isSubtitlesBuilt = 1;
		logger.debug("*** INFO : Processed lines : "+this.oSubtitleStats.getOriginalLines());								
		
		return;
	}
	/*-----------------------------------------------------------------------*/
	/*-----------------------------------------------------------------------*/
	/**
	 * This method allows clear a translated line.
	 * 
	 * Translated line is cleared from database and also cleared from 
	 * SubtitleSequenceBean object.
	 * 
	 * Once done, table containing objects of type SubtitleSequenceBean is updated. 
	 * 
	 * @param hashCode Indicates subtitle sequence translated line belongs to.
	 * @param translatedLine translatedLine Translated line to be removed.
	 */
	/*-----------------------------------------------------------------------*/
	public void clearTranslation(String hashCode, String translatedLine) throws SubtitleException {
        logger.debug("Parameter name= " + hashCode +" Translation= "+translatedLine);
		SubtitleSequenceHashCode oSubtitleSequenceHashCode = new SubtitleSequenceHashCode(hashCode);
		Integer lineNumber     = oSubtitleSequenceHashCode.getLineNumber();
		Integer sequenceNumber = oSubtitleSequenceHashCode.getSequenceNumber();

		/** Object SubtitleSequenceBean is retrieved thanks to hashcode provided from HTML page*/
		SubtitleSequenceBean oSubtitleSequenceBean = getSubtitleSequence(sequenceNumber); 
				
		if(null == oSubtitleSequenceBean){			
			String errorMessage ="*** ERROR : clearTranslation() : No Subtitle sequence found for hash code = " + hashCode;
			throw new SubtitleException(errorMessage);					
		}

		logger.debug("*** INFO : clearTranslation() : Sequence number = " + sequenceNumber + " Line number = "+lineNumber) ;

		/** Translated line is cleared  */
		Integer subtitleClearAction =oSubtitleSequenceBean.clearTranslatedLine(lineNumber, translatedLine); 
		if ( SubtitleSequenceBean.SUBTITLE_ACTION_NONE !=  subtitleClearAction){
			try {
				oSubtitleDAO.clearSubtitleTranslation(oSubtitleSequenceBean, lineNumber, subtitleClearAction);
			} catch (DAOException exceptionDAO) {
				logger.debug(exceptionDAO.getMessage()) ;
				exceptionDAO.printStackTrace();
				String errorMessage =exceptionDAO.getMessage();
				throw new SubtitleException(errorMessage);					
			}
			this.oSubtitleStats.decreaseTranslatedLine();
		}
		else{
			return;
		}
			
		/** Then, subtitle sequence is updated in the list of sequences */
		this.storeSubtitleSequenceList(oSubtitleSequenceBean);
		//oSubtitleSequenceHashTable.put(sequenceNumber, oSubtitleSequenceBean);
		
		return;
		
	}	
	/*-----------------------------------------------------------------------*/

	/*-----------------------------------------------------------------------*/
	/** 
	 * This method allows to store the translated line given as parameter into an object 
	 * of type SubtitleSequenceBean.
	 * 
	 * Once done, table containing objects of type SubtitleSequenceBean is updated.
	 *  
	 * @param hashCode Indicates subtitle sequence translated line belongs to.
	 * This parameter is formated as following : L4S18 where 
	 *   --> 45 is the sequence number ; sequence number is numbered from 1.
	 *   --> and 18 is the line into this sequence number. Line is numbered from 
	 * @param translatedLine Translated line to be stored.
	 */
	/*-----------------------------------------------------------------------*/
	public void storeTranslation(String hashCode, String translatedLine){
        logger.debug("Parameter name= " + hashCode +" Translation= "+translatedLine);
		SubtitleSequenceHashCode oSubtitleSequenceHashCode = new SubtitleSequenceHashCode(hashCode);
		Integer lineNumber     = oSubtitleSequenceHashCode.getLineNumber();
		Integer sequenceNumber = oSubtitleSequenceHashCode.getSequenceNumber();

		logger.debug("*** INFO : storeTranslation() : Sequence number = " + sequenceNumber + " Line number = "+lineNumber) ;

		/** Object SubtitleSequenceBean is retrieved thanks to hashcode provided from HTML page*/
		SubtitleSequenceBean oSubtitleSequenceBean = getSubtitleSequence(sequenceNumber);
		
		if(null == oSubtitleSequenceBean){			
			logger.debug("*** ERROR : No Subtitle sequence found for hash code = " + hashCode) ;
			return;
		}
		

		/** Translated line is added  */
		Integer subtitleStoreAction =oSubtitleSequenceBean.addTranslatedLine(lineNumber, translatedLine); 
		if ( SubtitleSequenceBean.SUBTITLE_ACTION_NONE !=  subtitleStoreAction){
			try {
				oSubtitleDAO.storeSubtitleTranslation(oSubtitleSequenceBean, lineNumber, subtitleStoreAction);
			} catch (DAOException exceptionDAO) {
				logger.debug(exceptionDAO.getMessage()) ;
				exceptionDAO.printStackTrace();
				return;
			}
			this.oSubtitleStats.increaseTranslatedLine();
		}
		else{
			return;
		}
			
		/** Then, subtitle sequence is updated in the list of sequences */
		this.storeSubtitleSequenceList(oSubtitleSequenceBean);
		//oSubtitleSequenceHashTable.put(sequenceNumber, oSubtitleSequenceBean);
		
		return;
	}
	/*-----------------------------------------------------------------------*/
		
	/*-----------------------------------------------------------------------*/
	/** This method allows to retrieve an object of type SubtitleSequenceBean
	 * from sequence index given as parameter. 
	 * 
	 * Returns the matched SubtitleSequenceBean object when found, null otherwise.
	 * 
	 * */
	/*-----------------------------------------------------------------------*/
	private SubtitleSequenceBean getSubtitleSequence(Integer sequenceNumber) {
		//SubtitleSequenceBean oSubtitleSequenceBean = this.oSubtitleSequenceHashTable.get(sequenceNumber-1);
		SubtitleSequenceBean oSubtitleSequenceBean = this.oSubtitleSequenceHashTable.get(sequenceNumber);
		return oSubtitleSequenceBean;
	}
	/*-----------------------------------------------------------------------*/

	/*-----------------------------------------------------------------------*/
	/** 
	 * This method allows to check if file name given as parameter is still 
	 * recorded into database. 
	 * 
	 * Returns the matched SubtitleSequenceBean object when found, null otherwise.
	 * @throws SubtitleException 
	 * */
	/*-----------------------------------------------------------------------*/
	public boolean isSubtitleFileNameStored(String subtitleFileName) throws SubtitleException {
		Boolean status = false;
		
		if( null == subtitleFileName){
			logger.debug("*** ERROR : isSubtitleFileNameStored() : Subtitle file name = "+subtitleFileName);
			status = false;
		}else{
			try {
				Integer file_id = oSubtitleDAO.checkFileName(subtitleFileName);;
				status = ( 0<file_id );
			} catch (DAOException exceptionDAO) {
				logger.debug("*** ERROR : isSubtitleFileNameStored() : DAOException : "+exceptionDAO.getLocalizedMessage());				
				String errorMessage ="*** ERROR : Accès au système de données= "+exceptionDAO.getLocalizedMessage();
				throw new SubtitleException(errorMessage);
			}
		}

		return status;
	}
	/*-----------------------------------------------------------------------*/

	/*-----------------------------------------------------------------------*/
	/** 
	 * This method allows to load all records from tables matching with 
	 * subtitle file name given as parameter. 
	 * @throws SubtitleException 
	 * 
	 * */ 
	/*-----------------------------------------------------------------------*/
	public void loadDatabase(String subtitleFileName) throws SubtitleException {
		Vector<Object> vectorSubtitleSequence = null;
		try {
			vectorSubtitleSequence = oSubtitleDAO.loadSubtitleSequence(subtitleFileName);
		} catch (DAOException exceptionDAO) {
			logger.debug(exceptionDAO);
			exceptionDAO.printStackTrace();
			throw new SubtitleException("*** ERROR : loading from database failed! Returned Exception= "+exceptionDAO.getMessage());
		}
		Enumeration<Object> enumSubtitleSequence = vectorSubtitleSequence.elements();
		sequenceCount = 0;
		Integer translatedLines = 0;
		Integer originalLines   = 0;
		
		while (enumSubtitleSequence.hasMoreElements()){
			SubtitleSequenceBean oSubtitleSequenceBean = (SubtitleSequenceBean) enumSubtitleSequence.nextElement();
			logger.debug("Sequence = "+oSubtitleSequenceBean.getSequenceNumber());
			
			//oSubtitleSequenceHashTable.put(oSubtitleSequenceBean.getSequenceNumber(), oSubtitleSequenceBean);
			this.storeSubtitleSequenceList(oSubtitleSequenceBean);
			sequenceCount++;
			translatedLines += oSubtitleSequenceBean.translatedLines.size();
			originalLines   += oSubtitleSequenceBean.originalLinesArrayList.size();
		}
		logger.debug("\n");
		this.oSubtitleStats.setTranslatedLines(translatedLines);
		this.oSubtitleStats.setOriginalLines(originalLines);
	}
	/*-----------------------------------------------------------------------*/
	
	/*-----------------------------------------------------------------------*/
	/**
	 * Store subtitle file name given as parameter into database.
	 * @param subtitleFileName
	 */
	/*-----------------------------------------------------------------------*/	
	public void storeSubtitleFileName(String subtitleFileName) throws SubtitleException {
		try {
			this.file_id = oSubtitleDAO.storeFileName(subtitleFileName);
		} catch (DAOException exceptionDAO) {
			exceptionDAO.printStackTrace();
			throw new SubtitleException("*** ERROR : File= "+subtitleFileName+" / "+exceptionDAO.getMessage());
		}
	}
	/*-----------------------------------------------------------------------*/	

	
	/*-----------------------------------------------------------------------*/	
	/**
	 * 
	 * @return
	 */
	/*-----------------------------------------------------------------------*/	
	public Integer getFile_id() {
		return file_id;
	}
	/*-----------------------------------------------------------------------*/
	
	/*-----------------------------------------------------------------------*/	
	/**
	 * This method allows to return a list of sorted keys from a given HashTable.
	 * 
	 * @param hashtable given hash table with keys to be sorted.
	 * @return The sorted keys from given hash table as a List type object of integers. 
	 */
	/*-----------------------------------------------------------------------*/	
	private List<Integer> getSortedKeys(Hashtable<Integer,String> hashtable){
    	Enumeration<Integer> oEnumerationKeys = hashtable.keys();
    	List<Integer> listKeys = Collections.list(oEnumerationKeys);
    	Collections.sort(listKeys);
		return listKeys;
	}
	/*-----------------------------------------------------------------------*/	

	/*-----------------------------------------------------------------------*/	
	/**
	 * This method allows to write translated lines and sequences into a file with the
	 * expected format.
	 * 
	 * Write is performed sequence per sequence. 
	 * For each sequence, translated lines are written.
	 * 
	 * NB: sequences with at least 1 translated line are written. Otherwise they are ignored.
	 * 
	 */
	/*-----------------------------------------------------------------------*/	
    public void writeTranslatedFile( ) throws IOException {
        
    	translatedFileName = this.fileDirectoryPathName+"/"+ PREFIX_FILE + this.substitleFileName;

    	File oFile = new File(translatedFileName); 

        try {
            /** File is created */
        	oFile.createNewFile();
            final FileWriter oFileWriter = new FileWriter(oFile);
            Enumeration<SubtitleSequenceBean> oEnumeration = this.oSubtitleSequenceHashTable.elements();
            
            try {
	            while ( oEnumeration.hasMoreElements() ){
	            	SubtitleSequenceBean oSubtitleSequenceBean = oEnumeration.nextElement();  
	            	if( 0 <  oSubtitleSequenceBean.translatedLines.size()){
		            	oFileWriter.write(oSubtitleSequenceBean.getSequenceNumber()+"\n");
		            	String startTime = oSubtitleSequenceBean.getStartTime();
		            	String endTime   = oSubtitleSequenceBean.getEndTime();
		            	
		            	oFileWriter.write(startTime+" --> "+endTime+"\n");
		                
		            	/** Write translated line from this sequence */
		            	List<Integer> listKeys = getSortedKeys(oSubtitleSequenceBean.translatedLines);
		            	/**
		            	*/
		                for( Integer key : listKeys){
		        		    logger.debug("*** INFO : writeTranslatedFile() : Index = " + key +" / Line= "+oSubtitleSequenceBean.translatedLines.get(key)) ;
		                	oFileWriter.write(oSubtitleSequenceBean.translatedLines.get(key)+"\n");		                	
		                }
		                oFileWriter.write("\n");
	            	}
	            }
            } finally {
                /** Whatever, file is closed */
            	oFileWriter.close();
            }   
        } catch (Exception exception) {
            logger.debug("*** ERROR : file= "+translatedFileName+" can't be created! Exception= "+exception.getMessage());
        }      
    }
	/*-----------------------------------------------------------------------*/

	/*-----------------------------------------------------------------------*/
    /**
     * This method allows to convert HashTable object containing Subtitle sequences 
     * into an array list object.
     * This is for the intend of use into JSP.
     * @return
     */
	/*-----------------------------------------------------------------------*/
	public ArrayList<SubtitleSequenceBean> getoSubtitleSequenceArrayList() {
		ArrayList<SubtitleSequenceBean> oSubtitleSequenceBeanArrayList = new ArrayList<SubtitleSequenceBean>();
		
		/** Keys from hash table are sorted */
		Enumeration<Integer> oEnumerationKeys = this.oSubtitleSequenceHashTable.keys();
		List<Integer> oListKeys = Collections.list(oEnumerationKeys);
		Collections.sort(oListKeys);
		
		/** ArrayList is filled with objects from oSubtitleSequenceHashTable */
		for (Integer key : oListKeys) {
			oSubtitleSequenceBeanArrayList.add(this.oSubtitleSequenceHashTable.get(key));			
		}
		/**
		logger.debug("\n*** INFO : getoSubtitleSequenceArrayList() : Elements= "+oSubtitleSequenceBeanArrayList.size());
		for( SubtitleSequenceBean oSubtitleSequenceBean : oSubtitleSequenceBeanArrayList ){
			logger.debug("*** INFO : getoSubtitleSequenceArrayList() : Sequence= "+oSubtitleSequenceBean.getSequenceNumber());
		}
		logger.debug("\n");
		*/
		return oSubtitleSequenceBeanArrayList;
	}	
	/*-----------------------------------------------------------------------*/
}
