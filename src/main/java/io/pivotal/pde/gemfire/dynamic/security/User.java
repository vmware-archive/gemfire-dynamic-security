package io.pivotal.pde.gemfire.dynamic.security;

import java.io.Serializable;
import java.nio.charset.Charset;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Arrays;

import org.apache.geode.security.ResourcePermission;

public class User implements Serializable, PrivilegedActor {
	
	public static enum Level{ PEER, MONITOR, READER, WRITER, ADMIN, SECADMIN }
	
	// TODO - I'm sure I could put this method in the enum
	public static boolean isValidRole(String name){
		if (name.equals(Level.PEER.name())) return true;
		if (name.equals(Level.MONITOR.name())) return true;
		if (name.equals(Level.READER.name())) return true;
		if (name.equals(Level.WRITER.name())) return true;
		if (name.equals(Level.ADMIN.name())) return true;
		if (name.equals(Level.SECADMIN.name())) return true;
		return false;
	}
	
	public static String validRolesString(){
		return Level.PEER.name() + "," + 
				Level.MONITOR.name() + "," +
				Level.READER.name() + "," +
				Level.WRITER.name() + "," +
				Level.ADMIN.name() + "," +
				Level.SECADMIN.name();
	}
	
	
	private static SecureRandom rand = new SecureRandom();
	
	private byte []passwordSalt;
	private byte []passwordHash;
	private String level;
	
	public User(){
	}

	public void setLevel(Level l){
		this.level = l.toString();
	}
	
	public Level getLevel(){
		return Level.valueOf(this.level);
	}
	
	public void setPassword(String pass){
		this.passwordSalt = new byte[32];
		rand.nextBytes(this.passwordSalt);
		this.passwordHash = hash(this.passwordSalt,pass);
	}
	
	
	public boolean passwordMatches(String pass){
		byte []candidate = hash(passwordSalt,pass);
		return Arrays.equals(candidate, this.passwordHash);
	}
	
	private byte []hash(byte []salt, String password){
		try {
			MessageDigest md5 = MessageDigest.getInstance("MD5");
			md5.update(salt);
			md5.update(password.getBytes(Charset.forName("UTF-8")));
			return(md5.digest());
		} catch(NoSuchAlgorithmException nx){
			throw new RuntimeException("Could not create MD5 digest object");
		}
	}

	@Override
	public boolean canDo(ResourcePermission perm){		
		
		// SECADMIN can do anything
		if (this.level == Level.SECADMIN.toString()) return true;

		// no one but SECADMIN can do anything at all with the gemusers  region
		if (perm.getResource() == ResourcePermission.Resource.DATA && (perm.getTarget().equals(DynamicSecurityManager.USERS_REGION) || perm.getTarget().equals("*")) )
			return false;
		
		// peer is handled separately
		if (this.getLevel() == Level.PEER){
			if (perm.getResource() == ResourcePermission.Resource.CLUSTER && perm.getOperation() == ResourcePermission.Operation.MANAGE) 
				return true;
			else
				return false;
		}
		
		if (perm.getResource() == ResourcePermission.Resource.CLUSTER){
			if (perm.getOperation() == ResourcePermission.Operation.READ) {
				return true;
			} else {
				// for write or manage, require admin
				return Level.valueOf(this.level).ordinal() >= Level.ADMIN.ordinal();
			}
		} else if (perm.getResource() == ResourcePermission.Resource.DATA){
			if (perm.getOperation() == ResourcePermission.Operation.READ) {
				return Level.valueOf(this.level).ordinal() >= Level.READER.ordinal();
			} else if (perm.getOperation() == ResourcePermission.Operation.WRITE){
				return Level.valueOf(this.level).ordinal() >= Level.WRITER.ordinal();
			} else if (perm.getOperation() == ResourcePermission.Operation.MANAGE){
				return Level.valueOf(this.level).ordinal() >= Level.ADMIN.ordinal();
			} else {
				return false;   // not expected to happen as of Gem 9.2 release
			}
		} else {
			return false;   // not expected to happen as of Gem 9.2 release
		}
	}

	
}
