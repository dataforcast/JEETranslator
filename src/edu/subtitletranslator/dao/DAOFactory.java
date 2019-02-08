package edu.subtitletranslator.dao;

import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

import edu.subtitletranslator.common.TranslatorProperties;
import edu.subtitletranslator.common.TranslatorPropertyException;

/**
 * This class provides services allowing to retrieve a JDBC driver.
 * It is an abstraction layer to access such drivers.
 * The one supported are : MySQL, SQLITE
 * @author bangui
 *
 */

public class DAOFactory {
	
	public  static String MYSQL_ENGINE  = "MYSQL_ENGINE";
	public  static String SQLITE_ENGINE = "SQLITE_ENGINE";
    
	private String url;
    private String username;
    private String password;
    private String jdbcEngine;

    DAOFactory(String url, String username, String password, String jdbcEngine) {
        this.url = url;
        this.username = username;
        this.password = password;
        this.setJdbcEngine(jdbcEngine);
    }
    /*-----------------------------------------------------------------------*/

    /*-----------------------------------------------------------------------*/
    /**
     * Load JDBC driver and returns an instance of DAOFactory.
     * @return an instance of DAOFactory singleton.  
     */
    /*-----------------------------------------------------------------------*/
    public static DAOFactory getInstance() throws DAOException {
    	String errorMessage = null;
    	
    	String jdbcURL     = null;
    	String jdbcDriver  = null;
    	String jdbcEngine  = null;

    	String userDatabase = null;
    	String userPasswd   = null;
    	
    	/** Get parameters, properties for configuration */
        TranslatorProperties oTranslatorProperties;
		try {
			oTranslatorProperties = TranslatorProperties.getTranslatorProperties();
		} catch ( IOException oIOException ) {
			oIOException.printStackTrace();
        	errorMessage = "*** ERROR : getInstance() : Loading properties for configuration failed! IOException = "+oIOException.getMessage();
        	throw new DAOException(errorMessage);				
		}
        try {
        		jdbcEngine = oTranslatorProperties.getPropertyValue("jdbc.engine");
			
			/** Check property issued from configuration file against supported JDBC database engines */
			if( jdbcEngine.equals(MYSQL_ENGINE) ){
				jdbcDriver = oTranslatorProperties.getPropertyValue("jdbc.mysql.driver");
				jdbcURL    = oTranslatorProperties.getPropertyValue("jdbc.mysql.url");
				userDatabase = oTranslatorProperties.getPropertyValue("database.user");
				userPasswd   = oTranslatorProperties.getPropertyValue("database.passwd");
			}else if( jdbcEngine.equals(SQLITE_ENGINE) ){
				jdbcDriver = oTranslatorProperties.getPropertyValue("jdbc.sqlite.driver");
				jdbcURL    = oTranslatorProperties.getPropertyValue("jdbc.sqlite.url");
				jdbcURL    = jdbcURL+"/"+oTranslatorProperties.getPropertyValue("translator.rootdir")+"/"+oTranslatorProperties.getPropertyValue("jdbc.dbfile");
				userDatabase = null;
				userPasswd   = null;
			}else{
	        	errorMessage = "*** ERROR : getInstance() : Unsupported databse engine= "+jdbcEngine;
	        	throw new DAOException(errorMessage);				
			}
		} catch (TranslatorPropertyException oTranslatorPropertyException) {
        	errorMessage = "*** ERROR : getInstance() : TranslatorPropertyException = "+oTranslatorPropertyException.getMessage();
        	throw new DAOException(errorMessage);
		} catch( Exception oException ){
        	errorMessage = "*** ERROR : getInstance() : Exception = "+oException.getMessage();
        	throw new DAOException(errorMessage);
		}
    	
    	/** Load JDBC driver */
        try {
            Class.forName(jdbcDriver);        		
        } catch (ClassNotFoundException classNotFoundException) {
        	errorMessage = "*** ERROR : getInstance() : ClassNotFoundException = "+classNotFoundException.getMessage();
        	throw new DAOException(errorMessage);
        }
        
        DAOFactory instance = new DAOFactory(jdbcURL, userDatabase, userPasswd, jdbcEngine);
        return instance;
    }
    /*-----------------------------------------------------------------------*/
    
    /*-----------------------------------------------------------------------*/
    /**
     * Get connection with auto-commit.
     * 
     * @return connection to database.
     * @throws SQLException
     */
    /*-----------------------------------------------------------------------*/
    public Connection getConnection() throws SQLException {
    	Connection connection = null;
    	connection = DriverManager.getConnection(this.url, this.username, this.password);

    	if( null == connection ){
    		String errorMessage="*** ERROR : Getting connection FAILED for JDBC Engine= "+this.jdbcEngine;
    		throw new SQLException(errorMessage);
    	}
    	connection.setAutoCommit(false);
    	return connection;
    }
    /*-----------------------------------------------------------------------*/

    /*-----------------------------------------------------------------------*/
    /**
     */
    /*-----------------------------------------------------------------------*/
    public SubtitleDAO getSubtitleDAO() throws DAOException {
        return new DAOSubtitleJBDC(this);
    }
    /*-----------------------------------------------------------------------*/

	public String getJdbcEngine() {
		return jdbcEngine;
	}

	public void setJdbcEngine(String jdbcEngine) {
		this.jdbcEngine = jdbcEngine;
	}

}