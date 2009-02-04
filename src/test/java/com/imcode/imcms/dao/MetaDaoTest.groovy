package com.imcode.imcms.dao

import org.dbunit.dataset.xml.FlatXmlDataSet
import static org.testng.Assert.*
public class MetaDaoTest extends DaoTest {
	
	MetaDao dao;
	
	@BeforeClass void init() {
		dao = Context.getBean("metaDao")
	}		
		
	@Override
	def getDataSetFileName() {
		"dbunit-meta-data.xml"
	}
	
	
	@Test void getDocumentVersions() {
		def versions = dao.getDocumentVersions(1001);
		
		assertTrue(versions.size() > 0)
	}

    @Test void getPublishedMeta() {
        def meta = dao.getMeta(1001, DocumentVersionTag.PUBLISHED)
        
        assertNotNull(meta)        
    }
    
    @Test void getWorkingMeta() {
        def meta = dao.getMeta(1001, DocumentVersionTag.WORKING)
        
        assertNotNull(meta)        
    }    

    @Test void getMetaById() {
        def meta = dao.getMeta(1001L)
        
        assertNotNull(meta)        
    }    

    
    @Test void getMetaByVersion() {
        def meta = dao.getMeta(1001, 1)
        
        assertNotNull(meta)        
    }    
}