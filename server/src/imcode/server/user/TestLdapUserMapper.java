package imcode.server.user;


public class TestLdapUserMapper extends UserBaseTestCase {
   private LdapUserMapper ldapUserMapper;

   public void setUp() {
      try {
         String ldapServerURL = "ldap://loke:389/CN=Users,DC=imcode,DC=com";
         String ldapAuthenticationType = "simple";
         String ldapUserName = "imcode\\hasbra";
         String ldapPassword = "hasbra";
         ldapUserMapper = new LdapUserMapper( ldapServerURL,
                                              ldapAuthenticationType,
                                              ldapUserName,
                                              ldapPassword,
                                              "se");
      } catch( LdapUserMapper.LdapInitException e ) {
         fail();
      }
   }

   public void testInvalidName() {
      User user = ldapUserMapper.getUser("");
      assertNull( user );
   }

   public void testNonExistingUser() {
      User user = ldapUserMapper.getUser("kalle banan som inte finns");
      assertNull( user );
   }

   public void testExistingUserCristoffer() {
      User user = ldapUserMapper.getUser("chrham");
      assertNotNull( user );
      assertEquals( "chrham", user.getLoginName());
      assertEquals( "", user.getPassword() );

      assertEquals( "Skeppsbron 24", user.getAddress() );
      assertEquals( "VISBY", user.getCity() );
      assertEquals( "Imcode", user.getCompany() );
      assertEquals( "SWEDEN", user.getCountry() );
      assertEquals( "Gotland", user.getCountyCouncil() );
      assertEquals( "kriger@imcode.se", user.getEmailAddress() );
      assertEquals( "Christoffer", user.getFirstName());
      assertEquals( "hemtelenummer", user.getHomePhone() );
      assertEquals( "Hammarström", user.getLastName());
      assertEquals( "Hacker", user.getTitle() );
      assertEquals( "0708 60 89 68", user.getMobilePhone() );
      assertEquals( "0498 200 300", user.getWorkPhone() );
      assertEquals( "621 57", user.getZip() );
      assertTrue( "SE".equalsIgnoreCase(user.getLangPrefix()) );
   }
}