package io.pivotal.pde.gemfire.dynamic.security;

import java.util.ArrayList;
import java.util.Collection;

import org.apache.geode.cache.CacheFactory;
import org.apache.geode.cache.CacheTransactionManager;
import org.apache.geode.cache.CommitConflictException;
import org.apache.geode.cache.Region;
import org.apache.geode.cache.execute.Function;
import org.apache.geode.cache.execute.FunctionContext;
import org.apache.geode.security.ResourcePermission;

import io.pivotal.pde.gemfire.dynamic.security.User.Level;

public class RemoveUserFunction extends AdminFunction implements Function {

	private ArrayList<ResourcePermission> requiredPermissions;
	
	public RemoveUserFunction(){
		requiredPermissions = new ArrayList<ResourcePermission>(2);
		requiredPermissions.add(new ResourcePermission(ResourcePermission.Resource.DATA,ResourcePermission.Operation.WRITE,DynamicSecurityManager.USERS_REGION));
		requiredPermissions.add(new ResourcePermission(ResourcePermission.Resource.DATA,ResourcePermission.Operation.READ,DynamicSecurityManager.USERS_REGION));
	}
	
	@Override
	public String getId() {
		return "remove_user";
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
		Object argsObj = ctx.getArguments();
		if (argsObj == null){
			sendError(ctx,"Please provide username argument");
			return; //RETURN
		}
		
		String []args = (String [])argsObj;
		if (args.length != 1){
			sendError(ctx,"Incorrect arguments were provided. Please provide username argument.");
			return; //RETURN
		}
		
		if (args[0].equals("gfadmin") || args[0].equals("gfpeer")){
			sendError(ctx,"The \'gfadmin\' and \'gfpeer\' users cannot be removed.");
			return; //RETURN
		}
				
		Region <String,User> userRegion = CacheFactory.getAnyInstance().getRegion(DynamicSecurityManager.USERS_REGION);
		userRegion.remove(args[0]);
		
		ctx.getResultSender().lastResult(String.format("All access for  \"%s\" has been removed.", args[0]));
	}

}
