package edu.subtitletranslator.beans;

public class SubtitleException extends Exception {
	private static final long serialVersionUID = 1L;

	/**
	 * 
	 */
	public SubtitleException(String errorMessage){
		super(errorMessage);
	}
}
