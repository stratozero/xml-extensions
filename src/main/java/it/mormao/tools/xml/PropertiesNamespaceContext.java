package it.mormao.tools.xml;

import javax.xml.namespace.NamespaceContext;
import java.io.IOException;
import java.io.Reader;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.Map;
import java.util.Properties;


@SuppressWarnings("unused")
public class PropertiesNamespaceContext implements NamespaceContext {
	private static Path PROP_FILE_PATH = getDefaultPath();
	private static final Properties GLOBAL_NAMESPACES = new Properties();
	private static final Object LOCK = new Object();
	private static long lastUpdate = 0L;

	// Using a local copy prevent concurrent modification to alter the running instance behaviour
	private final Properties localProp;

	private static Path getDefaultPath(){
		Path p = null;
		try {
			p = Paths.get(Thread.currentThread().getContextClassLoader().getResource("namespace.properties").toURI());
		} catch (URISyntaxException e) {
			e.printStackTrace();
		}
		return p;
	}

	// Default constructor
	{
		// each instance forces the global property to check for modification from disk and eventually reload, then clone global properties
		reloadIfNeeded();
		localProp = (Properties) GLOBAL_NAMESPACES.clone();
	}

	public void reloadIfNeeded(){
		try {
			long curLastUpdate = Files.getLastModifiedTime(PROP_FILE_PATH).toMillis();
			if(lastUpdate < curLastUpdate){
				synchronized (LOCK){
					try(Reader rd = Files.newBufferedReader(PROP_FILE_PATH, StandardCharsets.UTF_8)) {
						GLOBAL_NAMESPACES.load(rd);
						lastUpdate = curLastUpdate;
					}
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public static void setDefualtPath(Path p){
		if (p != null && Files.isRegularFile(p)) {
			// synchronization prevent concurrent modification on same object
			synchronized (LOCK) {
				PROP_FILE_PATH = p;
			}
		}
	}

	public static void resetDefaultPath(){
		synchronized (LOCK){
			PROP_FILE_PATH = getDefaultPath();
		}
	}

	@Override
	public String getNamespaceURI(String prefix) {
		return localProp.getProperty(prefix);
	}

	@Override
	public String getPrefix(String namespaceURI) {
		for (Map.Entry<Object, Object> entry : localProp.entrySet()) {
			if (entry.getValue().equals(namespaceURI))
				return entry.getKey().toString();
		}
		return null;
	}

	@Override
	public Iterator<String> getPrefixes(String namespaceURI) {
		return new EnumerationToIterator(localProp.propertyNames());
	}

	private static class EnumerationToIterator implements Iterator<String> {
		Enumeration<?> enumeration;

		EnumerationToIterator(Enumeration<?> enumeration) {
			this.enumeration = enumeration;
		}

		@Override
		public boolean hasNext() {
			return enumeration.hasMoreElements();
		}

		@Override
		public String next() {
			return String.valueOf(enumeration.nextElement());
		}

		@Override
		public void remove() {
			throw new IllegalStateException("Cannot remove element from enumeration");
		}
	}
}
