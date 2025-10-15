package com.example.chat.storage.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.ColumnDefault;

import java.time.OffsetDateTime;

@Getter
@Setter
@Entity
@Table(name = "ms_chat_room")
public class ChatChannel {
	@Id
	@GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "ms_chat_room_id_gen")
	@SequenceGenerator(name = "ms_chat_room_id_gen", sequenceName = "ms_chat_room_id_seq", allocationSize = 1)
	@Column(name = "id", nullable = false)
	private Long id;

	@Size(max = 50)
	@NotNull
	@Column(name = "room_type", nullable = false, length = 50)
	private String roomType;

	@jakarta.validation.constraints.NotNull
	@ColumnDefault("now()")
	@Column(name = "created_at", nullable = false)
	private OffsetDateTime createdAt;

}