package pro.kitedb.test.om;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.SuperBuilder;
import pro.kitedb.graph.Filter;

@SuperBuilder
public class UserFilter implements Filter<User> {
    private @Getter @Setter Integer id;
    private @Getter @Setter Boolean withoutNickname;
    private @Getter @Setter SaleFilter saleFilter;
}
