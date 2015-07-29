package utils;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.apache.commons.io.IOUtils;
import org.jdom2.Document;
import org.jdom2.JDOMException;
import org.jdom2.input.SAXBuilder;
import org.jdom2.output.Format;
import org.jdom2.output.XMLOutputter;

import com.intuit.tame.common.utility.XmlEditor;

public class TestUtil {

	public static String[][] mapToTwoDarray(Map<?, ?> map) {

		Object[][] twoDarray = new String[map.size()][2];

		Object[] keys = map.keySet().toArray();
		Object[] values = map.values().toArray();

		for (int row = 0; row < twoDarray.length; row++) {
			twoDarray[row][0] = keys[row];
			twoDarray[row][1] = values[row];
		}

		return (String[][]) twoDarray;
	}

	public String replaceXmlTree(String xmlpath, String xpath, String tree)
			throws JDOMException, IOException {
		InputStream input = getClass().getResourceAsStream(xmlpath);
		String in, new_xml = null;
		try {
			in = IOUtils.toString(input);
			new_xml = XmlEditor.replace(in, xpath, tree);
		} catch (IOException e) {
			e.printStackTrace();
		}
		return normalXML(new_xml);
	}

	public String updateXmlNodeValue(String node, String value) {
		return "<" + node + ">" + value + "</" + node + ">";
	}

	public String insertXmlTree(String xml, String xpath, String tree)
			throws JDOMException, IOException {
		InputStream input = getClass().getResourceAsStream(xml);
		String in, new_xml = null;
		try {
			in = IOUtils.toString(input);
			new_xml = XmlEditor.addChild(in, xpath, tree);
		} catch (IOException e) {
			e.printStackTrace();
		}
		return normalXML(new_xml);
	}

	public String insertXmlTree(String xml, String xpath, int i, String tree)
			throws JDOMException, IOException {
		InputStream input = getClass().getResourceAsStream(xml);
		String in, new_xml = null;
		try {
			in = IOUtils.toString(input);
			new_xml = XmlEditor.addChild(in, xpath, tree);
		} catch (IOException e) {
			e.printStackTrace();
		}
		return normalXML(new_xml);
	}

	public static String[][] addData(String[][] s) {
		List<String[][]> mmData = new ArrayList<String[][]>();
		mmData.add(s);
		String[][] mmDataNew = (String[][]) mmData.toArray();
		return mmDataNew;
	}

	public static String[][] addData(String[][] obj, String[][] s) {
		List<String[][]> mmData = new ArrayList<String[][]>();
		mmData.add(s);
		String[][] mmDataNew = (String[][]) mmData.toArray();
		return mmDataNew;
	}

	public String getIntuitId() {
		return UUID.randomUUID().toString();
	}

	/**
	 * Format XML string
	 */
	public String normalXML(String xmlString) throws JDOMException,
			IOException {
		xmlString = xmlString.replaceAll("\u0009", "");
		xmlString = xmlString.replaceAll("\u0006", "");

		Document doc = new SAXBuilder().build(new ByteArrayInputStream(
				xmlString.getBytes()));
		XMLOutputter outputter = new XMLOutputter(Format.getPrettyFormat());

		return outputter.outputString(doc);
	}

	/**
	 * Format XML InputStream
	 * 
	 */
	public InputStream normalXMLInputStream(InputStream input)
			throws JDOMException, IOException {
		String str = IOUtils.toString(input);
		return IOUtils.toInputStream(normalXML(str));
	}

}
