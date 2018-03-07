package ch.unifr.diva.divaservices.communicator.request;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * @author Marcel Würsch
 *         marcel.wuersch@unifr.ch
 *         http://diuf.unifr.ch/main/diva/home/people/marcel-w%C3%BCrsch
 *         Created on: 30.03.2016.
 */
public class DivaServicesRequest {
    private Optional<DivaCollection> collection;
    private Optional<DivaImage> image;
    private Map<String, String> data;

    public DivaServicesRequest() {
        data = new HashMap<>();
    }

    /**
     * Create a request using a collection
     *
     * @param collection the collection to use
     */
    public DivaServicesRequest(DivaCollection collection) {
        this.collection = Optional.of(collection);
        this.image = Optional.empty();
        data = new HashMap<>();
    }

    public DivaServicesRequest(DivaImage image) {
        this.collection = Optional.empty();
        this.image = Optional.of(image);
        data = new HashMap<>();
    }

    public Optional<DivaImage> getImage() {
        return image;
    }

    public Optional<DivaCollection> getCollection() {
        return collection;
    }

    public void addDataValue(String key, String value) {
        data.put(key, value);
    }

    public Map<String, String> getData() {
        return data;
    }
}
