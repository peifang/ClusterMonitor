package com.intel.fangpei.Schema;

import java.io.File;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Vector;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class ReferenceTree {
	private GenerateFunction GF = new GenerateFunction();
	private File schema = null;
	private LinkedList<node> root = new LinkedList<node>();
	private LinkedList<node> need_refresh = new LinkedList<node>();
	private HashMap<String,String> otherProperties = new HashMap<String,String>();

	public ReferenceTree(File schema) {
		this.schema = schema;
	}

	public boolean CreateTree() {
		Vector<String> vec = new Vector<String>();
		try {
			DocumentBuilderFactory factory = DocumentBuilderFactory
					.newInstance();
			DocumentBuilder db = factory.newDocumentBuilder();
			Document doc = db.parse(schema);
			Element elmtInfo = doc.getDocumentElement();
			NodeList nodes = elmtInfo.getChildNodes();
			for (int i = 0; i < nodes.getLength(); i++) {
				Node result = nodes.item(i);
				if (result.getNodeType() == Node.ELEMENT_NODE
						&& result.getNodeName().equals("name")) {
					String s = result.getTextContent();
					vec.add(s);
				}
				if (result.getNodeType() == Node.ELEMENT_NODE
						&& result.getNodeName().equals("family")) {
					otherProperties.put("family",result.getTextContent());
				}
				if (result.getNodeType() == Node.ELEMENT_NODE
						&& result.getNodeName().equals("column")) {
					otherProperties.put("column",result.getTextContent());
				}
			}
		} catch (Exception e) {
		}
		Iterator<String> i = vec.iterator();
		while (i.hasNext()) {
			String s = i.next();
			if (s.startsWith("@")) {
				String[] num = s.split("@");
				int parameternum = num.length;
				int paras = 0;
				node n = new node();
				n.nodes = new LinkedList<node>();
				root.add(n);
				while (parameternum-- > 0) {
					if (!num[num.length - parameternum - 1].equals(""))
						paras = Integer.parseInt(num[num.length - parameternum
								- 1]);
					else
						continue;
					Iterator<node> findduplicate = need_refresh.iterator();
					node tmp = null;
					boolean isfinddupilcate = false;
					while (findduplicate.hasNext()) {
						tmp = findduplicate.next();
						if (tmp.formula == paras) {
							n.nodes.add(tmp);
							isfinddupilcate = true;
							break;
						}
					}
					if (!isfinddupilcate) {
						node n2 = new node(paras);
						n.nodes.add(n2);
						addrefresh(n2);
					}
				}
			} else {
				node n = new node(s);
				root.add(n);
			}
		}
		return true;
	}

	public void addRootNode(node n) {
		root.add(n);
	}

	public boolean addNode(node rootnode, node n) {
		if (root.contains(rootnode)) {
			if (rootnode.nodes != null) {
				rootnode.nodes.add(n);
			} else
				return false;
		}
		return false;
	}

	public boolean addNode(node rootnode, node n, int place) {
		if (root.contains(rootnode)) {
			if (rootnode.nodes != null && rootnode.nodes.size() > place) {
				rootnode.nodes.add(place, n);
			} else
				return false;
		}
		return false;
	}

	public void addrefresh(node n) {
		if (!need_refresh.contains(n))
			need_refresh.add(n);
	}

	public String getdata() {
		node n = null;
		Iterator<node> i = need_refresh.iterator();
		while (i.hasNext()) {
			n = i.next();
			n.refresh();
		}
		i = root.iterator();
		String building = "";
		while (i.hasNext()) {
			building += i.next().GetData();
		}
		return building;
	}
	public String getAttribute(String attr){
		return otherProperties.get(attr);
	}
	class node {
		String section = "";
		int formula = 0;
		LinkedList<node> nodes = null;

		public node() {

		}

		public node(String section) {
			this.section = section;
		}

		public node(int formula) {
			this.formula = formula;
		}

		public void refresh() {
			section = "" + GF.Gen(formula);
		}

		public String GetData() {
			if (nodes == null) {
				return section;
			} else {
				String building = "";
				Iterator<node> i = nodes.iterator();
				while (i.hasNext()) {
					building += i.next().GetData();
				}
				return section + building;
			}
		}
	}
}
