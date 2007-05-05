package com.ats.db;

import java.io.Reader;
import java.util.Properties;

import org.apache.log4j.Logger;
import org.eclipse.jface.preference.PreferenceStore;

import com.ats.utils.Utils;
import com.ibatis.common.resources.Resources;
import com.ibatis.sqlmap.client.SqlMapClient;
import com.ibatis.sqlmap.client.SqlMapClientBuilder;

public class SqlMapUtil {
	private static final Logger logger = Logger.getLogger(SqlMapUtil.class);
	
	private static final SqlMapUtil instance = new SqlMapUtil();
	
	private SqlMapClient sqlClient;
	
	private SqlMapUtil() {
		try {
			Reader reader = Resources.getResourceAsReader("SqlMapConfig.xml");
			Properties properties = new Properties();
			PreferenceStore prefs = Utils.getPreferenceStore();
			

			if( prefs.getBoolean(Utils.DB_USE_DEFAULT)) {
				properties.setProperty("username", prefs.getDefaultString(Utils.DB_USER) );
				properties.setProperty("password", prefs.getDefaultString(Utils.DB_PASSWORD) );
				properties.setProperty("url", prefs.getDefaultString(Utils.DB_URL) );
				properties.setProperty("driver", prefs.getDefaultString(Utils.DB_PROVIDER) );
				
			} else {
				properties.setProperty("username", prefs.getString(Utils.DB_USER) );
				properties.setProperty("password", prefs.getString(Utils.DB_PASSWORD) );
				properties.setProperty("url", prefs.getString(Utils.DB_URL) );
				properties.setProperty("driver", prefs.getString(Utils.DB_PROVIDER) );
				
				// TODO: is there any way to automatically load the JDBC driver from
				// a specified JAR?
//				if( prefs.getString(Utils.DB_JARFILE).length() > 0 ) {
//					// add this to the classpath
//					try {
//						File jarfile = new File(prefs.getString(Utils.DB_JARFILE));
//						File currDir = new File(".");
//						URLClassLoader loader = URLClassLoader.newInstance(
//								new URL[] { jarfile.toURL(), currDir.toURL() }, 
//								DriverManager.class.getClassLoader());
//						Class clazz = loader.loadClass(prefs.getString(Utils.DB_PROVIDER));
//						Resources.setDefaultClassLoader(loader);
//						java.sql.DriverManager.registerDriver((java.sql.Driver)clazz.newInstance());
//						
//					} catch( Throwable e ) {
//						logger.error("Could not load library '" + prefs.getString(Utils.DB_JARFILE) +"': " + e);
//					}
//				}
			}
			sqlClient = SqlMapClientBuilder.buildSqlMapClient(reader, properties);
		} catch( Exception e ) {
			logger.error("Could not initialize SqlMapUtil", e);
		}
	}
	
	public static final SqlMapUtil getInstance() {
		return instance;
	}
	
	public SqlMapClient getSqlClient() {
		return sqlClient;
	}
}
