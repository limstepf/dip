import ch.unifr.diva.divaservices.communicator.DivaServicesAdmin;
import ch.unifr.diva.divaservices.communicator.DivaServicesConnection;
import ch.unifr.diva.divaservices.communicator.exceptions.*;
import ch.unifr.diva.divaservices.communicator.request.DivaCollection;
import javafx.util.Pair;
import org.apache.tika.mime.MimeTypeException;
import org.apache.tika.mime.MimeTypes;
import org.json.JSONObject;
import org.junit.jupiter.api.Test;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.net.URLConnection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * @author Marcel Würsch
 * marcel.wuersch@unifr.ch
 * http://diuf.unifr.ch/main/diva/home/people/marcel-w%C3%BCrsch
 * Created on: 5/18/2017.
 */
public class DivaServicesCommunicatorTest {

    @Test
    public void testSmallWorkflow() throws IOException, UserValueRequiredException, FileTypeConfusionException, UserParametersOverloadException, MethodNotAvailableException, IncompatibleValueException, ForgotKeyValueObjectException, CollectionException, MimeTypeException {
        DivaServicesConnection connection = new DivaServicesConnection("http://localhost:8080", 5);
        List<Pair<String, BufferedImage>> images = new LinkedList<>();

        File folder = new File("data");
        File[] imageFiles = folder.listFiles();
        for (File image : imageFiles) {
            if (image.isFile()) {
                images.add(new Pair<>(image.getName(), ImageIO.read(image)));
            }
        }

        DivaCollection collection = DivaCollection.createCollectionWithImages(images, connection,null);
        //DivaCollection collection = DivaCollection.createCollectionByName("circularnormalgaur", connection);

        Map<String, Object> parameters = new HashMap<>();
        for (Pair<String, BufferedImage> image : images) {
            parameters.put("inputImage", collection.getName() + "/" + image.getKey());
        }

        List<JSONObject> krakenResult = DivaServicesAdmin.runMethod("http://134.21.72.132:8080/binarization/krakenbinarization/1", parameters);
        parseResult(krakenResult, "krakenBinary");
        List<JSONObject> otsuResult = DivaServicesAdmin.runMethod("http://134.21.72.132:8080/binarization/otsubinarization/1", parameters);
        parseResult(otsuResult, "otsuBinary");

        for (double d = 0.1; d <= 0.5; d = d+0.1) {
            for (int i = 0; i < 30; i++) {
                Map<String, Object> sauvolaParams = new HashMap<>();
                sauvolaParams.put("inputImage", collection.getName() + "/" + images.get(0).getKey());
                sauvolaParams.put("radius", 2 + i);
                sauvolaParams.put("thres_tune", d);
                List<JSONObject> sauvolaResult = DivaServicesAdmin.runMethod("http://134.21.72.132:8080/binarization/sauvolabinarization/1", sauvolaParams);
                parseResult(sauvolaResult, "sauvolaBinary_" + d + "_" + (2 + i));
            }
        }
    }

    @Test
    public void testBinarization() throws MethodNotAvailableException, IOException, ForgotKeyValueObjectException, IncompatibleValueException, UserValueRequiredException, FileTypeConfusionException, UserParametersOverloadException, CollectionException {
        DivaServicesConnection connection = new DivaServicesConnection("http://localhost:8080", 5);
        List<Pair<String, BufferedImage>> images = new LinkedList<>();

        File folder = new File("/home/lunactic/Downloads/images/");
        File[] imageFiles = folder.listFiles();
        for (File image : imageFiles) {
            images.add(new Pair<>(image.getName(), ImageIO.read(image)));
        }

        //DivaCollection collection = DivaCollection.createCollectionByName("unwieldycoarsemonkey",connection);
        DivaCollection collection = DivaCollection.createCollectionWithImages(images, connection,null);

        Map<String, Object> parameters = new HashMap<>();
        for (Pair<String, BufferedImage> image : images) {
            parameters.put("inputImage", collection.getName() + "/" + image.getKey().split("\\.")[0]);
        }

        DivaServicesAdmin.runMethod("http://134.21.72.132:8080/binarization/krakenbinarization/1", parameters);

    }

    private void parseResult(List<JSONObject> result, String outputFileName) throws IOException, MimeTypeException {

        for (int i = 0; i < result.get(0).getJSONArray("output").length(); i++) {
            JSONObject resultFile = result.get(0).getJSONArray("output").getJSONObject(i);
            if (resultFile.keySet().toArray()[0].equals("file")) {
                JSONObject file = resultFile.getJSONObject("file");
                if (file.getJSONObject("options").getBoolean("visualization")) {
                    saveImage(new URL(file.getString("url")), outputFileName);
                }
            }
        }
    }

    private void saveImage(URL url, String outputFileName) throws MimeTypeException, IOException {
        BufferedImage saveImage = ImageIO.read(url);
        String ext = MimeTypes.getDefaultMimeTypes().forName(URLConnection.guessContentTypeFromName(url.getFile())).getExtension();
        ImageIO.write(saveImage, MimeTypes.getDefaultMimeTypes().forName(URLConnection.guessContentTypeFromName(url.getFile())).getAcronym(), new File("/home/lunactic/Downloads/images/results/" + outputFileName + ext));
    }
}
