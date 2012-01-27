package com.ats.utils;

import java.io.IOException;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.TimeZone;

import org.apache.log4j.Logger;
import org.eclipse.jface.preference.PreferenceStore;

public class Utils {
	private static final Logger logger = Logger.getLogger(Utils.class);
	
	public static final TimeZone nyseTimeZone = TimeZone.getTimeZone("America/New_York");
	
	public static final SimpleDateFormat timeAndDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm");
	
	public static final NumberFormat currencyForm = new DecimalFormat("#,###.00");
	public static final NumberFormat doubleDecForm = new DecimalFormat("#.00");
	public static final NumberFormat quadDecForm = new DecimalFormat("#.0000");
	public static final NumberFormat thousandsForm = new DecimalFormat("#,###");
	
	private static PreferenceStore prefStore = null;

	public static final String SLIPPAGE_PERCENT = "slippage.percent";
	public static final String SLIPPAGE_PERCENT_VALUE = "slippage.percent.value";
	public static final String SLIPPAGE_TICK = "slippage.tick";
	public static final String SLIPPAGE_TICK_VALUE = "slippage.tick.value";
	
	public static final String COMMISSION_SHARE = "commission.pershare";
	public static final String COMMISSION_SHARE_VALUE = "commission.pershare.value";
	public static final String COMMISSION_ORDER = "commission.perorder";
	public static final String COMMISSION_ORDER_VALUE = "commission.perorder.value";
	public static final String COMMISSION_TRANS = "commission.pertransaction";
	public static final String COMMISSION_TRANS_VALUE = "commission.pertransaction.value";
	
	public static final String INITIAL_CAPITAL_VALUE = "initialcapital.value";

	public static final String INTERACTIVE_BROKERS_HOST = "interactivebrokers.host";
	public static final String INTERACTIVE_BROKERS_PORT = "interactivebrokers.port";
	public static final String INTERACTIVE_BROKERS_CLIENTID = "interactivebrokers.client_id";
	
	public static final String OPENTICK_USER = "opentick.username";
	public static final String OPENTICK_PASSWORD = "opentick.password";


	// database preferences
	public static final String DB_URL = "database.url";
	public static final String DB_PASSWORD = "database.password";
	public static final String DB_USER = "database.username";
	public static final String DB_PROVIDER = "database.jdbc_provider";
	public static final String DB_JARFILE = "database.jdbc_jarfile";
	public static final String DB_USE_DEFAULT = "database.use_default";


	public synchronized static PreferenceStore getPreferenceStore() {
		if( prefStore == null ) {
			prefStore = new PreferenceStore("ats.properties");
			try {
				prefStore.load();
			} catch( IOException e) {
				logger.debug("Could not retrieve preference store");
			}
			
			// set defaults
			prefStore.setDefault(DB_USE_DEFAULT, true);
//			prefStore.setDefault(DB_USER, "guest");
//			prefStore.setDefault(DB_PASSWORD, "guest");
//			prefStore.setDefault(DB_PROVIDER, "com.mysql.jdbc.Driver");
//			prefStore.setDefault(DB_URL, "jdbc:mysql://localhost/ats?enable-named-pipe&socketFactory=com.mysql.jdbc.NamedPipeSocketFactory");
			prefStore.setDefault(DB_USER, "guest");
			prefStore.setDefault(DB_PASSWORD, "guest");
			prefStore.setDefault(DB_PROVIDER, "com.mysql.jdbc.Driver");
			prefStore.setDefault(DB_URL, "jdbc:mysql://localhost/ats?enable-named-pipe&socketFactory=com.mysql.jdbc.NamedPipeSocketFactory");
//			prefStore.setDefault(DB_PROVIDER, "org.apache.derby.jdbc.EmbeddedDriver");
//			prefStore.setDefault(DB_URL, "jdbc:derby://localhost/ats;create=true");
			
			prefStore.setDefault(INTERACTIVE_BROKERS_HOST, "127.0.0.1");
			prefStore.setDefault(INTERACTIVE_BROKERS_CLIENTID, 0);
			prefStore.setDefault(INTERACTIVE_BROKERS_PORT, 7496);
			
		}
		return prefStore;
	}



}
