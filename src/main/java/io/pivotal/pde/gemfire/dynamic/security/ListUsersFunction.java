package io.pivotal.pde.gemfire.dynamic.security;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Map.Entry;
import java.util.TreeMap;

import org.apache.geode.cache.CacheFactory;
import org.apache.geode.cache.Region;
import org.apache.geode.cache.execute.Function;
import org.apache.geode.cache.execute.FunctionContext;
import org.apache.geode.security.ResourcePermission;

public class ListUsersFunction extends AdminFunction implements Function {

	private ArrayList<ResourcePermission> requiredPermissions;
	
	public ListUsersFunction(){
		requiredPermissions = new ArrayList<ResourcePermission>(2);
		requiredPermissions.add(new ResourcePermission(ResourcePermission.Resource.DATA,ResourcePermission.Operation.WRITE,DynamicSecurityManager.USERS_REGION));
		requiredPermissions.add(new ResourcePermission(ResourcePermission.Resource.DATA,ResourcePermission.Operation.READ,DynamicSecurityManager.USERS_REGION));
	}
	
	@Override
	public String getId() {
		return "list_users";
	}

	@Override
	public Collection getRequiredPermissions(String regionName) {
		return requiredPermissions;
	}

	@Override
	public boolean hasResult() {
		return true;
	}

	@Override
	public boolean isHA() {
		return true; 
		// all functions are idempotent
	}

	@Override
	public boolean optimizeForWrite() {
		return true;
	}

	@Override
	public void execute(FunctionContext ctx) {
		Region <String,User> userRegion = CacheFactory.getAnyInstance().getRegion(DynamicSecurityManager.USERS_REGION);
		
		// doing this just so results will be sorted
		TreeMap<String,String> resultList = new TreeMap<String,String>();
		for(Entry<String,User> entry: userRegion.entrySet()){
			resultList.put(entry.getKey(), entry.getValue().getLevel().toString());
		}
		resultList.put("gfadmin", User.Level.SECADMIN.toString());
		resultList.put("gfpeer", User.Level.PEER.toString());
		
		for(Entry<String,String> entry: resultList.entrySet()){
			ctx.getResultSender().sendResult(String.format("%20s  %s", entry.getKey(),entry.getValue()));
		}
		ctx.getResultSender().lastResult(String.format("Total Users: %d",resultList.size()));
	}

}
