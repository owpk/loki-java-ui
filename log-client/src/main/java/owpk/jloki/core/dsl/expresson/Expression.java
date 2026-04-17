package owpk.jloki.core.dsl.expresson;

public interface Expression {
    String eval();
    String pretty(int indent);
}
