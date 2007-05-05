package com.ats.engine;

import com.ats.platform.Bar;
import com.ats.platform.Trade;

public interface TickListener {
    public void onTrade(Trade trade);

    public void onBar(Bar bar);

    public void onBarOpen(Bar bar);
}
