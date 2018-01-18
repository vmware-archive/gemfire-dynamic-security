package io.pivotal.pde.gemfire.dynamic.security;

public class GFPeer extends User {

	public GFPeer(){
		super();
		super.setLevel(Level.PEER);
	}
	
	@Override
	public void setLevel(Level l) {
		throw new RuntimeException("Cannot set the privilege level of GFPeer user");
	}

	@Override
	public void setPassword(String pass) {
		throw new RuntimeException("Cannot set the password of GFPeer user");
	}

	@Override
	public boolean passwordMatches(String pass) {
		throw new RuntimeException("Cannot check the password of GFPeer user");	
	}
	
}
