package io.github.ragHub.api.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import io.github.ragHub.api.entity.ConversationMessage;

@Entity
@Table(name = "conversations")
public class Conversation {
    @Id
    private String id;

    @Column(name = "created_at")
    private LocalDateTime createdAt = LocalDateTime.now();

    @Column(name = "updated_at")
    private LocalDateTime updatedAt = LocalDateTime.now();

    @OneToMany(mappedBy = "conversation", cascade = CascadeType.ALL, fetch = FetchType.LAZY, orphanRemoval = true)
    @OrderBy("id ASC")
    private List<ConversationMessage> messages = new ArrayList<>();

    public Conversation() {}
    public Conversation(String id) { this.id = id; }

    public String getId() { return id; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime t) { this.updatedAt = t; }
    public List<ConversationMessage> getMessages() { return messages; }
}
