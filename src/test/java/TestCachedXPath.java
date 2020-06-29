import it.mormao.tools.xml.CachedXPath;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.xml.sax.InputSource;

import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;
import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;

public class TestCachedXPath {
	@Test
	public void extractSimpleXPath() throws XPathExpressionException {
		XPath xp = new CachedXPath();
		Reader rdr = new StringReader("<document><meta id=\"111\">AAA</meta></document>");
		InputSource source = new InputSource(rdr);
		String extracted =  xp.evaluate("//meta[1]", source);
		//System.out.println("read value: " + extracted);
		Assertions.assertEquals("AAA", extracted);
	}

	@Test
	public void compareXPath() throws XPathExpressionException, IOException {
		XPath cachedXPath = new CachedXPath();
		XPath xp = XPathFactory.newInstance().newXPath();
		String xml = "<document><meta id=\"111\">AAA</meta></document>";
		Reader rdr = new StringReader(xml);
		InputSource source = new InputSource(rdr);
		String extracted =  cachedXPath.evaluate("//meta[1]", source);
		rdr = new StringReader(xml);
		source = new InputSource(rdr);
		String extracted2 =  xp.evaluate("//meta[1]", source);
		//System.out.println("read value: " + extracted);
		Assertions.assertEquals(extracted2, extracted);
	}
}
