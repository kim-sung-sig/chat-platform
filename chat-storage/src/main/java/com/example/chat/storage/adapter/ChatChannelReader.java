package com.example.chat.storage.adapter;

import com.example.chat.storage.entity.ChatChannel;
import com.example.chat.storage.repository.ChatChannelRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Component
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class ChatChannelReader {

	private final ChatChannelRepository chatChannelRepository;

	public List<ChatChannel> findAllChannels() {
		return chatChannelRepository.findAll();
	}
}