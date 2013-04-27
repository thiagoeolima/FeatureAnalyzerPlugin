package br.ufal.ic.featureanalyzer.models;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.JDOMException;
import org.jdom2.input.SAXBuilder;

import br.ufal.ic.featureanalyzer.util.Log;

public class XMLParserTypeChef {
	private SAXBuilder builder;
	private File xmlFile;
	private List<Log> logList;

	public XMLParserTypeChef() {
		builder = new SAXBuilder();
		logList = new ArrayList<Log>();
	}

	public void setXMLFile(File xmlFile) {
		this.xmlFile = xmlFile;
	}

	public void processFile() {
		logList.clear();
		try {
			Document document = builder.build(xmlFile);
			Element rootNode = document.getRootElement();

			TypeErroProcessFile(rootNode, "typeerror");
			TypeErroProcessFile(rootNode, "parsererror");

		} catch (IOException io) {
			System.out.println(io.getMessage());
		} catch (JDOMException jdomex) {
			System.out.println(jdomex.getMessage());
		}
	}

	private void TypeErroProcessFile(Element rootNode, String type) {

		List<Element> list = rootNode.getChildren(type);

		for (int i = 0; i < list.size(); i++) {
			Element node = list.get(i);
			logList.add(new Log(node.getChild("position").getChildText("file"),
					node.getChild("position").getChildText("line"), node
							.getChild("position").getChildText("col"), node
							.getChildText("featurestr"), node
							.getChildText("severity"), node.getChildText("msg")));
		}

	}

	public List<Log> getLogList() {
		return logList;
	}

	public Object[] getLogs() {
		return logList.toArray(new Log[logList.size()]);
	}
}
