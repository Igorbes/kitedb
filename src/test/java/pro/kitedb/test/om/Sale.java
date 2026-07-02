package pro.kitedb.test.om;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;
import pro.kitedb.graph.Filter;

import java.time.LocalDate;

@NoArgsConstructor
@AllArgsConstructor
public class Sale {
    private @Getter @Setter Integer id;
    private @Getter @Setter Integer userId;
    private @Getter @Setter LocalDate timestamp;
    private @Getter @Setter Integer amount;
    private @Getter @Setter Integer summary;
    private @Getter @Setter Address address;

    public Sale(int userId, int amount, int summary, Address address) {
        this.userId = userId;
        this.amount = amount;
        this.summary = summary;
        this.address = address;
    }

    @AllArgsConstructor
    @NoArgsConstructor
    public static class Address {
        private @Getter @Setter Integer postal;
        private @Getter @Setter String city;
        private @Getter @Setter Street street;
    }

    @AllArgsConstructor
    @NoArgsConstructor
    @SuperBuilder
    public static class Street {
        private @Getter @Setter String name;
    }
}
