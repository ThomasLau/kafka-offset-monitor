package unfiltered.jettty;

import org.eclipse.jetty.security.ConstraintMapping;
import org.eclipse.jetty.security.ConstraintSecurityHandler;
import org.eclipse.jetty.security.HashLoginService;
import org.eclipse.jetty.security.SecurityHandler;
import org.eclipse.jetty.security.authentication.BasicAuthenticator;
import org.eclipse.jetty.util.security.Constraint;
import org.eclipse.jetty.util.security.Credential;

public class Utils {
    public static final SecurityHandler newBasicAuth(String authPath, String pathSpec) {
        HashLoginService hashLoginService = newHashLoginService(authPath);
        return newSercurityHandler(hashLoginService,pathSpec);
    }

    private static ConstraintSecurityHandler newSercurityHandler(HashLoginService hashLoginService,String pathSpec) {
        Constraint constraint = new Constraint();
        constraint.setName(Constraint.__BASIC_AUTH);
        constraint.setRoles(new String[]{"user"});
        constraint.setAuthenticate(true);

        ConstraintMapping cm = new ConstraintMapping();
        cm.setConstraint(constraint);
        if (!pathSpec.endsWith("*")){
            pathSpec+=(pathSpec.endsWith("/")?"*":"/*");
        }
        if (!pathSpec.endsWith("/*")){
            pathSpec = pathSpec.substring(0,pathSpec.length()-1)+"/*";
        }
        cm.setPathSpec(pathSpec);

        ConstraintSecurityHandler csh = new ConstraintSecurityHandler();
        csh.setAuthenticator(new BasicAuthenticator());
        csh.setRealmName("myrealm");
        csh.addConstraintMapping(cm);
        csh.setLoginService(hashLoginService);
        return csh;
    }

    public static final SecurityHandler newBasicAuth(String username, String password, String realm, String pathSpec) {
        HashLoginService hashLoginService = newHashLoginService(username,password,realm);
        return newSercurityHandler(hashLoginService,pathSpec);
    }

    public static HashLoginService newHashLoginService(String path) {
        HashLoginService loginServ = new HashLoginService();
        loginServ.setName("REALM");
        loginServ.setConfig(Utils.class.getClassLoader().getResource(path).getPath()); //location of authentication file
        loginServ.setRefreshInterval(60);// 1mins
        return loginServ;
    }
    public static HashLoginService newHashLoginService(String username, String password,String realm) {
        HashLoginService loginServ = new HashLoginService();
        loginServ.putUser(username, Credential.getCredential(password), new String[] {"user"});
        loginServ.setName(realm);
        return loginServ;
    }
}
