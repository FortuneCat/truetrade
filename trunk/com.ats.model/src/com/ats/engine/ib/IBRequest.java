package com.ats.engine.ib;

import java.util.HashMap;
import java.util.Map;

/**
 * Base class for all IB requests to facilitate common error handling
 * and notification
 * 
 * @author Adrian
 *
 */
public abstract class IBRequest {
	
	public enum RequestState {
		building,
		transmitted,
		processing,
		success,
		error
	}
	
	private static final Map<Integer, IBRequest> requests = new HashMap<Integer, IBRequest>();
	
	private final int id;
	private RequestState state = RequestState.building;
	private RequestListener listener;
	private int errorCode;
	private String errorMessage;
	
	public IBRequest(RequestListener listener) {
		this.listener = listener;
		id = IBHelper.getInstance().getNextId();
	}
	
	public final boolean isComplete() {
		return state == RequestState.success || state == RequestState.error;
	}
	
	@Override
	public boolean equals(Object o) {
		if( o instanceof IBRequest ) {
			IBRequest that = (IBRequest)o;
			return that.id == this.id && that.getClass().equals(this.getClass());
		}
		return false;
	}
	
	public final void setSuccess() {
		this.state = RequestState.success;
		notifyListener();
	}
	public final void setError() {
		this.state = RequestState.error;
		notifyListener();
	}
	public final void setError(int code, String message) {
		this.state = RequestState.error;
		this.errorCode = code;
		this.errorMessage = message;
		notifyListener();
	}
	public final void setProcessing() {
		if( this.state != RequestState.processing ) {
			this.state = RequestState.processing;
			notifyListener();
		}
	}
	
	public static IBRequest getRequest(int id) {
		return requests.get(id);
	}
	
	public final void sendRequest() {
		requests.put(id, this);
		doRequest(IBHelper.getInstance());
		state = RequestState.transmitted;
	}
	
	/**
	 * rally the parameters and transmit the request
	 * @param helper
	 */
	protected abstract void doRequest(IBHelper helper); 
	
	protected void notifyListener() {
		if( listener != null )
			listener.requestChanged(this);
	}

	public int getId() {
		return id;
	}

	public RequestState getState() {
		return state;
	}

	public void setState(RequestState state) {
		this.state = state;
	}

	public int getErrorCode() {
		return errorCode;
	}

	public void setErrorCode(int errorCode) {
		this.errorCode = errorCode;
	}

	public String getErrorMessage() {
		return errorMessage;
	}

	public void setErrorMessage(String errorMessage) {
		this.errorMessage = errorMessage;
	}

}
