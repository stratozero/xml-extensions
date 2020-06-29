package it.mormao.tools.xml;

import org.xml.sax.InputSource;

import javax.xml.namespace.NamespaceContext;
import javax.xml.namespace.QName;
import javax.xml.xpath.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * IMPORTANT: This class is thread-safe and is intended for use cases where
 * there is a lot of reuse of the same xpath expressions, having them compiled and cached
 * for multiple reuse. <br>
 * Every XPath object has its own map of compiled xpath expressions, so to use the provided cache
 * the application have to reuse the same {@link CachedXPath} object.
 * For the same reason, in cases where there is high use of parallelism it is a good idea to use a {@link java.lang.ThreadLocal}&lt;{@link CachedXPath}&gt; instead.
 * It uses a granular synchronization on every generated XPathExpression, which makes it fits
 * better for cases where there is little parallelism and lots of repeated xpath evaluation
 */
public class CachedXPath implements XPath {
	private static final XPathFactory XPF = XPathFactory.newInstance();
	private final XPath xPath = XPF.newXPath();
	private final ConcurrentHashMap<String, XPathExpression> xpMap = new ConcurrentHashMap<>();

	@Override
	public void reset() {
		xPath.reset();
	}

	@Override
	public void setXPathVariableResolver(XPathVariableResolver resolver) {
		xPath.setXPathVariableResolver(resolver);
	}

	@Override
	public XPathVariableResolver getXPathVariableResolver() {
		return xPath.getXPathVariableResolver();
	}

	@Override
	public void setXPathFunctionResolver(XPathFunctionResolver resolver) {
		xPath.setXPathFunctionResolver(resolver);
	}

	@Override
	public XPathFunctionResolver getXPathFunctionResolver() {
		return xPath.getXPathFunctionResolver();
	}

	@Override
	public void setNamespaceContext(NamespaceContext nsContext) {
		xPath.setNamespaceContext(nsContext);
	}

	@Override
	public NamespaceContext getNamespaceContext() {
		return xPath.getNamespaceContext();
	}

	@Override
	public synchronized XPathExpression compile(String expression) throws XPathExpressionException {
		return xPath.compile(expression);
	}

	private XPathExpression getXPathExpression(String expression) throws XPathExpressionException{
		XPathExpression xpe;
		if(xpMap.containsKey(expression))
			xpe = xpMap.get(expression);
		else {
			reset();
			xpe = compile(expression);
			xpMap.put(expression, xpe);
		}
		return xpe;
	}

	@Override
	public Object evaluate(String expression, Object item, QName returnType) throws XPathExpressionException {
		XPathExpression exp = getXPathExpression(expression);
		synchronized(exp) {
			return exp.evaluate(item, returnType);
		}
	}

	@Override
	public String evaluate(String expression, Object item) throws XPathExpressionException {
		XPathExpression exp = getXPathExpression(expression);
		synchronized(exp) {
			return exp.evaluate(item);
		}
	}

	@Override
	public Object evaluate(String expression, InputSource source, QName returnType) throws XPathExpressionException {
		XPathExpression exp = getXPathExpression(expression);
		synchronized(exp) {
			return exp.evaluate(source, returnType);
		}
	}

	@Override
	public String evaluate(String expression, InputSource source) throws XPathExpressionException {
		XPathExpression exp = getXPathExpression(expression);
		synchronized(exp) {
			return exp.evaluate(source);
		}
	}
}
