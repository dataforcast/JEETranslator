package edu.subtitletranslator.beans;

import edu.subtitletranslator.beans.TanslatorBean;

/** This class provides a unique object TranslatorBean. 
 * It this object does not exists, then it is returned.*/
public class TranslatorBeanFactory {
	private static TanslatorBean oTanslatorBean=null;
	public static TanslatorBean getTranslatorBean(String fileName)
	{
		if( null == oTanslatorBean)
		{
			oTanslatorBean = new TanslatorBean(fileName);
		}
		return oTanslatorBean;
	}
}
