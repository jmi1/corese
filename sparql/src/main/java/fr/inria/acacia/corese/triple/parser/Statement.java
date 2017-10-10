package fr.inria.acacia.corese.triple.parser;

import fr.inria.corese.compiler.java.JavaCompiler;

/**
 *
 * Toplevel of extended statements:
 * Function Let Loop
 * IfElse
 * @author Olivier Corby, Wimmics INRIA I3S, 2015
 *
 */
public class Statement  extends Term {
    int nbVariable = 0;
    
    Statement(){}
    
    Statement(String name, Expression fun, Expression body){
        super(name, fun, body);
        setFunction(true);
    }
    
    @Override
    public boolean isStatement(){
        return true;
    }
    
    /**
     * @return the nbVariable
     */
    @Override
    public int getNbVariable() {
        return nbVariable;
    }

    /**
     * @param nbVariable the nbVariable to set
     */
    @Override
    public void setNbVariable(int nbVariable) {
        this.nbVariable = nbVariable;
    }
    
    @Override
    public void toJava(JavaCompiler jc){
        jc.toJava(this);
    }
}
