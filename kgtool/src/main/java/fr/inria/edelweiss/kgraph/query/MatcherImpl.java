package fr.inria.edelweiss.kgraph.query;

import java.util.Hashtable;

import fr.inria.acacia.corese.api.IDatatype;
import fr.inria.edelweiss.kgram.api.core.Edge;
import fr.inria.edelweiss.kgram.api.core.Node;
import fr.inria.edelweiss.kgram.core.Query;
import fr.inria.edelweiss.kgram.api.query.Environment;
import fr.inria.edelweiss.kgram.api.query.Matcher;
import fr.inria.edelweiss.kgraph.core.Graph;
import fr.inria.edelweiss.kgraph.logic.Entailment;
import fr.inria.edelweiss.kgraph.logic.OWL;
import fr.inria.edelweiss.kgraph.logic.RDF;
import fr.inria.edelweiss.kgraph.logic.RDFS;

/**
 * Match
 * 
 * Draft subsumption for xxx rdf:type c:Person
 * Exploits graph subClassOf properties, use a cache 
 * 
 * TODO:
 * Remove dichotomy on constant class in EdgeIndex if entailment does not process rdfs:subClassOf
 * TODO:
 * ?x rdf:type ?c ?y rdf:type ?c
 * 
 * 
 * @author Olivier Corby, Edelweiss INRIA 2010
 *
 */
public class MatcherImpl implements Matcher {
	
	Graph graph;
	Entailment entail;
	Table table ;
	
	int mode = ONTOLOGY;
	
	class BTable extends Hashtable<Node,Boolean>{}
	
	class Table extends Hashtable<Node, BTable> {
		
		BTable getTable(Node q){
			BTable bt = get(q);
			if (bt == null){
				bt = new BTable();
				put(q, bt);
			}
			return bt;
		}
		
		Boolean get(Node q, Node t){
			BTable bt = getTable(q);
			return bt.get(t);
		}
		
		void put(Node q, Node t, Boolean b){
			BTable bt = getTable(q);
			bt.put(t, b);
		}
	} 
	
	
	
	MatcherImpl(){
		table = new Table();
	}
	
	public static MatcherImpl create(){
		return new MatcherImpl();
	}
	
	public static MatcherImpl create(Graph g){
		MatcherImpl m = new MatcherImpl();
		m.graph = g;
		m.entail = g.getEntailment();
		return m;
	}

	@Override
	public boolean match(Edge q, Edge r, Environment env) {
		
		if (q.getLabel().equals(RDF.TYPE)){
			return matchType(q, r, env);
		}
		
		if (! q.getLabel().equals(Graph.TOPREL) && 
			! q.getLabel().equals(r.getLabel())) {
			return false;
		}
			
		int max = q.nbNode();
		if (max > r.nbNode()) return false;
		for (int i=0; i<max; i++){
			Node qNode = q.getNode(i);
			Node node  = r.getNode(i);
			if (! match(qNode, node, env)){
				return false;
			}
		}
		return true;
	}
	
	
	boolean matchType(Edge q, Edge r, Environment env){
		if (! match(q.getNode(0), r.getNode(0), env)){
			return false;
		}
		
		if (env.getQuery().isRelax()){
			return true;
		}
		
		Query query = env.getQuery();
		int localMode = mode;
		if (query!=null && query.getMode()!=UNDEF){
			localMode = query.getMode();
		}
		
		Node qnode = q.getNode(1);
		
		switch (localMode){
			case STRICT: return match(qnode, r.getNode(1), env);
			case RELAX: return true;
		}
		
		
		if (qnode.isConstant() && entail!=null){
			
			if (entail.isTopClass(qnode)){
				// ?x rdf:type rdfs:Resource
				return true;
			}
		
			if (! entail.isSubClassOfInference()){
				// if rdf:type is completed by subClassOf, skip this and perform std match
				// if rdf:type is not completed by subClassOf, check whether r <: q
				boolean b = isSubClassOf(r.getNode(1), qnode, env);

				if (! b && localMode == SUBSUME){
					b = isSubClassOf(qnode, r.getNode(1), env);
				}

				return b;
			}
		
		}
		
		return match(q.getNode(1), r.getNode(1), env);
	}
	
	/**
	 * Store subsumption test in a cache 
	 * for each query type and each target type, store whether target subClassOf query
	 * the cache is bound to current query (via the environment)
	 */
	public boolean isSubClassOf(Node t, Node q, Environment env){
		Table table = getTable(env);
		Boolean b = table.get(q, t);
		if (b == null){
			// PRAGMA: use graph because entail may be null (cf PluginImpl)
			b = graph.isSubClassOf(t, q);
			table.put(q, t, b);
		}
		return b;
	}
	
	Table getTable(Environment env){
		Table table = (Table) env.getObject();
		if (table == null){
			table = new Table();
			env.setObject(table);
		}
		return table;
	}
	
	boolean isSubClassOf2(Node t, Node q, Environment env){
		boolean b = graph.isSubClassOf(t, q);
		return b;
	}
	
	@Override
	public boolean same(Node node, Node n1, Node n2, Environment env) {
		// TODO Auto-generated method stub
		return n1.same(n2);
	}

	@Override
	public void setMode(int mode) {
		this.mode = mode;
	}
	
	public boolean match(Node q, Node t, Environment env){
		if (q.isVariable()) return true;
		
		IDatatype qdt = (IDatatype) q.getValue();
		IDatatype tdt = (IDatatype) t.getValue();
		
		return qdt.sameTerm(tdt);
	}

}
