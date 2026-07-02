package pro.kitedb.dao.annotation;

import pro.kitedb.graph.Filter;
import pro.kitedb.graph.Graphable;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface DaoSelectAbility {
    String template();
    Class<? extends Graphable<?, ? extends Filter<?>>> graphable();
}
