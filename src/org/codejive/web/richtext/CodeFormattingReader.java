package org.codejive.web.richtext;

import java.util.logging.Logger;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

import org.codejive.common.CodejiveException;
import org.codejive.common.xml.DomWriter;
import org.codejive.common.xml.InjectingReader;
import org.codejive.common.xml.XmlHelper;
import org.codejive.common.xml.XmlInputFactory;
import org.codejive.common.xml.XmlOutputFactory;
import org.codejive.web.filters.ObjectInjector;
import org.syntax.jedit.tokenmarker.TextTokenMarkerIterator;
import org.syntax.jedit.tokenmarker.Token;
import org.syntax.jedit.tokenmarker.TokenMarker;
import org.syntax.jedit.tokenmarker.TokenMarkerFactory;
import org.w3c.dom.Element;

/**
 * This class wraps around an existing XMLStreamReader passing through
 * all calls to the delegate until a &lt;code> tag is encountered.
 * It then read the entire contents of this tag as text and passes
 * it to a syntax highlighter, inserting the result into the stream.
 * @author tako
 */
public class CodeFormattingReader extends InjectingReader {
	public static final String CODE_NAMESPACE = "http://www.codejive.org/NS/portico/code";

	private static final QName codeName = new QName(RichText.NAMESPACE, "code");
	
	private static Logger logger = Logger.getLogger(ObjectInjector.class.getName());
	
	public CodeFormattingReader(XMLStreamReader _reader) {
		super(_reader);
	}

	@Override
	public int next() throws XMLStreamException {
		int result = super.next();
		// Check if the next event is the start of the tag we're looking for
		if ((getEventType() == START_ELEMENT) && (getName().equals(codeName))) {
			// Check if it has a syntax attribute
			String syntax = getAttributeValue(null, "syntax");
			if (syntax != null) {
				// Get a reader with the hilighted contents of the element
				XMLStreamReader hiliReader = highlight(syntax);
				if (hiliReader != null) {
					// Skip the END_ELEMENT we're pointing at right now
					// (this is a side-effect of the highlight() call)
					super.next();
					// Inject the new contents in the stream
					inject(hiliReader, true);
					result = super.next();
				}
			} else {
				logger.fine("Encountered <code>, no syntax given, leave contents as it is.");
			}
		}
		return result;
	}

	// To make sure that this is implemented in a way that works for us
	// (we want all access to the underlying reader to go through next()
	// but it seems the implementation of nextTag() doesn't use it)
	@Override
	public int nextTag() throws XMLStreamException {
		int eventType = next();
		while((eventType == XMLStreamConstants.CHARACTERS && isWhiteSpace()) // skip
				// whitespace
				|| (eventType == XMLStreamConstants.CDATA && isWhiteSpace()) 
				// skip whitespace
				|| eventType == XMLStreamConstants.SPACE
				|| eventType == XMLStreamConstants.PROCESSING_INSTRUCTION
				|| eventType == XMLStreamConstants.COMMENT
		) {
			eventType = next();
		}
		if (eventType != XMLStreamConstants.START_ELEMENT && eventType != XMLStreamConstants.END_ELEMENT) {
			throw new XMLStreamException("expected start or end tag", getLocation());
		}
		return eventType;
	}
	
	private XMLStreamReader highlight(String syntax) throws XMLStreamException {
		XMLStreamReader result = null;
		try {
			// Try to create a highlighter for the given syntax
			TokenMarker marker = TokenMarkerFactory.create(syntax);
			if (marker != null) {
				logger.fine("Encountered <code>, syntax = " + syntax + ", starting highlighter...");
				String codeText =  getElementText();
				TextTokenMarkerIterator markerIterator = new TextTokenMarkerIterator(codeText, marker);
				if (markerIterator.hasNext()) {
					Element content = convertStreamToHighlightedDocument(markerIterator);
					result = XmlInputFactory.newInstance().createDomReader(content);
				}
			} else {
				logger.fine("Encountered <code>, syntax " + syntax + " is not supported, leave contents as it is.");
			}
		} catch (InstantiationException e) {
			throw new XMLStreamException("Can't create TokenMarker for syntax '" + syntax + "'", e);
		} catch (IllegalAccessException e) {
			throw new XMLStreamException("Can't create TokenMarker for syntax '" + syntax + "'", e);
		} catch (ClassNotFoundException e) {
			throw new XMLStreamException("Can't create TokenMarker for syntax '" + syntax + "'", e);
		}
		return result;
	}

	private Element convertStreamToHighlightedDocument(TextTokenMarkerIterator markerIterator) throws XMLStreamException {
		DomWriter writer = XmlOutputFactory.newInstance().createDomWriter();
		writer.writeStartDocument();
		writer.writeStartElement(getPrefix(), getLocalName(), getNamespaceURI());
		writer.writeNamespace("code", CODE_NAMESPACE);
		writer.setPrefix("code", CODE_NAMESPACE);
		while (markerIterator.hasNext()) {
			Token token = markerIterator.next();
			switch (token.id) {
			case Token.COMMENT1:
				writeElement(writer, "comment1", token.getText());
				break;
			case Token.COMMENT2:
				writeElement(writer, "comment1", token.getText());
				break;
			case Token.KEYWORD1:
				writeElement(writer, "keyword1", token.getText());
				break;
			case Token.KEYWORD2:
				writeElement(writer, "keyword2", token.getText());
				break;
			case Token.KEYWORD3:
				writeElement(writer, "keyword3", token.getText());
				break;
			case Token.LABEL:
				writeElement(writer, "label", token.getText());
				break;
			case Token.LITERAL1:
				writeElement(writer, "literal1", token.getText());
				break;
			case Token.LITERAL2:
				writeElement(writer, "literal2", token.getText());
				break;
			case Token.OPERATOR:
				writeElement(writer, "operator", token.getText());
				break;
			case Token.INVALID:
				writeElement(writer, "invalid", token.getText());
				break;
			case Token.NULL:
				writer.writeCharacters(token.getText());
				break;
			case Token.END:
				writer.writeCharacters("\n");
				break;
			}
		}
		writer.writeEndElement();
		writer.writeEndDocument();
		return writer.getDocument().getDocumentElement();
	}
	
	private void writeElement(DomWriter writer, String elementName, String elementText) throws XMLStreamException {
		writer.writeStartElement("code", elementName, CODE_NAMESPACE);
		writer.writeCharacters(elementText);
		writer.writeEndElement();
	}
}
