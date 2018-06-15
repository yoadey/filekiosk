package de.may.ac;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.stream.Collectors;

import org.apache.commons.io.FileUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import de.may.ac.model.ImageInfo;

@Component
public class ImagesInfoProvider {

	@Value("${slideshow.path}")
	private File rootPath;

	@Value("${slideshow.timeout.default:1000}")
	private int defaultTimeout;

	public byte[] image(int imageid) throws IOException {

		List<File> listFiles = new ArrayList<>(FileUtils.listFiles(rootPath, new String[] { "png" }, false));
		return listFiles.stream()//
				.filter(f -> ImageInfo.FILENAME_PATTERN.matcher(f.getName()).matches()) //
				.filter(f -> getImageNumber(f) == imageid) //
				.findFirst() //
				.map(t -> {
					try {
						return FileUtils.readFileToByteArray(t);
					} catch (IOException e) {
						return null;
					}
				}) //
				.orElseThrow(() -> new IllegalArgumentException("Image not found: " + imageid));
	}

	public List<ImageInfo> imageInfos() {
		List<File> listFiles = new ArrayList<>(FileUtils.listFiles(rootPath, new String[] { "png" }, false));
		return listFiles.stream()//
				.filter(f -> ImageInfo.FILENAME_PATTERN.matcher(f.getName()).matches())
				.sorted((f1, f2) -> getImageNumber(f1) - getImageNumber(f2))//
				.map(f -> new ImageInfo(f.getName(), defaultTimeout)) //
				.collect(Collectors.toCollection(ArrayList::new));
	}

	private int getImageNumber(File f) {
		Matcher matcher = ImageInfo.FILENAME_PATTERN.matcher(f.getName());
		if (matcher.matches()) {
			return Integer.parseInt(matcher.group(1));
		} else {
			return 0;
		}
	}
}
