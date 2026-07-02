package pro.kitedb.utils;

import org.apache.commons.lang.CharEncoding;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.Velocity;
import org.apache.velocity.runtime.RuntimeInstance;
import org.apache.velocity.runtime.parser.node.ASTReference;
import org.apache.velocity.runtime.parser.node.SimpleNode;
import org.apache.velocity.runtime.visitor.BaseVisitor;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import pro.kitedb.context.KiteContext;
import pro.kitedb.exception.DataException;

import java.io.StringReader;
import java.io.StringWriter;
import java.util.*;

public class KiteUtils {
    public static MapSqlParameterSource toMapSql(Object... param) {
        MapSqlParameterSource sqlParameterSource = new MapSqlParameterSource();
        for (int i = 0; i < param.length; i++) {
            sqlParameterSource.addValue((String) param[i], param[++i]);
        }
        return new MapSqlParameterSource(toMap(param));
    }

    public static Map<String, Object> toMap(Object... param) {
        Map<String, Object> sqlParameterSource = new HashMap<>();
        for (int i = 0; i < param.length; i++) {
            sqlParameterSource.put((String) param[i], param[++i]);
        }
        return sqlParameterSource;
    }

    public static String merge(String templatePath, KiteContext kiteContext) throws DataException {
        try {
            VelocityContext velocityContext = new VelocityContext();
            velocityContext.put("k", kiteContext);
            StringWriter writer = new StringWriter();
            Velocity.mergeTemplate(templatePath.toString(), CharEncoding.UTF_8, velocityContext, writer);
            writer.flush();
            String sql = writer.toString();

            RuntimeInstance ri = new RuntimeInstance();
            SimpleNode node = ri.parse(new StringReader(sql), templatePath.toString());
            Set<String> variables = new HashSet<>();
            node.jjtAccept(new BaseVisitor() {
                @Override
                public Object visit(ASTReference node, Object data) {
                    variables.add(node.getFirstToken().toString());
                    return super.visit(node, data);
                }
            }, null);
            if(!variables.isEmpty()) {
                VelocityContext nulles = new VelocityContext();
                variables.forEach(v -> nulles.put(v.substring(1), "null"));

                writer = new StringWriter();
                Velocity.evaluate(nulles, writer, templatePath, sql);
                sql = writer.toString();
            }
            return sql;
        } catch (Exception e) {
            throw new DataException(e);
        }
    }

    public static Object format(Object value) {
        if(value.getClass().isArray()) {
            Object[] res = (Object[]) value;
            Collection<Object> list = new ArrayList<>(res.length);
            for (Object re : res) {
                list.add(format(re));
            }
            return list;
        } else if(value instanceof Enum) {
            Enum e = (Enum) value;
            return e.name();
        } else if(value instanceof Collection) {
            Collection<Object> collection = (Collection<Object>) value;
            Collection<Object> res = new ArrayList<>(collection.size());
            for (Object o : collection) {
                res.add(format(o));
            }
            return res;
        }
        return value;
    }
}
