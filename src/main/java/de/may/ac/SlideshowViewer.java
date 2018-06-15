package de.may.ac;

import java.awt.EventQueue;

import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class SlideshowViewer {
	public static void main(String[] args) {
		ConfigurableApplicationContext ctx = new SpringApplicationBuilder(SlideshowViewer.class)
	            .headless(false).run(args);

	    EventQueue.invokeLater(() -> {
	        ImageDisplay id = ctx.getBean(ImageDisplay.class);
	        id.setVisible(true);
	    });
	}
}
