package ch.unifr.diva.services;

import org.apache.commons.codec.binary.Base64;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * Created by lunactic on 22.09.15.
 */
public class ImageEncoding {

	/**
	 * Creates a Base64 encoding of an image
	 *
	 * @param image image
	 * @return the image as base64 encoded string
	 */
	public static String encodeToBase64(BufferedImage image) {
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		try {
			ImageIO.write(image, "png", baos);
			byte[] bytes = baos.toByteArray();
			return Base64.encodeBase64String(bytes);
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}

	public static String encodeToMd5(BufferedImage image) {
		ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
		try {
			ImageIO.write(image, "png", outputStream);
			byte[] data = outputStream.toByteArray();
			MessageDigest md = MessageDigest.getInstance("MD5");
			md.update(Base64.encodeBase64(data));
			byte[] hash = md.digest();
			return hexString(hash);
		} catch (IOException e) {
			e.printStackTrace();
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		}
		return "";
	}

	public static BufferedImage decodeBas64(String base64) {
		try {
			byte[] buffer = Base64.decodeBase64(base64);
			ByteArrayInputStream bais = new ByteArrayInputStream(buffer);
			return ImageIO.read(bais);
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}

	private static String hexString(byte[] bytes) {
		StringBuffer hexString = new StringBuffer();
		for (int i = 0; i < bytes.length; i++) {
			String hex = Integer.toHexString(0xff & bytes[i]);
			if (hex.length() == 1) {
				hexString.append('0');
			}
			hexString.append(hex);
		}
		return hexString.toString();
	}

}
