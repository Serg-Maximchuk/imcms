package com.imcode.imcms;

import clojure.lang.RT;
import clojure.lang.Symbol;
import clojure.lang.Var;
import org.hibernate.SessionFactory;

import javax.sql.DataSource;

/**
 * Integration with scripted code.
 *
 * Part of test related code is implemented in Clojure.
 */
public class Script {

    public static final String TEST_SQL_SCRIPTS_HOME = "src/test/resources/sql";

    public static final Var mNsResolve;

    static {
        try {
            RT.load("com/imcode/imcms/boot");
            mNsResolve = RT.var("com.imcode.imcms.boot", "m-ns-resolve");
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static Var var(String nsName, String varName) {
        try {
            return (Var)mNsResolve.invoke(Symbol.create(nsName), Symbol.create(varName));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

    }

    
    public static String getDBName() throws Exception {
        return (String)var("com.imcode.imcms.db-test", "db-name").invoke();
    }

    public static DataSource createDBDataSource(boolean autocommit) {
        try {
            return (DataSource)var("com.imcode.imcms.db-test", "create-ds").invoke(autocommit);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static void recreateDB() {
        try {
            var("com.imcode.imcms.db-test", "recreate").invoke();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }


    public static void prepareDB(boolean recreateBofrePrepare) {
        try {
            var("com.imcode.imcms.db-test", "prepare").invoke(getDBName(), recreateBofrePrepare);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    
    public static void runDBScripts(String... sqlScriptsPaths) {
        try {
            var("com.imcode.imcms.db-test", "run-scripts").invoke(createPaths(sqlScriptsPaths));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }    
    

    public static void initImcms(Boolean prepareDBOnStart) {
        try {
            var("com.imcode.imcms.project", "init-imcms").invoke(prepareDBOnStart);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }


    public static String[] createPaths(String[] scriptsNames) {
        String[] scriptsPaths = new String[scriptsNames.length];

        for (int i = 0; i < scriptsNames.length; i++) {
            scriptsPaths[i] = TEST_SQL_SCRIPTS_HOME + "/" + scriptsNames[i];
        }

        return scriptsPaths;
    }


    public static SessionFactory createHibernateSessionFactory(Class... annotatedClasses) {
        return createHibernateSessionFactory(annotatedClasses, new String[0]);
    }


    public static SessionFactory createHibernateSessionFactory(Class[] annotatedClasses, String... xmlFiles) {

        try {
            return (SessionFactory)var("com.imcode.imcms.db-test", "create-hibernate-sf")
                .invoke(annotatedClasses, xmlFiles);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}