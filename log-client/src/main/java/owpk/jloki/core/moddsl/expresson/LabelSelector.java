package owpk.jloki.core.moddsl.expresson;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import owpk.jloki.core.moddsl.steps.LabelSelector.LabelEntry;

public record LabelSelector(List<LabelEntry> labels) implements Expression {

    public LabelSelector() {
        this(new ArrayList<>());
    }

    public void add(LabelEntry entry) {
        labels.add(entry);
    }

    @Override
    public String render() {
        return labels.stream()
                .map(e -> e.key() + e.matcher().getValue() + "\"" + e.value() + "\"")
                .collect(Collectors.joining(",", "{", "}"));
    }

    @Override
    public String pretty(int indent) {
        String pad = "    ".repeat(indent);
        return pad + labels.stream()
                .map(e -> e.key() + e.matcher().getValue() + "\"" + e.value() + "\"")
                .collect(Collectors.joining("\n" + pad));
    }

    public boolean isEmpty() {
        return labels.isEmpty();
    }
}
