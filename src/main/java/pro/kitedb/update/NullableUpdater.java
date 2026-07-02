package pro.kitedb.update;

public interface NullableUpdater<T> {
    T getModel();

    DefineCondition getDefineCondition();
}
