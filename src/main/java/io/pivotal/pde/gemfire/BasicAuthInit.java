package io.pivotal.pde.gemfire;

import java.util.Properties;

import org.apache.geode.LogWriter;
import org.apache.geode.distributed.DistributedMember;
import org.apache.geode.security.AuthInitialize;
import org.apache.geode.security.AuthenticationFailedException;

public class BasicAuthInit implements AuthInitialize {

	@Override
	public void close() {
		// TODO Auto-generated method stub

	}

	@Override
	public Properties getCredentials(Properties arg0, DistributedMember arg1, boolean arg2)
			throws AuthenticationFailedException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void init(LogWriter arg0, LogWriter arg1) throws AuthenticationFailedException {
		// TODO Auto-generated method stub

	}

}
