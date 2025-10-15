package com.example.chat.message;

import com.example.chat.storage.repository.ChatChannelRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;

@SpringBootApplication(scanBasePackages = "com.example.chat")
public class ChatMessageServerApplication {

	@Autowired
	private ChatChannelRepository chatChannelRepository;

	public static void main(String[] args) {
		SpringApplication.run(ChatMessageServerApplication.class, args);
	}

	@EventListener(ApplicationReadyEvent.class)
	public void init() {
		chatChannelRepository.findAll().forEach(channel -> {
			System.out.println("Channel: " + channel.getId());
		});
	}
}