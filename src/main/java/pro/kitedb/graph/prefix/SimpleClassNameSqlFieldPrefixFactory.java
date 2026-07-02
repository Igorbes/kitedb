package pro.kitedb.graph.prefix;

import lombok.Getter;
import lombok.Setter;

public class SimpleClassNameSqlFieldPrefixFactory<O> implements SqlFieldPrefixFactory<O> {
    private @Getter @Setter Class<? super O> clazz;
    private @Getter @Setter String prefix;

    public SimpleClassNameSqlFieldPrefixFactory(Class<? super O> clazz) {
        this.clazz = clazz;
        this.prefix = clazz.getSimpleName();
    }
}
