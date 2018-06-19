package de.may.ac;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.temporal.ChronoField;
import java.util.List;
import java.util.Objects;

import javax.swing.JFrame;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import de.may.ac.model.ImageInfo;
import lombok.extern.log4j.Log4j2;

@Component
@Log4j2
public class ImageDisplay extends JFrame {

	private static final long serialVersionUID = 1L;

	@Autowired
	private ImagesInfoProvider imagesController;

	private ImageInfo imageInfo;

	public ImageDisplay() {
		super("Image kiosk");
		initUI();
	}

	/**
	 * Init the UI with a full screen window and black background, the only
	 * component shows the embedded buffered image
	 */
	private void initUI() {
		log.debug("Initializing UI");
		setExtendedState(JFrame.MAXIMIZED_BOTH);
		setUndecorated(true);
		setDefaultCloseOperation(EXIT_ON_CLOSE);
		GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
		GraphicsDevice gs = ge.getDefaultScreenDevice();
		gs.setFullScreenWindow(this);

		add(new java.awt.Component() {
			private static final long serialVersionUID = 1L;

			@Override
			public void paint(Graphics g) {
				log.trace(() -> "Repainting invoked");
				super.paint(g);

				g.setColor(Color.black);
				g.fillRect(0, 0, getWidth(), getHeight());
				if (imageInfo != null) {
					BufferedImage img = imageInfo.getImage();
					if (img != null) {
						int x = (getWidth() - img.getWidth(null)) / 2;
						int y = (getHeight() - img.getHeight(null)) / 2;
						g.drawImage(img, x, y, img.getWidth(null), img.getHeight(null), this);
					} else {
						log.warn(() -> "Image could not be retrieved from imageInfo, repainting was only black image");
					}
				} else {
					log.info(() -> "No imageInfo available, repainting was only black image");
				}
			}
		});
		log.debug(() -> "UI initialization finished");
	}

	/**
	 * Checks if the image needs to be updated and if yes, loads the next image.
	 * 
	 * @throws IOException
	 */
	@Scheduled(fixedDelay = 50)
	public void updateImage() throws IOException {
		log.trace(() -> "updateImage() started");
		List<ImageInfo> imagesInfos = imagesController.imageInfos();
		int imageNumber = calcCurrentImage(imagesInfos);
		log.trace(() -> "next image to show is: " + imageNumber);

		// Do not repaint, if not necessary
		ImageInfo imageInfo = imagesInfos.get(imageNumber);
		if (Objects.equals(this.imageInfo, imageInfo)) {
			log.trace(() -> "updateImage() finished  in first return clause: nothing has changed");
			return;
		}

		// If the image is not already loaded, load it now
		imageInfo.loadImage(this.getWidth(), this.getHeight());
		this.imageInfo = imageInfo;
		repaint();
		// Already load the next image to avoid having long load times when image
		// changes
		imagesInfos.get((imageNumber + 1) % imagesInfos.size()).loadImage(this.getWidth(), this.getHeight());
		log.trace(() -> "updateImage() finished");
	}

	/**
	 * Calculate, which image should be currently shown based on the time starting
	 * from midnight. If all displaying terminals have the exact same time and
	 * images, they should always show the exact same image.
	 * 
	 * @param imageInfos
	 *            infos about the images which can be displayed
	 * @return number of current image
	 */
	private int calcCurrentImage(List<ImageInfo> imageInfos) {
		long msRemaining = LocalDateTime.now().getLong(ChronoField.MILLI_OF_DAY);
		long msOfFullPass = imageInfos.stream()//
				.mapToLong(ImageInfo::getTimeout)//
				.sum();
		msRemaining %= msOfFullPass;

		for (int i = 0; i < imageInfos.size(); i++) {
			if (imageInfos.get(i).getTimeout() >= msRemaining) {
				return i;
			}
			msRemaining -= imageInfos.get(i).getTimeout();
		}

		throw new IllegalStateException("Could not determine, what the current image should be.");
	}
}
