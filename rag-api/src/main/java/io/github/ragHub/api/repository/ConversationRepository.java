package io.github.ragHub.api.repository;

import io.github.ragHub.api.entity.Conversation;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ConversationRepository extends JpaRepository<Conversation, String> {}
