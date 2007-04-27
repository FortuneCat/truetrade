package com.ats.db;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;

import com.ats.engine.StrategyDefinition;
import com.ats.platform.Bar;
import com.ats.platform.BarSeries;
import com.ats.platform.Instrument;
import com.ats.platform.TimeSpan;
import com.ibatis.sqlmap.client.SqlMapClient;

public class PlatformDAO {
	private static final Logger logger = Logger.getLogger(PlatformDAO.class);
	
	
	// -----------
	// Instrument
	// ----------
	
	public static final List<Instrument> getAllInstruments() {
		List<Instrument> ret = null;
		try {
			SqlMapClient sqlMap = SqlMapUtil.getInstance().getSqlClient();
			ret = (List<Instrument>)sqlMap.queryForList("getAllInstruments");
		} catch( Exception e) {
			logger.error("Error retrieving instruments", e);
		}
		return ret;
	}
	
	public static void insertInstrument(Instrument curr) {
		try {
			SqlMapClient sqlMap = SqlMapUtil.getInstance().getSqlClient();
			sqlMap.insert("insertInstrument", curr);
		} catch( Exception e ) {
			logger.error("Could not insert instrument", e);
		}
	}
	
	public static void updateInstrument(Instrument curr) {
		try {
			SqlMapClient sqlMap = SqlMapUtil.getInstance().getSqlClient();
			sqlMap.update("updateInstrument", curr);
		} catch( Exception e ) {
			logger.error("Could not update instrument", e);
		}
	}
	
	
	// -----------
	// StrategyDefinition
	// -----------
	public static final List<StrategyDefinition> getAllStrategyDefinitions() {
		List<StrategyDefinition> ret = null;
		try {
			SqlMapClient sqlMap = SqlMapUtil.getInstance().getSqlClient();
			ret = (List<StrategyDefinition>)sqlMap.queryForList("getAllStrategies");
		} catch( Exception e) {
			logger.error("Error retrieving strategy definitions", e);
		}
		return ret;
	}
	
	public static StrategyDefinition getStrategyDefinition(int id) {
		StrategyDefinition ret = null;
		try {
			SqlMapClient sqlMap = SqlMapUtil.getInstance().getSqlClient();
			ret = (StrategyDefinition)sqlMap.queryForObject("getStrategyById", id);
		} catch( Exception e ) {
			logger.error("Could not retrieve strategy", e);
		}
		return ret;
	}
	
	public static void insertStrategyDefinition(StrategyDefinition curr) {
		try {
			SqlMapClient sqlMap = SqlMapUtil.getInstance().getSqlClient();
			sqlMap.insert("insertStrategy", curr);
		} catch( Exception e ) {
			logger.error("Could not insert strategy definition", e);
		}
	}

	public static void updateStrategyDefinition(StrategyDefinition curr) {
		try {
			SqlMapClient sqlMap = SqlMapUtil.getInstance().getSqlClient();
			sqlMap.update("updateStrategy", curr);
		} catch( Exception e ) {
			logger.error("Could not update strategy definition", e);
		}
	}

	public static void addInstrumentToStrategy(StrategyDefinition defn, Instrument instr) {
		try {
			Map<String, Object> args = new HashMap<String, Object>();
			args.put("stratDefId", defn.getId());
			args.put("instrumentId", instr.getId());
			SqlMapClient sqlMap = SqlMapUtil.getInstance().getSqlClient();
			sqlMap.insert("addInstrumentToStrategy", args);
		} catch( Exception e ) {
			logger.error("Could not add instrument to strategy", e);
		}
	}
	
	public static void deleteInstrumentFromStrategy(StrategyDefinition defn, Instrument instr) {
		try {
			Map<String, Object> args = new HashMap<String, Object>();
			args.put("stratDefId", defn.getId());
			args.put("instrumentId", instr.getId());
			SqlMapClient sqlMap = SqlMapUtil.getInstance().getSqlClient();
			sqlMap.delete("deleteInstrumentFromStrategy", args);
		} catch( Exception e ) {
			logger.error("Could not delete instrument from strategy", e);
		}
	}
	

	
	public static void main(String args[]) {
		System.out.println("Strat 2: " + getStrategyDefinition(2));
		System.out.println("Strat 1: " + getStrategyDefinition(1));
		System.out.println("Strategies: " + getAllStrategyDefinitions());
		
	}
	
	
	// --------------
	// Bar and BarSeries
	// --------------
	public static BarSeries getBarSeries(Instrument instr, TimeSpan span) {
		BarSeries ret = null;
		try {
			Map<String, Object> args = new HashMap<String, Object>();
			args.put("instrumentId", instr.getId());
			args.put("timespanId", span.ordinal());
			SqlMapClient sqlMap = SqlMapUtil.getInstance().getSqlClient();
			ret = (BarSeries)sqlMap.queryForObject("getSeriesByInstrumentAndTime", args);
		} catch( Exception e ) {
			logger.error("Could not retrieve Bar Series", e);
		}
		return ret;
	}
	
	public static List<Map<String, Object>> getSeriesOverview(Instrument instrument) {
		List<Map<String, Object>> ret = null;
		try {
			SqlMapClient sqlMap = SqlMapUtil.getInstance().getSqlClient();
			ret = (List<Map<String, Object>>)sqlMap.queryForList("getSeriesOverview", instrument.getId());
		} catch( Exception e) {
			logger.error("Error retrieving series overview", e);
		}
		return ret;
	}
	
	public static List<Bar> getSampleBarsForSeries(int seriesId) {
		List<Bar> ret = null;
		try {
			SqlMapClient sqlMap = SqlMapUtil.getInstance().getSqlClient();
			ret = (List<Bar>)sqlMap.queryForList("getBarSelectionBySeriesId", seriesId);
		} catch( Exception e) {
			logger.error("Error retrieving sample bars", e);
		}
		return ret;
	}


	public static void insertBarSeries(BarSeries series) {
		try {
			// first insert the series
			SqlMapClient sqlMap = SqlMapUtil.getInstance().getSqlClient();
			sqlMap.insert("insertBarSeries", series);
			
			insertBars(series);
			
		} catch( Exception e ) {
			logger.error("Could not insert bar series", e);
		}
	}

	public static void insertBar(BarSeries series, Bar bar) {
		try {
			SqlMapClient sqlMap = SqlMapUtil.getInstance().getSqlClient();
			Map<String, Object> args = new HashMap<String, Object>();
			args.put("barSeries", series);
			args.put("bar", bar);
			sqlMap.insert("insertBar", args);
		} catch( Exception e ) {
			logger.error("Could not insert bar", e);
		}
	}

	public static void insertBars(BarSeries series) {
		// now insert its bars
		try {
			SqlMapClient sqlMap = SqlMapUtil.getInstance().getSqlClient();
			Map<String, Object> args = new HashMap<String, Object>();
			args.put("barSeries", series);
			Iterator<Bar> it = series.getBarsFromOldest();
			while (it.hasNext() ) {
				Bar bar = it.next();
				args.put("bar", bar);
				sqlMap.insert("insertBar", args);
			}
		} catch( Exception e ) {
			logger.error("Could not insert bar", e);
		}
	}
	
	

	
	
}
