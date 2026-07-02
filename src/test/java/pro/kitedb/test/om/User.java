package pro.kitedb.test.om;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.SuperBuilder;
import pro.kitedb.graph.Filter;

import java.util.Collection;

@NoArgsConstructor
@ToString
public class User {
    private @Getter @Setter Integer id;
    private @Getter @Setter String name;
    private @Getter @Setter String surname;
    private @Getter @Setter String nickname;
    private @Getter @Setter Collection<Sale> sales;

    public User(String name, String surname) {
        this.name = name;
        this.surname = surname;
    }
}
