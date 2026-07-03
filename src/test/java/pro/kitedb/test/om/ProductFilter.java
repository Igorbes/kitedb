package pro.kitedb.test.om;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.SuperBuilder;
import pro.kitedb.graph.Filter;

@SuperBuilder
public class ProductFilter implements Filter<Product> {
}
