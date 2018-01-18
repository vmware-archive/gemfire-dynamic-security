package io.pivotal.pde.gemfire.dynamic.security;

import org.apache.geode.cache.execute.Function;
import org.apache.geode.cache.execute.FunctionContext;

public class AdminFunction {

	public AdminFunction() {
		super();
	}

	protected void sendError(FunctionContext ctx, String msg) {
		ctx.getResultSender().lastResult("Error: " + msg);
	}

}