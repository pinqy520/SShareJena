package network;

import rdftools.JenaParser;

public class StorageNode extends Node{
	private IndexNode linkTo;
	public StorageNode(String IP, IndexNode know){
		this.IP = IP;
		this.Index = false;
		this.alive = false;
		this.linkTo = know;
	}
	
	public void resetLink(IndexNode link){
		this.linkTo = link;
	}
	
	public IndexNode showIink(){
		return this.linkTo;
	}
	
	public void parseRDFtoNet(){
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
				IndexNode indexnode = linkTo.findSuccessor(keyGroup[i]);
				indexnode.storeRDFKey(keyGroup[i], this.IP);
			}
		}
	}

}
