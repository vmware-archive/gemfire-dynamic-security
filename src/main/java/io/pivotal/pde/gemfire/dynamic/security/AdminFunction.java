package io.pivotal.pde.gemfire.dynamic.security;

import java.util.ArrayList;
import java.util.Collection;

import org.apache.geode.cache.execute.Function;
import org.apache.geode.cache.execute.FunctionContext;
import org.apache.geode.security.ResourcePermission;

public abstract class AdminFunction implements Function {
	
	private ArrayList<ResourcePermission> requiredPermissions;

	public AdminFunction() {
		super();
		requiredPermissions = new ArrayList<ResourcePermission>(2);
		requiredPermissions.add(new ResourcePermission(ResourcePermission.Resource.DATA,ResourcePermission.Operation.WRITE,DynamicSecurityManager.USERS_REGION));
		requiredPermissions.add(new ResourcePermission(ResourcePermission.Resource.DATA,ResourcePermission.Operation.READ,DynamicSecurityManager.USERS_REGION));
	}
	
	@Override
	public Collection getRequiredPermissions(String regionName) {
		return requiredPermissions;
	}


	protected void sendError(FunctionContext ctx, String msg) {
		ctx.getResultSender().lastResult("Error: " + msg);
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

}