package com.imcode.imcms.dao

import org.dbunit.DataSourceDatabaseTester
import static org.junit.Assert.*
public abstract class DaoTest {
	
	static final dataSetRootPath = "src/test/resources/"
	
	def tester
		
	@BeforeClass void initTester() {
        def dataSource = Context.getBean("dataSourceWithAutoCommit")
        def dataSetFilePath = dataSetRootPath +  dataSetFileName
        def dataSet = new FlatXmlDataSet(new FileReader(dataSetFilePath))
        
        tester = new DataSourceDatabaseTester(dataSource)
        //tester.setSetUpOperation DatabaseOperation.REFRESH
        tester.setDataSet dataSet
	}
		
	
    @BeforeMethod final void refreshDatabase() {
    	tester.onSetup()
    }
	
		
	@AfterMethod final void afterMethod() {
		tester.onTearDown()
	}
	
	abstract getDataSetFileName();
}