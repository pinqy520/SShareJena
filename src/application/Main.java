package application;

import java.util.Scanner;

import network.ChordException;
import network.IndexNode;

public class Main {
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// input setting
		Scanner keyboardInput = new Scanner(System.in);
		System.out.println("How many index nodes?:\n>");
		int indexnodeNum = keyboardInput.nextInt();
		System.out.println("How many storage nodes?:\n>");
		int storagenodeNum = keyboardInput.nextInt();
		keyboardInput.close();
		Simulator sysSimulator = new Simulator();
		
		byte[] iparray = {10, 1, 1, 1};
		int ipPos = 3;
		
		for(int i = 0; i < indexnodeNum; i++){
			String ipaddressString = String.format("%d.%d.%d.%d", iparray[0], iparray[1], iparray[2], iparray[3]);
			try {
				sysSimulator.network.createNode(ipaddressString, true);
			} catch (ChordException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			while(iparray[ipPos] == 254){
				iparray[ipPos] = 1;
				ipPos--;
			}
			iparray[ipPos]++;
			ipPos = 3;
		}
		
		for (int i = 1; i < indexnodeNum; i++) {
			IndexNode node = (IndexNode)sysSimulator.network.getNode(i);
			node.join((IndexNode)sysSimulator.network.getNode(0));
			node.fixFingers();
			node.getSuccessor().notifyPredecessor(node);
			node.getSuccessor().fixFingers();
			for(int j=0;j<i;j++){                        
				((IndexNode)sysSimulator.network.getNode(j)).stabilize();         
				
				((IndexNode)sysSimulator.network.getNode(j)).fixFingers();
			}
			//node.fixFingers(out);
		
		}
		
		for(int i = 0; i < storagenodeNum; i++){
			String ipaddressString = String.format("%d.%d.%d.%d", iparray[0], iparray[1], iparray[2], iparray[3]);
			try {
				sysSimulator.network.createNode(ipaddressString, false);
			} catch (ChordException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			while(iparray[ipPos] == 254){
				iparray[ipPos] = 1;
				ipPos--;
			}
			iparray[ipPos]++;
			ipPos = 3;
		}
		
		sysSimulator.network.addAllRDFFiletoNet("ex/0/");
		
		String[] sparqlGroup = sysSimulator.splittoSPARQLGroup("ex/sparql.query");
		
		for(int i = 0; i< sparqlGroup.length; i++)
		{
			sparqlGroup[i] = sparqlGroup[i].trim();
			String outputFilename = String.format("finalresult-%d-of-%d.txt", i+1, sparqlGroup.length);
			sysSimulator.querySparql(sparqlGroup[i],outputFilename);
			System.out.println(outputFilename);
		}
		
		System.out.println("Exporting data!");
		sysSimulator.exportSimData();
		System.out.println("Finished!");
		//sysSimulator.network.PrintIndexNodes();

	}

}
