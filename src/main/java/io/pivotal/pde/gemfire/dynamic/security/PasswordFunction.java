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

public class PasswordFunction extends AdminFunction implements Function {

	private ArrayList<ResourcePermission> requiredPermissions;
	
	public PasswordFunction(){
		requiredPermissions = new ArrayList<ResourcePermission>(2);
		requiredPermissions.add(new ResourcePermission(ResourcePermission.Resource.DATA,ResourcePermission.Operation.WRITE,DynamicSecurityManager.USERS_REGION));
		requiredPermissions.add(new ResourcePermission(ResourcePermission.Resource.DATA,ResourcePermission.Operation.READ,DynamicSecurityManager.USERS_REGION));
	}
	
	@Override
	public String getId() {
		return "passwd";
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
		if (argsObj == null) {
			sendError(ctx,"Please provide username and password arguments" );
			return; //RETURN
		}
		
		String []args = (String [])argsObj;
		if (args.length !=2 ){
			sendError(ctx, "Incorrect arguments were provided.  Please provide 2 arguments: username, password");
			return; //RETURN
		}

		
		if (args[0].equals("gfadmin") || args[0].equals("gfpeer")){
			sendError(ctx,"The password for \'gfadmin\' and \'gfpeer\' cannot be set dynamically.");
			return; //RETURN
		}
		
		Region <String,User> userRegion = CacheFactory.getAnyInstance().getRegion(DynamicSecurityManager.USERS_REGION);
		// this protects against the extremely unlikely situation that the same users password 
		// and permissions are being updated simultaneously - without this there is a small possiblity
		// of a lost update 
		CacheTransactionManager tm = CacheFactory.getAnyInstance().getCacheTransactionManager();
		tm.begin();
		try {
			User u = userRegion.get(args[0]);
			if (u == null) {
				u = new User();
				u.setLevel(User.Level.MONITOR);
			}
			
			u.setPassword(args[1]);
			userRegion.put(args[0], u);
			tm.commit();
		} catch(CommitConflictException x){
			sendError(ctx,"Operation failed due to concurrent update by another user.  Please try again.");
			return;  //RETURN
		} finally {
			if (tm.exists()) tm.rollback();
		}
		
		ctx.getResultSender().lastResult(String.format("The password for %s was set.", args[0]));
	}

}
