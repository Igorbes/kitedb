package pro.kitedb.dao;

import pro.kitedb.graph.Graphable;

public interface DaoFullAccess<Ident, Obj, F extends pro.kitedb.graph.Filter<? super Obj>> extends
        DaoSelect<Obj, F>,
        DaoDelete<Ident>,
        DaoUpdate<Obj, Ident>,
        DaoInsert<Obj, Ident>,
        DaoCount<Obj, F>,
        DaoCopyable<Obj, F>,
        Graphable<Obj, F>,
        Executable {
}
