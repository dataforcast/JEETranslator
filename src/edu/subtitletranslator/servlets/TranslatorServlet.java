package edu.subtitletranslator.servlets;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Enumeration;


import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.Part;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;

import edu.subtitletranslator.common.TranslatorLoggerLevel;

import edu.subtitletranslator.beans.SubtitleDataManagerBean;
import edu.subtitletranslator.beans.SubtitleSequenceBean;
import edu.subtitletranslator.beans.SubtitleException;

/**
 * Servlet implementation class TranslatorServlet
 */
@WebServlet("/TranslatorServlet")
public class TranslatorServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;
	private static final String TRANSLATOR_SERVLET = "/WEB-INF/translator.jsp";
	private SubtitleDataManagerBean oSubtitleDataManagerBean = null;

	private Boolean isSubstitlesSourced = false;
	private String subtitleFileName = null;
	
	private static Logger logger = Logger.getLogger(TranslatorServlet.class);
	
	/*-----------------------------------------------------------------------*/
    /**
     * Default constructor.  
     */
	/*-----------------------------------------------------------------------*/
    public TranslatorServlet() {
    	Level oLevel = TranslatorLoggerLevel.get();
    	logger.setLevel(oLevel);
		logger.debug( "Initialization done! (2)");
    }
	/*-----------------------------------------------------------------------*/

	/*-----------------------------------------------------------------------*/
	/**
	 * 
	 */
	/*-----------------------------------------------------------------------*/
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
				
		logger.debug( "Entering...");
		ServletContext context = getServletContext();
		/*
		 * Read file containing subtitles to be translated or load subtitles informations from database.
		 */
		logger.debug("Subtitle file name= "+subtitleFileName+" isSubstitlesSourced= "+isSubstitlesSourced);
		logger.debug("Test for sourcing subtitles infos. = "+( null != subtitleFileName && !subtitleFileName.isEmpty()));
		if ( null != subtitleFileName && !subtitleFileName.isEmpty()){
			if ( false == isSubstitlesSourced ){
				try{
					oSubtitleDataManagerBean = new SubtitleDataManagerBean(subtitleFileName);
				
					/** Check if translation related to this subtitle file has been already stored into databse */
					if ( true == oSubtitleDataManagerBean.isSubtitleFileNameStored(subtitleFileName) ){
						/** Subtitle translation is in database; all informations are loaded from database */
						logger.debug("Loading from database...");
						oSubtitleDataManagerBean.loadDatabase(subtitleFileName);
					}else{
						/** No translation related to this file is recorded into database */
						logger.debug("Reading from file= "+subtitleFileName);
						
						//oSubtitleDataManagerBean.cleanDatabase();
						String realPathFileName = context.getRealPath("/WEB-INF/"+subtitleFileName);

						/** File name is stored into database*/
						oSubtitleDataManagerBean.storeSubtitleFileName(subtitleFileName);
						
						/** All informations are read from original file, mean, file to be translated */
						oSubtitleDataManagerBean.readOriginalFile(realPathFileName);
						
						/** Subtitle sequences are built from information issued from file */
						oSubtitleDataManagerBean.buildSubtitleSequences();
						
						/** Subtitle sequences are stored into database */
						oSubtitleDataManagerBean.storeSubtitleSequence();
						
						/** Flag for new uploaded subtitle file is turned on. */
						isSubstitlesSourced = true;						
					}
				}catch(SubtitleException oSubtitleException){
					request.setAttribute("error", oSubtitleException.getMessage());
				}
			}

			/** If previous steps did not throws any exception, then continue...*/
			if( null != oSubtitleDataManagerBean ){
				ArrayList<SubtitleSequenceBean> oSubtitleSequenceArrayList = oSubtitleDataManagerBean.getoSubtitleSequenceArrayList();
				
				if( null != oSubtitleSequenceArrayList){				
					logger.debug("Number of Sequences = "+oSubtitleSequenceArrayList.size());
					request.setAttribute("oSubtitleSequenceArrayList", oSubtitleSequenceArrayList);
					request.setAttribute("subtitleFileName", oSubtitleDataManagerBean.getSubstitleFileName());
					request.setAttribute("oSubtitleStats", oSubtitleDataManagerBean.getoSubtitleStats());
				}
			}
		}

		
		this.getServletContext().getRequestDispatcher(TRANSLATOR_SERVLET).forward(request, response);
	}
	/*-----------------------------------------------------------------------*/
	/*-----------------------------------------------------------------------*/
	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	/*-----------------------------------------------------------------------*/
	protected void doPost(HttpServletRequest request, HttpServletResponse response) 
	throws ServletException, IOException 
	{
		boolean isDumpFile = false;
		boolean isReset    = false;

		/** Get attribute and set it as same on HTML page */
		request.setCharacterEncoding("UTF-8");
        String description = request.getParameter("description");
        request.setAttribute("description", description );
        
        /** Get all informations from file */
        Part part = request.getPart("fichier");

        /** Has a new subtitle file been uploaded ?*/
        String loadedSubtitleFileName = getSubtitleFileName(part);
		logger.debug("loadedSubtitleFileName = "+loadedSubtitleFileName+" subtitleFileName= "+subtitleFileName);
		
        if (loadedSubtitleFileName != null && !loadedSubtitleFileName.isEmpty()) {
            String fieldName = part.getName();
            /** Corrige un bug du fonctionnement d'Internet Explorer */
            loadedSubtitleFileName = loadedSubtitleFileName.substring(loadedSubtitleFileName.lastIndexOf('/') + 1)
                    .substring(loadedSubtitleFileName.lastIndexOf('\\') + 1);
            
            /** Check if translation is already in progress; if not, field subtitleFileName is updated*/
            if( ! loadedSubtitleFileName.equals(subtitleFileName) ){
            	
            	/** A new subtitle file has been uploaded for translation */
            	subtitleFileName = loadedSubtitleFileName;
                isSubstitlesSourced = false;
        		logger.debug("isSubstitlesSourced= "+isSubstitlesSourced);
            }
            request.setAttribute(fieldName, subtitleFileName);
        }
        
        /** Processing of parameters from view */
        Enumeration<String> paramNames = request.getParameterNames();
        
        while(paramNames.hasMoreElements()) {
            String hashCode        = (String)paramNames.nextElement();
            String translatedLine  = request.getParameter(hashCode);
    		logger.debug("Recording hashcode= "+hashCode+" Translation= "+translatedLine);
    		if(hashCode.contains("C")){
            	if ( null != oSubtitleDataManagerBean ){
                	try {
    					oSubtitleDataManagerBean.clearTranslation(hashCode,translatedLine);
    				} catch (SubtitleException oSubtitleException) {
    					oSubtitleException.printStackTrace();
    					request.setAttribute("error", oSubtitleException.getMessage());
    				}            		
            	}else{
            		String errorMessage ="*** ERROR : Please check loaded file!"; 
    				request.setAttribute("error",errorMessage);
            	}
    		}else if(hashCode.contains("DUMP")){
    			isDumpFile = true;
    			/** When translation is written into a file, then database related to this translation is cleaned */
    			isReset = true;
    		}else if(hashCode.contains("RESET")){
    			/** Dump current translation*/
    			isReset = true;
    		}else{
    			/** Translated lines are stored */
                if( 0<translatedLine.length() ){
                	if ( null != oSubtitleDataManagerBean ){
                    	oSubtitleDataManagerBean.storeTranslation(hashCode,translatedLine);            		
                	}else{
                		String errorMessage ="*** ERROR : Please check loaded file = "+loadedSubtitleFileName; 
    					request.setAttribute("error",errorMessage);
                	}
                }
    		}
         }
 
		if ( true == isDumpFile ){
		    isDumpFile = false;
			oSubtitleDataManagerBean.writeTranslatedFile();
			String dumpedFileName = oSubtitleDataManagerBean.getTranslatedFileName();
			logger.debug("Dumped file = "+dumpedFileName);
		    request.setAttribute("dumpedFileName", dumpedFileName);
		}
		
        if( true == isReset ){
        	isReset = false;
            this.isSubstitlesSourced = false;

			try {
				if( null != this.oSubtitleDataManagerBean ){
					this.oSubtitleDataManagerBean.cleanDatabase();
					this.oSubtitleDataManagerBean = null;
	                this.subtitleFileName         = null;
				}                
			} catch (SubtitleException e) {
				request.setAttribute("error",e.getMessage());
			}  
        }
		doGet(request, response);
	}
	/*-----------------------------------------------------------------------*/

	/*-----------------------------------------------------------------------*/
	/**
	 * This method allows to retrieve file name from HTTP request  
	 * @param part
	 * @return
	 */
	/*-----------------------------------------------------------------------*/
	private String getSubtitleFileName(Part part) {
        for ( String contentDisposition : part.getHeader( "content-disposition" ).split( ";" ) ) {
            if ( contentDisposition.trim().startsWith( "filename" ) ) {
                return contentDisposition.substring( contentDisposition.indexOf( '=' ) + 1 ).trim().replace( "\"", "" );
            }
        }
        return null;
    }   
	/*-----------------------------------------------------------------------*/
}
