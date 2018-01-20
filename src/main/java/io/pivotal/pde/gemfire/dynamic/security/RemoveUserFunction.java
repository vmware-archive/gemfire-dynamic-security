/*
 * Copyright (c) 2018 Pivotal Software, Inc. All Rights Reserved.
 */
package io.pivotal.pde.gemfire.dynamic.security;

import org.apache.geode.cache.CacheFactory;
import org.apache.geode.cache.Region;
import org.apache.geode.cache.execute.FunctionContext;

public class RemoveUserFunction extends AdminFunction {

	@Override
	public String getId() {
		return "remove_user";
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
