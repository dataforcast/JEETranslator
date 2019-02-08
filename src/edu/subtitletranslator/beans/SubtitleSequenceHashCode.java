package edu.subtitletranslator.beans;

public class SubtitleSequenceHashCode {

	private static final String REGEXP_FORM_HASHCODE_RECORD = "(^S{1}[0-9]+(L{1}[0-9]+$))";
	private static final String REGEXP_FORM_HASHCODE_CLEAR  = "(^C{1}[0-9]+(L{1}[0-9]+$))";
	private Integer lineNumber = -1;
	private Integer sequenceNumber = -1;

	/*-----------------------------------------------------------------------*/
	/**
	 * 
	 * @param hashCode
	 */
	/*-----------------------------------------------------------------------*/
	public SubtitleSequenceHashCode(String hashCode){
		if( hashCode.matches(REGEXP_FORM_HASHCODE_RECORD)){
			int sequenceIndex = hashCode.indexOf('S');
			int lineIndex = hashCode.indexOf('L');
			int length    = hashCode.length();		
			sequenceNumber = new Integer (hashCode.substring(sequenceIndex+1,lineIndex));
			lineNumber     = new Integer (hashCode.substring(lineIndex+1,length));

		}else if ( hashCode.matches(REGEXP_FORM_HASHCODE_CLEAR)){
			int sequenceIndex = hashCode.indexOf('C');
			int lineIndex = hashCode.indexOf('L');
			int length    = hashCode.length();		
			sequenceNumber = new Integer (hashCode.substring(sequenceIndex+1,lineIndex));
			lineNumber     = new Integer (hashCode.substring(lineIndex+1,length));
			
		}
	}
	/*-----------------------------------------------------------------------*/

	/*-----------------------------------------------------------------------*/
	/**
	 * 
	 * @return
	 */
	/*-----------------------------------------------------------------------*/
	public Integer getLineNumber() {
		return lineNumber;
	}
	/*-----------------------------------------------------------------------*/

	/*-----------------------------------------------------------------------*/
	/**
	 * 
	 * @return
	 */
	/*-----------------------------------------------------------------------*/
	public Integer getSequenceNumber() {
		return sequenceNumber;
	}
	/*-----------------------------------------------------------------------*/

}
