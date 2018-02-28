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

public class UserGroup extends ObjectBI {
	
	//-----------------------------------------------------
	//***************----VARIABLES----*********************
	//-----------------------------------------------------
	
	private static final String UGURN = "/v1/usergroups/";
	private static final String USERURN = "/users";
	private static final String UGINUGURN = "/usergroups";
	private URL baseURL;
	private JSONObject token;
	private JSONObject userGroup;
	private JSONObject usersInGroup;
	private JSONObject childrenInGroup;
	private URL userGroupURL;
	private JSONArray groupUsers;
	private JSONArray userGroupChildren;
	
	//-----------------------------------------------------
	//**************----CONSTRUCTORS----*******************
	//-----------------------------------------------------
	
	public UserGroup() {};
	
	public UserGroup(URL baseURL, String userGroupID, JSONObject logonToken) throws IOException {
		this.baseURL = baseURL;
		this.userGroupURL = new URL(baseURL+UGURN+userGroupID);
		this.token = logonToken;
		getUserGroupInfo();
		this.groupUsers = getUsersInUserGroup().getJSONArray("entries");
		this.userGroupChildren = getUserGroupChildInfo().getJSONArray("entries");
		this.id = userGroup.getInt("id");
		this.cuid = userGroup.getString("cuid");
		this.name = userGroup.getString("name");
		this.createdOn = userGroup.getString("created");
		this.parentCuid = userGroup.getString("parentcuid");
		this.parentID = userGroup.getInt("parentid");
	}
	
	//-----------------------------------------------------
	//**************----HTTP METHODS----*******************
	//-----------------------------------------------------
	
	private JSONObject getUserGroupInfo() throws IOException {
		String[][] requestProps = {{"Accept","application/json"},
				{"Content-Type","application/json"},
				{"X-SAP-LogonToken", (String) token.get("logonToken")}};
		String requestMethod = "GET";
		Connection conn = new Connection();
		conn.openConn(userGroupURL);
		conn.buildRequest(requestProps, requestMethod);
		conn.testResponse();
		userGroup = new JSONObject(conn.getResponse().toString());
		conn.closeConn();
		return userGroup;
	}
	
	private JSONObject getUserGroupChildInfo() throws IOException {
		String[][] requestProps = {{"Accept","application/json"},
				{"Content-Type","application/json"},
				{"X-SAP-LogonToken", (String) token.get("logonToken")}};
		String requestMethod = "GET";
		URL childURL = new URL(userGroupURL+UGINUGURN);
		Connection conn = new Connection();
		conn.openConn(childURL);
		conn.buildRequest(requestProps, requestMethod);
		conn.testResponse();
		childrenInGroup = new JSONObject(conn.getResponse().toString());
		conn.closeConn();
		return childrenInGroup;
	}
	
	private JSONObject getUsersInUserGroup() throws IOException {
		String[][] requestProps = {{"Accept","application/json"},
				{"Content-Type","application/json"},
				{"X-SAP-LogonToken", (String) token.get("logonToken")}};
		String requestMethod = "GET";
		URL usersInGroupURL = new URL(userGroupURL+USERURN);
		Connection conn = new Connection();
		conn.openConn(usersInGroupURL);
		conn.buildRequest(requestProps, requestMethod);
		conn.testResponse();
		usersInGroup = new JSONObject(conn.getResponse().toString());
		conn.closeConn();
		return usersInGroup;
	}
	
	//-----------------------------------------------------
	//*****************----STREAM----**********************
	//-----------------------------------------------------
	public static List<UserGroup> getUserGroupObjects(List<ObjectBI> objects) throws JSONException, IOException {			
		List<UserGroup> userGroup = objects.stream()
		.filter(ug -> ug instanceof UserGroup)
		.map(w -> (UserGroup) w)
		.collect(Collectors.toList());
		return userGroup;
	}
	
	//-----------------------------------------------------
	//*****************----GETTER----**********************
	//-----------------------------------------------------
	
	public JSONObject getUserGroupObject(){
		return this.userGroup;
	}
	
	public JSONArray getUsersInGroup() {
		return groupUsers;
	}
	
	public JSONArray getChildrenInGroup() {
		return userGroupChildren;
	}
	
	public JSONObject getToken() {
		return token;
	}
	
	public URL getBaseURL() {
		return baseURL;
	}
	
	public List<UserGroup> createChildrenList(JSONArray userGroupChildren, UserGroup ug) throws JSONException, IOException {
		List<UserGroup> childrenList = new ArrayList<UserGroup>();
		for(int i = 0; i <= userGroupChildren.length()-1; i++) {
		JSONObject child = (JSONObject) userGroupChildren.getJSONObject(i);
		childrenList.add(
				new UserGroup(ug.getBaseURL(),(String)child.get("id"),ug.getToken()));
		}
		return childrenList;
	}
	
	//-----------------------------------------------------
	//*****************----PRINTER----*********************
	//-----------------------------------------------------

	
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
	
	public List <UserGroup> getUserGroupDescendants (UserGroup ug, List<UserGroup> descendants) throws JSONException, IOException {
		for(UserGroup child : ug.createChildrenList(ug.getChildrenInGroup(), ug)) {
			descendants.add(child);
			getUserGroupDescendants(child, descendants);
		}		
		return descendants;
	}
}
