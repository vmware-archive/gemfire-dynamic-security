package io.pivotal.pde.gemfire.dynamic.security;

import java.io.File;

import org.apache.geode.cache.Cache;
import org.apache.geode.cache.CacheFactory;
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
		String diskStoreName = "gemusers-disk-store";
		
		Cache cache = CacheFactory.getAnyInstance();
		if (cache.getRegion(DynamicSecurityManager.USERS_REGION) == null){
			cache.createDiskStoreFactory().setAutoCompact(true).setDiskDirs(new File[]{new File(securityDiskDir)}).create(diskStoreName);
			cache.createRegionFactory(RegionShortcut.REPLICATE_PERSISTENT).setDiskStoreName(diskStoreName).create(DynamicSecurityManager.USERS_REGION);
		}
		
		if (cache.getRegion(DynamicSecurityManager.ROLES_REGION) == null){
			cache.createRegionFactory(RegionShortcut.REPLICATE_PERSISTENT).setDiskStoreName(diskStoreName).create(DynamicSecurityManager.ROLES_REGION);
		}
		
		ctx.getResultSender().lastResult("BoostrapFunction SUCCEEDED ON " + cache.getDistributedSystem().getDistributedMember().getName());
	}
	
}
