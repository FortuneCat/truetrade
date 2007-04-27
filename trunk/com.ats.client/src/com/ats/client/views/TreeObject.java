package com.ats.client.views;

import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.ui.views.properties.IPropertySource;

import com.ats.engine.StrategyDefinition;
import com.ats.platform.Instrument;

public 	class TreeObject implements IAdaptable {
	private Object object;
	private TreeParent parent;
	
	public TreeObject(Object object) {
		this.object = object;
	}
	public Object getObject() {
		return object;
	}
	public void setParent(TreeParent parent) {
		this.parent = parent;
	}
	public TreeParent getParent() {
		return parent;
	}
	public Object getAdapter(Class adapter) {
		if( adapter == IPropertySource.class ) {
			if( object instanceof StrategyDefinition ) {
				return new StrategyPropertySource((StrategyDefinition)object);
			} else if( object instanceof Instrument ) {
				return new InstrumentPropertySource((Instrument)object);
			}
		}
		return null;
	}
}
