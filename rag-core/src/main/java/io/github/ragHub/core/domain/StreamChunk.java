package io.github.ragHub.core.domain;

import java.util.List;

public sealed interface StreamChunk permits StreamChunk.Token, StreamChunk.Done {
    record Token(String text) implements StreamChunk {}
    record Done(List<RagAnswer.SourceReference> sources, String provider, long latencyMs) implements StreamChunk {}
}
