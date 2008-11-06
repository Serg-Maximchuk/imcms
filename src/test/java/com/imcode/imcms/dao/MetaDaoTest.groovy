package com.imcode.imcms.dao

import org.dbunit.dataset.xml.FlatXmlDataSet
import static org.testng.Assert.*
//todo: Test named queries
public class MetaDaoTest extends DaoTest {
	
	MetaDao dao;
	
	@BeforeClass void init() {
		dao = Context.getBean("metaDao")
	}		
		
	@Override
	def getDataSetFileName() {
		"dbunit-meta-data.xml"
	}
	

    @Test void getMeta() {
        def meta = dao.getMeta(1001)
        
        assertNotNull(meta)
        
        meta.getProperties().each {k, v ->
        	println "property: ${k}=${v}" 
        }
        
        
        
        meta.roleRights.each {k, v ->
        	println "Role to set: ${k}=${v}"        	
        }
        
        meta.permissionSetBits.each {k, v ->
        	println "Perm set it to bits: ${k}=${v}"  
        }

        meta.docPermisionSetEx.each {
        	println it.dump()
        }
        
        meta.docPermisionSetExForNew.each {
        	println it.dump()
        }
    }
}