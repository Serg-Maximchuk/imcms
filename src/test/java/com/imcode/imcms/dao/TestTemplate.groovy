package com.imcode.imcms.dao

import org.junit.After
import org.junit.AfterClass
import org.junit.Before
import org.junit.BeforeClass
import org.junit.Test

import static org.junit.Assert.*
//todo: Test named queries
public class TestTemplate {
	
	static beanName = ""
	
	static dao;
	
	@BeforeClass static void setUpBeforeClass() {
		dao = Context.getBean(beanName)
	}
	
	@AfterClass static void tearDownAfterClass() {
	}
	
	@Before void setUp() {
	}
	
	@After void tearDown() {
	}	   
	
	@Test void test() {
		assertTrue(false)
	}
	
	static final junit.framework.Test suite(){
		return new junit.framework.JUnit4TestAdapter(TestTemplate.class);
	}	
}