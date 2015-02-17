package network;

import java.io.RandomAccessFile;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map.Entry;
import java.util.Vector;


public class LocationTable {
//	private String tablePath;
//	private RandomAccessFile tableFile;
	private HashMap<String, KeyInfo> keyIndex;
	
	public LocationTable(String IP){
//		this.tablePath = "tables/"+IP+"_lt.txt";
//		try{
//			this.tableFile = new RandomAccessFile(this.tablePath,"rw");
//		}catch(Exception e){
//			System.out.println("new file error");
//			System.exit(0);
//		}
		keyIndex = new HashMap<String, KeyInfo>();
	}
	
	public void put(String key, String IP){
		KeyInfo keyinfo = keyIndex.get(key);
		if(keyinfo != null){
				if(keyinfo.ipList.size() != keyinfo.seqenceList.size())
				{
					System.out.println("key: "+key+" , table error!");
					System.exit(0);	
				}
				for(int i = 0; i < keyinfo.ipList.size(); i++)
				{
					//this.tableFile.seek(keyinfo.pos.get(i));
					String Snode = keyinfo.ipList.get(i);
					if(Snode.equals( IP)){
						keyinfo.seqenceList.set(i, keyinfo.seqenceList.get(i)+1);
					}else{
						if(i+1 == keyinfo.ipList.size()){
							keyinfo.ipList.add(IP);
							keyinfo.seqenceList.add(1);
							//this.tableFile.seek(this.tableFile.length());
							//this.tableFile.writeBytes(IP+"\n");
						}
					}
				}
		}else{
			keyinfo = new KeyInfo();
			keyinfo.ipList.add(IP);
			keyinfo.seqenceList.add(1);
				//this.tableFile.seek(this.tableFile.length());
				//this.tableFile.writeBytes(IP+"\n");
			this.keyIndex.put(key, keyinfo);
		}
	}
	
	public String[] get(String key){
		KeyInfo keyinfo = keyIndex.get(key);
		if(keyinfo != null){
			if(keyinfo.ipList.size() != keyinfo.seqenceList.size())
			{
				System.out.println("key: "+key+" , table error!");
				System.exit(0);	
			}
			String[] IPs = new String[keyinfo.ipList.size()];
			for(int i = 0; i < IPs.length; i++){
					//this.tableFile.seek(keyinfo.pos.get(i));
					IPs[i] = keyinfo.ipList.get(i);
			}
			return IPs;
		}
		return null;
	}
	
	public int getSeq(String key){
		KeyInfo keyinfo = keyIndex.get(key);
		if(keyinfo != null){
			int seq = 0;
			for(int i = 0; i < keyinfo.seqenceList.size(); i++){
				seq += keyinfo.seqenceList.get(i);
			}
			return seq;
		}
		return 0;
	}
	
	public void exportLocationTable(String thisip){
		String exportPath = "export/" + thisip + ".lt";
		RandomAccessFile exportFile;
		try{
			exportFile = new RandomAccessFile(exportPath,"rw");
			Iterator<Entry<String, KeyInfo>> iter = keyIndex.entrySet().iterator();
			while (iter.hasNext()) {
				Entry<String, KeyInfo> entry = iter.next();
				String key = entry.getKey();
				KeyInfo keyinfo = entry.getValue();
				if(keyinfo.ipList.size() != keyinfo.seqenceList.size())
				{
					System.out.println("key: "+key+" , table error!");
					System.exit(0);	
				}
				//String[] IPs = new String[keyinfo.pos.size()];
				for(int i = 0; i < keyinfo.ipList.size(); i++){
					//try{
						//this.tableFile.seek(keyinfo.pos.get(i));
						String getIp = keyinfo.ipList.get(i);
						int getFre = keyinfo.seqenceList.get(i);
						String writeLocationItem = key + "\t" + getIp + "\t" + String.format("%d\n", getFre);
						exportFile.seek(exportFile.length());
						exportFile.writeBytes(writeLocationItem);
					//}catch(Exception e){
						//System.out.println("get error!");
						//System.exit(0);			
					//}
				}
			}		
		}catch(Exception e){
			System.out.println("new file error");
			System.exit(0);
		}
	}
	
	public class KeyInfo{
		public Vector<String> ipList = new Vector<String>();
		public Vector<Integer> seqenceList = new Vector<Integer>();
	}
	
	public class LocationItem {
		public ChordKey key;
		public long itemPos;
		
	}

}
