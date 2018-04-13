package de.may.ac;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.Calendar;
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

	private BufferedImage img;

	private BufferedImage nextImg;

	public ImageDisplay() {
		super("ImageViewer");
		initUI();
	}

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
					double factor = Math.min(((double) getHeight()) / img.getHeight(),
							((double) getWidth()) / img.getHeight());
					int width = (int) (img.getWidth() * factor);
					int height = (int) (img.getHeight() * factor);
					int x = (getWidth() - width) / 2;
					int y = (getHeight() - height) / 2;
					g.drawImage(img, x, y, width, height, this);
				}
			}
		});
	}

	@Scheduled(fixedDelay = 100)
	public void updateImage() throws IOException {
		List<ImageInfo> imagesInfos = imagesController.imageInfos();
		Calendar date = Calendar.getInstance();
		date.set(Calendar.HOUR, 0);
		date.set(Calendar.MINUTE, 0);
		date.set(Calendar.SECOND, 0);
		date.set(Calendar.MILLISECOND, 0);
		long msPassed = System.currentTimeMillis() - date.getTimeInMillis();
		// set to half a second so it is clear, to which second it belongs
		long msCalc = 00;
		int imageNumber = -1;
		if (imagesInfos != null && !imagesInfos.isEmpty()) {
			while (msCalc < msPassed) {
				imageNumber++;
				imageNumber %= imagesInfos.size();
				ImageInfo info = imagesInfos.get(imageNumber);
				msCalc += info.getTimeout();
			}
			if (imgInfo == null || imgInfo.getId() != imagesInfos.get(imageNumber).getId()) {
				imgInfo = imagesInfos.get(imageNumber);
				repaint();
				img = nextImg;
				nextImg = null;
				byte[] imageData = imagesController.image(imagesInfos.get(imageNumber).getId());
				nextImg = ImageIO.read(new ByteArrayInputStream(imageData));
			}
		}
	}
}
