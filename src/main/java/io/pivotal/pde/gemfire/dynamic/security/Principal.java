package io.pivotal.pde.gemfire.dynamic.security;

import java.io.Serializable;

import org.apache.geode.cache.CacheFactory;
import org.apache.geode.security.ResourcePermission;

public class Principal implements Serializable {
	public static enum Level{ PEER, MONITOR, READER, WRITER, DBADMIN, ADMIN }
	
	private int level;
	
	public Principal() {
		level = Level.PEER.ordinal();
	}
	
	public Principal(Level l){
		this.level = l.ordinal();
	}
	
	public Level getLevel() {
		return  Level.values()[level];
	}
	
	public boolean canDo(ResourcePermission perm){		
		
		CacheFactory.getAnyInstance().getSecurityLogger().info("in authorize - perm.getRegionName(): " + perm.getRegionName());
		
		// ADMIN can do anything
		if (this.level == Level.ADMIN.ordinal()) return true;

		// no one but ADMIN can do anything at all with the gemusers or the roles regions
		if (perm.getResource() == ResourcePermission.Resource.DATA && perm.getRegionName().equals(DynamicSecurityManager.USERS_REGION))
			return false;
		
		if (perm.getResource() == ResourcePermission.Resource.DATA && perm.getRegionName().equals(DynamicSecurityManager.ROLES_REGION))
			return false;
		
		if (perm.getResource() == ResourcePermission.Resource.CLUSTER){
			if (perm.getOperation() == ResourcePermission.Operation.READ) {
				return true;
			} else if (perm.getOperation() == ResourcePermission.Operation.WRITE){
				return false;   // only admin can do this
			} else if (perm.getOperation() == ResourcePermission.Operation.MANAGE){
				return this.level == Level.PEER.ordinal();  // only ADMIN and PEER    
			} else {
				return false;
			}
		} else if (perm.getResource() == ResourcePermission.Resource.DATA){
			if (perm.getOperation() == ResourcePermission.Operation.READ) {
				return this.level >= Level.READER.ordinal();
			} else if (perm.getOperation() == ResourcePermission.Operation.WRITE){
				return this.level >= Level.WRITER.ordinal();
			} else if (perm.getOperation() == ResourcePermission.Operation.MANAGE){
				return this.level >= Level.DBADMIN.ordinal();
			} else {
				return false;
			}
		} else {
			return false;
		}
	}
}
