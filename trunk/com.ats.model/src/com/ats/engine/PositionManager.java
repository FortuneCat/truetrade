package com.ats.engine;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeSet;

import org.apache.log4j.Logger;

import com.ats.platform.JOrder;
import com.ats.platform.Position;
import com.ats.platform.Strategy;


public class PositionManager implements ExecutionListener {
	private static final Logger logger = Logger.getLogger(PositionManager.class);
	
	private static final PositionManager instance = new PositionManager();
	
	private Map<Strategy, Position> positions = new HashMap<Strategy, Position>();
	
	private PropertyChangeSupport propertySupport;
	
	public static final String PROP_EXECUTION = "execution";
	public static final String PROP_RESET = "reset";
	public static final String PROP_ADD_STRATEGY = "add_strategy";
	public static final String PROP_ADD_POSITION = "add_position";
	
	/** singleton constructor */
	private PositionManager() {
		 propertySupport = new PropertyChangeSupport(this);
		 
		// TODO: should go to persistent source to load all expected current positions
		// TODO: go to TWS and query for actual positions?  Seek to rectify differences
		// TODO: add a facility to reset positions if they were closed down after
		//       ATS was shut down
	}
	
	public static final PositionManager getInstance() {
		return instance;
	}
	
	public synchronized void reset() {
		positions = new HashMap<Strategy, Position>();
		propertySupport.firePropertyChange(PROP_RESET, null, null);
	}
	
	public synchronized Position getPosition(Strategy engine) {
		Position pos = positions.get(engine);
		if( pos == null ) {
			pos = new Position(engine.getInstrument());
			pos.setStrategy(engine);
			positions.put(engine, pos);
			propertySupport.firePropertyChange(PROP_ADD_POSITION, null, pos);
		}
		return pos;
	}

	public synchronized Collection<TradeSummary> getAllTrades() {
		TreeSet<TradeSummary> ret = new TreeSet<TradeSummary>();
		for( Position pos : getAllPositions() ) {
			ret.addAll(pos.getTradeSummary());
		}
		return ret;
	}

	public synchronized Collection<Position> getAllPositions() {
		return positions.values();
	}
	
	public synchronized Collection<Strategy>  getAllStrategies() {
		return positions.keySet();
	}

	public synchronized void execution(JOrder order, JExecution execution) {
		Position pos = getPosition(order.getStrategy());
		pos.addExecution(execution);
		propertySupport.firePropertyChange(PROP_EXECUTION, pos, execution);
	}

	public void addPropertyChangeListener(PropertyChangeListener listener) {
		propertySupport.addPropertyChangeListener(listener);
	}

	public void addPropertyChangeListener(String propertyName, PropertyChangeListener listener) {
		propertySupport.addPropertyChangeListener(propertyName, listener);
	}

	public void removePropertyChangeListener(PropertyChangeListener listener) {
		propertySupport.removePropertyChangeListener(listener);
	}

	public void removePropertyChangeListener(String propertyName, PropertyChangeListener listener) {
		propertySupport.removePropertyChangeListener(propertyName, listener);
	}
	
	

}
