package edu.subtitletranslator.common;

public class TranslatorPropertyException extends Exception {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public TranslatorPropertyException(String message){
		super("*** ERROR : TranslatorPropertyException : "+message);
	}
}
