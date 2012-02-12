package cef.egliseactu.utils;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class DOMXmlUtils {
	public static Element getFirstElement(Node parent) {
		if (parent == null)
			return null;
		Node n = parent.getFirstChild();
		while (n != null && Node.ELEMENT_NODE != n.getNodeType()) {
			n = n.getNextSibling();
		}
		if (n == null) {
			return null;
		}
		return (Element) n;
	}

	public static Element getFirstElementByTagName(Element parent, String tagName) {
		if (parent == null)
			return null;
		NodeList n = parent.getElementsByTagName(tagName);
		if (n == null || n.getLength() == 0) {
			return null;
		}
		return (Element) n.item(0);
	}
	
	public static Element getFirstElementByTagName(Document parent, String tagName) {
		if (parent == null)
			return null;
		return getFirstElementByTagName(parent.getDocumentElement(), tagName);
	}

	
	public static Element getNextElement(Element el) {
		if (el == null)
			return null;
		Node nd = el.getNextSibling();
		while (nd != null) {
			if (nd.getNodeType() == Node.ELEMENT_NODE) {
				return (Element) nd;
			}
			nd = nd.getNextSibling();
		}
		return null;
	}

	public static String getElementText(Element e) {
		if (e == null)
			return null;
		StringBuffer buf = new StringBuffer();
		for (Node n = e.getFirstChild(); n != null; n = n.getNextSibling()) {
			if (n.getNodeType() == Node.TEXT_NODE
					|| n.getNodeType() == Node.CDATA_SECTION_NODE) {
				buf.append(n.getNodeValue());
			}
		}
		return buf.toString();
	}

	public static String getAttribute(Node node, String name) {
		if (node == null)
			return null;
		Node namedItem = node.getAttributes().getNamedItem(name);
		if (namedItem != null)
			return namedItem.getNodeValue();
		return null;
	}

}