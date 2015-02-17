package network;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.SortedMap;
import java.util.TreeMap;


public class NetWork {
	List<Node> nodeList =  new ArrayList<Node>();
	List<IndexNode> indexNodeList =  new ArrayList<IndexNode>();
	SortedMap<ChordKey, IndexNode> indexNodeMap = new TreeMap<ChordKey, IndexNode>();
	SortedMap<String, Node> NodeMap = new TreeMap<String, Node>();
	
	/**
	 * create a new node and add it to the net
	 * 
	 * @param IP
	 * 			node ip address
	 * @param isIndex
	 * 			if the Node is a index node or a storage node
	 * 
	 */
	public void createNode(String IP, boolean isIndex) throws ChordException {
		if(isIndex){
			IndexNode node = new IndexNode(IP);
			if(this.indexNodeMap.get(node.getNodeID()) != null){
				throw new ChordException("Duplicated Key: " + node);
			}
			if(this.NodeMap.get(IP) != null){
				throw new ChordException("Duplicated IP: " + node);
			}
			this.nodeList.add(node);
			this.indexNodeList.add(node);
			this.indexNodeMap.put(node.getNodeID(), node);
			this.NodeMap.put(IP, (Node)node);
			node.setAlive(true);
		}
		else{
			if(nodeList.isEmpty()){
				System.out.println("error: No network");
				return;
			}
			Random rn = new Random();
			int slink = rn.nextInt(this.indexNodeList.size());
			StorageNode node = new StorageNode(IP, this.indexNodeList.get(slink));
			if(this.NodeMap.get(IP) != null){
				throw new ChordException("Duplicated IP: " + node);
			}
			this.nodeList.add(node);
			this.NodeMap.put(IP, (Node)node);
			node.setAlive(true);
		}
		
	}
	
	public void addAllRDFFiletoNet(String path){
		for(int i = 0; i < nodeList.size(); i++){
			String filePath = String.format("%s%d.n3", path, i);
			nodeList.get(i).setRDFFile(filePath);
		}
		for(int i = 0; i < nodeList.size(); i++){
			nodeList.get(i).parseRDFtoNet();
			System.out.print('>');
		}
		System.out.print('\n');
	}
	
	public int getFrequencyByKey(String key){
		IndexNode centerNode = indexNodeList.get(0);
		IndexNode holderNode = centerNode.findSuccessor(key);
		return holderNode.getLocationTable().getSeq(key);
	}
	
	public IndexNode getIndexNodeByKey(String key){
		IndexNode centerNode = indexNodeList.get(0);
		return centerNode.findSuccessor(key);
	}
	
	public Node[] getRDFHoldersByKey(String key){
		IndexNode centerNode = indexNodeList.get(0);
		IndexNode holderNode = centerNode.findSuccessor(key);
		String[] ips = holderNode.getLocationTable().get(key);
		Node[] nodes = new Node[ips.length];
		for(int i = 0; i < ips.length; i++)
		{
			nodes[i] = NodeMap.get(ips[i]);
		}
			
		return nodes;
	}
	
	public Node getNode(int i){
		return this.nodeList.get(i);
	}
	
	public void exportNetData(){
		for(int i = 0; i < nodeList.size(); i++){
			if(nodeList.get(i).Index){
				((IndexNode)nodeList.get(i)).getLocationTable().exportLocationTable(nodeList.get(i).getIP());
			}
			nodeList.get(i).exportDataRecord();
		}
	}
	
	public void PrintIndexNodes(){
		for(int i = 0; i < indexNodeList.size(); i++){
			System.out.println(indexNodeList.get(i));
		}
	}

}
