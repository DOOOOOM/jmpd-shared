package dooooom.jmpd.client;

import java.util.Map;

/*
 * This interface will be used by the client.
 * I haven't yet decided what class will implement it
 * (likely will be MainViewController although I may
 * just turn this into a class of its own that keeps
 * a reference to MVC)
 *
 * Its job is to take a response from the server and
 * decide what to do about it. This could include updating
 * the library,
 */
public interface ResponseController {
    /*
     * Called whenever a response is received from the server...
     * allows the implementation to do whatever it needs to with
     * this information
     */
    public void processResponse(Map<String,Object> request, Map<String,Object> response);

    /*
     * Method to call whenever the connection is established
     */
    public void onConnect();

    /*
     * Method to call whenever the connection is lost
     */
    public void onDisconnect();

    /*
     * Method to give general status information to the controller
     *
     * eg. "Reconnecting (5)"
     */
    public void giveStatusInformation(String s);
}
