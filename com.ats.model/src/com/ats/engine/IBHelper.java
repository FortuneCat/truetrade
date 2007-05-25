package com.ats.engine;

import java.text.ParseException;
import java.util.HashMap;
import java.util.Map;

import javax.swing.JOptionPane;

import org.apache.log4j.Logger;

import com.ats.platform.Instrument;
import com.ats.platform.BaseSystemException;
import com.ats.platform.Strategy;
import com.ats.utils.Utils;
import com.ib.client.Contract;
import com.ib.client.EClientSocket;
import com.ib.client.ExecutionFilter;
import com.ib.client.Order;

public class IBHelper {
	private static final Logger logger = Logger.getLogger(IBHelper.class);
	
    private String host = "";
    private int port = 7496, clientID = 0;

    private EClientSocket socket;
    private final Map<Integer, Strategy> strategies;
    protected static int strategyID, orderID;
    private int serverVersion;

	private String accountCode;
//    private static final IBWrapperAdapter wrapper = IBWrapperAdapter.getWrapper();
    
    private static final IBHelper instance = new IBHelper();
    


    private IBHelper() {
    	
        // maps IDs to strategies
        strategies = new HashMap<Integer, Strategy> ();
        // maps IDs to orders statuses
        //orders = new HashMap<Integer, OrderStatus> ();
    }
    
    public static IBHelper getInstance() {
    	return instance;
    }

//    public Map<Integer, OrderStatus> getOrders() {
//        return orders;
//    }
    
    private synchronized EClientSocket getSocket() {
    	if( socket == null || ! socket.isConnected()) {
    		try {
				connect();
			} catch (Exception e) {
				logger.error("Could not connect", e);
			}
    	}
    	return socket;
    }

    private void connect() throws ParseException, BaseSystemException {
        logger.info("Connecting to TWS");
        socket = new EClientSocket(IBWrapperAdapter.getWrapper());

        host = Utils.getPreferenceStore().getString(Utils.INTERACTIVE_BROKERS_HOST);
        port = Utils.getPreferenceStore().getInt(Utils.INTERACTIVE_BROKERS_PORT);
        clientID = Utils.getPreferenceStore().getInt(Utils.INTERACTIVE_BROKERS_CLIENTID);
        
        socket.eConnect(host, port, clientID);
        if (!socket.isConnected()) {
            throw new BaseSystemException("Could not connect to TWS. See log for details.");
        }

        // Make sure that system clock is the same as TWS clock
//        TimeSyncChecker.timeCheck(socket.TwsConnectionTime());

        // IB Log levels: 1=SYSTEM 2=ERROR 3=WARNING 4=INFORMATION 5=DETAIL
        socket.setServerLogLevel(2);
        socket.reqNewsBulletins(true);
        serverVersion = socket.serverVersion();

        logger.info("Connected to TWS");
    }

    public int getServerVersion() {
        return serverVersion;
    }


    public void disconnect() {
        if (socket != null && socket.isConnected()) {
            socket.cancelNewsBulletins();
            socket.eDisconnect();
        }
    }

    void reqMktData(int id, Instrument instrument) {
    	logger.info("Requesting market data for " + instrument + " with id " + id);
        try {
            getSocket().reqMktData(id, instrument.getContract(), null);
        } catch (Throwable t) {
            // Do not allow exceptions come back to the socket -- it will cause disconnects
            logger.error("Could not request market data", t);
        }
    }

    public void resubscribeToMarketData() {
        logger.info("Re-subscribing to market data.");
        try {
            for (Map.Entry mapEntry : strategies.entrySet()) {
                int strategyID = (Integer) mapEntry.getKey();
                Strategy strategy = (Strategy) mapEntry.getValue();
                Contract contract = strategy.getInstrument().getContract();
                getSocket().cancelMktData(strategyID);
                getSocket().reqMktData(strategyID, contract, null);
            }
        } catch (Throwable t) {
            // Do not allow exceptions come back to the socket -- it will cause disconnects
            logger.error("Could not resubscribe to market data", t);
        }
        logger.info("Re-subscribed to market data.");
    }

    public void requestExecutions() {
        try {
            logger.info("Requested executions.");
            getSocket().reqExecutions(new ExecutionFilter());
        } catch (Throwable t) {
            // Do not allow exceptions come back to the socket -- it will cause disconnects
            logger.error("Error during requestedExecutions()", t);
        }
    }

    public void cancelHistoricalData(int strategyID) {
        getSocket().cancelHistoricalData(strategyID);
    }

	void reqHistoricalData(int tickerId, Instrument instrument, String endDateTime, String durationStr, int barSizeSetting, String whatToShow, int useRTH, int formatDate) {
		getSocket().reqHistoricalData(tickerId, instrument.getContract(), endDateTime, durationStr, barSizeSetting, whatToShow, useRTH, formatDate);
	}

    public void setAccountCode(String accountCode) {
        this.accountCode = accountCode;
    }

    
    public void realAccountCheck() throws BaseSystemException {
        getSocket().reqAccountUpdates(true, "");
//
//        try {
//            synchronized (wrapper) {
//                while (accountCode == null) {
//                    wrapper.wait();
//                }
//            }
//        } catch (InterruptedException ie) {
//            throw new JSystemTraderException(ie);
//        }
//
//        getSocket().reqAccountUpdates(false, "");
//        if (!accountCode.startsWith("D")) {
//            String lineSep = System.getProperty("line.separator");
//            String warning = "Connected to the real (not simulated) IB account. Running " + JSystemTrader.APP_NAME +
//                             " against the real" + lineSep;
//            warning += "account may cause significant losses in your account. Are you sure you want to proceed?";
//            int response = JOptionPane.showConfirmDialog(null, warning, JSystemTrader.APP_NAME,
//                    JOptionPane.YES_NO_OPTION);
//            if (response == JOptionPane.NO_OPTION) {
//                disconnect();
//            }
//        }
    }

	public void placeOrder(int orderID2, Contract contract, Order order) {
		getSocket().placeOrder(orderID2, contract, order);
	}

	public void cancelOrder(int orderId2) {
		getSocket().cancelOrder(orderId2);
	}

}
