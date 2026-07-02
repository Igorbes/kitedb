package pro.kitedb.dao.annotation;

import pro.kitedb.ppss.KiteParameterizedPreparedStatementSetter;
import pro.kitedb.factory.returning.Returning;
import pro.kitedb.factory.returning.ReturningFirstNumberColumn;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface DaoInsertAbility {
    String template();
    Class<?> clazz() default Object.class; /* fix late */
    Class<? extends Returning<?>> returning() default ReturningFirstNumberColumn.class;
    int batchSize() default 10;
    Class<? extends KiteParameterizedPreparedStatementSetter<?>> ppss();
}
