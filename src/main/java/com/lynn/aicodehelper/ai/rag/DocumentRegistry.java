package com.lynn.aicodehelper.ai.rag;

import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.stream.Collectors;

@Component
public class DocumentRegistry {

    private final Set<String> names = new CopyOnWriteArraySet<>();

    public void add(String name) {
        if (name != null && !name.isBlank()) {
            names.add(name);
        }
    }

    public void addAll(List<String> list) {
        if (list != null) {
            list.forEach(this::add);
        }
    }

    public List<String> list() {
        return names.stream().sorted().collect(Collectors.toList());
    }

    public void clear() {
        names.clear();
    }
}

