package pro.kitedb.test.om;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

import java.util.Collection;
import java.util.Set;

@AllArgsConstructor
@SuperBuilder
public class Product {
    private @Getter @Setter Integer id;
    private @Getter @Setter String name;
    private @Getter @Setter Set<Integer> discount;
    private @Getter @Setter String[] coupons;
}
