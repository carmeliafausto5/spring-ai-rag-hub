package io.github.ragHub.retrieval.reranker;

import org.springframework.ai.document.Document;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.stream.Collectors;

@Component
public class KeywordReranker {

    public List<Document> rerank(String query, List<Document> docs) {
        if (docs == null || docs.isEmpty()) return docs;
        Set<String> queryTokens = tokenize(query);
        return docs.stream()
                .sorted(Comparator.comparingDouble(
                        (Document d) -> -overlap(queryTokens, tokenize(d.getText()))))
                .collect(Collectors.toList());
    }

    private Set<String> tokenize(String text) {
        if (text == null) return Set.of();
        return Arrays.stream(text.toLowerCase().split("[\\W]+"))
                .filter(t -> t.length() > 2)
                .collect(Collectors.toSet());
    }

    private double overlap(Set<String> a, Set<String> b) {
        if (a.isEmpty() || b.isEmpty()) return 0;
        long common = a.stream().filter(b::contains).count();
        return (double) common / Math.sqrt(a.size() * b.size());
    }
}
