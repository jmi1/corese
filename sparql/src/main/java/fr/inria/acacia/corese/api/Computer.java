
package fr.inria.acacia.corese.api;

import fr.inria.edelweiss.kgram.api.core.Expr;
import fr.inria.edelweiss.kgram.api.query.Environment;
import fr.inria.edelweiss.kgram.api.query.Producer;

/**
 *
 * @author corby
 */
public interface Computer {
    
    ComputerProxy getComputerProxy();
    ComputerProxy getComputerPlugin();
    ComputerProxy getComputerTransform();
    Computer getComputer(Environment env, Producer p, Expr function); 
    Environment getEnvironment();  
    
    IDatatype function(Expr exp, Environment env, Producer p);
    IDatatype exist(Expr exp, Environment env, Producer p);
        
    Expr getDefine(Expr exp, Environment env);  
    Expr getDefineGenerate(Expr exp, Environment env, String name, int n); 
    Expr getDefineMethod(Environment env, String name, IDatatype type, IDatatype[] param);   
    boolean isCompliant();
}
