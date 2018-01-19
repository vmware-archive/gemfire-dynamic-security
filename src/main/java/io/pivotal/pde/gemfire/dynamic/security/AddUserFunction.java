package io.pivotal.pde.gemfire.dynamic.security;

import org.apache.geode.cache.CacheFactory;
import org.apache.geode.cache.CacheTransactionManager;
import org.apache.geode.cache.CommitConflictException;
import org.apache.geode.cache.Region;
import org.apache.geode.cache.execute.FunctionContext;

public class AddUserFunction extends AdminFunction  {

	
	@Override
	public String getId() {
		return "add_user";
	}
	
	@Override
	public void execute(FunctionContext ctx) {
		Object argsObj = ctx.getArguments();
		if (argsObj == null) {
			sendError(ctx,"Please provide username, password and role arguments" );
			return; //RETURN
		}
		
		String []args = (String [])argsObj;
		if (args.length !=3 ){
			sendError(ctx, "Incorrect arguments were provided.  Please provide 3 arguments: username, password, role");
			return; //RETURN
		}
		
		if (args[0].equals("gfadmin") || args[0].equals("gfpeer")){
			sendError(ctx,"Built in users  \'gfadmin\' and \'gfpeer\' cannot be modified dynamically.");
			return; //RETURN
		}

		String role = args[2];
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
			if (u == null) {
				u = new User();
			}
			
			u.setPassword(args[1]);
			u.setLevel(User.Level.valueOf(role));
			
			userRegion.put(args[0], u);
			tm.commit();
		} catch(CommitConflictException x){
			sendError(ctx,"Operation failed due to concurrent update by another user.  Please try again.");
			return;  //RETURN
		} finally {
			if (tm.exists()) tm.rollback();
		}
		
		ctx.getResultSender().lastResult(String.format("The password and role for %s were set.", args[0]));
	}

}
