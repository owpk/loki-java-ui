package owpk.jloki.core.dsl.expresson;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import owpk.jloki.core.dsl.utils.LabelEntry;

public record LabelSelector(List<LabelEntry> labels) implements Expression {

    public LabelSelector() {
        this(new ArrayList<>());
    }

    public void add(LabelEntry entry) {
        labels.add(entry);
    }

    @Override
    public String eval() {
        return labels.stream()
                .map(e -> "%s%s\"%s\"".formatted(e.key(), e.matcher().getValue(), e.value()))
                .collect(Collectors.joining(",", "{", "}"));
    }

    @Override
    public String pretty(int indent) {
        String pad = "    ".repeat(indent);
        return pad + eval() + "\n";
    }

    public boolean isEmpty() {
        return labels.isEmpty();
    }
}
