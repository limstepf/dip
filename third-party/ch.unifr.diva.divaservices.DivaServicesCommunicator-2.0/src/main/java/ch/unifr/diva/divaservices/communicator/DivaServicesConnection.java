package ch.unifr.diva.divaservices.communicator;




/**
 * @author Marcel Würsch
 *         marcel.wuersch@unifr.ch
 *         http://diuf.unifr.ch/main/diva/home/people/marcel-w%C3%BCrsch
 *         Created on: 18.10.2016.
 */
public class DivaServicesConnection {
    private int checkInterval;
    private String serverUrl;

    /**
     * Initialize a new DivaServicesConnection class
     *
     * @param serverUrl     base url to use (e.g. http://divaservices.unifr.ch)
     * @param checkInterval How often to check for computed results on the server
     */
    public DivaServicesConnection(String serverUrl, int checkInterval){
        this.checkInterval = checkInterval;
        this.serverUrl = serverUrl;
    }

    public int getCheckInterval() {
        return checkInterval;
    }

    public String getServerUrl() {
        return serverUrl;
    }


}
