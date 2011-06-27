package com.sqt001.ipcall.util;

public class NetworkFailException extends Exception {
    private static final long serialVersionUID = 1L;
    
    public NetworkFailException() {
        super();
    }
    
    public NetworkFailException(Exception e) {
        super(e);
    }
    
    public NetworkFailException(String reason) {
        super(reason);
    }
    
    public NetworkFailException(String reason, Exception e) {
        super(reason, e);
    }
    
    public String toString() {
        return "Network Error";
    }
}
