import static org.junit.Assert.assertTrue;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

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
	
//	@Test
//	public void testSerializationInGem(){
//       ServerLauncher serverLauncher  = 
//    		   new ServerLauncher.Builder().build();
//
//	   	serverLauncher.start();
//		try {
//			Region<String, User> testRegion = CacheFactory.getAnyInstance() 
//					.<String,User>createRegionFactory(RegionShortcut.LOCAL).create("test");
//			
//			User u = new User();
//			u.setPassword("pass");
//			testRegion.put("fred", u);
//			
//			u = testRegion.get("fred");
//			assertTrue(u.passwordMatches("pass"));	
//		} finally {
//			serverLauncher.stop();
//		}
//	}

	@Test
	public void testJavaSerialization(){
		try {
			User u = new User();
			u.setPassword("opensesame");
			
			ByteArrayOutputStream bos = new ByteArrayOutputStream();
			ObjectOutputStream oos = new ObjectOutputStream(bos);
			oos.writeObject(u);
			oos.close();
			
			ByteArrayInputStream bis = new ByteArrayInputStream(bos.toByteArray());
			ObjectInputStream ois = new ObjectInputStream(bis);
			User newU = (User) ois.readObject();
			
			assertTrue(newU.passwordMatches("opensesame"));	
			
		} catch(IOException iox){
			throw new RuntimeException(iox);
		} catch (ClassNotFoundException cnfx) {
			throw new RuntimeException(cnfx);
		}
	}
	
}
