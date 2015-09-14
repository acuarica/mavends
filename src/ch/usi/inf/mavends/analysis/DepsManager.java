package ch.usi.inf.mavends.analysis;

import java.io.File;
import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import ch.usi.inf.mavends.index.PomDependency;

public class DepsManager {

	public static List<PomDependency> extractDeps(String pomPath) throws SAXException, IOException,
			ParserConfigurationException {
		SAXParserFactory spf = SAXParserFactory.newInstance();

		final List<PomDependency> deps = new ArrayList<PomDependency>();

		File f = new File(pomPath);
		SAXParser p = spf.newSAXParser();

		p.parse(f, new DefaultHandler() {
			private PomDependency dep;
			private String value;

			@Override
			public InputSource resolveEntity(String publicId, String systemId) throws IOException, SAXException {
				return new InputSource(new StringReader(""));
			}

			@Override
			public void startElement(String uri, String localName, String qName, Attributes attributes)
					throws SAXException {
				if (qName.equals("dependency")) {
					dep = new PomDependency();
				}
			}

			@Override
			public void characters(char[] ch, int start, int length) throws SAXException {
				value = new String(ch, start, length);
			}

			@Override
			public void endElement(String uri, String localName, String qName) throws SAXException {
				if (qName.equals("dependency")) {
					if (dep != null) {
						deps.add(dep);
						dep = null;
					}
				} else if (dep != null && qName.equals("groupId")) {
					dep.groupId = value;
				} else if (dep != null && qName.equals("artifactId")) {
					dep.artifactId = value;
				} else if (dep != null && qName.equals("version")) {
					dep.version = value;
				} else if (dep != null && qName.equals("scope")) {
					dep.scope = value;
				}
			}
		});

		return deps;
	}
}
