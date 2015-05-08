package jef.orm.multitable;

import org.easyframe.enterprise.spring.SessionFactoryBean;
import org.junit.Test;

public class InitDataTest {

	@Test
	public void testInitData() {
		SessionFactoryBean bean = new SessionFactoryBean();
		bean.setDataSource("jdbc:mysql://localhost:3307/test", "root", "admin");
		bean.setAnnotatedClasses(new String[]{"jef.orm.multitable.model.Person"});
		bean.setInitDataAfterCreate(true);
		bean.setInitDataIfTableExists(true);
		bean.build();
	}

}
