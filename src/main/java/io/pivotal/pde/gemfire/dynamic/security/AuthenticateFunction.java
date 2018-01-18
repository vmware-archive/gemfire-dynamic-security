package io.pivotal.pde.gemfire.dynamic.security;

import java.io.Serializable;

import org.apache.geode.cache.CacheFactory;
import org.apache.geode.cache.Region;
import org.apache.geode.cache.execute.Function;
import org.apache.geode.cache.execute.FunctionContext;
import org.apache.geode.security.AuthenticationFailedException;
import org.omg.CORBA.Principal;

public class AuthenticateFunction implements Function {

	
	
	@Override
	public String getId() {
		return "authenticate";
	}

	@Override
	public boolean hasResult() {
		return true;
	}

	@Override
	public boolean isHA() {
		return true;
	}

	@Override
	public boolean optimizeForWrite() {
		return true;
	}

	@Override
	public void execute(FunctionContext ctx) {
		if (! (ctx.getArguments() instanceof String [])){
			throw new RuntimeException("ERROR - the authenticate function expects to be invokes with a String[] argument containing two Strings");
		}

		String []args = (String [])ctx.getArguments();
		if(args.length != 2){
			throw new RuntimeException("ERROR - the authenticate function expects to be invokes with a String[] argument containing two Strings");
		}
		
		Region<String,User> gemusersRegion = CacheFactory.getAnyInstance().getRegion(DynamicSecurityManager.USERS_REGION);
		if (gemusersRegion == null){
			throw new RegionNotFoundException();
		}
		
		
		DynamicSecurityManager.bypass.set(Boolean.TRUE);
		Object result = null;
		try {
			User u = gemusersRegion.get(args[0]);
			if (u == null || ! u.passwordMatches(args[1]))
				throw new AuthenticationFailedException("User does not exist or password does not match");
			
			result = u;
		} finally {
			DynamicSecurityManager.bypass.set(Boolean.FALSE);
		}
		
		ctx.getResultSender().lastResult(result);
	}
	
	public static class RegionNotFoundException extends RuntimeException implements Serializable {
		
	}
}
		
		
