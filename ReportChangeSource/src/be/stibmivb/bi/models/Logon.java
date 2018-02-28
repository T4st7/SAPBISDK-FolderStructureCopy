package be.stibmivb.bi.models;

import java.io.IOException;
import java.net.URL;

import org.json.JSONObject;

import com.crystaldecisions.sdk.exception.SDKException;
import com.crystaldecisions.sdk.framework.CrystalEnterprise;
import com.crystaldecisions.sdk.framework.IEnterpriseSession;
import com.crystaldecisions.sdk.occa.security.ILogonTokenMgr;

import be.stibmivb.bi.http.Connection;
import be.stibmivb.bi.utilities.LoadProperties;

public class Logon{
	
	private static final String LOGONURN = "/logon/long";
	private URL logonURL;
	private URL baseURL;
	private String userName;
	private String password;
	private String authentication;
	private String CMS;
	private String portCMS;
	private JSONObject logonJSON;
	private JSONObject tokenJSON;
	
	//-----------------------------------------------------
	//**************----CONSTRUCTOR----********************
	//-----------------------------------------------------
	
	public Logon() throws IOException {}
	
	//Constructor for specific environment
	public Logon(LoadProperties props) throws IOException {
		if (props.getEnvironment() == "DEV") {
			password = props.getPasswordD();
			CMS = props.getCMSDEV();
			baseURL = new URL(props.getBODEV());
		} else if (props.getEnvironment() == "UAT") {
			password = props.getPasswordQ();
			CMS = props.getCMSUAT();
			baseURL = new URL(props.getBOUAT());
		} else if (props.getEnvironment() == "PRD") {
			password = props.getPasswordP();
			CMS = props.getCMSPRD();
			System.out.println(props.getBOPRD().toString());
			baseURL = new URL(props.getBOPRD());
		}
		
		//User Credentials
		portCMS = props.getPortCMS();
		userName = props.getUserName();
		authentication = props.getAuthentication();
		logonURL = new URL(baseURL + LOGONURN); 
		System.out.println(logonURL.toString());
				
		//All methods to retrieve logonToken
		
		bodyLogonJSON();
		authLogonJson();
		createLogonToken();
	}
	
	//-----------------------------------------------------
	//******************----GET----************************
	//-----------------------------------------------------
	
	public URL getBaseURL() {
		return baseURL;
	}	
	//VARIABLES
	public JSONObject getLogonJSON() {
		return logonJSON;
	}
	public JSONObject getTokenJSON() {
		return tokenJSON;
	}
	
	public String getUserName() {
		return userName;
	}
	
	public String getAuthentication() {
		return authentication;
	}
	
	public String getPassword() {
		return password;
	}
	
	public URL getLogonURI() {
		return logonURL;
	}
	
	public String getCMS() {
		return CMS;
	}
	public String getPortCMS() {
		return portCMS;
	}
	
	
	//-----------------------------------------------------
	//**************----HTTP METHODS----*******************
	//-----------------------------------------------------
	
	//GET LogonJSON
	private void bodyLogonJSON() throws IOException {
		String[][] requestProps = {{"Accept","application/json"}};
		String requestMethod = "GET";
		Connection conn = new Connection();
		conn.openConn(logonURL);
		conn.buildRequest(requestProps, requestMethod);
		conn.testResponse();
		logonJSON = new JSONObject(conn.getResponse().toString());
		conn.closeConn();
	}
	
	//GET LogonToken
	private void createLogonToken() throws IOException {
		String[][] requestProps = {{"Accept","application/json"},{"Content-Type","application/json"}};
		String requestMethod = "POST";
		Connection conn = new Connection();
		conn.openConn(logonURL);
		conn.buildRequest(requestProps, requestMethod, logonJSON);
		conn.testResponse();
		tokenJSON = new JSONObject(conn.getResponse().toString());
		conn.closeConn();
	}
	
	//-----------------------------------------------------
	//***************----JSON MANIP----********************
	//-----------------------------------------------------
	
	private void authLogonJson() throws IOException {
		logonJSON.put("userName", userName);
		logonJSON.put("password", password);
	}
	
	//-----------------------------------------------------
	//*****************----JAVA SDK----********************
	//-----------------------------------------------------
	
	public IEnterpriseSession getEnterpriseSession() throws SDKException {
		  IEnterpriseSession enterpriseSession = CrystalEnterprise.getSessionMgr().logon(getUserName(), getPassword(),
				  getCMS()+":"+getPortCMS(), getAuthentication());
		  ILogonTokenMgr tokenMgr = enterpriseSession.getLogonTokenMgr();
		  String defaultLogonToken = tokenMgr.getDefaultToken();
		  IEnterpriseSession enterpriseSessionToken = CrystalEnterprise.getSessionMgr().logonWithToken(defaultLogonToken);
		  enterpriseSession.logoff();
		return enterpriseSessionToken;
	}
	
}
