package edu.subtitletranslator.dao;

public class DAOException extends Exception {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public DAOException(String message){
		super("*** ERROR : DAO : "+message);
	}
}
