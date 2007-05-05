package com.ats.client.views;

import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.ui.views.properties.ComboBoxPropertyDescriptor;
import org.eclipse.ui.views.properties.IPropertyDescriptor;
import org.eclipse.ui.views.properties.IPropertySource;
import org.eclipse.ui.views.properties.PropertyDescriptor;
import org.eclipse.ui.views.properties.TextPropertyDescriptor;

import com.ats.db.PlatformDAO;
import com.ats.engine.StrategyDefinition;
import com.ats.platform.Instrument;
import com.ats.platform.TimeSpan;

public class InstrumentPropertySource implements IPropertySource {

	private static final String CURRENCY = "currency";
	private static final String EXCHANGE = "exchange";
	private static final String INSTR_TYPE = "instrType";
	private static final String SYMBOL = "symbol";
	private static final String MULTIPLIER = "multiplier";
	private static final String TICK_SIZE = "tickSize";
	
	private Instrument instrument;
	private IPropertyDescriptor propertyDescriptors[];
	
	public InstrumentPropertySource(Instrument def) {
		instrument = def;
	}
	
	public Instrument getInstrument() {
		return instrument;
	}

	public Object getEditableValue() {
		return null;
	}

	public IPropertyDescriptor[] getPropertyDescriptors() {
		if( propertyDescriptors == null ) {
			PropertyDescriptor symbolPD = new PropertyDescriptor(SYMBOL, "Symbol");
			symbolPD.setDescription("Ticker symbol for the instrument.");
			
			PropertyDescriptor typePD = new PropertyDescriptor(INSTR_TYPE, "Instrument type");
			
			ComboBoxPropertyDescriptor exchangePD = new ComboBoxPropertyDescriptor(
					EXCHANGE, "Exchange", Instrument.EXCHANGES);
			exchangePD.setDescription("Exchange");
			
			PropertyDescriptor currencyPD = new TextPropertyDescriptor(CURRENCY, "Currency");
			
			TextPropertyDescriptor tickSizePD = new TextPropertyDescriptor(TICK_SIZE, "Tick size");
			
			TextPropertyDescriptor multiplierPD = new TextPropertyDescriptor(MULTIPLIER, "Multiplier");
			
			propertyDescriptors = new IPropertyDescriptor[] {
					symbolPD,
					typePD,
					exchangePD,
					currencyPD,
					tickSizePD,
					multiplierPD
			};
		}
		return propertyDescriptors;
	}

	public Object getPropertyValue(Object id) {
		if(id.equals(SYMBOL)) {
			return instrument.getSymbol();
		} else if( id.equals(INSTR_TYPE)) {
			return instrument.getInstrumentType().toString();
		} else if( id.equals(EXCHANGE)) {
			String exchange = instrument.getExchange();
			for( int ret = 0; ret < Instrument.EXCHANGES.length; ret++ ) {
				if( Instrument.EXCHANGES[ret].equals(exchange)) {
					return ret;
				}
			}
			return 0;
			//return instrument.getExchange();
			
		} else if( id.equals(CURRENCY)) {
			return instrument.getCurrency();
		} else if( id.equals(TICK_SIZE)) {
			return "" + instrument.getTickSize();
		} else if( id.equals(MULTIPLIER)) {
			return "" + instrument.getMultiplier();
		}
		return null;
	}

	public boolean isPropertySet(Object id) {
		return true;
	}

	public void resetPropertyValue(Object id) {
		// TODO: support reset?
	}

	public void setPropertyValue(Object id, Object value) {
		boolean isDirty = false;
		if( id.equals(EXCHANGE)) {
			instrument.setExchange(Instrument.EXCHANGES[(Integer)value]);
//			instrument.setExchange((String)value);
			isDirty = true;
		} else if( id.equals(CURRENCY)) {
			instrument.setCurrency((String)value);
			isDirty = true;
		} else if( id.equals(TICK_SIZE)) {
			try {
				instrument.setTickSize(Double.parseDouble((String)value));
				isDirty = true;
			} catch( Exception e) {
			}
		} else if( id.equals(MULTIPLIER)) {
			try {
				instrument.setMultiplier(Integer.parseInt((String)value));
				isDirty = true;
			} catch( Exception e ) {
			}
		}
		if( isDirty ) {
			PlatformDAO.updateInstrument(instrument);
		}
	}

}
