package fr.inria.edelweiss.kgraph.query;

import java.util.List;

import org.apache.log4j.Logger;

import fr.inria.acacia.corese.triple.parser.ASTQuery;
import fr.inria.acacia.corese.triple.update.Basic;
import fr.inria.acacia.corese.triple.update.Update;
import fr.inria.edelweiss.kgram.core.Mappings;
import fr.inria.edelweiss.kgraph.api.Loader;
import fr.inria.edelweiss.kgraph.core.Graph;
import fr.inria.edelweiss.kgraph.logic.Entailment;
import fr.inria.edelweiss.kgraph.rule.RuleEngine;
import fr.inria.edelweiss.kgtool.load.LoadException;

/**
 * SPARQL 1.1 Update
 * 
 * KGRAM Extensions:
 * 
 * create/drop graph kg:entailment
 * create      graph kg:rule
 *  
 * @author Olivier Corby, Edelweiss, INRIA 2011
 *
 */
public class ManagerImpl implements Manager {
	
	static Logger logger = Logger.getLogger(ManagerImpl.class);
	
	Graph graph;
	Loader load;
	QueryProcess exec;
	List<String> from, named;
	
	static final int COPY = 0;
	static final int MOVE = 1;
	static final int ADD  = 2;
	
	ManagerImpl(Graph g, Loader ld){
		graph = g;
		graph.init();
		load = ld;
	}
	
	ManagerImpl(QueryProcess exec){
		this(exec.getGraph(), exec.getLoader());
		this.exec = exec;
	}
	
	static ManagerImpl create(Graph g, Loader ld){
		return new ManagerImpl(g, ld);
	}
	
	static ManagerImpl create(QueryProcess exec){
		return new ManagerImpl(exec);
	}
	
	static ManagerImpl create(QueryProcess exec, List<String> from, List<String> named){
		ManagerImpl m = new ManagerImpl(exec);
		m.setFrom(from);
		m.setNamed(named);
		return m;
	}
	
	public void setFrom(List<String> l){
		from = l;
	}
	
	public void setNamed(List<String> l){
		named = l;
	}
	
	public boolean isDebug (){
		return exec.isDebug();
	}
	
	
	public Mappings query(ASTQuery ast){
		return exec.query(ast, from, named);
	}
	
	public boolean process(Basic ope){
		String uri 			= ope.expand(ope.getGraph());
		boolean isDefault 	= ope.isDefault();
		boolean isNamed 	= ope.isNamed();
		boolean isAll 		= ope.isAll();
		boolean isSilent 	= ope.isSilent();
		
		system(ope);

		switch (ope.type()){
		
		case Update.LOAD: 	return load(ope); 
			
		case Update.CREATE: return create(ope);
			
		case Update.CLEAR: 	return clear(ope);

		case Update.DROP: 	return drop(ope);
			
		case Update.ADD:	return add(ope);

		case Update.MOVE: 	return move(ope);

		case Update.COPY: 	return copy(ope);
			
		}
		
		return false;
		
	}
	
	/**
	 * kgraph extension:
	 * 
	 * clear graph kg:entailment suspend entailments
	 * add   graph kg:entailment resume  entailments
	 * 
	 * add graph kg:rule process rules if any
	 *
	 */
	void system(Basic ope){
		String uri = ope.expand(ope.getGraph());
		
		if (! isSystem(uri)){
			return;
		}
		
		RuleEngine rengine = load.getRuleEngine();
		
		

		switch (ope.type()){
		
		case Update.DROP: 
			
			if (isRule(uri) && rengine != null){
				// clear also the rule base
				rengine.clear();
			}
			
		case Update.CLEAR: 
			
			if (isEntailment(uri)){
				graph.setEntailment(false);
			}
			break;
			
			
		case Update.CREATE:
			
			if (isEntailment(uri)){
				graph.setEntailment(true);
				graph.setUpdate(true);
			}
			else if (isRule(uri)){
				if (rengine != null){
					rengine.process();
				}
			}
			break;
		}
	}
	
	boolean isSystem(String uri){
		return uri != null &&  uri.startsWith(Entailment.KGRAPH);
	}
	
	boolean isEntailment(String uri){
		return uri.equals(Entailment.ENTAIL);
	}
	
	boolean isRule(String uri){
		return uri.equals(Entailment.RULE);
	}
	
	private boolean clear(Basic ope) {
		return clear(ope, false);
	}
	
	private boolean drop(Basic ope) {
		return clear(ope, true);
	}
	
	private boolean clear(Basic ope, boolean drop) {
		if (named!=null && (ope.isNamed() || ope.isAll())){
			for (String gg : named){
				graph.clear(ope.expand(gg), ope.isSilent());
				if (drop) graph.deleteGraph(ope.expand(gg));
			}
		}
		if (from != null && (ope.isDefault() || ope.isAll())){
			for (String gg : from){
				graph.clear(ope.expand(gg), ope.isSilent());
				if (drop) graph.deleteGraph(ope.expand(gg));
			}
		}
		if (ope.getGraph()!=null){
			graph.clear(ope.expand(ope.getGraph()), ope.isSilent());
			if (drop) graph.deleteGraph(ope.expand(ope.getGraph()));
		}
		return true;
	}

	/**
	 * 
	copy graph  | default 
	to   target | default
	 */

	
	private boolean update(Basic ope, int mode) {
		String source = ope.expand(ope.getGraph());
		String target = ope.expand(ope.getTarget());
		
		if (source != null){
			if (target != null){
				update(ope, mode, source, target);
			}
			else {
				// skip copy to default
			}
		}
		else if (target != null && from != null) {
			for (String gg : from){
				String name = ope.expand(gg);
				update(ope, mode, name, target);
			}
		}

		return true;
	}
	
	
	private boolean update(Basic ope, int mode, String source, String target) {
		if (source.equals(target)) return true;
		
		switch (mode){
		case ADD:   return graph.add(source, target, ope.isSilent()); 
		case MOVE:  return graph.move(source, target, ope.isSilent());
		case COPY: 	return graph.copy(source, target, ope.isSilent());
		}
		return true;
	}

	private boolean copy(Basic ope) {
		return update(ope, COPY);
	}
	
	private boolean move(Basic ope) {
		return update(ope, MOVE);
	}

	private boolean add(Basic ope) {
		return update(ope, ADD);
	}



	private boolean create(Basic ope) {
		String uri = ope.expand(ope.getGraph());
		graph.addGraph(uri);
		return true;
	}

	private boolean load(Basic ope) {
		if (load == null){
			logger.error("Load " + ope.getURI() + ": Loader is undefined");
			return ope.isSilent();
		}
		String uri = ope.expand(ope.getURI());
		String src = ope.expand(ope.getTarget());
		try {
			load.loadWE(uri, src);
		} catch (LoadException e) {
			return ope.isSilent();
		}
		return true;
	}

}
