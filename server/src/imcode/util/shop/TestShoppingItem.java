package imcode.util.shop ;

import java.util.* ;

import junit.framework.* ;

public class TestShoppingItem extends TestCase {

    public TestShoppingItem(String name) {
	super(name) ;
    }

    public void testPrice() {
	ShoppingItem item1 = new ShoppingItem() ;
	assertEquals(0, item1.getPrice(), 0) ;
	item1.setPrice(1.33) ;
	assertEquals(1.33, item1.getPrice(), 0) ;
    }

    public void testEquals() {
	ShoppingItem item1 = new ShoppingItem() ;
	ShoppingItem item2 = new ShoppingItem() ;
	item1.setPrice(2.66) ;
	assertEquals(item1, item1) ;
	assertTrue(!item1.equals(item2)) ;
	assertTrue(!item2.equals(item1)) ;
	item2.setPrice(2.66) ;
	assertEquals(item1,item2) ;
	item2.setDescription(1, "Item 2 Desc 1") ;
	assertTrue(!item2.equals(item1)) ;
    }

    public void testDescriptions() {
	ShoppingItem item1 = new ShoppingItem() ;
	item1.setDescription(1,"Item 1 Desc 1") ;
	item1.setDescription(3,"Item 1 Desc 3") ;
	assertEquals("Item 1 Desc 1", item1.getDescription(1)) ;
	assertEquals("Item 1 Desc 3", item1.getDescription(3)) ;
	item1.setDescription(1,null) ;
	assertEquals("", item1.getDescription(1)) ;
    }

    public void testCompareTo() {
	ShoppingItem item1 = new ShoppingItem() ;
	item1.setDescription(1,"Desc 1") ;
	item1.setDescription(2,"Desc 2") ;
	item1.setPrice(1) ;
	ShoppingItem item2 = new ShoppingItem() ;
	item2.setDescription(1,"Desc 1") ;
	item2.setDescription(2,"Desc 2") ;
	item2.setPrice(2) ;

	assertEquals(0, item1.compareTo(item1)) ;
	assertEquals(0, item2.compareTo(item2)) ;

	assertEquals(-1, item1.compareTo(item2)) ;
	assertEquals(1,  item2.compareTo(item1)) ;

	ShoppingItem item3 = new ShoppingItem() ;
	item3.setDescription(1,"Desc 1") ;
	item3.setDescription(3,"Desc 3") ;
	item3.setPrice(1) ;

	assertEquals(1, item2.compareTo(item3)) ;

	item1.setPrice(4) ;
	item1.setDescription(3, "Desc 3") ;

	assertEquals(1, item1.compareTo(item3)) ;

	item1.setDescription(2, "Desc 3-1") ;
	item3.setDescription(2, "Desc 2") ;
	assertEquals(1, item1.compareTo(item3)) ;
	assertEquals(1, item1.compareTo(item2)) ;

	item1.setDescription(1,null) ;
	assertEquals(-1, item1.compareTo(item2)) ;
    }

    public void testComparePriceTo() {
	ShoppingItem item1 = new ShoppingItem() ;
	item1.setPrice(1.33) ;
	ShoppingItem item2 = new ShoppingItem() ;
	item2.setPrice(2.66) ;
	assertEquals( 0, item1.compareTo(item1)) ;
	assertEquals( 0, item2.compareTo(item2)) ;
	assertEquals(-1, item1.compareTo(item2)) ;
	assertEquals( 1, item2.compareTo(item1)) ;
    }

    public void testCompareDescriptionTo() {
	ShoppingItem item1 = new ShoppingItem() ;
	item1.setDescription(2,"Desc") ;
	ShoppingItem item2 = new ShoppingItem() ;
	item2.setDescription(1,"Desc") ;
	ShoppingItem item3 = new ShoppingItem() ;
	item3.setDescription(1,"Desc") ;
	item3.setDescription(2,"Desc") ;
	ShoppingItem item4 = new ShoppingItem() ;
	item4.setDescription(1,"XXX") ;
	item4.setDescription(2,"Desc") ;

	assertTrue(0 > item1.compareDescriptionTo(item2)) ;
	assertTrue(0 > item1.compareDescriptionTo(item3)) ;
	assertTrue(0 > item1.compareDescriptionTo(item4)) ;
	assertTrue(0 < item2.compareDescriptionTo(item1)) ;
	assertTrue(0 > item2.compareDescriptionTo(item3)) ;
	assertTrue(0 > item2.compareDescriptionTo(item4)) ;
	assertTrue(0 < item3.compareDescriptionTo(item2)) ;
	assertTrue(0 < item3.compareDescriptionTo(item1)) ;
	assertTrue(0 > item3.compareDescriptionTo(item4)) ;
	assertTrue(0 < item4.compareDescriptionTo(item1)) ;
	assertTrue(0 < item4.compareDescriptionTo(item2)) ;
	assertTrue(0 < item4.compareDescriptionTo(item3)) ;
    }

    public void testCompareToSort() {
	ShoppingItem item1 = new ShoppingItem() ;
	item1.setDescription(2,"Desc") ;
	ShoppingItem item2 = new ShoppingItem() ;
	item2.setDescription(1,"Desc") ;
	ShoppingItem item3 = new ShoppingItem() ;
	item3.setDescription(1,"Desc") ;
	item3.setDescription(2,"Desc") ;
	ShoppingItem item4 = new ShoppingItem() ;
	item4.setDescription(1,"XXX") ;
	item4.setDescription(2,"Desc") ;

	ShoppingItem[] items = new ShoppingItem[] { item1, item2, item3, item4 } ;

	Arrays.sort(items) ;

	assertTrue(item2 != items[0]) ;
	assertTrue(item3 != items[0]) ;
	assertTrue(item4 != items[0]) ;
	assertSame(item1, items[0]) ;

	assertTrue(item1 != items[1]) ;
	assertTrue(item3 != items[1]) ;
	assertTrue(item4 != items[1]) ;
	assertSame(item2, items[1]) ;

	assertTrue(item1 != items[2]) ;
	assertTrue(item2 != items[2]) ;
	assertTrue(item4 != items[2]) ;
	assertSame(item3, items[2]) ;

	assertTrue(item1 != items[3]) ;
	assertTrue(item2 != items[3]) ;
	assertTrue(item3 != items[3]) ;
	assertSame(item4, items[3]) ;
    }
}
