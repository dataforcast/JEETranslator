package edu.subtitletranslator.common;

/**
 * This class allows to access configuration file handling application properties.
 * It is implemented as a singleton.
 * @author bangui
 *
 */
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

//import org.apache.log4j.Logger;

public class TranslatorProperties {
	private static TranslatorProperties oTranslatorProperties = null;
	private static Properties oProperties = null;
	private static final String propertyFile = "translator.properties";
	/*-----------------------------------------------------------------------*/
	/**
	 * 
	 * @throws IOException
	 */
	/*-----------------------------------------------------------------------*/
	private TranslatorProperties() throws IOException{
		
	}
	/*-----------------------------------------------------------------------*/
	
	/*-----------------------------------------------------------------------*/
	/**
	 */
	/*-----------------------------------------------------------------------*/
	public static TranslatorProperties getTranslatorProperties() throws IOException{
		if( null == oTranslatorProperties ){
			oTranslatorProperties = new TranslatorProperties();	
			InputStream oInputStream= null;
			oProperties = new Properties();
			try {
				ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
				oInputStream = classLoader.getResourceAsStream( propertyFile );
				if( null == oInputStream ){
					String errorMessage = "*** ERROR (1): TranslatorProperties() : Accessing to configuration file "+propertyFile+" FAILED!";
					throw new IOException(errorMessage);
				}
				oProperties.load(oInputStream);
			} catch (IOException ioe) {
				String errorMessage = "*** ERROR (2): TranslatorProperties() : Accessing to configuration file "+propertyFile+" FAILED!";				
				System.out.println(errorMessage+ioe.getMessage());
			}
		}
		return oTranslatorProperties;
	}
	/*-----------------------------------------------------------------------*/
	/**
	 * This method allows to retrieve a property value recorded into property file 
	 * "translator.properties" from a key given as parameter.
	 * 
	 * @param propertyKey : key matching with property value
	 * @return property value matching with propertyKey 
	 * @throws TranslatorPropertyException
	 */
	public String getPropertyValue(String propertyKey) throws TranslatorPropertyException {
		String propertyValue = null;
		propertyValue = oProperties.getProperty(propertyKey);
		
		if( null ==  propertyValue){
			String errorMessage  = "*** ERROR : Unkown key= "+propertyKey+"\n --> Please check config. file= "+propertyFile;
			throw new TranslatorPropertyException(errorMessage);
		}
		return propertyValue;
	}
}
