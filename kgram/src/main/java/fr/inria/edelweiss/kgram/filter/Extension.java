package fr.inria.edelweiss.kgram.filter;

import fr.inria.edelweiss.kgram.api.core.Expr;
import java.util.HashMap;

/**
 * Manage extension functions 
 * Expr exp must have executed exp.local() to tag
 * local variables
 *
 * @author Olivier Corby, Wimmics INRIA I3S, 2015
 *
 */
public class Extension {

    static final String NL = System.getProperty("line.separator");
    //FunMap map;
    FunMap[] maps;
    private String name;
    private Object pack;
    
    class FunMap extends HashMap<String, Expr> {}

    public Extension() {
        //map = new FunMap();
        maps = new FunMap[11];
        for (int i=0; i<maps.length; i++){
            maps[i] = new FunMap();
        }
    }
    
    public Extension(String n){
        this();
        name = n;
    }
    
    FunMap getMap(Expr exp){
        return getMap(exp.arity());
    }
    
    FunMap getMap(int n){
         if (n >= maps.length){
            return null;
        }
        return maps[n];
    }
    
    FunMap[] getMaps(){
        return maps;
    }

    /**
     *
     * exp: st:fac(?x) = if (?x = 1, 1, ?x * st:fac(?x - 1))
     */
    public void define(Expr exp) {
        Expr fun = exp.getExp(0);
        getMap(fun).put(fun.getLabel(), exp);
    }
    
    public void add(Extension ext){
        for (FunMap m : ext.getMaps()){
            for (Expr e : m.values()){
                if (! isDefined(e.getExp(0))){
                    define(e);
                }
            }
        }
    }

    public boolean isDefined(Expr exp) {
        return getMap(exp).containsKey(exp.getLabel());
    }

    /**
     * exp: st:fac(?n) values: #[10] actual values of parameters return body of
     * fun
     */
    public Expr get(Expr exp, Object[] values) {
        return getMap(exp).get(exp.getLabel());
    }

    public Expr get(Expr exp) {
        Expr def = getMap(exp).get(exp.getLabel());
        return def;
    }
    
    public Expr get(String label) {
        for (int i = 0; i<maps.length; i++){
            Expr exp = get(label, i);
            if (exp != null){
                return exp;
            }
        }
        return null;
    }
    
    public Expr get(String label, int n) {
        FunMap m = getMap(n);
        if (m == null){
            return null;
        }
        return m.get(label);
    }
    
    public String toString() {
        StringBuilder sb = new StringBuilder();
        if (getName() != null){
            sb.append("extension: ");
            sb.append(getName()); 
            sb.append(NL);
        }
        for (FunMap m : getMaps()){
            for (Expr exp : m.values()) {
                sb.append(exp);
                sb.append(NL);
                sb.append(NL);
            }
        }
        return sb.toString();
    }

    /**
     * @return the name
     */
    public String getName() {
        return name;
    }

    /**
     * @param name the name to set
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * @return the pack
     */
    public Object getPackage() {
        return pack;
    }

    /**
     * @param pack the pack to set
     */
    public void setPackage(Object pack) {
        this.pack = pack;
    }
}