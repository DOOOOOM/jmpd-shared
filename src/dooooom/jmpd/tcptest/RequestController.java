package dooooom.jmpd.tcptest;

import java.util.Map;

public interface RequestController {
    public Map<String,String> processRequest(Map<String,String> request);
}
