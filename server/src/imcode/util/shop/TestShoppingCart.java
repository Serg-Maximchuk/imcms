package imcode.util.shop ;

import java.util.Arrays ;

import junit.framework.* ;

public class TestShoppingCart extends TestCase {

    public TestShoppingCart(String name) {
	super(name) ;
    }

    public void testAddAndCountAndRemove() {
	ShoppingCart theCart = new ShoppingCart() ;
	ShoppingItem item1 = new ShoppingItem() ;
	item1.setPrice(1) ;
	ShoppingItem item2 = new ShoppingItem() ;
	item2.setPrice(2) ;

	theCart.addItem(item1,1) ;
	assertEquals(1, theCart.countItem(item1)) ;
	assertEquals(1,theCart.countItems()) ;

	theCart.addItem(item2,2) ;
	assertEquals(2, theCart.countItem(item2)) ;
	assertEquals(3,theCart.countItems()) ;

	theCart.removeItem(item1) ;
	assertEquals(0,theCart.countItem(item1)) ;
	assertEquals(2,theCart.countItem(item2)) ;
	assertEquals(2,theCart.countItems()) ;

	theCart.putItem(item2,4) ;
	assertEquals(0,theCart.countItem(item1)) ;
	assertEquals(4,theCart.countItem(item2)) ;
	assertEquals(4,theCart.countItems()) ;
    }

    public void testSorted() {
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

	ShoppingCart cart = new ShoppingCart() ;

	cart.addAll(Arrays.asList(new ShoppingItem[] { item2, item3, item4, item1 })) ;

	ShoppingItem[] items = cart.getItems() ;

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
