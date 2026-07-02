package pro.kitedb.graph;

import lombok.Getter;
import pro.kitedb.KiteJdbcTemplate;
import pro.kitedb.context.KiteContext;
import pro.kitedb.context.KiteContextImpl;
import pro.kitedb.exception.DataException;
import pro.kitedb.utils.KiteUtils;

import java.util.Map;

public class JoinTemplateImpl implements JoinTemplate {
    private final @Getter String templateName;
    private final @Getter KiteJdbcTemplate kiteJdbcTemplate;

    public JoinTemplateImpl(String templateName, KiteJdbcTemplate kiteJdbcTemplate) {
        this.templateName = templateName;
        this.kiteJdbcTemplate = kiteJdbcTemplate;
    }

    @Override
    public <O, F extends Filter<? super O>> String getJoinTemplate(DataGraph<O, F> subgraph) throws DataException {
        KiteContext context = new KiteContextImpl(this.kiteJdbcTemplate);
        context.put(subgraph);
        return KiteUtils.merge(templateName, context);
    }
}
