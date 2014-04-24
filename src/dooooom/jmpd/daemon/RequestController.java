package dooooom.jmpd.daemon;

import java.util.Map;

public interface RequestController {
    public Map<String,Object> processRequest(Map<String,Object> request);
}
