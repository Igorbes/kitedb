package pro.kitedb.dao.annotation;

import pro.kitedb.ppss.KiteParameterizedPreparedStatementSetter;
import pro.kitedb.factory.returning.Returning;
import pro.kitedb.factory.returning.ReturningFirstNumberColumn;
import pro.kitedb.ppss.NopeKitePPSS;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface DaoDeleteAbility {
    String template();
    Class<? extends Returning<?>> returning() default ReturningFirstNumberColumn.class;
    Class<? extends KiteParameterizedPreparedStatementSetter<?>> ppss() default NopeKitePPSS.class;
    int batchSize() default 10;
}
