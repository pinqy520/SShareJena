package rdftools;

import java.io.FileNotFoundException;
import java.io.PrintStream;

import com.hp.hpl.jena.query.Query;
import com.hp.hpl.jena.query.QueryExecution;
import com.hp.hpl.jena.query.QueryExecutionFactory;
import com.hp.hpl.jena.query.QueryFactory;
import com.hp.hpl.jena.query.ResultSet;
import com.hp.hpl.jena.query.ResultSetFormatter;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;

public class JenaQuery {
	
	public JenaQuery(){
		
	}
	
	public void query(String[] input, String output, String sparql ){
		int isSelect = sparql.indexOf("SELECT");
		int isConstruct = sparql.indexOf("CONSTRUCT");
		if(isSelect == -1 && isConstruct == -1)
			return;
		if(isSelect != -1 && isConstruct == -1)
			this.querySELECT(input, output, sparql);
		if(isSelect == -1 && isConstruct != -1)
			this.queryCONSTRUCT(input, output, sparql);
		if(isSelect != -1 && isConstruct != -1)
			if(isSelect < isConstruct)
				this.querySELECT(input, output, sparql);
			else
				this.queryCONSTRUCT(input, output, sparql);
		
	}
	
	private void querySELECT(String[] input, String output, String sparql ){
		Model model = ModelFactory.createDefaultModel();
		PrintStream out;
		try {
			out = new PrintStream(output);
			for(int i = 0; i < input.length; i++){
				model.read(input[i]);
			}
			Query query = QueryFactory.create(sparql);
			QueryExecution qexec = QueryExecutionFactory.create(query,model);
			ResultSet results = qexec.execSelect();
			ResultSetFormatter.out(out, results);
			qexec.close();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	private void queryCONSTRUCT(String[] input, String output, String sparql ){
		
		Model model = ModelFactory.createDefaultModel();
		PrintStream out;
		try {
			out = new PrintStream(output);
			for(int i = 0; i < input.length; i++){
				model.read(input[i]);
			}
			Query query = QueryFactory.create(sparql);
			QueryExecution qexec = QueryExecutionFactory.create(query,model);
			Model results = qexec.execConstruct();
			results.write(out, "N3");
			qexec.close();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}
