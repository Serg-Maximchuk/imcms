package imcode.server;

import com.google.common.collect.Sets;
import imcode.server.user.ldap.MappedRoles;
import org.junit.Test;

public class XMLConfigTest {

    @Test
    public void constructor() throws Exception {
        String configFilePath = ClassLoader.getSystemClassLoader().getResource("test-server.xml").getFile();
        XMLConfig config = new XMLConfig(configFilePath);

        MappedRoles mappedRoles = config.getLdapMappedRoles();
        System.out.println(mappedRoles.roles());
        System.out.println(mappedRoles.rolesToAttributes().attributesNames());
        System.out.println(mappedRoles.rolesToAdGroups().roles());
        System.out.println(mappedRoles.rolesToAttributes().role("title", "manager"));
        System.out.println(mappedRoles.rolesToAdGroups().roles(Sets.newHashSet(
                "CN=imGrpGlobalSec,CN=Users,DC=d01,DC=imcode,DC=com",
                "CN=imGrpUniversalSec,CN=Users,DC=d01,DC=imcode,DC=com",
                "CN=imGrpUniversalSec,CN=Users,DC=d01,DC=imcode,DC=com")));
    }
}
