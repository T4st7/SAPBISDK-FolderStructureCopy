package be.stibmivb.bi.utilities;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class LoadProperties {
	
	private String environment;
	private String authentication;
	private String BODEV;
	private String BOUAT;
	private String BOPRD;
	private String CMSDEV;
	private String CMSUAT;
	private String CMSPRD;
	private String portCMS;
	private String userName;
	private String passwordD;
	private String passwordQ;
	private String passwordP;
	private String saveAsStoreID;
	private String standardReportsID;
	private String rootBusinessID;
	private Properties prop;
	private InputStream propFile;
	
	//Constructor
	public LoadProperties(String env) throws IOException {
		//Instantiate prop using property file
		try {
		propFile = getPropFile();
		prop = new Properties();
		prop.load(propFile);
		} catch (IOException ex) {
			ex.printStackTrace();	
		} finally {
			if (propFile != null) {
				try {
					propFile.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}	
		//************************************
		environment = setEnvironment(env);
		authentication = prop.getProperty("authentication");
		BODEV = prop.getProperty("BODEV");
		BOUAT = prop.getProperty("BOUAT");
		BOPRD = prop.getProperty("BOPRD");
		userName = prop.getProperty("userName");
		passwordD = prop.getProperty("passwordD");
		passwordQ = prop.getProperty("passwordQ");
		passwordP = prop.getProperty("passwordP");
		saveAsStoreID = prop.getProperty("saveAsStore");
		standardReportsID = prop.getProperty("standardReports");
		CMSDEV = prop.getProperty("CMSDEV");
		CMSUAT = prop.getProperty("CMSUAT");
		CMSPRD = prop.getProperty("CMSPRD");
		portCMS = prop.getProperty("portCMS");
		rootBusinessID = prop.getProperty("rootBusiness");
	}
	
	//Read the property file
	private InputStream getPropFile() throws FileNotFoundException {
		return propFile = new FileInputStream("resources/config.properties");
	}
	
	//-----------------------------------------------------
	//******************----SET----************************
	//-----------------------------------------------------
	
    public final String setEnvironment (String env) throws IllegalArgumentException { 
        if (env.equals("DEV") || env.equals("UAT") || env.equals("PRD"))
            return env;
            throw new IllegalArgumentException("Environment argument " + env + " is incorrect");
    }
	
	//-----------------------------------------------------
	//******************----GET----************************
	//-----------------------------------------------------
    public String getAuthentication() {
    	return this.authentication;
    }
    public String getEnvironment() {
    	return this.environment;
    }    
	public String getUserName() {
		return this.userName;
	}
	public String getSaveAsStoreID() {
		return this.saveAsStoreID;
	}
	public String getStandardRootFolderID() {
		return this.standardReportsID;
	}
	public String getRootBusinessID() {
		return this.rootBusinessID;
	}
	
	//Passwords
	public String getPasswordD() {
		return this.passwordD;
	}
	public String getPasswordQ() {
		return this.passwordQ;
	}
	public String getPasswordP() {
		return this.passwordP;
	}
	
	//URL's
	
	public String getBODEV() {
		return this.BODEV;
	}
	public String getBOUAT() {
		return this.BOUAT;
	}
	public String getBOPRD() {
		return this.BOPRD;
	}
	
	//SYSINFO
	
	public String getCMSDEV() {
		return this.CMSDEV;
	}
	public String getCMSUAT() {
		return this.CMSUAT;
	}
	public String getCMSPRD() {
		return this.CMSPRD;
	}
	public String getPortCMS() {
		return this.portCMS;
	}	
}
