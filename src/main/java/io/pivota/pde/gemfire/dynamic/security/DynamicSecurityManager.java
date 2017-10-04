package io.pivota.pde.gemfire.dynamic.security;

import java.util.Properties;

import org.apache.geode.security.AuthenticationFailedException;
import org.apache.geode.security.ResourcePermission;
import org.apache.geode.security.SecurityManager;
import org.apache.logging.log4j.LogManager;

public class DynamicSecurityManager implements SecurityManager {

	private static final String SECURITY_PEER_UNAME_PROP = "security-peer-username";
	private static final String SECURITY_PEER_PASS_PROP = "security-peer-password";
	
	private String peerUserName;
	private String peerPassword;
	
	
	@Override
	public void init(Properties securityProps) {
		this.peerUserName = securityProps.getProperty(SECURITY_PEER_UNAME_PROP);
		this.peerPassword = securityProps.getProperty(SECURITY_PEER_PASS_PROP);
		
		if (this.peerUserName == null)
			throw new RuntimeException("Could not initialize security manager due to missing required property: " + SECURITY_PEER_UNAME_PROP);
		
		if (this.peerPassword == null)
			throw new RuntimeException("Could not initialize security manager due to missing required property: " + SECURITY_PEER_PASS_PROP);
	}

	@Override
	public Object authenticate(Properties props) throws AuthenticationFailedException {
		String uname = props.getProperty(SECURITY_PEER_UNAME_PROP);
		String pass = props.getProperty(SECURITY_PEER_PASS_PROP);
		
		if (uname == null || pass == null)
			throw new AuthenticationFailedException("Authentication failed due to missing required credentials. The following credentials are required: " + SECURITY_PEER_UNAME_PROP + "," + SECURITY_PEER_PASS_PROP);

		if ( !(uname.equals(peerUserName) && pass.equals(peerPassword)))
			throw new AuthenticationFailedException("bad user name or password");
		
		return new PeerPrincipal();
	}

	@Override
	public boolean authorize(Object principal, ResourcePermission permission) {
		LogManager.getLogger("SECURITY").info("authorize: " + permission);
		return true;
	}

}
