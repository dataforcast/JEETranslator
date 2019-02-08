package edu.subtitletranslator.dao;

import java.util.Vector;

import edu.subtitletranslator.beans.SubtitleSequenceBean;


/** This interface is an abstraction to access subtitles objects. 
 * It allows multiples implementations ways for accessing subtitles from a data source*/

public interface SubtitleDAO {
	/** Store a new subtitle object into a data source 
	 * @throws DAOException */
	Boolean storeSubtitleSequence( SubtitleSequenceBean oSubtitleBEAN ) throws DAOException;
    Boolean storeSubtitleTranslation(SubtitleSequenceBean oSubtitleSequenceBean, Integer lineNumber, Integer storeAction) throws DAOException;
	Boolean clean(Integer file_id) throws DAOException;
	Integer checkFileName(String subtitleFileName)  throws DAOException;
	Vector<Object> loadSubtitleSequence(String subtitleFileName) throws DAOException;
	Integer storeFileName(String subtitleFileName) throws DAOException;
	void clearSubtitleTranslation(SubtitleSequenceBean oSubtitleSequenceBean, Integer lineNumber,
			Integer subtitleClearAction) throws DAOException;
}