package com.ats.client.views;

import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.swt.graphics.Device;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.ISharedImages;
import org.eclipse.ui.PlatformUI;

import com.ats.client.Activator;
import com.ats.engine.StrategyDefinition;
import com.ats.platform.Instrument;

public class TreeViewLabelProvider  extends LabelProvider {

    private Image enabledStrategy; // 
    private Image disabledStrategy; // = Activator.getImageDescriptor("/icons/strategy_disabled.gif").createImage();

    public TreeViewLabelProvider() {
    	try {
    		ClassLoader cl = TreeViewLabelProvider.class.getClassLoader();
    		enabledStrategy = new Image(Display.getDefault(), cl.getResourceAsStream("/icons/strategy_enabled.gif"));
    		disabledStrategy = new Image(Display.getDefault(), cl.getResourceAsStream("/icons/strategy_disabled.gif"));
    	} catch( Exception e) {
    		System.out.println("Could not load images: " + e);
    		e.printStackTrace();
    	}
    }
    
	public String getText(Object base) {
		Object obj = ((TreeObject)base).getObject();
		if( obj instanceof Instrument ) {
			Instrument instr = (Instrument)obj;
			if( instr.isForex() ) {
				return instr.getSymbol() + "." + instr.getCurrency();
			} else {
				return instr.getSymbol();
			}
		} else if( obj instanceof StrategyDefinition ) {
			StrategyDefinition def = ((StrategyDefinition)obj); 
			return def.getStrategyClass() != null ? def.getStrategyClass().getSimpleName() : "<undefined>";
		}
		return obj.toString();
	}
	public Image getImage(Object obj) {
		String imageKey = ISharedImages.IMG_OBJ_ELEMENT;
		if (obj instanceof TreeParent) {
			if( ((TreeParent)obj).getObject() instanceof StrategyDefinition ) {
				StrategyDefinition stdef = (StrategyDefinition)((TreeParent)obj).getObject();
				return stdef.isRuntime() ? enabledStrategy : disabledStrategy;
			}
		}
		return PlatformUI.getWorkbench().getSharedImages().getImage(imageKey);
	}

}
