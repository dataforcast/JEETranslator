package edu.subtitletranslator.dao;

import java.util.ArrayList;
import java.util.Hashtable;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.text.ParseException;
import java.text.SimpleDateFormat;

/**
 * CREATE TABLE `subtitle_db`.`subtitleSequence` ( `id` INT NOT NULL AUTO_INCREMENT , `seqNumber` INT NOT NULL , `startDate` DATE NOT NULL , `endDate` DATE NOT NULL , PRIMARY KEY (`id`), UNIQUE (`seqNumber`)) ENGINE = InnoDB;
 * CREATE TABLE `subtitle_db`.`original` ( `id` INT NOT NULL AUTO_INCREMENT , `seqNumber` INT NOT NULL , `textLine` VARCHAR(80) NOT NULL , PRIMARY KEY (`id`), INDEX (`seqNumber`)) ENGINE = InnoDB;
 * CREATE TABLE `subtitle_db`.`translation` ( `id` INT NOT NULL AUTO_INCREMENT , `seqNumber` INT NOT NULL , `textLine` VARCHAR(80) NOT NULL , PRIMARY KEY (`id`), INDEX (`seqNumber`)) ENGINE = InnoDB;
 * ALTER  TABLE `subtitle_db`.`translation` ADD `lineNumber` INT NOT NULL AFTER `seqNumber`, ADD INDEX (`lineNumber`);

 */
import edu.subtitletranslator.beans.SubtitleSequenceBean;
import edu.subtitletranslator.common.TranslatorLoggerLevel;

public class DAOSubtitleJBDC implements SubtitleDAO {
	private static final String SQL_SELECT_SEQUENCE = "SELECT seqNumber, startDate, endDate, file_id FROM subtitleSequence, subtitle_file WHERE "
			+ " subtitleSequence.file_id = subtitle_file.id AND subtitle_file.name = ? ORDER BY seqNumber";
	private static final String SQL_SELECT_ORIGINAL = "SELECT textLine FROM original WHERE seqNumber= ? AND file_id= ? ORDER BY id";
	private static final String SQL_SELECT_TRANSLATION = "SELECT textLine FROM translation WHERE seqNumber= ? AND lineNumber= ? AND file_id= ? ORDER BY seqNumber";
	private static final String SQL_SELECT_FILE_ID  = "SELECT id FROM subtitle_file WHERE name= ? ORDER BY id";
	private static final String SQL_INSERT_FILEMANE = "INSERT INTO subtitle_file (id, name) VALUES (NULL,?)";

	private static final String SQL_INSERT_SEQUENCE    = "INSERT INTO subtitleSequence (id, seqNumber, startDate, endDate, file_id) VALUES(NULL, ?,?,?,?)";
	
	private static final String SQL_INSERT_ORIGINAL    = "INSERT INTO original (id, seqNumber, textLine, file_id) VALUES (NULL,?,?,?)";
	
	private static final String SQL_UPDATE_TRANSLATION = "UPDATE translation SET textLine=? WHERE seqNumber= ? AND lineNumber= ? AND file_id= ?";
	private static final String SQL_INSERT_TRANSLATION = "INSERT INTO translation (id, seqNumber, lineNumber, textLine, file_id) VALUES(NULL,?,?,?,?)";
	private static final String SQL_DELETE_TRANSLATION = "DELETE FROM translation WHERE seqNumber = ? AND lineNumber = ? AND file_id = ?";
	
	private static final String SQL_CLEAN_SEQUENCE   = "DELETE FROM subtitleSequence WHERE subtitleSequence.file_id = ? ";
	private static final String SQL_CLEAN_ORIGINAL   = "DELETE FROM original         WHERE original.file_id = ? ";
	private static final String SQL_CLEAN_TRANSLATON = "DELETE FROM translation      WHERE translation.file_id = ? ";
	private static final String SQL_CLEAN_FILE       = "DELETE FROM subtitle_file    WHERE subtitle_file.id = ? ";
	
	private DAOFactory daoFactory = null;
	private Connection connection = null;
	
	private static Logger logger = Logger.getLogger(DAOSubtitleJBDC.class);
	/*-----------------------------------------------------------------------*/
	/**
	 * 
	 * @param daoFactory
	 * @throws DAOException
	 */
	/*-----------------------------------------------------------------------*/
	public DAOSubtitleJBDC(DAOFactory daoFactory) throws DAOException {
		this.daoFactory = daoFactory;
    	Level oLevel = TranslatorLoggerLevel.get();
    	logger.setLevel(oLevel);

		try {
			connection = daoFactory.getConnection();
		} catch (SQLException sQLException) {
			String errorMessage ="*** ERROR : DAOSubtitleJBDC() :  SQLException = "+sQLException.getMessage();
			throw new DAOException(errorMessage);
		}
	}
	/*-----------------------------------------------------------------------*/
	
	/*-----------------------------------------------------------------------*/
	/**
	 * Check from database is subtitle file name given as parameter exists into
	 * database table.
	 * If yes, this mean a translation is there into database.
	 */
	/*-----------------------------------------------------------------------*/
	public Integer checkFileName(String subtitleFileName) throws DAOException {
		Integer status = 0;
		PreparedStatement preparedStatement = null;
		
		if ( null == subtitleFileName ){
			return status;
		}
		
		try {
			connection = daoFactory.getConnection();
		} catch (SQLException sQLException) {
			logger.debug("*** ERROR : checkFileName :  SQLException = "+sQLException.getMessage());
			return status;
		}
		
		try {
			/** Table subtitleSequence is populated */
			preparedStatement = connection.prepareStatement(SQL_SELECT_FILE_ID);
			preparedStatement.setString(1, subtitleFileName);
			ResultSet resultset = preparedStatement.executeQuery();
			
			while( resultset.next() ){
				Integer file_id = resultset.getInt(1);
				status = file_id;
			}
			connection.commit();
		} catch ( SQLException sQLException ) {
			sQLException.printStackTrace();
			if( null != connection ){
				try {
					connection.rollback();
				} catch (SQLException e) {
					e.printStackTrace();
				}
			}			
			throw new DAOException(sQLException.getMessage());
		}finally{
			try {
				connection.close();
			} catch (SQLException e) {
				e.printStackTrace();
				throw new DAOException(e.getMessage());
			}
		}

		return status;
	}
	/*-----------------------------------------------------------------------*/

	
	/*-----------------------------------------------------------------------*/
	/**
	 * This method allows to store translated lines included into oSubtitleSequenceBean 
	 * given as parameter and matching with lineNumber given as parameter.
	 * 
	 * The check if line is already into database table or if line needs to be updated 
	 * has been previously performed from the caller side.
	 */
	/*-----------------------------------------------------------------------*/
	public Boolean storeSubtitleTranslation(SubtitleSequenceBean oSubtitleSequenceBean, Integer lineNumber, Integer storeAction) 
			throws DAOException {
		
		Integer seqNumber = oSubtitleSequenceBean.getSequenceNumber();
		PreparedStatement preparedStatement = null;
		
		try {
			connection = daoFactory.getConnection();
		} catch (SQLException sQLException) {
			String errorMessage ="storeSubtitleTranslation() : daoFactory.getConnection() throws SQLException= "+sQLException.getMessage(); 
			throw new DAOException(errorMessage);
		}

		/** Table translation is populated */
		try {
			Hashtable<Integer, String> translatedLines = oSubtitleSequenceBean.getTranslatedLines();
			if( null == translatedLines ){
				connection.close();
				String errorMessage = "*** INFO : storeSubtitleTranslation() : INSERT translatedLines= "+ translatedLines; 
				throw new DAOException(errorMessage);
			}else{
				/** Some lines have been translated */
			    String translation = oSubtitleSequenceBean.translatedLines.get(lineNumber);
				if( SubtitleSequenceBean.SUBTITLE_ACTION_INSERT == storeAction ){
					preparedStatement = connection.prepareStatement(SQL_INSERT_TRANSLATION);
			    	logger.debug("*** INFO : storeSubtitleTranslation() : INSERT translation= "+translation);
			    	preparedStatement.setInt(1, seqNumber.intValue());
			    	preparedStatement.setInt(2, lineNumber );
			    	preparedStatement.setString(3, translation );
			    	preparedStatement.setInt(4, oSubtitleSequenceBean.getFile_id() );		
				}else if(SubtitleSequenceBean.SUBTITLE_ACTION_UPDATE == storeAction){
					preparedStatement = connection.prepareStatement(SQL_UPDATE_TRANSLATION);			
			    	logger.debug("*** INFO : storeSubtitleTranslation() : UPDATE translation= "+translation);
			    	preparedStatement.setString(1, translation );
			    	preparedStatement.setInt(2, seqNumber.intValue());
			    	preparedStatement.setInt(3, lineNumber );
			    	preparedStatement.setInt(4, oSubtitleSequenceBean.getFile_id() );		
					
				}else{
				    connection.close();
					String errorMessage ="*** ERROR : storeSubtitleTranslation() : Unknown action= "+storeAction; 
				    logger.debug(errorMessage);;
				}
		    	
		    	preparedStatement.executeUpdate();
			    connection.commit();
		    }
		}catch (SQLException sQLException) {
			if( null != connection ){
				try {
					connection.rollback();
				} catch (SQLException e) {
					e.printStackTrace();
				}
			}
			String errorMessage ="storeSubtitleTranslation() : SQLException= "+sQLException.getMessage(); 
			throw new DAOException(errorMessage);
		}finally {
			try {
				connection.close();
			} catch (SQLException e) {
				throw new DAOException(e.getMessage());
			}
		}
		return true;
	}
	/*-----------------------------------------------------------------------*/

	/*-----------------------------------------------------------------------*/
	/**
	 * This method allows to insert a  subtitle sequence into database tables.
	 * A subtitle sequence handle all informations for translation, excepted translated 
	 * lines.
	 * 2 tables are involved in this process : 
	 * a) subtitleSequence table containing all informations from subtitle file.
	 * b) original table containing lines to be translated. 
	 * Existence of sequence into table is checked along with sequence number.
	 * If record exists, it is updated. Otherwise, it is created. 
	 * @param oSubtitleSequenceBean object to be inserted into database table.
	 */
	/*-----------------------------------------------------------------------*/
	@Override
	public Boolean storeSubtitleSequence( SubtitleSequenceBean oSubtitleSequenceBean ) 
			throws DAOException {

		
		Integer seqNumber = oSubtitleSequenceBean.getSequenceNumber();
		String startTime = oSubtitleSequenceBean.getStartTime();
		String endTime   = oSubtitleSequenceBean.getEndTime();
		Integer file_id = oSubtitleSequenceBean.getFile_id();
		PreparedStatement preparedStatement = null;
		if( seqNumber == null || startTime == null || endTime == null || file_id == null){
			String errorMessage = "Subtitle sequence : NULL parameter from file! ";
			if ( null == startTime ){
				errorMessage = "Subtitle sequence : Start date is NULL; check file format! ";
			}else if( null == endTime ){
				errorMessage = "Subtitle sequence : End date is NULL; check file format! ";
			}else if( null == seqNumber ){
				errorMessage = "Subtitle sequence : Sequence number is NULL; check file format! ";
			}else if( null == file_id ){
				errorMessage = "Subtitle sequence : File ID is NULL!";
			}else{
				
			}
			throw new DAOException(errorMessage);
		}


		try {
			connection = daoFactory.getConnection();
		} catch (SQLException sQLException) {
			String errorMessage ="storeSubtitleSequence() : daoFactory.getConnection() throws SQLException = "+sQLException.getMessage(); 
			throw new DAOException(errorMessage);
		}
		SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss,SSS");

		java.util.Date parsedTimeStamp = null;
	    Timestamp timestampStart = null;
	    Timestamp timestampEnd = null;
	    try {
			parsedTimeStamp = (java.util.Date) dateFormat.parse("0000-00-00"+" "+startTime);
			timestampStart  = new Timestamp(parsedTimeStamp.getTime());			
			parsedTimeStamp = (java.util.Date) dateFormat.parse("0000-00-00"+" "+endTime);
			timestampEnd    = new Timestamp(parsedTimeStamp.getTime());			
		} catch (ParseException e) {
			String errorMessage ="*** ERROR on date convertion! "; 
			throw new DAOException(errorMessage);
		}
		
		try {
			/** Table subtitleSequence is populated */
			preparedStatement = connection.prepareStatement(SQL_INSERT_SEQUENCE); 

			preparedStatement.setInt(1, seqNumber.intValue());
			preparedStatement.setTimestamp(2, timestampStart);
			preparedStatement.setTimestamp(3, timestampEnd);
			preparedStatement.setInt(4, file_id.intValue());		

			preparedStatement.executeUpdate();

			/** Table original table is populated */
			preparedStatement = connection.prepareStatement(SQL_INSERT_ORIGINAL);			
			
			ArrayList<String> originalLinesArrayList = oSubtitleSequenceBean.getoriginalLinesArrayList();

			for( String textLine : originalLinesArrayList){
				preparedStatement.setInt(1, seqNumber.intValue());
				preparedStatement.setString(2, textLine );
				preparedStatement.setInt(3, file_id.intValue());		

				preparedStatement.executeUpdate();
			}
			connection.commit();
		} catch ( SQLException sQLException ) {
			String errorMessage ="storeSubtitleSequence() : throws SQLException = "+sQLException.getMessage();
			if( null != connection ){
				try {
					connection.rollback();
				} catch (SQLException e) {
					e.printStackTrace();
				}
			}
			throw new DAOException(errorMessage);
		}finally{
			if( null != connection ){
				try {
					connection.close();
				} catch (SQLException e) {
					e.printStackTrace();
					throw new DAOException(e.getMessage());
				}
			}
			
		}
		return true;
	}
	/*-----------------------------------------------------------------------*/
	
	/*-----------------------------------------------------------------------*/
	/**
	 * Clean tables from database.
	 */
	/*-----------------------------------------------------------------------*/
	@Override
	public Boolean clean(Integer file_id) throws DAOException {		
		PreparedStatement preparedStatement = null;
		Boolean status = false;
		try {
			connection = daoFactory.getConnection();
		} catch (SQLException sQLException) {
			String errorMessage ="clean() : daoFactory.getConnection() throws SQLException = "+sQLException.getMessage(); 
			throw new DAOException(errorMessage);
		}
		if( null == file_id ){
			String errorMessage =" NULL file identifier "; 
			throw new DAOException(errorMessage);
		}
		try {
			preparedStatement = connection.prepareStatement(SQL_CLEAN_SEQUENCE);
			preparedStatement.setInt(1, file_id);
			preparedStatement.executeUpdate();	
			
			preparedStatement = connection.prepareStatement(SQL_CLEAN_ORIGINAL);			
			preparedStatement.setInt(1, file_id);
			preparedStatement.executeUpdate();	

			preparedStatement = connection.prepareStatement(SQL_CLEAN_TRANSLATON);			
			preparedStatement.setInt(1, file_id);
			preparedStatement.executeUpdate();	

			preparedStatement = connection.prepareStatement(SQL_CLEAN_FILE);			
			preparedStatement.setInt(1, file_id);
			preparedStatement.executeUpdate();	

			connection.commit();

		} catch ( SQLException sQLException ) {
			String errorMessage ="clean() : throws SQLException= "+sQLException.getMessage(); 
			if( null != connection ){
				try {
					connection.rollback();
				} catch (SQLException e) {
					e.printStackTrace();
				}
			}
			throw new DAOException(errorMessage);
		} finally {
			try {
				connection.close();
			} catch (SQLException e) {
				e.printStackTrace();
				throw new DAOException(e.getMessage());
			}			
			status = true;
		}
		return status;		
	}
	/*-----------------------------------------------------------------------*/

	/*-----------------------------------------------------------------------*/
	/**
	 * Load records from tables into database matching with subtitleFileName.
	 */
	/*-----------------------------------------------------------------------*/
	@Override
	public java.util.Vector<Object> loadSubtitleSequence(String subtitleFileName) throws DAOException {
		PreparedStatement preparedStatement = null;
		java.util.Vector<Object> vectorSubtitleSequence = new java.util.Vector<Object>(); 
		try {
			connection = daoFactory.getConnection();
		} catch (SQLException sQLException) {
			String errorMessage ="loadSubtitleSequence() : daoFactory.getConnection() throws SQLException = "+sQLException.getMessage(); 
			throw new DAOException(errorMessage);
		}
		try {
			/** Sequence informations matching with subtitle file name are retrieved */
			preparedStatement = connection.prepareStatement(SQL_SELECT_SEQUENCE);			
			preparedStatement.setString(1, subtitleFileName);
			ResultSet resultset = preparedStatement.executeQuery();

			while( resultset.next() ){
				SubtitleSequenceBean oSubtitleSequenceBean = new SubtitleSequenceBean(); 
				Integer sequenceNumber = resultset.getInt(1);
				Timestamp    startTime      = resultset.getTimestamp(2);
				Timestamp    endTime        = resultset.getTimestamp(3);
				Integer file_id        = new Integer(resultset.getInt(4));
				
				oSubtitleSequenceBean.setStartTime(startTime.toString());
				oSubtitleSequenceBean.setEndTime(endTime.toString());
				oSubtitleSequenceBean.setSequenceNumber(sequenceNumber);
				oSubtitleSequenceBean.setFile_id(file_id);
				
				/** Original lines matching with sequence number and file are retrieved from table */
				preparedStatement = connection.prepareStatement(SQL_SELECT_ORIGINAL);			
				preparedStatement.setInt(1, sequenceNumber);
				preparedStatement.setInt(2, file_id);
				ResultSet resultset2 = preparedStatement.executeQuery();
				Integer lineNumber = 0;
				while( resultset2.next() ){
					String originalLine = resultset2.getString(1);
					oSubtitleSequenceBean.addOriginalLine(lineNumber, originalLine);
					preparedStatement = connection.prepareStatement(SQL_SELECT_TRANSLATION);			
					preparedStatement.setInt(1, sequenceNumber);
					preparedStatement.setInt(2, lineNumber);
					preparedStatement.setInt(3, file_id);
					ResultSet resultset3 = preparedStatement.executeQuery();
					while( resultset3.next() ){
						String translatedLine = resultset3.getString(1);
						oSubtitleSequenceBean.addTranslatedLine(lineNumber, translatedLine);
					}
					lineNumber++;
				}
				
				/** Sequence is added into array list */
				vectorSubtitleSequence.addElement(oSubtitleSequenceBean);
				connection.commit();
			}
		} catch ( SQLException sQLException ) {
			String errorMessage ="loadSubtitleSequence() : throws SQLException = "+sQLException.getMessage();
			if( null != connection ){
				try {
					connection.rollback();
				} catch (SQLException e) {
					e.printStackTrace();
				}
			}
			throw new DAOException(errorMessage);
		}finally{
			try {
				connection.close();
			} catch (SQLException e) {
				e.printStackTrace();
				throw new DAOException(e.getMessage());
			}
		}
		
		return vectorSubtitleSequence;
	}
	/*-----------------------------------------------------------------------*/
	
	/*-----------------------------------------------------------------------*/
	/**
	 * 
	 */
	/*-----------------------------------------------------------------------*/
	@Override
	public Integer storeFileName(String subtitleFileName) throws DAOException {
		PreparedStatement preparedStatement = null;
		Integer file_id = 0;
		try {
			connection = daoFactory.getConnection();
		} catch (SQLException sQLException) {
			String errorMessage ="storeFileName() : storeFileName() throws SQLException = "+sQLException.getMessage(); 
			throw new DAOException(errorMessage);
		}

		try {
			preparedStatement = connection.prepareStatement(SQL_INSERT_FILEMANE);	
			preparedStatement.setString(1, subtitleFileName);
			preparedStatement.executeUpdate();				
			
			/** Id is retrieved from this insertion */
			preparedStatement = connection.prepareStatement(SQL_SELECT_FILE_ID);	
			preparedStatement.setString(1, subtitleFileName);
			ResultSet resultset = preparedStatement.executeQuery();		
			connection.commit();
			while( resultset.next() ){
				file_id = resultset.getInt(1);				
				logger.debug("*** INFO : storeFileName() : File ID= "+file_id);
			}
		} catch ( SQLException sQLException ) {
			String errorMessage ="storeFileName() : throws SQLException= "+sQLException.getMessage();
			if( null != connection ){
				try {
					connection.rollback();
				} catch (SQLException e) {
					e.printStackTrace();
				}
			}
			throw new DAOException(errorMessage);
		}finally {
			try {
				connection.close();
			} catch (SQLException e) {
				e.printStackTrace();
				throw new DAOException(e.getMessage());
			}			
		}
		logger.debug("*** INFO : storeFileName() : File ID= "+file_id);
		return file_id;
	}
	/*-----------------------------------------------------------------------*/

	/*-----------------------------------------------------------------------*/
	/**
	 * This method allows to delete a record from table translation.
	 * @param  oSubtitleSequenceBean sequence translated line belongs to.
	 * @param  lineNumber translated line number from inside sequence.  
	 */
	/*-----------------------------------------------------------------------*/
	@Override
	public void clearSubtitleTranslation(SubtitleSequenceBean oSubtitleSequenceBean, Integer lineNumber,
		Integer subtitleClearAction) throws DAOException {

		PreparedStatement preparedStatement = null;
		

		try {
			connection = daoFactory.getConnection();
		} catch (SQLException sQLException) {
			String errorMessage ="storeFileName() : storeFileName() throws SQLException = "+sQLException.getMessage(); 
			throw new DAOException(errorMessage);
		}
		try {
			Integer file_id = oSubtitleSequenceBean.getFile_id();
			Integer seqNumber = oSubtitleSequenceBean.getSequenceNumber();

			preparedStatement = connection.prepareStatement(SQL_DELETE_TRANSLATION);	
			preparedStatement.setInt(1, seqNumber);
			preparedStatement.setInt(2, lineNumber);
			preparedStatement.setInt(3, file_id);
			
			preparedStatement.executeUpdate();				
			connection.commit();

		} catch ( SQLException sQLException ) {
			String errorMessage ="storeFileName() : throws SQLException= "+sQLException.getMessage(); 
			if( null != connection ){
				try {
					connection.rollback();
				} catch (SQLException e) {
					e.printStackTrace();
				}
			}
			throw new DAOException(errorMessage);
		}finally{
			try {
				connection.close();
			} catch (SQLException e) {
				e.printStackTrace();
				throw new DAOException(e.getMessage());
			}			
		}
		
	}
	/*-----------------------------------------------------------------------*/

}
