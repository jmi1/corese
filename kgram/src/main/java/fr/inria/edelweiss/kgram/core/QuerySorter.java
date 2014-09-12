package fr.inria.edelweiss.kgram.core;

import fr.inria.edelweiss.kgram.api.core.ExpType;
import fr.inria.edelweiss.kgram.api.core.Expr;
import fr.inria.edelweiss.kgram.api.core.ExprType;
import fr.inria.edelweiss.kgram.api.core.Filter;
import fr.inria.edelweiss.kgram.api.core.Node;
import fr.inria.edelweiss.kgram.api.query.Producer;
import fr.inria.edelweiss.kgram.filter.Compile;
import java.util.ArrayList;
import java.util.List;

/**
 * Sort query edges to optimize query processing Including exists {} edges
 * Insert filters at place where variables are bound
 *
 * @author Olivier Corby, Wimmics Inria I3S, 2014
 *
 */
public class QuerySorter implements ExpType {

    private boolean isSort = true;
    private boolean testJoin = false;

    private Sorter sort;
    Query query;
    Compile compiler;
    Producer prod;

    //todo assign sorter here
    QuerySorter(Query q) {
        query = q;
        compiler = new Compile(q);
        sort = new Sorter();
    }

    /**
     * Contract: For each BGP: Sort Query Edges Insert Filters at first place
     * where variables are bound This must be recursively done for exists {}
     * pattern in filter and bind including select, order by, group by and
     * having
     */
    void compile(Producer prod) {
        this.prod = prod;
        VString bound = new VString();
        compile(query, bound, false);
    }

    /**
     * Compile exists {} pattern when filter contains exists {}.
     */
    void compile(Filter f) {
        compile(f, new VString(), false);
    }

    /**
     * Compile additional filters that may contain exists {}.
     */
    void modifier(Query q) {
        compile(q.getSelectFun());
        compile(q.getOrderBy());
        compile(q.getGroupBy());
        if (q.getHaving() != null) {
            compile(q.getHaving().getFilter());
        }
    }

    /**
     * ***************************************************
     */
    /**
     * Recursively sort edges and filters edges are sorted wrt connection:
     * successive edges share variables if possible In each BGP, filters are
     * inserted at the earliest place where their variables are bound lVar is
     * the List of variables already bound
     */
    Exp compile(Exp exp, VString lVar, boolean option) {
        int type = exp.type();

        switch (type) {

            case EDGE:
            case PATH:
            case XPATH:
            case EVAL:
            case NODE:
            case GRAPHNODE:
                break;

            case FILTER:
                // compile inner exists {} if any
                compile(exp.getFilter(), lVar, option);
                break;

            case BIND:
                compile(exp.getFilter(), lVar, option);
                break;

            case QUERY:
                // match query and subquery
                Query q = exp.getQuery();
                modifier(q);
                // lVar = select varList
                if (!lVar.isEmpty()) {
                    VString list = new VString();
                    for (Exp ee : q.getSelectFun()) {
                        Node node = ee.getNode();
                        if (lVar.contains(node.getLabel())) {
                            list.add(node.getLabel());
                        }
                    }
                    lVar = list;
                }

            // continue with subquery body
            default:

                if (type == OPTION || type == OPTIONAL
                        || type == UNION || type == MINUS) {
                    option = true;
                }

                //******* Query plan BEGIN********
                if (isSort) {

                    int num = exp.size();

                    // identify remarkable filters such as ?x = <uri>
                    // create OPT_BIND(?x = <uri>) store it in FILTER 
                    List<Exp> lBind = findBindings(exp);
                    if (exp.type() == AND) {
                        // sort edges wrt connection
                        // take OPT_BIND(var = exp) into account
                        // TODO: graph ?g does not take into account OPT_BIND ?g = uri
                        switch (query.getPlanProfile()) {
                            
                            case Query.NO_PLAN:
                                break;
                            case Query.PLAN_DEFAULT:
                                sort.sort(query, exp, lVar, lBind);
                                sortFilter(exp, lVar);
                                exp.setBind();
                                break;
                            case Query.PLAN_RULE_BASED:
                                sort = new SorterNew();
                                ((SorterNew) sort).sort(exp, lBind, null, Query.PLAN_RULE_BASED);
                                setBind(exp, lBind);
                                break;
                            case Query.PLAN_STATS_BASED:
                                //!! the first time using stats based method
                                // then do statistics (create the instance)
//                                IProducer ip = (IProducer) prod;
//                                if (ip.statsEnabled() && !ip.statsInitialized()) {
//                                    ip.createStatsInstance();
//                                }
                                //DO STATS WHEN LOADING
                                
                                sort = new SorterNew();
                                ((SorterNew) sort).sort(exp, lBind, prod, Query.PLAN_STATS_BASED);
                                setBind(exp, lBind);
                                break;
                            case Query.PLAN_COMBINED:
                                
                                break;
                        }
                        service(exp);
                    }
                    // put filters where they are bound ASAP
                    //sortFilter(exp, lVar);

                    // set BIND expressions before right edge
                    // ?x ?p ?y . ?x ?q ?t . filter(?t = 12)
                    // ->
                    // OPT_BIND(?t = 12) . ?x ?q ?t . filter(?t = 12) .  ?x ?p ?y                        
                    //exp.setBind();
                    //setBind(exp, lBind);
                    // set graph filter into graph
                    //exp.graphFilter();
                }
                //******* Query plan END********

                int size = lVar.size();

                // bind graph variable
                if (exp.isGraph() && exp.getGraphName().isVariable()) {
                    // GRAPH {GRAPHNODE NODE} {EXP}
                    Node gNode = exp.getGraphName();
                    lVar.add(gNode.getLabel());
                }

                for (Exp e : exp) {
                    compile(e, lVar, option);
                    if (exp.type() == AND) {
                        e.addBind(lVar);
                    }
                }

                lVar.clear(size);

                if (testJoin && exp.type() == AND) {
                    // group statements that are not connected in separate BGP 
                    // generate a JOIN between them.
                    Exp res = exp.join();
                    if (res != exp) {
                        exp.getExpList().clear();
                        exp.add(res);
                    }
                }

        }

        if (exp.isOptional()) {
            // A optional B
            // variables bound by A
            exp.first().setNodeList(exp.first().getNodes());
        } else if (exp.isJoin()) {
            exp.bindNodes();
        }

        return exp;
    }

    // put the binding variables to concerned edge
    void setBind(Exp exp, List<Exp> bindings) {
        for (Exp bid : bindings) {
            Node n = bid.get(0).getNode();
            if (bid.type() == OPT_BIND
                    // no bind (?x = ?y) in case of JOIN
                    && (!Query.testJoin || bid.isBindCst())) {

                for (Exp g : exp) {
                    if (((g.isEdge() || g.isPath()) && g.getEdge().contains(n))
                            && (bid.isBindCst() ? g.bind(bid.first().getNode()) : true)) {
                        if (g.getBind() == null) {
                            bid.status(true);
                            g.setBind(bid);
                        }
                    }
                }
            }
        }
    }

    boolean contains(Exp exp, Node n) {
        if (!exp.isEdge()) {
            return false;
        }
        return exp.getEdge().contains(n);
    }

    void compile(Filter f, VString lVar, boolean opt) {
        compile(f.getExp(), lVar, opt);
    }

    /**
     * Compile pattern of exists {} if any
     */
    void compile(Expr exp, VString lVar, boolean opt) {
        if (exp.oper() == ExprType.EXIST) {
            compile(query.getPattern(exp), lVar, opt);
        } else {
            for (Expr ee : exp.getExpList()) {
                compile(ee, lVar, opt);
            }
        }
    }

    void compile(List<Exp> list) {
        for (Exp ee : list) {
            // use case: group by (exists{?x :p ?y} as ?b)
            // use case: order by exists{?x :p ?y} 
            if (ee.getFilter() != null) {
                compile(ee.getFilter());
            }
        }
    }

    /**
     * Move filter at place where variables are bound in exp expVar: list of
     * bound variables TODO: exists {} could be eval earlier
     */
    void sortFilter(Exp exp, VString expVar) {
        int size = expVar.size();
        List<String> filterVar;
        List<Exp> done = new ArrayList<Exp>();

        for (int jf = exp.size() - 1; jf >= 0; jf--) {
            // reverse to get them in same order after placement

            Exp f = exp.get(jf);

            if (f.isFilter() && !done.contains(f)) {

                Filter ff = f.getFilter();

                if (compiler.isLocal(ff)) {
                    //TODO: fix it
                    // optional {} !bound()
                    // !bound() should not be moved
                    done.add(f);
                    continue;
                }

                expVar.clear(size);

                filterVar = ff.getVariables();
                boolean isExist = ff.getExp().isExist();

                for (int je = 0; je < exp.size(); je++) {
                    // search exp e after which filter f is bound
                    Exp e = exp.get(je);

                    e.share(filterVar, expVar);
                    boolean bound = query.bound(filterVar, expVar);

                    if (bound || (isExist && je + 1 == exp.size())) {
                        // insert filter after exp
                        // an exist filter that is not bound is moved at the end because it 
                        // may bound its own variables.
                        if (bound && e.isOptional() && e.first().size() > 0) {
                            e.first().add(ff);
                        }
                        e.addFilter(ff);
                        done.add(f);
                        if (jf < je) {
                            // filter is before, move it after
                            exp.remove(f);
                            exp.add(je, f);
                        } else if (jf > je + 1) {
                            // put if just behind
                            exp.remove(f);
                            exp.add(je + 1, f);
                            jf++;
                        }

                        break;
                    }
                }
            }
        }

        expVar.clear(size);

    }

    /**
     * Identify remarkable filter ?x < ?y ?x = ?y or ?x = cst or !bound() filter
     * is tagged
     */
    List<Exp> findBindings(Exp exp) {
        for (Exp ee : exp) {
            if (ee.isFilter()) {
                compiler.process(query, ee);
            }
        }
        return exp.varBind();
    }

    /**
     * @return the sort
     */
    public Sorter getSorter() {
        return sort;
    }

    /**
     * @param sort the sort to set
     */
    public void setSorter(Sorter sort) {
        this.sort = sort;
    }

    /**
     * JOIN service
     */
    void service(Exp exp) {
        int hasService = 0;
        for (Exp ee : exp) {
            if (isService(ee)) {
                hasService += 1;
            }
        }
        if (hasService > 0) { // && q.isOptimize()){
            // replace pattern . service by join(pattern, service)
            service(exp, hasService);
        }
    }

    /**
     * It is a service and it has an URI (not a variable)
     */
    boolean isService(Exp exp) {
        return exp.type() == Exp.SERVICE && !exp.first().getNode().isVariable();
    }

    /**
     * Draft: for each service in exp replace pattern . service by join(pattern,
     * service) it may be join(service, service)
     */
    void service(Exp exp, int nbs) {

        if (nbs < 1 || (nbs == 1 && isService(exp.get(0)))) {
            // nothing to do
            return;
        }

        int count = 0;
        int i = 0;

        Exp and = Exp.create(Exp.AND);

        while (count < nbs) {
            // there are services

            while (!isService(exp.get(i))) {
                // find next service
                and.add(exp.get(i));
                exp.remove(i);
            }

            // exp.get(i) is a service
            count++;

            if (and.size() == 0) {
                and.add(exp.get(i));
            } else {
                Exp join = Exp.create(Exp.JOIN, and, exp.get(i));
                and = Exp.create(Exp.AND);
                and.add(join);
            }

            exp.remove(i);

        }

        while (exp.size() > 0) {
            // no more service
            and.add(exp.get(0));
            exp.remove(0);
        }

        exp.add(and);

    }

    class VString extends ArrayList<String> {

        void clear(int size) {
            if (size == 0) {
                clear();
            } else {
                while (size() > size) {
                    remove(size() - 1);
                }
            }
        }

        public boolean add(String var) {
            if (!contains(var)) {
                super.add(var);
            }
            return true;
        }
    }
}
