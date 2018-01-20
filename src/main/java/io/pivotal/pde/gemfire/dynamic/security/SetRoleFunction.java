/*
 * Copyright (c) 2018 Pivotal Software, Inc. All Rights Reserved.
 */
package io.pivotal.pde.gemfire.dynamic.security;

import org.apache.geode.cache.CacheFactory;
import org.apache.geode.cache.CacheTransactionManager;
import org.apache.geode.cache.CommitConflictException;
import org.apache.geode.cache.Region;
import org.apache.geode.cache.execute.FunctionContext;

public class SetRoleFunction extends AdminFunction {

	
	@Override
	public String getId() {	
		return "set_role";
	}

	@Override
	public void execute(FunctionContext ctx) {
		Object argsObj = ctx.getArguments();
		if (argsObj == null){
			sendError(ctx,"Please provide username and role arguments.");
			return; //RETURN
		}
		
		String []args = (String [])argsObj;
		if (args.length != 2){
			sendError(ctx,"Incorrect arguments were provided. Please provide two arguments: username, role");
			return; //RETURN
		}
		
		if (args[0].equals("gfadmin") || args[0].equals("gfpeer")){
			sendError(ctx,"The role for \'gfadmin\' and \'gfpeer\' cannot be set dynamically.");
			return; //RETURN
		}
		
			
		String role = args[1];
		if ( !User.isValidRole(role)){
			sendError(ctx, "\'" + role + "\' is not a valid role.  Please provide one of: " + User.validRolesString());
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
			if (u == null)
				throw new RuntimeException( "\"" + args[0] + "\" is not an existing user." );

			u.setLevel(User.Level.valueOf(role));
			userRegion.put(args[0], u);
			tm.commit();
		} catch(CommitConflictException x){
			sendError(ctx,"Operation failed due to concurrent update by another user.  Please try again.");
			return; //RETURN
		} finally {
			if (tm.exists()) tm.rollback();
		}
		
		ctx.getResultSender().lastResult(String.format("\"%s\" now has the %s role", args[0],args[1]));
	}

}
