package be.stibmivb.bi.entryPoint;


import java.io.IOException;
import java.net.URL;
import java.util.*;
import java.util.stream.Collectors;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.businessobjects.sdk.plugin.desktop.customrole.ICustomRole;
import com.crystaldecisions.sdk.exception.SDKException;
import com.crystaldecisions.sdk.framework.IEnterpriseSession;
import com.crystaldecisions.sdk.occa.infostore.IExplicitPrincipal;
import com.crystaldecisions.sdk.occa.infostore.IExplicitPrincipals;
import com.crystaldecisions.sdk.occa.infostore.IExplicitRole;
import com.crystaldecisions.sdk.occa.infostore.IExplicitRoles;
import com.crystaldecisions.sdk.occa.infostore.IInfoObject;
import com.crystaldecisions.sdk.occa.infostore.IInfoObjects;
import com.crystaldecisions.sdk.occa.infostore.IInfoStore;
import com.crystaldecisions.sdk.occa.infostore.ISecurityInfo2;
import com.crystaldecisions.sdk.occa.infostore.ISecurityLimitAdmin;
import com.crystaldecisions.sdk.occa.infostore.ISecurityRightAdmin;
import com.crystaldecisions.sdk.occa.infostore.ISecurityRoleAdmin;
import com.crystaldecisions.sdk.occa.security.ISecCacheControllerAdmin;
import com.crystaldecisions.sdk.occa.security.ISecRightsAdmin;
import com.crystaldecisions.sdk.occa.security.ISecurityInfoMgr;
import com.crystaldecisions.sdk.occa.security.ISecurityInfoResult;
import com.crystaldecisions.sdk.occa.security.ISecurityResult;
import com.crystaldecisions.sdk.plugin.desktop.folder.IFolder;
import com.sap.ip.bi.zen.boe.client.EnterpriseSessionHelper;

import be.stibmivb.bi.models.AccessLevel;
import be.stibmivb.bi.models.Folder;
import be.stibmivb.bi.models.Logon;
import be.stibmivb.bi.models.ObjectBI;
import be.stibmivb.bi.models.UserGroup;
import be.stibmivb.bi.models.Webi;
import be.stibmivb.bi.utilities.LoadProperties;


public class Main {

	public static void main(String[] args) throws IOException, SDKException {
		// TODO Auto-generated method stub	
		
		//INCOMING ARGUMENTS
		String environment = "PRD";
		String copyFrom = "AeCHf_kVciJEiFiiwfj5PqI";
		String copyTo = "ASwAcbr5S71PlRHpS3PhPow";
		LoadProperties props;
		
		props = new LoadProperties(environment);
		//Get Logon token
		Logon log = new Logon(props);	
			
		/* Delete all objects in saveAsStore
		 * ---------------------------------
		 * a. Create a folder intance of saveAsStore
		 * b. Get all children of saveAsStore in an objectList
		 * c. Delete all objects in the objectList
		 */
		
		Folder copyToFolder = new Folder(log.getBaseURL(), copyTo,log.getTokenJSON());
		List<ObjectBI> copyToObjects = new ArrayList<ObjectBI>();
		copyToObjects = copyToFolder.determineObjectType(copyToFolder.getChildrenInFolder(), log.getBaseURL(),
				log.getTokenJSON());	
		List<Folder> copyToChildFolders = new ArrayList<Folder>();
		copyToChildFolders = Folder.streamFolderObjects(copyToObjects);
		copyToObjects.forEach(o -> {
			if (o instanceof Folder){
				Folder delFolder = (Folder) o;
			try {
				delFolder.deleteFolder();
				} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				}
			} else if (o instanceof Webi) {
				Webi delWebi = (Webi) o;
				try {
					delWebi.deleteWebi();
					} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
					}
			}	
		});
		/**************************************************************************************************/
		
		/* Recreate all Objects in saveAsStore
		 * a. Create instance of root source folder
		 * b. Get all children of root source folder
		 * c. Synchronise source structure with a destination of your choice
		 * d. Add security principals to the newly created folders
		 */
		
		Folder copyFromFolder = new Folder(log.getBaseURL(),copyFrom, log.getTokenJSON());

		copyToObjects.clear();
		copyToObjects.add(copyToFolder);
	
		//Create BusinessUserGroupHierarchy
		UserGroup busRootGroup = new UserGroup(log.getBaseURL(),props.getRootBusinessID(),log.getTokenJSON());
		List<UserGroup> busUserGroups = new ArrayList<UserGroup>();
		busUserGroups.add(busRootGroup);
		busUserGroups = busRootGroup.getUserGroupDescendants(busRootGroup, busUserGroups);
		
		//Get all AccessLevels
		IInfoObjects accessLevels = AccessLevel.getAccessLevelAll(log);
		ICustomRole viewOnlyRole = null;
		ICustomRole builderRole = null;
		for(int i = 0; i <= accessLevels.size()-1; i++) {
			ICustomRole customRole = (ICustomRole) accessLevels.get(i);
			if (customRole.getTitle().equals("CAL-View_Only")) {
				viewOnlyRole = customRole;
			} else if (customRole.getTitle().equals("CAL-Reporter")) {
				builderRole = customRole;
			}}
			;
		
		List<ObjectBI> sourceObjects = new ArrayList<ObjectBI>();
		List<ObjectBI> targetObjects = new ArrayList<ObjectBI>();
	
		List<ObjectBI> copyFromObjects = new ArrayList<ObjectBI>();
		copyFromObjects.add(copyFromFolder);	
		
		copyFromObjects = copyFromFolder.getFolderDescendants(copyFromFolder, copyFromObjects);

			
		IInfoStore infoStore = (IInfoStore) log.getEnterpriseSession().getService("InfoStore");
		ISecurityInfoMgr secInfoMgr = log.getEnterpriseSession().getSecurityInfoMgr();
		ISecCacheControllerAdmin secCacheControllerAdmin = secInfoMgr.getSecCacheAdmin();
		secCacheControllerAdmin.batch();
		try {
			copyFromObjects.forEach(o -> {
									if (o instanceof Folder){
										try {
											secCacheControllerAdmin.cacheSecurityInfo(o.getID());
										} catch (SDKException e) {
											// TODO Auto-generated catch block
											e.printStackTrace();
										}
										
									}
									});
			secCacheControllerAdmin.commit();
		}
		catch (Throwable e) {
			secCacheControllerAdmin.rollback();
			
		}
		
		
		ISecRightsAdmin secRightsAdmin = secInfoMgr.getRightsAdmin();

			
		copyToObjects = copyFolderStructure(copyFromObjects,copyToObjects, copyFromFolder, 
				copyToFolder, log.getBaseURL(), log.getTokenJSON(),secRightsAdmin, viewOnlyRole,builderRole, infoStore, busUserGroups); 
		
	}
	
	private static List<ObjectBI> copyFolderStructure(List<ObjectBI> copyFromObjects, List<ObjectBI> copyToObjects,
			Folder copyFromParent, Folder copyToParent, URL baseURL, JSONObject logonToken, ISecRightsAdmin secRightsAdmin, ICustomRole viewOnlyRole,  ICustomRole builderRole, IInfoStore infoStore,
			List<UserGroup> busUserGroups) throws IOException{
			
		List<ObjectBI> toCopy = new ArrayList<ObjectBI>();
			
			toCopy = copyFromObjects.stream()
					.filter(o -> o.getParentID().equals(copyFromParent.getID()))
					.collect(Collectors.toList());
				
			toCopy.forEach(o -> {
									if (o instanceof Folder){
										Folder copyFolder = (Folder) o;
										JSONObject newFolder = null;
										Folder copiedFolder = null;
										ISecurityResult securityResult;
										System.out.println("this is a folder");

										try {
																	
											newFolder = copyFolder.createFolder(copyFolder.getFolderTemplate(), copyToParent);										
											copiedFolder = new Folder(baseURL, newFolder.getString("cuid"), logonToken);
											
											IInfoObjects copiedFolders = infoStore.query("Select SI_ID from CI_INFOOBJECTS where SI_ID="+copiedFolder.getID());
											IInfoObject copiedFolderIO = (IInfoObject)copiedFolders.get(0);
											securityResult = secRightsAdmin.getSecurityInfo(copyFolder.getID());
											ISecurityInfoResult securityInfoResult = (ISecurityInfoResult)securityResult.getResult();
											ISecurityRoleAdmin[] securityRoleAdminArray = securityInfoResult.getRoles();
											

											for (ISecurityRoleAdmin securityRoleAdmin : securityRoleAdminArray)
											{
												if (securityRoleAdmin.isInherited() == false) {
													//System.out.println(securityRoleAdmin.getPrincipalName());
													ISecurityInfo2 securityInfo = copiedFolderIO.getSecurityInfo2();
													IExplicitPrincipals explicitPrincipals = securityInfo.getExplicitPrincipals();
													if(securityRoleAdmin.getPrincipalName().substring(0,3).equals("BIS")) {
														System.out.println("DING");
														String testUG = securityRoleAdmin.getPrincipalName().substring(4, 10);
														for (UserGroup busUser : busUserGroups) {
															if (busUser.getName().equals("BIB_"+testUG+"_BUILD")) {
																System.out.println("DONG");
																IExplicitPrincipal explicitPrincipal = explicitPrincipals.add(busUser.getID());
																IExplicitRoles folderRoles = explicitPrincipal.getRoles();
																IExplicitRole folderRole = folderRoles.add(builderRole.getID());
																infoStore.commit(copiedFolders);
																break;
															}
														}
													} 	else if (securityRoleAdmin.getID() == viewOnlyRole.getID()) {
														IExplicitPrincipal explicitPrincipal = explicitPrincipals.add(securityRoleAdmin.getPrincipalID());
														IExplicitRoles folderRoles = explicitPrincipal.getRoles();
														IExplicitRole folderRole = folderRoles.add(viewOnlyRole.getID());
														infoStore.commit(copiedFolders);
													}
												}
										    }
											
											copyFolderStructure(copyFromObjects, copyToObjects, copyFolder, copiedFolder, baseURL, logonToken, secRightsAdmin,
													viewOnlyRole,builderRole, infoStore, busUserGroups);
											copyToObjects.add(copiedFolder);
										} catch (JSONException | IOException e) {
											// TODO Auto-generated catch block
											e.printStackTrace();
										} catch (SDKException e) {
											// TODO Auto-generated catch block
											e.printStackTrace();
										}
							
									} else if (o instanceof Webi) {
										Webi copyWebi = (Webi) o;
										JSONObject newWebi = null;
										Webi copiedWebi = null;						

										try {
											newWebi = copyWebi.createWebi(copyToParent);
											copiedWebi = new Webi(baseURL, newWebi.getJSONObject("success").getString("id"), logonToken);
											copyToObjects.add(copiedWebi);
										} catch (JSONException | IOException e) {
											// TODO Auto-generated catch block
											e.printStackTrace();
										}
									}
								});
			return copyToObjects;
	}
}
