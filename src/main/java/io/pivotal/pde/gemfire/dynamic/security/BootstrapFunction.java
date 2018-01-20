/*
 * Copyright (c) 2018 Pivotal Software, Inc. All Rights Reserved.
 */
package io.pivotal.pde.gemfire.dynamic.security;

import java.io.File;

import org.apache.geode.cache.Cache;
import org.apache.geode.cache.CacheFactory;
import org.apache.geode.cache.ExpirationAction;
import org.apache.geode.cache.ExpirationAttributes;
import org.apache.geode.cache.Region;
import org.apache.geode.cache.RegionShortcut;
import org.apache.geode.cache.execute.Function;
import org.apache.geode.cache.execute.FunctionContext;
import org.apache.geode.cache.execute.FunctionService;

public class BootstrapFunction implements Function {

	
	
	@Override
	public String getId() {
		return "SecurityBootstrapFunction";
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
		String []args = (String[]) ctx.getArguments();
		String securityDiskDir = args[0];
		
		BootstrapFunction.initCluster(securityDiskDir);
				
		Cache cache = CacheFactory.getAnyInstance();
		ctx.getResultSender().lastResult("BoostrapFunction SUCCEEDED ON " + cache.getDistributedSystem().getDistributedMember().getName());
	}

	public static void initCluster(String securityDiskDir){
		String diskStoreName = "gemusers-disk-store";
		
		Cache cache = CacheFactory.getAnyInstance();
		if (cache.getRegion(DynamicSecurityManager.USERS_REGION) == null){
			cache.createDiskStoreFactory().setAutoCompact(true).setDiskDirs(new File[]{new File(securityDiskDir)}).create(diskStoreName);
			Region userRegion = 
					cache.createRegionFactory(RegionShortcut.REPLICATE_PERSISTENT)
						.setDiskStoreName(diskStoreName)
						.create(DynamicSecurityManager.USERS_REGION);

			FunctionService.registerFunction(new RemoveUserFunction());
			FunctionService.registerFunction(new AddUserFunction());
			FunctionService.registerFunction(new SetRoleFunction());
			FunctionService.registerFunction(new PasswordFunction());
		}
	}
}
