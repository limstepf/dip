package ch.unifr.diva.divaservices.communicator.request;

import ch.unifr.diva.divaservices.communicator.DivaServicesConnection;
import ch.unifr.diva.divaservices.communicator.HttpRequest;
import ch.unifr.diva.divaservices.communicator.ImageEncoding;
import ch.unifr.diva.divaservices.communicator.exceptions.CollectionException;
import javafx.util.Pair;
import org.json.JSONArray;
import org.json.JSONObject;
import org.slf4j.Logger;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.List;

/**
 * @author Marcel Würsch
 * marcel.wuersch@unifr.ch
 * http://diuf.unifr.ch/main/diva/home/people/marcel-w%C3%BCrsch
 * Created on: 18.10.2016.
 */
public class DivaCollection {
    private String name;

    private DivaCollection(String name) {
        this.name = name;
    }


    public static DivaCollection createCollectionWithImages(List<Pair<String, BufferedImage>> images, DivaServicesConnection connection, Logger log) throws IOException {
        JSONObject request = new JSONObject();
        JSONArray jsonImages = new JSONArray();
        int i = 0;
        for (Pair<String, BufferedImage> image : images) {
            JSONObject jsonImage = new JSONObject();
            jsonImage.put("type", "image");
            jsonImage.put("value", ImageEncoding.encodeToBase64(image.getValue()));
            jsonImage.put("name", image.getKey().split("\\.")[0]);
            jsonImage.put("extension", image.getKey().split("\\.")[1]);
            jsonImages.put(jsonImage);
            i++;
        }
        request.put("files", jsonImages);
        JSONObject response = HttpRequest.executePost(connection.getServerUrl() + "/collections", request);
        String collection = response.getString("collection");
        String url = connection.getServerUrl() + "/collections/" + collection;
        log.info(response.toString(1));
        try {
            Thread.sleep(1000);
            JSONObject getResponse = HttpRequest.executeGet(url);
            while (!(getResponse.getInt("percentage") == 100)) {
                Thread.sleep(connection.getCheckInterval() * 1000);
                getResponse = HttpRequest.executeGet(url);
                log.info(getResponse.toString(1));
            }
        } catch (InterruptedException | IOException e) {
            e.printStackTrace();
        }

        return new DivaCollection(collection);
    }

    public static DivaCollection createCollectionByName(String name, DivaServicesConnection connection) throws CollectionException, IOException {
        JSONObject response = HttpRequest.executeGet(connection.getServerUrl() + "/collections/" + name);
        if (response.getInt("statusCode") == 200) {
            return new DivaCollection(name);
        } else {
            throw new CollectionException("Collection: " + name + " does not exists on the remote system");
        }
    }

    public String getName() {
        return name;
    }
}
