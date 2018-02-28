package be.stibmivb.bi.models;

import com.crystaldecisions.sdk.exception.SDKException;
import com.crystaldecisions.sdk.occa.infostore.IInfoObjects;
import com.crystaldecisions.sdk.occa.infostore.IInfoStore;

public class AccessLevel {
	
	public AccessLevel() {}
	
	public static IInfoObjects getAccessLevelAll(Logon log) throws SDKException {
		IInfoStore infostore = (IInfoStore) log.getEnterpriseSession().getService("InfoStore");
		IInfoObjects accessLevels = infostore.query("select * from CI_systemobjects where si_KIND = 'CustomRole'");	
		return accessLevels;	
	}
}
