package fr.inria.edelweiss.kgraph.query;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

import fr.inria.acacia.corese.api.IDatatype;
import fr.inria.acacia.corese.cg.datatype.DatatypeMap;
import fr.inria.edelweiss.kgram.api.core.Node;
import fr.inria.edelweiss.kgram.api.query.Producer;
import fr.inria.edelweiss.kgram.core.Mapping;
import fr.inria.edelweiss.kgram.core.Mappings;


/**
 * Translation from SQL Java ResultSet to KGRAM Mapping
 * 
 * @author Olivier Corby, Edelweiss, INRIA 2010
 *
 */
public class Mapper {
	
	Producer producer;
	
	public Mapper(Producer p){
		producer = p;
	}
	
	public Producer getProducer(){
		return producer;
	}
	
	Mappings map(List<Node> lNodes, Object object){
		if (object instanceof ResultSet){
			return sql(lNodes, (ResultSet) object);
		}
		else return new Mappings();
	}
	
	public Mappings sql(List<Node> lNodes, ResultSet rs){
		Mappings lMap = new Mappings();
		Node[] qNodes = new Node[lNodes.size()];
		int j = 0;
		for (Node n : lNodes){
			qNodes[j++] = n;
		}
		
    	try {
			int size = rs.getMetaData().getColumnCount();
			while (rs.next()) {
				Node[] nodes = new Node[size];
				
				for (int i=1; i<=size; i++){
					Node node = getNode(rs, i);
					nodes[i-1] = node;
				}
				
				Mapping map =  Mapping.create(qNodes, nodes);
				lMap.add(map);
			}

		} 
    	catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			lMap = null;
		}
    	return lMap;
	}
		
	public Node getNode(ResultSet rs, int i) throws SQLException{
		IDatatype dt = null;
		
		switch(rs.getMetaData().getColumnType(i)){
		
		case java.sql.Types.NULL: break;

		case java.sql.Types.INTEGER:
		case java.sql.Types.NUMERIC:
		case java.sql.Types.DECIMAL:
			dt = DatatypeMap.newInstance(rs.getInt(i));break;
			
		case java.sql.Types.BIGINT:
			dt = DatatypeMap.newInstance(rs.getLong(i));break;
			
		case java.sql.Types.FLOAT: 
			dt = DatatypeMap.newInstance(rs.getFloat(i));break;
			
		case java.sql.Types.DOUBLE:
			dt = DatatypeMap.newInstance(rs.getDouble(i));break;
			
		case java.sql.Types.BOOLEAN: 
			dt = DatatypeMap.newInstance(rs.getBoolean(i));break;
			
		case java.sql.Types.DATE: 
			dt = DatatypeMap.newDate(rs.getDate(i).toString());break;
			
		default: 
			dt = DatatypeMap.newInstance(rs.getString(i).trim());
		}
		
		Node node = null;
		if (dt != null){
			node = producer.getNode(dt);
		}
		return node;
	}
	
	
	/**
	 * Rename query nodes
	 */
	Mappings map(List<Node> lNodes, Mappings lMap){
		for (Mapping map : lMap){
			map.setNodes(lNodes);
		}
		return lMap;
	}
	
	
	Mappings map(List<Node> nodes, IDatatype dt){
		Node[] qNodes = new Node[nodes.size()];
		int i = 0;
		for (Node qNode : nodes){
			qNodes[i++] = qNode;
		}
		Mappings lMap = new Mappings();
		List<Node> lNode = producer.toNodeList(dt);
		for (Node node : lNode){
			Node[] tNodes = new Node[1];
			tNodes[0] = node;
			Mapping map =  Mapping.create(qNodes, tNodes);
			lMap.add(map);
		}
		return lMap;
	}
	
	
	
	
	
	

}
