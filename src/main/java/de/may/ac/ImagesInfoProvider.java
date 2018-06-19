package de.may.ac;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;

import javax.annotation.PostConstruct;
import javax.xml.bind.DatatypeConverter;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import de.may.ac.model.ImageInfo;
import lombok.SneakyThrows;
import lombok.extern.log4j.Log4j2;

@Component
@Log4j2
public class ImagesInfoProvider {

	@Value("${slideshow.path}")
	private String rootPath;

	@Value("${slideshow.timeout.default:1000}")
	private int defaultTimeout;

	private Map<String, ImageInfo> cache = new HashMap<>();

	private List<ImageInfo> imageInfos;

	public List<ImageInfo> imageInfos() {
		return imageInfos;
	}

	@PostConstruct
	@Scheduled(fixedDelay = 1000)
	public void updateImages() throws IOException {
		log.trace(() -> "updateImages() started");
		imageInfos = Files.list(Paths.get(rootPath))//
				.filter(p -> p.toString().toLowerCase().endsWith(".png")) //
				.map(p -> cache.computeIfAbsent(hash(p), h -> new ImageInfo(p, h, defaultTimeout))) //
				.sorted((i1, i2) -> i1.getId() - i2.getId()) //
				.collect(Collectors.toCollection(ArrayList::new));

		if (imageInfos.size() != cache.size()) {
			log.debug(() -> "updateImges: Images have changed on disk, remove old images from cache");
			// Collect hashes which are not used anymore
			List<String> keysToRemove = cache.entrySet().stream() //
					.filter(e -> !imageInfos.contains(e.getValue())) //
					.map(Entry::getKey) //
					.collect(Collectors.toList());

			// Remove unused hashes
			keysToRemove.stream().forEach(k -> cache.remove(k));
		}
		log.trace(() -> "updateImages() finished");
	}

	@SneakyThrows
	private String hash(Path p) {
		MessageDigest digest = MessageDigest.getInstance("MD5");
		byte[] encodedHash = digest.digest(Files.readAllBytes(p));
		return DatatypeConverter.printHexBinary(encodedHash);
	}
}
