import static org.junit.Assert.assertTrue;

import org.apache.geode.cache.CacheFactory;
import org.apache.geode.cache.Region;
import org.apache.geode.cache.RegionShortcut;
import org.apache.geode.distributed.ServerLauncher;
import org.junit.Test;

import io.pivotal.pde.gemfire.dynamic.security.User;

public class UserTest {

	@Test
	public void testPasswordCode() {
		User u = new User();
		u.setPassword("opensesame");
		assertTrue(u.passwordMatches("opensesame"));
	}
	
	@Test
	public void testSerializationInGem(){
       ServerLauncher serverLauncher  = 
    		   new ServerLauncher.Builder().build();

	   	serverLauncher.start();
		try {
			Region<String, User> testRegion = CacheFactory.getAnyInstance() 
					.<String,User>createRegionFactory(RegionShortcut.LOCAL).create("test");
			
			User u = new User();
			u.setPassword("pass");
			testRegion.put("fred", u);
			
			u = testRegion.get("fred");
			assertTrue(u.passwordMatches("pass"));	
		} finally {
			serverLauncher.stop();
		}
	}

	
}
