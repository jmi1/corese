package fr.inria.edelweiss.kgenv.parser;

import java.lang.reflect.InvocationTargetException;

import fr.inria.acacia.corese.api.IDatatype;
import fr.inria.acacia.corese.exceptions.CoreseDatatypeException;
import fr.inria.acacia.corese.triple.cst.RDFS;
import fr.inria.acacia.corese.triple.parser.ASTQuery;
import fr.inria.acacia.corese.triple.parser.Atom;
import fr.inria.acacia.corese.triple.parser.BasicGraphPattern;
import fr.inria.acacia.corese.triple.parser.Constant;
import fr.inria.acacia.corese.triple.parser.RDFList;
import fr.inria.acacia.corese.triple.parser.Source;
import fr.inria.acacia.corese.triple.parser.Triple;
import fr.inria.edelweiss.kgram.api.core.ExpType;
import fr.inria.edelweiss.kgram.api.query.Matcher;
import fr.inria.edelweiss.kgram.api.query.Producer;
import fr.inria.edelweiss.kgram.core.Eval;
import fr.inria.edelweiss.kgram.core.Query;
import fr.inria.edelweiss.kgram.event.EvalListener;
import fr.inria.edelweiss.kgram.event.EventListener;
import fr.inria.edelweiss.kgram.tool.Message;
import fr.inria.edelweiss.kgram.tool.MetaProducer;

/**
 * Pragma processor
 * 
 * @author Olivier Corby, Edelweiss, INRIA 2010
 *
 */
public class Pragma  {
	public static final String KG 		= ExpType.KGRAM;
	// subject
	public static final String SELF 	= KG + "kgram";
	public static final String MATCH 	= KG + "match";
	public static final String PATH 	= KG + "path";
	public static final String QUERY 	= KG + "query";
	public static final String SERVICE  = KG + "service";
	public static final String PRAGMA	= KG + "pragma";
	public static final String GRAPH	= KG + "graph";

	// kgram
	public static final String OPTIM 	= KG + "optimize";
	public static final String TEST 	= KG + "test";
	public static final String DEBUG 	= KG + "debug";
	public static final String SORT		= KG + "sort";
	public static final String LISTEN 	= KG + "listen";
	public static final String LOOP 	= KG + "loop";
	public static final String NODE 	= KG + "node";
	public static final String EDGE 	= KG + "edge";
	public static final String LOAD 	= KG + "load";
	public static final String LIST 	= KG + "list";
	public static final String DISPLAY	= KG + "display";
	public static final String EXPAND 	= KG + "expand";
	public static final String PRELAX 	= KG + "relax";
	public static final String SLICE 	= KG + "slice";
	public static final String TIMEOUT 	= KG + "timeout";
	public static final String MAP 		= KG + "map";
	public static final String COUNT 	= KG + "count";
	public static final String SIZE 	= KG + "size";
	public static final String INSERT 	= KG + "insert";
	public static final String DELETE 	= KG + "delete";
	public static final String LISTEN_INSERT = KG + "listenInsert";
	public static final String LISTEN_DELETE = KG + "listenDelete";
	public static final String STATUS	= KG + "status";
	public static final String DESCRIBE	= KG + "describe";
	public static final String CHECK	= KG + "check";
	public static final String DETAIL	= KG + "detail";
	public static final String PRIORITY	= KG + "priority";
	public static final String FILE		= KG + "file";


	public static final String HELP 	= KG + "help";

	

	// match
	static final String MODE 	= KG + "mode";
	// match mode
	static final String RELAX 		= "relax";
	static final String GENERAL 	= "general";
	static final String STRICT  	= "strict";
	static final String SUBSUME  	= "subsume";
	static final String INFERENCE  	= "inference";
	static final String MIX  		= "mix";


	protected Eval kgram;
	protected Query query;
	protected Transformer transform;
	protected ASTQuery ast;
	
	public Pragma(Eval e, Query q, ASTQuery a){
		kgram = e;
		query = q;
		ast = a;
	}
	
	public Pragma(Query q, ASTQuery a){
		query = q;
		ast = a;
	}
	
	public Pragma(Transformer t, ASTQuery a){
		ast = a;
		transform = t;
	}
	
	public void parse(){
		parse(null, ast.getPragma());
	}
	
	public void parse(fr.inria.acacia.corese.triple.parser.Exp pragma){
		parse(null, pragma);
	}
	
	public void parse(Atom g, fr.inria.acacia.corese.triple.parser.Exp exp){
		
		for (fr.inria.acacia.corese.triple.parser.Exp pragma : exp.getBody()){
			
			if (query != null && query.isDebug()) Message.log(Message.PRAGMA, pragma);
			
			if (pragma.isTriple()){
				triple(g, pragma.getTriple(), exp);
			}
			else if (pragma.isGraph()){
				Source gp = (Source) pragma;
				parse(gp.getSource(), gp.getBody().get(0));
			}
			else if (pragma.isRDFList()){
				RDFList list = (RDFList) pragma;
				list(g, list);
			}
			else if (pragma.isAnd()){
				parse(g, pragma);
			}
		}
	}
	
	// redefined if PragmaImpl
	public void list(Atom g, RDFList list){
		parse(g, list);
	}
	
	
	public void compile(){
		//System.out.println("** Compile: " + ast.getPragma());
		compile(null, ast.getPragma());
	}
	
	public void compile(BasicGraphPattern p){
		//System.out.println("** Compile: " + ast.getPragma());
		compile(null, p);
	}
	
	
	public void compile(Atom g, fr.inria.acacia.corese.triple.parser.Exp exp){
		
		for (fr.inria.acacia.corese.triple.parser.Exp pragma : exp.getBody()){
			
			if (ast.isDebug()) Message.log(Message.PRAGMA, pragma);

			if (pragma.isTriple()){
				compile(g, pragma.getTriple(), exp);
			}
			else if (pragma.isGraph()){
				Source gp = (Source) pragma;
				compile(gp.getSource(), gp.getBody().get(0));
			}
		}
	}
	
	
	public void compile(Atom g, Triple t, fr.inria.acacia.corese.triple.parser.Exp pragma){
			
			String subject  = t.getSubject().getLabel();
			String property = t.getProperty().getLabel();
			String object   = t.getObject().getLabel();
			//if (object == null) object = t.getObject().getName();

			if (subject.equals(PATH)){
				if (property.equals(EXPAND)){
					Constant cst = t.getObject().getConstant();
					IDatatype dt = cst.getDatatypeValue();
					if (dt.isNumber()){
						transform.add(ExpandPath.create(dt.intValue()));
					}
					else {
						transform.add(ExpandPath.create());
					}
				}
			} 
			else if (subject.equals(SELF)){
				if (property.equals(PRELAX)){
					if (t.getObject().isBlankNode()){
						// kg:kgram kg:relax (foaf:type)
						RDFList list = getList(t.getObject(), pragma);
						ast.setRelax(list.getList());
					}
					else {
						ast.addRelax(t.getObject());
					}
				}
				
			}
			else if (subject.equals(QUERY)){
				if (property.equals(CHECK)){
					ast.setCheck(value(object));
				}
				else if (property.equals(PRIORITY)){					
					IDatatype dt = t.getObject().getDatatypeValue();
					ast.setPriority(dt.intValue());
				}
			}
			
	}
	
	
	String help (){
		String query = 
			"select where {}\n" +
			"pragma {\n" +
			"kg:kgram kg:debug true        # debug mode \n" +
			"kg:kgram kg:list  true        # list group result \n" +
			"\n" +
			"kg:match kg:mode 'strict'     # strict type match \n" +
			"kg:match kg:mode 'relax'      # approximate type match \n" +
			"kg:match kg:mode 'subsume'    # subsume type match \n" +
			"kg:match kg:mode 'general'    # generalize type match \n" +
			"kg:match kg:mode 'mix'        # subsume + generalize type match \n" +
			"\n" +
			"kg:path  kg:list  true        # list path result (no thread) \n" +
			"kg:path  kg:loop  false       # path without loop \n" +
			
			"kg:query kg:display true      # pprint query AST \n" +


			"}";
		
		return query;
	}
	
	
	public void triple(Atom g, Triple t, fr.inria.acacia.corese.triple.parser.Exp pragma){
		
		String subject  = t.getSubject().getLabel();
		String property = t.getProperty().getLabel();
		String object   = t.getObject().getLabel();
		//if (object == null) object = t.getObject().getName();
		
		if (subject.equals(SELF)){
			if (property.equals(TEST)){
				query.setTest(value(object));
			}
			else if (property.equals(OPTIM)){
				query.setOptimize(value(object));
			}
			else if (property.equals(DEBUG)){
				query.setDebug(value(object));	
			}
			else if (property.equals(SORT)){
				query.setSort(value(object));	
			}
			else if (property.equals(LISTEN) && value(object)){
				kgram.addEventListener(EvalListener.create());
			}
			else if (property.equals(LIST)){
				query.setListGroup(value(object));
			}
			else if (property.equals(DETAIL)){
				query.setDetail(value(object));
			}
		}
		else if (subject.equals(MATCH)){
			if (property.equals(MODE)){
				int mode = getMode(object);
				query.setMode(mode);
			}
			else if (property.equals(RDFS.RDFTYPE)){
				// kg:match rdf:type <fr.inria.edelweiss.kgramenv.util.MatcherImpl> 
				Matcher match = (Matcher) create(object);
				if (match != null){
					kgram.setMatcher(match);
				}
			}
		}
		else if (subject.equals(LISTEN)){
			if (property.equals(RDFS.RDFTYPE)){
				// kg:listen rdf:type <fr.inria.edelweiss.kgram.event.StatListener> 
				EventListener el = (EventListener) create(object);
				if (el != null){
					kgram.addEventListener(el);
				}
			}
		}
		else if (subject.equals(PATH)){
			 if (property.equals(COUNT)){
				query.setCountPath(value(object));
			}
			if (property.equals(LIST)){
				query.setListPath(value(object));
			}
			else if (property.equals(LOOP)){
				query.setCheckLoop(! value(object));
			}
		}
		else if (subject.equals(QUERY)){
			if (property.equals(DISPLAY)){
				query.addInfo("AST:\n", ast);
			}
		}
		else if (subject.equals(PRAGMA)){
			if (property.equals(HELP) && value(object)){
				query.addInfo(help(), null);
			}
		}
		else if (subject.equals(SERVICE)){
			if (property.equals(SLICE)){
				int slice = t.getObject().getDatatypeValue().intValue();
				query.setSlice(slice);
			}
			else if (property.equals(TIMEOUT)){
				Integer value = t.getObject().getDatatypeValue().intValue();
				query.setPragma(TIMEOUT, value);
			}
		}
		
	}
	
	/**
	 * 
	 */
	RDFList getList(Atom head, fr.inria.acacia.corese.triple.parser.Exp pragma){
		for (fr.inria.acacia.corese.triple.parser.Exp exp : pragma.getBody()){
			if (exp.isRDFList()){
				RDFList list = (RDFList) exp;
				if (list.head().getName().equals(head.getName())){
					return list;
				}
			}
		}
		return null;
	}
	

	public boolean value(String value){
		return value.equals("true");
	}
	
	int getMode(String mode){
		if (mode.equals(STRICT)) 	return Matcher.STRICT;
		if (mode.equals(RELAX)) 	return Matcher.RELAX;
		if (mode.equals(SUBSUME)) 	return Matcher.SUBSUME;
		if (mode.equals(GENERAL))	return Matcher.GENERAL;
		if (mode.equals(MIX))		return Matcher.MIX;
		if (mode.equals(INFERENCE)) return Matcher.INFERENCE;
		// default
		return Matcher.SUBSUME;
	}
	
	

	
	Object create(String name){
		try {
			//EventListener el ;
			//= (EventListener) Class.forName(object).newInstance();
			Class cname =  Class.forName(name);
			Object object =  cname.getMethod("create").invoke(cname);
			return object;
		} 

		catch (IllegalAccessException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} 
		catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} 
		catch (SecurityException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} 
		catch (NoSuchMethodException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} 
		catch (IllegalArgumentException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} 
		catch (InvocationTargetException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}
	
	
}
