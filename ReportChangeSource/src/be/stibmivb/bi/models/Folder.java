package be.stibmivb.bi.models;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;


import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import be.stibmivb.bi.http.Connection;

public class Folder extends ObjectBI {
	
	//-----------------------------------------------------
	//***************----VARIABLES----*********************
	//-----------------------------------------------------
	private static final String FOLDERURN = "/v1/folders/";
	private static final String TEMPLATEURN = "/v1/folders/folder";
	private URL baseURL;
	private URL folderURL;
	private JSONObject token;
	private JSONObject folder;
	private JSONObject children;
	private JSONArray folderChildren;
	
	
	//-----------------------------------------------------
	//**************----CONSTRUCTORS----*******************
	//-----------------------------------------------------
	public Folder() {};
	
	public Folder(URL baseURL, String folderID, JSONObject logonToken) throws IOException {
		this.baseURL = baseURL;
		this.folderURL = new URL(baseURL+FOLDERURN+folderID);
		this.token = logonToken;
		getFolderInfo();
		this.folderChildren = getFolderChildInfo().getJSONArray("entries");
		this.id = folder.getInt("id");
		this.cuid = folder.getString("cuid");
		this.name = folder.getString("name");
		this.type = folder.getString("type");
		this.createdOn = folder.getString("created");
		this.changedOn = folder.getString("updated");
		this.ownerID = folder.getInt("ownerid");
		this.parentCuid = folder.getString("parentcuid");
		this.parentID = folder.getInt("parentid");
	}
	
	//-----------------------------------------------------
	//**************----HTTP METHODS----*******************
	//-----------------------------------------------------
	
	//Get folderTemplate
	public JSONObject getFolderTemplate() throws IOException {
		String[][] requestProps = {{"Accept","application/json"},
				{"Content-Type","application/json"},
				{"X-SAP-LogonToken", (String) token.get("logonToken")}};
		String requestMethod = "GET";
		URL templateURI = new URL(this.baseURL+TEMPLATEURN);
		JSONObject folderTemplate;
		Connection conn = new Connection();
		conn.openConn(templateURI);
		conn.buildRequest(requestProps, requestMethod);
		conn.testResponse();
		folderTemplate = new JSONObject(conn.getResponse().toString());
		conn.closeConn();
		return folderTemplate;
	}
	
	//Get folderInfo
	private JSONObject getFolderInfo() throws IOException {
		String[][] requestProps = {{"Accept","application/json"},
				{"Content-Type","application/json"},
				{"X-SAP-LogonToken", (String) token.get("logonToken")}};
		String requestMethod = "GET";
		Connection conn = new Connection();
		conn.openConn(folderURL);
		conn.buildRequest(requestProps, requestMethod);
		conn.testResponse();
		this.folder = new JSONObject(conn.getResponse().toString());
		conn.closeConn();
		return this.folder;
	}
	//Get folderChildren
	private JSONObject getFolderChildInfo() throws IOException {
		String[][] requestProps = {{"Accept","application/json"},
				{"Content-Type","application/json"},
				{"X-SAP-LogonToken", (String) token.get("logonToken")}};
		String requestMethod = "GET";
		URL childURL = new URL(folderURL+"/children");
		Connection conn = new Connection();
		conn.openConn(childURL);
		conn.buildRequest(requestProps, requestMethod);
		conn.testResponse();
		this.children = new JSONObject(conn.getResponse().toString());
		conn.closeConn();
		return this.children;
	}
	//Delete folderObjects
	public void deleteFolder() throws IOException {
		String[][] requestProps = {{"Accept","application/json"},
				{"Content-Type","application/json"},
				{"X-SAP-LogonToken", (String) token.get("logonToken")}};
		String requestMethod = "DELETE";
		Connection conn = new Connection();
		conn.openConn(folderURL);
		conn.buildRequest(requestProps, requestMethod);
		conn.testResponse();	
		conn.closeConn();
	}
	public JSONObject createFolder(JSONObject folderTemplate, Folder targetParent) throws IOException {
		String[][] requestProps = {{"Accept","application/json"},
				{"Content-Type","application/json"},
				{"X-SAP-LogonToken", (String) token.get("logonToken")}};
		String requestMethod = "POST";
		URL templateURL = new URL(baseURL+TEMPLATEURN);
		folderTemplate = folderJSON(folderTemplate, targetParent);
		Connection conn = new Connection();
		conn.openConn(templateURL);
		conn.buildRequest(requestProps, requestMethod, folderTemplate);	
		conn.testResponse();
		JSONObject newFolder =  new JSONObject(conn.getResponse().toString());
		conn.closeConn();
		return newFolder;	
	}
	
	//-----------------------------------------------------
	//*****************----STREAM----**********************
	//-----------------------------------------------------
	
	public static List<Folder> streamFolderObjects(List<ObjectBI> objects) throws JSONException, IOException {			
		List<Folder> folders = objects.stream()
		.filter(f -> f instanceof Folder)
		.map(f -> (Folder) f)
		.collect(Collectors.toList());
		return folders;
	}
	
	//-----------------------------------------------------
	//*****************----GETTER----**********************
	//-----------------------------------------------------
	
	public JSONArray getChildrenInFolder() {
		return this.folderChildren;
	}
	
	public JSONObject getFolderChildrenObject() {
		return this.children;
	}
	
	public JSONObject getFolderObject(){
		return this.folder;
	}
	
	public void printFolderProperties(Folder folder) {
        System.out.println(folder.getID() + " " + folder.getName()+ " " + folder.getType());
	}
	
	public URL getBaseURL(){
		return this.baseURL;
	}
	
	public JSONObject getToken(){
		return this.token;
	}
	
	public List<ObjectBI> createChildrenList(JSONArray folderChildren, Folder folder) throws JSONException, IOException {
		List<ObjectBI> childrenList = new ArrayList<ObjectBI>();
		childrenList = folder.determineObjectType(folderChildren, folder.getBaseURL(), folder.getToken());
		return childrenList;
	}
	
	//-----------------------------------------------------
	//***************----JSON MANIP----********************
	//-----------------------------------------------------
	private JSONObject folderJSON(JSONObject folderJSON, Folder targetParent) throws IOException {
		folderJSON.put("name", getName());
		folderJSON.put("parentid", Integer.toString(targetParent.id));
		return folderJSON;
	}
	
	public List <ObjectBI> getFolderDescendants (Folder folder, List<ObjectBI> descendants) throws JSONException, IOException {
		
		for(ObjectBI child : folder.createChildrenList(folder.getChildrenInFolder(), folder)) {
			descendants.add(child);
			if (child instanceof Folder) {
				getFolderDescendants((Folder) child, descendants);
			}
		}		
		return descendants;
	}
}
