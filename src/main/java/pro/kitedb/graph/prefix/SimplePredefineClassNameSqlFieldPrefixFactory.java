package pro.kitedb.graph.prefix;

import lombok.Getter;
import lombok.Setter;

public class SimplePredefineClassNameSqlFieldPrefixFactory<O> implements SqlFieldPrefixFactory<O> {
    private @Getter @Setter Class<? super O> clazz;
    private @Getter @Setter String prefix;

    public SimplePredefineClassNameSqlFieldPrefixFactory(String prefix, Class<O> clazz) {
        this.clazz = clazz;
        this.prefix = prefix;
    }
}
