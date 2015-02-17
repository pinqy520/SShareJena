package network;

import java.io.PrintStream;

import rdftools.JenaParser;

public class IndexNode extends Node {
	private ChordKey ID;
	private FingerTable fingerTable;
	private LocationTable locationTable;
	private IndexNode predecessor;
	private IndexNode successor;
	
	public  int num_of_visited;    //the  times  visited

	public IndexNode(String IP)  {
		this.IP = IP;
		this.ID = new ChordKey(IP);
		this.Index = true;
		this.alive = false;
		this.fingerTable = new FingerTable(this);
		this.locationTable = new LocationTable(IP);
		num_of_visited=0;
		this.create();

	}

	/**
	 * Lookup a successor of given identifier
	 * 
	 * @param identifier
	 *            an identifier to lookup
	 * @return the successor of given identifier
	 */
	public IndexNode findSuccessor(String identifier) {
		ChordKey key = new ChordKey(identifier);
		return findSuccessor(key);
	}

	/**
	 * Lookup a successor of given key
	 * 
	 * @param identifier
	 *            an identifier to lookup
	 * @return the successor of given identifier
	 */
	public IndexNode findSuccessor(ChordKey key) {         //查找后继

		if (this == successor) {
			return this;
		}
		if (key.isBetween(this.getNodeID(), successor.getNodeID())
				|| key.compareTo(successor.getNodeID()) == 0) {
			return successor;
			
		} 
		else {
			IndexNode node = closestPrecedingNode(key);
			if (node == this) {
				return successor.findSuccessor(key);
			}
			num_of_visited++;
			return node.findSuccessor(key);
		}
	}

	private IndexNode closestPrecedingNode(ChordKey key) {   //由后往前查找路由表
		for (int i = Hash.KEY_LENGTH - 1; i >= 0; i--) {
			FingerItem finger = fingerTable.getFinger(i);
			ChordKey fingerKey = finger.getNode().getNodeID();
			if (fingerKey.isBetween(this.getNodeID(), key)) {
				return finger.getNode();
			}
		}
		return this;
	}

	/**
	 * Creates a new Chord ring.
	 */
	public void create() {                 //节点加入时先创建
		predecessor = null;
		successor = this;
	}

	/**
	 * Joins a Chord ring with a node in the Chord ring
	 * 
	 * @param node
	 *            a bootstrapping node
	 */
	public void join(IndexNode node) {      //节点加入初始化
		predecessor = null;
		successor = node.findSuccessor(this.getNodeID());
	}

	/**
	 * Verifies the successor, and tells the successor about this node. Should
	 * be called periodically.
	 */
	public void stabilize() {            //看其后继是否是自己
		IndexNode node = successor.getPredecessor();
		if (node != null) {
			ChordKey key = node.getNodeID();
			if ((this == successor)
					|| key.isBetween(this.getNodeID(), successor.getNodeID())) {
				successor = node;
			}
		}
		successor.notifyPredecessor(this);
	}

	public void notifyPredecessor(IndexNode node) {   //令节点node确认其前驱
		ChordKey key = node.getNodeID();
		if (predecessor == null
				|| key.isBetween(predecessor.getNodeID(), this.getNodeID())) {
			predecessor = node;
		}
	}

	/**
	 * Refreshes finger table entries.
	 */
	public void fixFingers() {                     //更新路由表
		for (int i = 0; i <Hash.KEY_LENGTH; i++) {
			FingerItem finger = fingerTable.getFinger(i);
			ChordKey key = ID.createindexKey(i);
		//	out.println("key"+i+"   "+key);
			finger.setNode(findSuccessor(key));
		//	out.println("finger"+i+"    "+finger.getNode().getNodeKey());
		}
	}

	public void storeRDFKey(String key, String address){
		this.locationTable.put(key, address);
	}
	
	public String[] getStNodesbyRDFKey(String key){
		return this.locationTable.get(key);
	}
	
	public String toString() {                     //节点信息转化为字符串
		StringBuilder sb = new StringBuilder();
		sb.append("IndexNode [");
		sb.append("IP=" + IP);
		sb.append(",ID=" + ID);
		sb.append("]");
		return sb.toString();
	}

	public void printFingerTable(PrintStream out) {              //打印路由表
		out.println("=======================================================");
		out.println("FingerTable: " + this);
		out.println("-------------------------------------------------------");
		out.println("Predecessor: " + predecessor);
		out.println("Successor: " + successor);
		out.println("-------------------------------------------------------");
		for (int i = 0; i < Hash.KEY_LENGTH; i++) {
			FingerItem finger = fingerTable.getFinger(i);
			out.println("finger"+i+"\t" + finger.getNode());
		}
		out.println("=======================================================");
	}

	public ChordKey getNodeID() {
		return ID;
	}

	public void setNodeID(ChordKey nodeID) {
		this.ID = nodeID;
	}

	public IndexNode getPredecessor() {
		return predecessor;
	}
	
	public void setPredecessor(IndexNode predecessor) {
		this.predecessor = predecessor;
	}

	public IndexNode getSuccessor() {
		return successor;
	}

	public void setSuccessor(IndexNode successor) {
		this.successor = successor;
	}

	public FingerTable getFingerTable() {
		return fingerTable;
	}

	public void setFingerTable(FingerTable fingerTable) {
		this.fingerTable = fingerTable;
	}
	
	public LocationTable getLocationTable(){
		return this.locationTable;
	}

	@Override
	public void parseRDFtoNet() {
		JenaParser parser = new JenaParser(this.file);
		while(parser.hasNext()){
			String[] triple = parser.run();
			String[] keyGroup = {"s:"+triple[0],
								"p:"+triple[1],
								"o:"+triple[2],
								"s:"+triple[0]+"|p:"+triple[1],
								"s:"+triple[0]+"|o:"+triple[2],
								"p:"+triple[1]+"|o:"+triple[2]};
			
			for(int i = 0; i < 6; i++){
				IndexNode indexnode = this.findSuccessor(keyGroup[i]);
				indexnode.storeRDFKey(keyGroup[i], this.IP);
			}
		}
	}

}
