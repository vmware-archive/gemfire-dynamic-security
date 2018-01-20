/*
 * Copyright (c) 2018 Pivotal Software, Inc. All Rights Reserved.
 */
package io.pivotal.pde.gemfire.dynamic.security;

public class GFAdmin extends User {

	public GFAdmin(){
		super();
		super.setLevel(Level.SECADMIN);
	}
	
	@Override
	public void setLevel(Level l) {
		throw new RuntimeException("Cannot set privilege level of GFAdmin user");
	}
	
	@Override
	public void setPassword(String pass) {
		throw new RuntimeException("Cannot set the password of GFAdmin user");
	}

	@Override
	public boolean passwordMatches(String pass) {
		throw new RuntimeException("Cannot check the password of GFAdmin user");	
	}

	
}
