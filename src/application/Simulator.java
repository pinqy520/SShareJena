package application;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Scanner;
import java.util.Vector;
import java.util.Map.Entry;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


import network.ChordKey;
import network.Hash;
import network.NetWork;
import network.Node;

public class Simulator {

	public NetWork network = new NetWork();
	public String resultPath = "result/";
	protected HashMap<String, DataRecord> dataRecordMap = new HashMap<String, DataRecord>();

	
//	public void designedFile(String n3Path, int size, int index){
//		String outputPath = String.format("ex/%d", index);
//		
//	}
	
	public String[] splittoSPARQLGroup(String filepath){
	    String sparql="";
	    File file=new File(filepath);
	    try {
	        FileInputStream in=new FileInputStream(file);
	        // size  为字串的长度 ，这里一次性读完
	        int size=in.available();
	        byte[] buffer=new byte[size];
	        in.read(buffer);
	        in.close();
	        sparql=new String(buffer);
	    } catch (IOException e) {
	        // TODO Auto-generated catch block
	        e.printStackTrace();
	        return null;
	    }
	    return sparql.split("==========");
	}
	
	
	public void querySparql(String sparql, String outputFilename){
		QueryTransformResult sl = SPARQLtoQuery(sparql);
		sl = Optimize_Query(sl);
		Vector<String> filePathVec = procSubqueries(sl.WHERE.Sq, sl.PREFIX);
		String outputPath = resultPath + outputFilename;
		String[] filePathList = new String[filePathVec.size()];
		filePathVec.toArray(filePathList);
		String sparqlhash = new ChordKey(sparql).toString();
		long startMili=System.currentTimeMillis();
		network.getNode(0).query(filePathList, outputPath, sparql, "");
		long endMili=System.currentTimeMillis();
		if(dataRecordMap.get(sparqlhash) == null){
			DataRecord dr = new DataRecord();
			dr.time = endMili-startMili;
			File outputFile = new File(outputPath);
			dr.size = outputFile.length();
			dataRecordMap.put(sparqlhash, dr);
		}
		
	}
	
	public Vector<String> procSubqueries(Vector<SubQuery> subqueries, String PREFIX){
		Vector<String> fileVec = new Vector<String> ();
		String middleFile = "";
		String subqueryContent = "";
		String mixedKey = "";
		for(int i = 0; i < subqueries.size(); i++)
		{
			if(subqueries.get(i).role == -1)
			{
				Node[] relatedNodes = network.getRDFHoldersByKey(subqueries.get(i).key);
				String[] subqueriesResults;
				
				if(subqueryContent == ""){
					subqueriesResults = new String[relatedNodes.length];
				}else{
					subqueriesResults = new String[relatedNodes.length + 1];
				}
				String subquerysparql = PREFIX + "\nCONSTRUCT { " + subqueries.get(i).content + " } \n WHERE { " + subqueries.get(i).content + " }";
				String subqueryKey = new ChordKey(subqueries.get(i).key).toString();
				for(int n = 0; n < relatedNodes.length; n++){
					subqueriesResults[n] = relatedNodes[n].query(subquerysparql, subqueryKey);
				}
				
				if(subqueryContent != "")
					subqueriesResults[relatedNodes.length] = middleFile;
				
				String middleResult = String.format("%smiddle%d.n3", resultPath, i);
				subqueryContent += subqueries.get(i).content + ".\n";
				mixedKey += subqueries.get(i).key + "|";
				String mixedsparql = PREFIX + "\nCONSTRUCT { " + subqueryContent + " } \n WHERE { " + subqueryContent + " }";
				network.getIndexNodeByKey(subqueries.get(i).key).query(subqueriesResults, middleResult, mixedsparql, mixedKey);
				middleFile = middleResult;
			}else if(subqueries.get(i).role != 3){
				fileVec.addAll(procSubqueries(subqueries.get(i).Sq, PREFIX));
			}
		}
		fileVec.add(middleFile);
		return fileVec;
	}
	
public Vector<SubQuery> Analize_Content(String cont, Vector<prefix> plist){
		Vector<SubQuery> sqlist = new Vector<SubQuery>();
		cont = cont.trim();
		if(cont == "")
			return sqlist;
		Pattern subPat = Pattern.compile("(WHERE|UNION|OPTIONAL)\\s*(\\{([^\\}]*\\})+)([^\\}]*)");  
		Matcher subMat = subPat.matcher(cont);
		if(subMat.find()){
			String subType = subMat.group(1).trim();
			if(subType.equals("OPTIONAL")){
				SubQuery sq = new SubQuery();
				sq.content = subMat.group();
				sq.role = 2;
				sq.key = "";
				String temp = subMat.group(2);
				temp = temp.substring(1, temp.length() - 1).trim();
				sq.Sq = Analize_Content(temp, plist);
				cont = cont.replace(sq.content, "");
				sqlist.add(sq);
			}
			else if (subType.equals("UNION")){
				Pattern unionPat = Pattern.compile("(\\{([^\\}]*\\})+)\\s*UNION\\s*(\\{([^\\}]*\\})+)");  
				Matcher unionMat = unionPat.matcher(cont);
				if(unionMat.find()){
					//System.out.println(unionMat.group());
					SubQuery sq = new SubQuery();
					sq.content = unionMat.group();
					sq.key = "";
					sq.role = 1;
					sq.Sq = new Vector<SubQuery>();
					SubQuery u1 =  new SubQuery(), u2 = new SubQuery();
					String u1_temp = unionMat.group(1), u2_temp = unionMat.group(3);
					u1.content = u1_temp; u2.content = u2_temp;
					u1.key = u2.key = "";
					u1.role = u2.role = 4;
					u1_temp = u1_temp.substring(1, u1_temp.length() - 1).trim();
					u2_temp = u2_temp.substring(1, u2_temp.length() - 1).trim();
					u1.Sq = Analize_Content(u1_temp, plist); u2.Sq = Analize_Content(u2_temp, plist);
					sq.Sq.add(u1); sq.Sq.add(u2);
					sqlist.add(sq);
					cont = cont.replace(sq.content, "");
				}
				
			}
		}
		Pattern filterPat = Pattern.compile("FILTER\\s*\\([^\\(\\)]*(\\([^\\(\\)]*\\)[^\\(\\)]*)*\\)");
		Matcher filterMat = filterPat.matcher(cont);
		while (filterMat.find()){
			String filterInfo = filterMat.group();
			SubQuery sq = new SubQuery();
			sq.content = filterInfo;
			sq.key = "";
			sq.role = 3;
			sqlist.add(sq);
			cont = cont.replace(filterInfo, "");
		}
		Pattern triplePat = Pattern.compile("(((\".+\")?[^\\s]+)\\s+((\".+\")?[^\\s]+)\\s+((\".+\")?[^\\s]+))\\s*\\.?");
		Matcher tripleMat = triplePat.matcher(cont);
		while(tripleMat.find()){
			String s = tripleMat.group(2), p = tripleMat.group(4), o = tripleMat.group(6);
			SubQuery sq = new SubQuery();
			sq.key  = "";
			sq.content = tripleMat.group(1);
			sq.role = -1;
			if(s.charAt(0) != '?'){
				int x = s.indexOf(':');
				if(x!=-1){
					for(int u = 0; u<plist.size(); u++) s = s.replace(plist.get(u).name,plist.get(u).value);
				}
				sq.key = "s:" + s;
			}
			if(p.charAt(0) != '?'){
				int x = p.indexOf(':');
				if(x!=-1){
					for(int u = 0; u<plist.size(); u++) p = p.replace(plist.get(u).name,plist.get(u).value);
				}
				if(sq.key != "") sq.key = sq.key + "|";
				sq.key = sq.key + "p:" + p;
			}
			if(o.charAt(0) != '?'){
				int x = o.indexOf(':');
				if(x!=-1){
					for(int u = 0; u<plist.size(); u++) o = o.replace(plist.get(u).name,plist.get(u).value);
				}
				if(sq.key != "") sq.key = sq.key + "|";
				sq.key = sq.key + "o:" + o;
			}
			sq.key = sq.key.replaceAll("\"", "");
			sqlist.add(sq);
		}
		return sqlist;
	}
	
	
	public  QueryTransformResult SPARQLtoQuery(String Sparql){
		if(Sparql == "")
			return null;
		QueryTransformResult qtr = new QueryTransformResult();
		qtr.Query = Sparql;
		Pattern prefixpat = Pattern.compile("PREFIX\\s*(.+:)\\s*<(.+)>");  
		Matcher prefixmat = prefixpat.matcher(Sparql); 
		Vector<prefix> plist = new Vector<prefix>();
        while (prefixmat.find()) {
        	prefix newp = new prefix();
        	newp.name = prefixmat.group(1); 
        	newp.value = prefixmat.group(2); 
        	//System.out.println("("+ prefixmat.group() +")|" + newp.name+":"+newp.value); 
        	plist.add(newp);
	    }
		qtr.PREFIX = "";
		for(int i = 0; i < plist.size(); i++)
		{
			qtr.PREFIX += "PREFIX " + plist.get(i).name + " <" + plist.get(i).value + ">\n";
		}
		
		Pattern wherePat = Pattern.compile("(WHERE|UNION|OPTIONAL)\\s*(\\{([^\\}]*\\})+)([^\\}]*)");  
		Matcher whereMat = wherePat.matcher(Sparql); 
		qtr.WHERE.role = 0;
		if(whereMat.find()){
			qtr.WHERE.content = whereMat.group(); 
			qtr.Consider = whereMat.group(4);
			System.out.println(whereMat.group(4) ); 
			//System.out.println(whereMat.group(2) ); 
			String temp = whereMat.group(2);
			temp = temp.substring(1, temp.length() - 1);
			//System.out.println(temp); 
			qtr.WHERE.Sq = Analize_Content(temp, plist);
		}
		return qtr;
	}
	
	public QueryTransformResult Optimize_Query(QueryTransformResult q)
	{
		QueryTransformResult qtr = new QueryTransformResult();
		qtr.Query = q.Query;
		qtr.PREFIX = q.PREFIX;
		qtr.Consider = q.Consider;
		qtr.WHERE.content = q.WHERE.content;
		qtr.WHERE.key = q.WHERE.key;
		qtr.WHERE.role = q.WHERE.role;
		qtr.WHERE.Sq = Sort_SubQuery(q.WHERE.Sq);
		return qtr;
	}

	public Vector<SubQuery> Sort_SubQuery(Vector<SubQuery> s)
	{
		Vector<SubQuery> sq = new Vector<SubQuery>();
		class keySeq
		{
			keySeq(int n,int m)
			{
				this.No = n;
				this.Seq = m;
			}
			int No;
			int Seq;
		};
		Vector<keySeq> sort = new Vector<keySeq>();
		for(int i=0;i<s.size();i++)
		{
			if(s.get(i).role == -1)
			{
				int seq = network.getFrequencyByKey(s.get(i).key);
				keySeq ks = new keySeq(i,seq);
				sort.add(ks);
			}
		}
		for(int i=sort.size();i>0;i--)
		{
			for(int j=1;j<i;j++)
			{
				if(sort.get(j-1).Seq > sort.get(j).Seq)
				{
					keySeq temp = sort.get(j-1);
					sort.set(j-1, sort.get(j));
					sort.set(j, temp);
				}
			}
		}
		for(int i=0;i<s.size();i++)
		{
			if(s.get(i).role != -1)
			{	
				if(s.get(i).role == 3)
				{
					sq.add(s.get(i));
				}
				else
				{
					SubQuery stemp = new SubQuery();
					stemp.content = s.get(i).content;
					stemp.key = s.get(i).content;
					stemp.role = s.get(i).role;
					stemp.Sq = Sort_SubQuery(s.get(i).Sq);
					sq.add(stemp);
				}
			}
		}
		for(int i=0;i<sort.size();i++)
		{
			int num = sort.get(i).No;
			sq.add(s.get(num));
		}
		return sq;
	}

	
	public class prefix
	{
		String name;
		String value;
	};

	public class SubQuery
	{
		String content;
		String key;
		int role; // triple:-1 ; where:0 ; union:1 ; optional:2 ; filter:3 ; conjunction:4
		Vector<SubQuery> Sq = new Vector<SubQuery>();
	};

	public class QueryTransformResult
	{
		String Query;
		String PREFIX;
		SubQuery WHERE = new SubQuery();
		String Consider;
	};
	
	public void exportSimData(){
		this.network.exportNetData();
		String exportPath = "export/common.drt";
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
