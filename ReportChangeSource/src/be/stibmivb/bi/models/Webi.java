package be.stibmivb.bi.models;

import java.io.IOException;
import java.net.URL;
import java.util.List;
import java.util.stream.Collectors;

import org.json.JSONException;
import org.json.JSONObject;

import be.stibmivb.bi.http.Connection;

public class Webi extends ObjectBI {
	
	//-----------------------------------------------------
	//***************----VARIABLES----*********************
	//-----------------------------------------------------
	
	private static final String WEBIURN = "/v1/documents/";
	private static final String WEBICOPYURN = "/raylight/v1/documents?sourceId=";
	private static final JSONObject WEBITEMPLATE = 
			new JSONObject("{\"document\":{\"name\":\"\",\"folderId\":\"\"}}");
	private URL baseURL;
	private JSONObject token;
	private JSONObject webi;
	private URL webiURL;
	
	//-----------------------------------------------------
	//**************----CONSTRUCTORS----*******************
	//-----------------------------------------------------
	
	public Webi() {};
	
	public Webi(URL baseURL, String webiID, JSONObject logonToken) throws IOException {
		this.baseURL = baseURL;
		this.webiURL = new URL(baseURL+WEBIURN+webiID);
		this.token = logonToken;
		getWebiInfo();
		this.id = webi.getInt("id");
		this.cuid = webi.getString("cuid");
		this.name = webi.getString("name");
		this.type = webi.getString("type");
		this.createdOn = webi.getString("created");
		this.changedOn = webi.getString("updated");
		this.ownerID = webi.getInt("ownerid");
		this.parentCuid = webi.getString("parentcuid");
		this.parentID = webi.getInt("parentid");
	}
	
	//-----------------------------------------------------
	//**************----HTTP METHODS----*******************
	//-----------------------------------------------------
	
	private JSONObject getWebiInfo() throws IOException {
		String[][] requestProps = {{"Accept","application/json"},
				{"Content-Type","application/json"},
				{"X-SAP-LogonToken", (String) token.get("logonToken")}};
		String requestMethod = "GET";
		Connection conn = new Connection();
		conn.openConn(webiURL);
		conn.buildRequest(requestProps, requestMethod);
		conn.testResponse();
		webi = new JSONObject(conn.getResponse().toString());
		conn.closeConn();
		return webi;
	}
	
	public JSONObject createWebi(Folder targetParent) throws IOException {
		String[][] requestProps = {{"Accept","application/json"},
				{"Content-Type","application/json"},
				{"X-SAP-LogonToken", (String) token.get("logonToken")}};
		String requestMethod = "POST";
		URL copyWebiURN = new URL(baseURL+WEBICOPYURN+getID());
		JSONObject webiTemplate = webiJSON(WEBITEMPLATE, targetParent);
		Connection conn = new Connection();
		conn.openConn(copyWebiURN);
		conn.buildRequest(requestProps, requestMethod, webiTemplate);
		conn.testResponse();
		JSONObject newWebi =  new JSONObject(conn.getResponse().toString());
		conn.closeConn();
		return newWebi;	
	}
	
	public JSONObject deleteWebi() throws IOException {
		String[][] requestProps = {{"Accept","application/json"},
				{"Content-Type","application/json"},
				{"X-SAP-LogonToken", (String) token.get("logonToken")}};
		String requestMethod = "DELETE";
		Connection conn = new Connection();
		conn.openConn(webiURL);
		conn.buildRequest(requestProps, requestMethod);
		conn.testResponse();
		JSONObject newWebi =  new JSONObject(conn.getResponse().toString());
		conn.closeConn();
		return newWebi;	
	}
	
	//-----------------------------------------------------
	//*****************----STREAM----**********************
	//-----------------------------------------------------
	public static List<Webi> getWebiObjects(List<ObjectBI> objects) throws JSONException, IOException {			
		List<Webi> webis = objects.stream()
		.filter(w -> w instanceof Webi)
		.map(w -> (Webi) w)
		.collect(Collectors.toList());
		return webis;
	}
	
	//-----------------------------------------------------
	//*****************----GETTER----**********************
	//-----------------------------------------------------
	
	public JSONObject getWebiObject(){
		return this.webi;
	}
	
	//-----------------------------------------------------
	//*****************----PRINTER----*********************
	//-----------------------------------------------------

	public void printWebi(Webi webi) {
        System.out.println(webi.getID() + " " + webi.getType());
	}
	
	//-----------------------------------------------------
	//***************----JSON MANIP----********************
	//-----------------------------------------------------
	
	private JSONObject webiJSON(JSONObject webiJSON, Folder targetParent) throws IOException {
		JSONObject webi = new JSONObject();
		webi.put("name", getName());
		webi.put("folderId", targetParent.getID());
		webiJSON.put("document", webi);
		return webiJSON;
	}
}
