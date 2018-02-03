package ch.unifr.diva.services;

import ch.unifr.diva.services.returnTypes.DivaServicesResponse;
import ch.unifr.diva.services.returnTypes.PointHighlighter;
import ch.unifr.diva.services.returnTypes.PolygonHighlighter;
import ch.unifr.diva.services.returnTypes.RectangleHighlighter;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import org.json.JSONArray;
import org.json.JSONObject;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.lang.reflect.Type;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.*;
import java.util.List;

/**
 * DivaServices Communicator.
 */
public class DivaServicesCommunicator {

	private String serverUrl;
	private HttpRequest httpRequest;

	/**
	 * Initialize a new DivaServicesCommunicator class
	 *
	 * @param serverUrl base url to use (e.g. http://divaservices.unifr.ch)
	 */
	public DivaServicesCommunicator(String serverUrl) {
		this.serverUrl = serverUrl;
		this.httpRequest = new HttpRequest();
	}

	/**
	 * uploads an image to the server
	 *
	 * @param image the image to upload
	 * @return the md5Hash to use in future requests
	 */
	public String uploadImage(BufferedImage image) {
		if (!checkImageOnServer(image)) {
			String base64Image = ImageEncoding.encodeToBase64(image);
			Map<String, Object> highlighter = new HashMap();

			JSONObject request = new JSONObject();
			JSONObject high = new JSONObject(highlighter);
			JSONObject inputs = new JSONObject();
			request.put("highlighter", high);
			request.put("inputs", inputs);
			request.put("image", base64Image);
			JSONObject result = HttpRequest.executePost(serverUrl + "/upload", request);
			return result.getString("md5");
		} else {
			return ImageEncoding.encodeToMd5(image);
		}

	}

	/**
	 * run text extraction
	 *
	 * @param url url to the image on the Divaservices Server
	 * @return
	 */
	public DivaServicesResponse runOcropyTextExtraction(String url) {
		Map<String, Object> highlighter = new HashMap();
		JSONObject request = new JSONObject();
		JSONObject high = new JSONObject(highlighter);
		JSONObject inputs = new JSONObject();
		inputs.put("languageModel", "English");
		request.put("highlighter", high);
		request.put("inputs", inputs);
		request.put("url", url);
		JSONObject result = HttpRequest.executePost(serverUrl + "/ocropy/recognize", request);
		Map<String, Object> output = extractOutput(result.getJSONObject("output"));
		return new DivaServicesResponse(null, output, null);

	}

	/**
	 * @param image
	 * @param requireOutputImage
	 * @return
	 */
	public DivaServicesResponse runOtsuBinarization(BufferedImage image, boolean requireOutputImage) {
		JSONObject request = createImageOnlyRequest(image, requireOutputImage);
		JSONObject result = HttpRequest.executePost(serverUrl + "/imageanalysis/binarization/otsu", request);
		String resImage = (String) result.get("image");
		return new DivaServicesResponse(ImageEncoding.decodeBas64(resImage), null, null);
	}

	/**
	 * Extracts interest points from an image
	 *
	 * @param image the image to extract points from
	 * @param detector the detector to use
	 * @param blurSigma the amount of blur applied
	 * @param numScales the number of scales
	 * @param numOctaves the number of octaves
	 * @param threshold the threshold
	 * @param maxFeaturesPerScale the maximal number of features to use
	 * @return A list of points, representing the interest points
	 */
	public DivaServicesResponse runMultiScaleInterestPointDetection(BufferedImage image, String detector, float blurSigma, int numScales, int numOctaves, float threshold, int maxFeaturesPerScale) {
		DecimalFormat df = new DecimalFormat("0", DecimalFormatSymbols.getInstance(Locale.ENGLISH));
		df.setMaximumFractionDigits(10);

		JSONObject request = new JSONObject();
		JSONObject high = new JSONObject();
		JSONObject inputs = new JSONObject();
		inputs.put("detector", detector);
		inputs.put("blurSigma", blurSigma);
		inputs.put("numScales", numScales);
		inputs.put("numOctaves", numOctaves);
		inputs.put("threshold", df.format(threshold));
		inputs.put("maxFeaturesPerScale", maxFeaturesPerScale);

		request.put("highlighter", high);
		request.put("inputs", inputs);
		addImageToRequest(image, request);
		//System.out.println(request.toString());
		JSONObject result = HttpRequest.executePost(serverUrl + "/ipd/multiscale", request);
		return new DivaServicesResponse(null, null, extractPoints(result));
	}

	/**
	 * @param image
	 * @param rectangle
	 * @return A list of polygons, each representing a text line
	 */
	public DivaServicesResponse runHistogramTextLineExtraction(BufferedImage image, Rectangle rectangle) {
		Map<String, Object> highlighter = new HashMap();
		highlighter.put("segments", prepareRectangle(rectangle));
		highlighter.put("closed", true);
		highlighter.put("type", "rectangle");

		JSONObject request = new JSONObject();
		JSONObject high = new JSONObject(highlighter);
		JSONObject inputs = new JSONObject();
		request.put("highlighter", high);
		request.put("inputs", inputs);

		addImageToRequest(image, request);
		JSONObject result = HttpRequest.executePost(serverUrl + "/segmentation/textline/hist", request);
		return new DivaServicesResponse(null, null, extractRectangles(result));
	}

	/**
	 * @param image
	 * @param requireOutputImage
	 * @return
	 */
	public DivaServicesResponse runSauvolaBinarization(BufferedImage image, boolean requireOutputImage) {
		JSONObject request = createImageOnlyRequest(image, requireOutputImage);
		JSONObject result = HttpRequest.executePost(serverUrl + "/imageanalysis/binarization/sauvola", request);
		String resImage = (String) result.get("image");
		return new DivaServicesResponse(ImageEncoding.decodeBas64(resImage), null, null);
	}

	/**
	 * @param image
	 * @param rectangle
	 * @param requireOutputImage
	 * @return
	 */
	public DivaServicesResponse runSeamCarvingTextlineExtraction(BufferedImage image, Rectangle rectangle, boolean requireOutputImage) {
		Map<String, Object> highlighter = new HashMap();
		highlighter.put("segments", prepareRectangle(rectangle));
		highlighter.put("closed", true);
		highlighter.put("type", "rectangle");

		JSONObject request = new JSONObject();
		JSONObject high = new JSONObject(highlighter);
		JSONObject inputs = new JSONObject();
		request.put("highlighter", high);
		request.put("inputs", inputs);
		request.put("requireOutputImage", requireOutputImage);
		addImageToRequest(image, request);
		JSONObject result = HttpRequest.executePost(serverUrl + "/segmentation/textline/seam", request);
		PolygonHighlighter polygons = extractPolygons(result);
		return new DivaServicesResponse(null, null, polygons);
	}

	/**
	 * run a canny edge detection algorithm
	 *
	 * @param image input image
	 * @param requireOutputImage
	 * @return result image
	 */
	public DivaServicesResponse runCannyEdgeDetection(BufferedImage image, boolean requireOutputImage) {
		JSONObject request = createImageOnlyRequest(image, requireOutputImage);
		JSONObject result = HttpRequest.executePost(serverUrl + "/imageanalysis/edge/canny", request);
		String resImage = (String) result.get("image");
		return new DivaServicesResponse(ImageEncoding.decodeBas64(resImage), null, null);
	}

	/**
	 * run simple histogram enhancement
	 *
	 * @param image input image
	 * @param requireOutputImage
	 * @return output image
	 */
	public DivaServicesResponse runHistogramEnhancement(BufferedImage image, boolean requireOutputImage) {
		JSONObject request = createImageOnlyRequest(image, requireOutputImage);
		JSONObject result = HttpRequest.executePost(serverUrl + "/imageanalysis/enhancement/histogram", request);
		String resImage = (String) result.get("image");
		return new DivaServicesResponse(ImageEncoding.decodeBas64(resImage), null, null);
	}

	/**
	 * Image enhancement using Laplacian Sharpening
	 *
	 * @param image input image
	 * @param sharpLevel sharpening Level (4 or 8)
	 * @param requireOutputImage
	 * @return output image
	 */
	public DivaServicesResponse runLaplacianSharpening(BufferedImage image, int sharpLevel, boolean requireOutputImage) {
		JSONObject request = new JSONObject();
		JSONObject high = new JSONObject();
		JSONObject inputs = new JSONObject();
		inputs.put("sharpLevel", sharpLevel);
		request.put("highlighter", high);
		request.put("inputs", inputs);
		request.put("requireOutputImage", requireOutputImage);
		addImageToRequest(image, request);
		JSONObject result = HttpRequest.executePost(serverUrl + "/imageanalysis/enhancement/sharpen", request);
		String resImage = (String) result.get("image");
		return new DivaServicesResponse(ImageEncoding.decodeBas64(resImage), null, null);
	}

	/**
	 * runs the binarization algorithm of ocropy
	 * (https://github.com/tmbdev/ocropy)
	 *
	 * @param image input image
	 * @param requireOutputImage
	 * @return the binarized output image
	 */
	public DivaServicesResponse runOcropyBinarization(BufferedImage image, boolean requireOutputImage) {
		JSONObject request = createImageOnlyRequest(image, requireOutputImage);
		JSONObject result = HttpRequest.executePost(serverUrl + "/ocropy/binarization", request);
		String resImage = (String) result.get("image");
		return new DivaServicesResponse(ImageEncoding.decodeBas64(resImage), null, null);
	}

	/**
	 * run the page segmentation algorithm of ocropy
	 * (https://github.com/tmbdev/ocropy)
	 *
	 * @param image input image
	 * @param requireOutputImage
	 * @return
	 */
	public DivaServicesResponse runOcropyPageSegmentation(BufferedImage image, boolean requireOutputImage) {
		JSONObject request = createImageOnlyRequest(image, requireOutputImage);
		JSONObject result = HttpRequest.executePost(serverUrl + "/ocropy/pageseg", request);
		//extract output
		Map<String, Object> output = extractOutput(result.getJSONObject("output"));
		//extract image
		String resImage = (String) result.get("image");
		BufferedImage resultImage = ImageEncoding.decodeBas64(resImage);

		return new DivaServicesResponse(resultImage, output, null);
	}

	/**
	 * extracts variable output from the "output" field into a Map for easier
	 * processing
	 *
	 * @param output
	 * @return
	 */
	private Map<String, Object> extractOutput(JSONObject output) {
		Gson gson = new Gson();
		Type type = new TypeToken<TreeMap<String, Object>>() {
		}.getType();
		Map myMap = gson.fromJson(output.toString(), type);
		return myMap;
	}

	/**
	 * Creates the JSON payload for a request that requires only an image and no
	 * parameters
	 *
	 * @param image the input image
	 * @return the JSON object to be sent to the server
	 */
	private JSONObject createImageOnlyRequest(BufferedImage image, boolean requireOutputImage) {
		Map<String, Object> highlighter = new HashMap();
		JSONObject request = new JSONObject();
		JSONObject high = new JSONObject(highlighter);
		JSONObject inputs = new JSONObject();
		request.put("highlighter", high);
		request.put("inputs", inputs);
		request.put("requireOutputImage", requireOutputImage);
		addImageToRequest(image, request);
		return request;
	}

	/**
	 * converts a Rectangle into a List<int[]> for sending it as JSON
	 *
	 * @param rectangle the rectangle
	 * @return a List<int[]>
	 */
	private List<int[]> prepareRectangle(Rectangle rectangle) {
		List<int[]> points = new ArrayList<>();
		//top left
		points.add(new int[]{rectangle.x, rectangle.y});
		//bottom left
		points.add(new int[]{rectangle.x, rectangle.y + rectangle.height});
		//bottom right
		points.add(new int[]{rectangle.x + rectangle.width, rectangle.y + rectangle.height});
		//top right
		points.add(new int[]{rectangle.x + rectangle.width, rectangle.y});
		return points;

	}

	/**
	 * Creates polygons from polygons as "lines" in DivaServices
	 *
	 * @param result the JSON result object from DIVAServices
	 * @return a list of polygons
	 */
	private PolygonHighlighter extractPolygons(JSONObject result) {
		JSONArray highlighters = result.getJSONArray("highlighters");
		List<Polygon> polygons = new ArrayList<>();
		for (int i = 0; i < highlighters.length(); i++) {
			JSONObject line = highlighters.getJSONObject(i).getJSONObject("line");
			JSONArray segments = line.getJSONArray("segments");
			Polygon polygon = new Polygon();
			for (int j = 0; j < segments.length(); j++) {
				JSONArray coordinates = segments.getJSONArray(j);
				polygon.addPoint(coordinates.getInt(0), coordinates.getInt(1));
			}
			polygons.add(polygon);
		}
		return new PolygonHighlighter(polygons);
	}

	/**
	 * Creates a list of rectangles from "rectangles" returned from DIVAServices
	 *
	 * @param result the JSONObject returned from DivaServices
	 * @return A list of rectangles
	 */
	private RectangleHighlighter extractRectangles(JSONObject result) {
		JSONArray highlighters = result.getJSONArray("highlighters");
		List<Rectangle> rectangles = new ArrayList<>();
		for (int i = 0; i < highlighters.length(); i++) {
			JSONObject line = highlighters.getJSONObject(i).getJSONObject("rectangle");
			JSONArray segments = line.getJSONArray("segments");
			//get top left point
			JSONArray topLeft = segments.getJSONArray(0);
			JSONArray bottomRight = segments.getJSONArray(2);
			Rectangle rectangle = new Rectangle(topLeft.getInt(0), topLeft.getInt(1), bottomRight.getInt(0) - topLeft.getInt(0), bottomRight.getInt(1) - topLeft.getInt(1));
			rectangles.add(rectangle);
		}
		return new RectangleHighlighter(rectangles);
	}

	private PointHighlighter extractPoints(JSONObject result) {
		JSONArray highlighters = result.getJSONArray("highlighters");
		List<Point> points = new ArrayList<>();
		for (int i = 0; i < highlighters.length(); i++) {
			JSONObject line = highlighters.getJSONObject(i).getJSONObject("point");
			JSONArray position = line.getJSONArray("position");
			//get top left point
			points.add(new Point(position.getInt(0), position.getInt(1)));
		}
		return new PointHighlighter(points);
	}

	/**
	 * <p>
	 * Checks if an image is available on the server
	 *
	 * @param image The image
	 * @return True if the image is already saved on the server, false otherwise
	 */
	private boolean checkImageOnServer(BufferedImage image) {
		String md5 = ImageEncoding.encodeToMd5(image);
		String url = "http://divaservices.unifr.ch/api/v1/image/" + md5;
		JSONObject response = HttpRequest.executeGet(url);
		return response.getBoolean("imageAvailable");
	}

	/**
	 * checks if an image is available on the server and responds with the
	 * correct JSONObject for the request payload
	 *
	 * @param image
	 * @return
	 */
	private JSONObject checkImage(BufferedImage image) {
		JSONObject result = new JSONObject();
		if (!checkImageOnServer(image)) {
			result.put("image", ImageEncoding.encodeToBase64(image));
		} else {
			result.put("md5Image", ImageEncoding.encodeToMd5(image));
		}
		return result;
	}

	/**
	 * Adds an image to the JSON request It will check wheter the image is
	 * available on the server or not
	 *
	 * @param image the input image
	 * @param request the request object where the image will be added
	 */
	private void addImageToRequest(BufferedImage image, JSONObject request) {
		JSONObject imageObj = checkImage(image);
		for (String key : imageObj.keySet()) {
			request.put(key, imageObj.getString(key));
		}
	}

	private void logJsonObject(JSONObject object) {
		System.out.println(object.toString());
	}
}
