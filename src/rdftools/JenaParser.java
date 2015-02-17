package rdftools;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.Statement;
import com.hp.hpl.jena.rdf.model.StmtIterator;
import com.hp.hpl.jena.util.FileManager;

public class JenaParser {
	private String path;
	private Model model;
	StmtIterator iter;
	public JenaParser(String file){
		this.path = file;
		model = ModelFactory.createDefaultModel();
		FileManager.get().readModel(model, path);
		iter = model.listStatements();
	}
	
	public boolean hasNext() {
		return iter.hasNext();
	}
	
	public String[] run() {
        // print out the predicate, subject and object of each statement
		Statement stmt = iter.nextStatement(); // get next statement
		Resource subject = stmt.getSubject(); // get the subject
		Property predicate = stmt.getPredicate(); // get the predicate
		RDFNode object = stmt.getObject(); // get the object
		String[] triple = {subject.toString(), predicate.toString(), object.toString()};
		return triple;
	}

}
