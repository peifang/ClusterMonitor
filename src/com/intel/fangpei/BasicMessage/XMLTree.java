package com.intel.fangpei.BasicMessage;

import java.io.File;
import java.io.IOException;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

public class XMLTree {
	
	static String column = "hbase";
	static String data = "report.windows.num";
	public static void main(String[] args) {
		try {
			DocumentBuilderFactory factory = DocumentBuilderFactory
					.newInstance();
			DocumentBuilder db = factory.newDocumentBuilder();
			Document doc = db.parse(new File(
					"C:\\Users\\Administrator\\Desktop\\Monitor_conf.xml"));
			Element elmtInfo = doc.getDocumentElement();
			NodeList nodes = elmtInfo.getChildNodes();
			for (int i = 0; i < nodes.getLength(); i++) {
				Node result = nodes.item(i);
				if (result.getNodeType() == Node.ELEMENT_NODE
						&& result.getNodeName().equals(column)) {
					NodeList ns = result.getChildNodes();

					for (int j = 0; j < ns.getLength(); j++) {
						Node record = ns.item(j);

						if (record.getNodeType() == Node.ELEMENT_NODE
								&& record.getNodeName().equals(data)) {
							System.out.println(record.getTextContent());
						}
					}
				}
			}
		} catch (ParserConfigurationException e) {
			e.printStackTrace();
		} catch (SAXException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

}
