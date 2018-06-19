package de.may.ac.model;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.GraphicsConfiguration;
import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;
import java.awt.Image;
import java.awt.Transparency;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.imageio.ImageIO;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import lombok.extern.log4j.Log4j2;

@Data
@EqualsAndHashCode(exclude = "image")
@ToString(exclude = "image")
@Log4j2
public class ImageInfo {

	public static final Pattern FILENAME_PATTERN = Pattern.compile("(\\d+)(-(\\d+))?\\.png");

	private int id;

	private int timeout;

	private Path file;

	/** required for equals comparison */
	private String hashCode;

	private BufferedImage image;

	public ImageInfo(Path file, String hash, int defaultTimeout) {
		this.file = file;
		Matcher matcher = FILENAME_PATTERN.matcher(file.getFileName().toString());
		if (!matcher.matches()) {
			log.warn(() -> "File schema does not match, pattern should be 00.png or 00-00.png, but was: "
					+ file.getFileName().toString() + "");
			throw new IllegalArgumentException("File schema does not match!");
		}
		id = Integer.parseInt(matcher.group(1));
		this.hashCode = hash;
		timeout = Optional.ofNullable(matcher.group(3))//
				.map(Integer::parseInt) //
				.map(i -> i * 1000)//
				.orElse(defaultTimeout);
		log.trace(() -> "ImageInfo object created with values: " + this.toString());
	}

	/**
	 * Preloads this image in a display compatible BufferedImage for rendering speed
	 * optimized
	 * 
	 * @param width
	 *            the width of the target window
	 * @param height
	 *            the height of the target window
	 * @throws IOException
	 *             if the image can not be read
	 */
	public void loadImage(int width, int height) throws IOException {
		log.trace(() -> "loadImage(width: " + width + ", height: " + height + ") for " + this.toString() + " started");
		if (image != null && image.getWidth() == width && image.getHeight() == height) {
			// image already loaded, do not load again
			log.trace(() -> "loadImage(width: " + width + ", height: " + height + ") for " + this.toString()
					+ " finished in first return (image was already loaded)");
			return;
		}
		byte[] imageData = Files.readAllBytes(file);
		BufferedImage temp = ImageIO.read(new ByteArrayInputStream(imageData));

		// Create a buffered image which can be rendered in paint method very fast
		GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
		GraphicsDevice gs = ge.getDefaultScreenDevice();
		GraphicsConfiguration config = gs.getDefaultConfiguration();
		image = config.createCompatibleImage(width, height, Transparency.TRANSLUCENT);

		// calculate the stretch factor to have it full screen size
		double factor = Math.min(((double) height) / temp.getHeight(), ((double) width) / temp.getHeight());
		int stretchedWidth = (int) (temp.getWidth() * factor);
		int stretchedHeight = (int) (temp.getHeight() * factor);
		Image nextImg = temp.getScaledInstance(stretchedWidth, stretchedHeight, Image.SCALE_SMOOTH);

		// draw the stretched image with black background
		Graphics g = image.getGraphics();
		g.setColor(Color.BLACK);
		g.fillRect(0, 0, image.getWidth(), image.getHeight());
		int x = (width - nextImg.getWidth(null)) / 2;
		int y = (height - nextImg.getHeight(null)) / 2;
		g.drawImage(nextImg, x, y, stretchedWidth, stretchedHeight, null);
		log.trace(() -> "loadImage(width: " + width + ", height: " + height + ") for " + this.toString() + " finished");
	}
}
