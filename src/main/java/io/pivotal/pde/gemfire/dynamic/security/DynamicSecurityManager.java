package io.pivotal.pde.gemfire.dynamic.security;

import java.security.Principal;
import java.util.List;
import java.util.Properties;
import java.util.Set;

import org.apache.geode.cache.CacheFactory;
import org.apache.geode.cache.Region;
import org.apache.geode.cache.execute.Execution;
import org.apache.geode.cache.execute.Function;
import org.apache.geode.cache.execute.FunctionService;
import org.apache.geode.cache.execute.ResultCollector;
import org.apache.geode.distributed.DistributedMember;
import org.apache.geode.security.AuthenticationFailedException;
import org.apache.geode.security.ResourcePermission;
import org.apache.geode.security.SecurityManager;

import io.pivotal.pde.gemfire.dynamic.security.AuthenticateFunction;
import io.pivotal.pde.gemfire.dynamic.security.BootstrapFunction;
import io.pivotal.pde.gemfire.dynamic.security.GFAdmin;
import io.pivotal.pde.gemfire.dynamic.security.GFPeer;
import io.pivotal.pde.gemfire.dynamic.security.User;

public class DynamicSecurityManager implements SecurityManager {

	static final String SECURITY_PEER_USER = "gfpeer";
	static final String SECURITY_ADMIN_USER = "gfadmin";
	
	static final String SECURITY_ADMIN_PASS_PROP = "security-admin-password";
	static final String SECURITY_PEER_PASS_PROP = "security-peer-password";
	static final String SECURITY_UNAME_PROP = "security-username";
	static final String SECURITY_PASS_PROP = "security-password";
	static final String SECURITY_DISK_STORE_DIR_PROP = "security-disk-store-dir";
	
	static final String USERS_REGION = "_gemusers";
	
	static Flag bypass = new Flag();  // thread local
	static ThreadLocalUser currPrincipal = new ThreadLocalUser();
	
	private String peerPassword;
	private String adminPassword;
	
	String securityDiskDir;
	
	private Flag initializing = new Flag();  // prevents recursion
	
	private DistributedMember gotoMember = null;
	
	
	@Override
	public void init(Properties securityProps) {
		this.peerPassword = securityProps.getProperty(SECURITY_PEER_PASS_PROP);
		this.adminPassword = securityProps.getProperty(SECURITY_ADMIN_PASS_PROP);
		this.securityDiskDir = securityProps.getProperty(SECURITY_DISK_STORE_DIR_PROP);
		
		if (securityDiskDir == null)
			throw new RuntimeException("Could not initialize security manager due to missing required property: " + SECURITY_DISK_STORE_DIR_PROP);
		
		if (this.peerPassword == null)
			throw new RuntimeException("Could not initialize security manager due to missing required property: " + SECURITY_PEER_PASS_PROP);
		
		if (this.adminPassword == null)
			throw new RuntimeException("Could not initialize security manager due to missing required property: " + SECURITY_ADMIN_PASS_PROP);		
	}
	
	private Object authenticateOnMember(DistributedMember m, String uname, String pass){
		Execution exec = FunctionService.onMember(m).setArguments(new String[]{uname, pass});
		Function f = new AuthenticateFunction();
		ResultCollector<Object, List<Object>> results = exec.execute(f);
		List<Object> plist = results.getResult();
		if (plist.size() != 1)
			throw new AuthenticationFailedException("Unexpected behavior: AuthenticateFunction returned " + plist.size() +  " results");
		
		return plist.get(0);
	}

	@Override
	public Object authenticate(Properties props) throws AuthenticationFailedException {
		String uname = props.getProperty(SECURITY_UNAME_PROP);
		String pass = props.getProperty(SECURITY_PASS_PROP);
		
		if (uname == null || pass == null)
			throw new AuthenticationFailedException("Authentication failed due to missing credentials");

		if ( uname.equals(SECURITY_PEER_USER) && pass.equals(peerPassword))
			return new GFPeer();
		
		if ( uname.equals(SECURITY_ADMIN_USER) && pass.equals(adminPassword)){
			return new GFAdmin();
		}
		

		// well this is a pain in the butt
		// It's all because sometimes this will happen on a locator where 
		// the region isn't present.
		Object result = null;
		Region<String, User> gemusersRegion = CacheFactory.getAnyInstance().getRegion(USERS_REGION);
		if (gemusersRegion != null){
			DynamicSecurityManager.bypass.set(true);
			try {
				User u = gemusersRegion.get(uname);
				if (u == null || ! u.passwordMatches(pass))
					throw new AuthenticationFailedException("User does not exist or password does not match");
				
				result = u;
			} finally {
				DynamicSecurityManager.bypass.set(false);
			}
			
			return result;  //RETURN
		} else {
			if (gotoMember != null) {
				try {
					result = authenticateOnMember(gotoMember, uname, pass);
					return result; //RETURN
				} catch(Exception  x){
					// ok
				}
			}
			
			// ok, gotoMember was null or did not succeed
			Set<DistributedMember> others = CacheFactory.getAnyInstance().getDistributedSystem().getAllOtherMembers();
			for(DistributedMember m: others){
				try {
					result = authenticateOnMember(m, uname, pass);
					gotoMember = m;
					return result; //RETURN
				} catch(Exception x){
					// ok
				}				
			}
		}
		
		throw new AuthenticationFailedException("Something unexpected happened during authentication");
	}

	@Override
	public boolean authorize(Object principal, ResourcePermission permission) {
		if (bypass.get()) return true;
				
		User u = (User) principal;
		// peers authorize while joining the cluster but 
		// admin will only access the cluster after it is up
		// that is the perfect time to run initialization code
		if (u.getLevel() == User.Level.SECADMIN){		
			if ( ! initializing.get()){
				initializing.set(Boolean.TRUE);
				initCluster();
			}
		}
		
		currPrincipal.set(u); 
		return u.canDo(permission);
	}

	private void initCluster(){
		// this will cause it to target only data nodes
		String []args = new String[] { securityDiskDir };
		Execution exec = FunctionService.onMembers().setArguments(args);
		Function bootstrapFunction = new BootstrapFunction();
		ResultCollector<String,List<String>> results = exec.execute(bootstrapFunction);
		List<String> result = results.getResult();
	}
	
	static class Flag extends ThreadLocal<Boolean> {
		@Override
		protected Boolean initialValue() {
			return Boolean.FALSE;
		}
		
	}
	
	static class ThreadLocalUser extends ThreadLocal<User>{
		
	}
	
}
