package be.stibmivb.bi.models;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class ObjectBI {
	//VARIABLES
	protected JSONArray folderObjects;
	protected String cuid;
	protected Integer id;
	protected String name;
	protected String type;
	protected String createdOn;	
	protected String changedOn;
	protected Integer ownerID;
	protected String parentCuid;
	protected Integer parentID;
	
	//CONSTRUCT
	public ObjectBI() {};
	
	public ObjectBI(String type, Integer id) {
		this.type = type;
		this.id = id;				
	}
	
	//-----------------------------------------------------
	//***************----GET DETAILS----*******************
	//-----------------------------------------------------
	public String getCuid() {
		return this.cuid;
	}
	public Integer getID() {
		return this.id;
	}
	public String getName() {
		return this.name;
	}
	public String getType() {
		return this.type;
	}
	public String getCreatedOn() {
		return this.createdOn;
	}
	public String getChangedOn() {
		return this.changedOn;
	}
	public Integer getOwnerID() {
		return this.ownerID;
	}
	public String getParentCuid() {
		return this.parentCuid;
	}
	public Integer getParentID() {
		return this.parentID;
	}
	
	//-----------------------------------------------------
	//*****************----FILTERS----*********************
	//-----------------------------------------------------
	
	//-----------------------------------------------------
	//*****************----PARSER----**********************
	//-----------------------------------------------------
	
	
	public List<ObjectBI> determineObjectType(JSONArray obj, URL baseURL, JSONObject logonToken) throws JSONException, IOException {
		List<ObjectBI> objects = new ArrayList<ObjectBI>(); 
		for(int i = 0; i <= obj.length()-1; i++) {
			JSONObject child = (JSONObject) obj.getJSONObject(i);
			if (child.get("type").equals("Folder")) {
			objects.add(
					new Folder(baseURL,(String)child.get("id"),logonToken));
			} else if (child.get("type").equals("Webi")) {
			objects.add(
					new Webi(baseURL,(String)child.get("id"),logonToken));
			}
		}
		return objects;
	}
	
	public static List<ObjectBI> synchroniseObjects(List<ObjectBI> sourceObjects, List<ObjectBI> targetFolders,Folder parentFolder, URL baseURL, JSONObject logonToken, List<ObjectBI> targetObjects) throws IOException{
		List<ObjectBI> tO = new ArrayList<ObjectBI>();
		List<Folder> sourceFolders = new ArrayList<Folder>();
		
		Integer count = 0;
		for(int i = 0; i <= sourceObjects.size()-1; i++) {
			Object sourceObj = sourceObjects.get(i);
			if (sourceObj instanceof Folder) {
				count++;
				Folder targetFolder = (Folder) sourceObj;
				targetFolder.createFolder(targetFolder.getFolderTemplate(), parentFolder);
				sourceFolders.add((Folder) sourceObj);
			} else if (sourceObj instanceof Webi) {
				Webi targetWebi = (Webi) sourceObj;
				targetWebi.createWebi(parentFolder);
			}
		}
		Folder parentChildren = new Folder(baseURL,parentFolder.getCuid(), logonToken);
		tO = parentChildren.determineObjectType(parentChildren.getChildrenInFolder(), baseURL, logonToken);
		targetObjects.addAll(tO);
		
		if (count > 0) {
			for(int i = 0; i <= sourceFolders.size()-1; i++) {
			Object sourceF = sourceFolders.get(i);
				if (sourceF instanceof Folder) {
					Folder sourceFolder = (Folder)sourceF;
					for(int k = 0; k <= tO.size()-1; k++) {
						Object targetF = tO.get(k);
						if (targetF instanceof Folder) {
							Folder targetFolder = (Folder) targetF;
							if(sourceFolder.getName().equals(targetFolder.getName())) {
								sourceObjects = sourceFolder.determineObjectType(sourceFolder.getChildrenInFolder(), baseURL, logonToken);
								 ObjectBI.synchroniseObjects(sourceObjects,targetFolders,targetFolder, baseURL, logonToken, targetObjects);
							}
						}
					}
				}
			}
		} else {
			
			return targetObjects;
		}
		return targetObjects;
	}
}
