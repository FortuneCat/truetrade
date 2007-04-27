package com.ats.platform;

public interface MessageListener {
    public void error(int id, int errorCode, String errorMsg);
    public void updateNewsBulletin(int msgId, int msgType, String message, String origExchange);
}
