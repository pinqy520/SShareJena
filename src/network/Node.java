package network;

import java.io.File;
import java.io.RandomAccessFile;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map.Entry;

import application.DataRecord;
import rdftools.JenaQuery;

public abstract class Node {
	protected String IP;
	protected boolean alive;
	protected boolean Index;
	protected String file;
	protected JenaQuery queryTool = new JenaQuery();
	protected HashMap<String, DataRecord> dataRecordMap = new HashMap<String, DataRecord>();
	
	/**
	 * Checking if the node is in the network
	 * 
	 * 
	 */
	public boolean isAlive(){
		return this.alive;
	}
	
	public abstract void parseRDFtoNet();
	
	public void query(String[] input, String output, String sparql, String keytoStore ){
		long startMili=System.currentTimeMillis();
		queryTool.query(input, output, sparql);
		long endMili=System.currentTimeMillis();
		if(dataRecordMap.get(keytoStore) == null&&!keytoStore.isEmpty()){
			DataRecord dr = new DataRecord();
			dr.time = endMili-startMili;
			File outputFile = new File(output);
			dr.size = outputFile.length();
			dataRecordMap.put(keytoStore, dr);
		}
	}
	
	public String query(String sparql, String keytoStore){
		String output = "result/node_" + IP + ".n3";
		String[] input = {file};
		long startMili=System.currentTimeMillis();
		queryTool.query(input, output, sparql);
		long endMili=System.currentTimeMillis();
		if(dataRecordMap.get(keytoStore) == null){
			DataRecord dr = new DataRecord();
			dr.time = endMili-startMili;
			File outputFile = new File(output);
			dr.size = outputFile.length();
			dataRecordMap.put(keytoStore, dr);
		}
		return output;
	}
	
	/**
	 * Set the Node's RDF source
	 */
	public void setRDFFile(String path){
		this.file = path;
	}
	
	/**
	 * Get the RDF Data of the node 
	 */
	public String getRDFFile(){
		return this.file;
	}
	
	/**
	 * when node leave or join a network 
	 */
	public void setAlive(boolean state){
		this.alive = state;
	}
	
	/**
	 * Get the IP address of the node 
	 */
	public String getIP(){
		return this.IP;
	}
	
	/**
	 * Check if the node is a index node
	 */
	public boolean isIndex(){
		return this.Index;
	}
	
	public void exportDataRecord(){
		String exportPath = "export/" + this.IP + ".drt";
		RandomAccessFile exportFile;
		try{
			exportFile = new RandomAccessFile(exportPath,"rw");
			Iterator<Entry<String, DataRecord>> iter = dataRecordMap.entrySet().iterator();
			while (iter.hasNext()) {
				Entry<String, DataRecord> entry = iter.next();
				String key = entry.getKey();
				DataRecord datar = entry.getValue();
				try{

					String writeDataRecord = key + "\t" + String.format("%d", datar.size) + "\t" + String.format("%d\n", datar.time);
					exportFile.seek(exportFile.length());
					exportFile.writeBytes(writeDataRecord);
				}catch(Exception e){
					System.out.println("write error!");
					System.exit(0);			
				}
			}		
		}catch(Exception e){
			System.out.println("new file error");
			System.exit(0);
		}
	}

}
