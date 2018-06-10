package de.may.ac;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.temporal.ChronoField;
import java.util.List;

import javax.imageio.ImageIO;
import javax.swing.JFrame;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import de.may.ac.model.ImageInfo;

@Component
public class ImageDisplay extends JFrame {

	private static final long serialVersionUID = 1L;

	@Autowired
	private ImagesInfoProvider imagesController;

	private ImageInfo imgInfo;

	private Image img;

	private Image nextImg;

	private int imageNumber;

	private long msCalc;

	private long msPassed;

	private long time;

	public ImageDisplay() {
		super("ImageViewer");
		initUI();
	}

	/**
	 * Init the UI with a full screen window and black background, the only
	 * component shows the embedded buffered image
	 */
	private void initUI() {
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
				super.paint(g);
				g.setColor(Color.black);
				g.fillRect(0, 0, getWidth(), getHeight());
				if (img != null) {
					int x = (getWidth() - img.getWidth(null)) / 2;
					int y = (getHeight() - img.getHeight(null)) / 2;
					g.drawImage(img, x, y, img.getWidth(null), img.getHeight(null), this);
				}
				g.setColor(Color.black);
				Graphics2D g2d = (Graphics2D) g;
				g2d.drawString("imageNumber: " + imageNumber + "\r\nmsCalc: " + msCalc + "\r\nmsPassed: " + msPassed
						+ "\r\ntime:" + time, 40, 200);
			}
		});
	}

	@Scheduled(fixedDelay = 100)
	public void updateImage() throws IOException {
		List<ImageInfo> imagesInfos = imagesController.imageInfos();
		this.imageNumber = calcCurrentImage(imagesInfos);
		if (imgInfo == null || imgInfo.getId() != imagesInfos.get(imageNumber).getId()) {
			// next image needs to be loaded to Buffer, current calculated image needs to be
			// shown

			// show current image, which is already in Buffer
			imgInfo = imagesInfos.get(imageNumber);
			img = nextImg;
			repaint();

			// load next image into Buffer
			nextImg = null;
			int nextImgNumber = (imageNumber + 1) % imagesInfos.size();
			byte[] imageData = imagesController.image(imagesInfos.get(nextImgNumber).getId());
			BufferedImage temp = ImageIO.read(new ByteArrayInputStream(imageData));

			double factor = Math.min(((double) getHeight()) / temp.getHeight(),
					((double) getWidth()) / temp.getHeight());
			int width = (int) (temp.getWidth() * factor);
			int height = (int) (temp.getHeight() * factor);
			nextImg = temp.getScaledInstance(width, height, Image.SCALE_SMOOTH);
		}
	}

	/**
	 * We have one image in the cache. Therefore we need to do the calculation based
	 * on the time for the next image.
	 * 
	 * @return number of current image
	 */
	private int calcCurrentImage(List<ImageInfo> imagesInfos) {
		long msRemaining = LocalDateTime.now().getLong(ChronoField.MILLI_OF_DAY);
		long msOfFullPass = imagesInfos.stream()//
				.mapToLong(ImageInfo::getTimeout)//
				.sum();
		msRemaining %= msOfFullPass;

		for (int i = 0; i < imagesInfos.size(); i++) {
			if (imagesInfos.get(i).getTimeout() >= msRemaining) {
				System.out.println("Current image: " + msRemaining);
				return i;
			}
			msRemaining -= imagesInfos.get(i).getTimeout();
		}

		throw new IllegalStateException("Could not determine, what the current image should be.");
	}
}
