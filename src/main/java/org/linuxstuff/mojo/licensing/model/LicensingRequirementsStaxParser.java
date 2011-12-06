package org.linuxstuff.mojo.licensing.model;

import java.io.InputStream;
import java.util.Iterator;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.events.Attribute;
import javax.xml.stream.events.EndElement;
import javax.xml.stream.events.StartElement;
import javax.xml.stream.events.XMLEvent;

import edu.umd.cs.findbugs.annotations.NonNull;

/**
 * I'm using stax instead of jaxb since I'm likely going to need to make changes
 * to the XML format and I don't want to be fighting JAXB down the road.
 */
public class LicensingRequirementsStaxParser {

	static final String ARTIFACTS_MISSING_LICENSES = "artifacts-missing-licenses";
	static final String COALESCE_LICENSES = "coalesce-licenses";
	static final String DISLIKED_LICENSES = "disliked-licenses";
	static final String DISLIKE_EXEMPTIONS = "dislike-exemptions";
	static final String ARTIFACT = "artifact";
	static final String ID = "id";
	static final String LICENSE = "license";
	static final String AKA = "aka";
	static final String NAME = "name";

	public void read(@NonNull LicensingRequirements licensingRequirements, @NonNull InputStream inputStream)
			throws XMLStreamException {
		XMLInputFactory inputFactory = XMLInputFactory.newInstance();
		XMLEventReader eventReader = inputFactory.createXMLEventReader(inputStream);

		while (eventReader.hasNext()) {
			XMLEvent event = eventReader.nextEvent();

			if (event.isStartElement()) {
				StartElement startElement = event.asStartElement();
				String localPart = startElement.getName().getLocalPart();
				if (ARTIFACTS_MISSING_LICENSES.equals(localPart)) {
					readArtifactsMissingLicenses(eventReader, licensingRequirements);
				} else if (COALESCE_LICENSES.equals(localPart)) {
					readCoalesceLicenses(eventReader, licensingRequirements);
				} else if (DISLIKED_LICENSES.equals(localPart)) {
					readDislikedLicenses(eventReader, licensingRequirements);
				} else if (DISLIKE_EXEMPTIONS.equals(localPart)) {
					readDislikeExemptions(eventReader, licensingRequirements);
				}

			}

		}

	}

	private void readDislikeExemptions(XMLEventReader eventReader, LicensingRequirements licensingRequirements)
			throws XMLStreamException {
		while (eventReader.hasNext()) {
			XMLEvent event = eventReader.nextEvent();
			if (event.isEndElement()
					&& ((EndElement) event.asEndElement()).getName().getLocalPart().equals(DISLIKE_EXEMPTIONS)) {
				return;
			}
			if (event.isStartElement()) {
				StartElement startElement = event.asStartElement();
				String localPart = startElement.getName().getLocalPart();
				if (ARTIFACT.equals(localPart)) {
					Attribute idAttribute = startElement.getAttributeByName(QName.valueOf(ID));
					if (idAttribute == null) {
						throw new XMLStreamException("In " + DISLIKE_EXEMPTIONS + ", the " + ARTIFACT
								+ " element requires the " + ID + " attribute.");
					}

					licensingRequirements.addDislikedExemption(idAttribute.getValue());

				}

			}
		}

	}

	private void readDislikedLicenses(XMLEventReader eventReader, LicensingRequirements licensingRequirements)
			throws XMLStreamException {
		while (eventReader.hasNext()) {
			XMLEvent event = eventReader.nextEvent();
			if (event.isEndElement()
					&& ((EndElement) event.asEndElement()).getName().getLocalPart().equals(DISLIKED_LICENSES)) {
				return;
			}
			if (event.isStartElement()) {
				StartElement startElement = event.asStartElement();
				String localPart = startElement.getName().getLocalPart();
				if (LICENSE.equals(localPart)) {
					Attribute licenseAttribute = startElement.getAttributeByName(QName.valueOf(NAME));
					if (licenseAttribute == null) {
						throw new XMLStreamException("In " + DISLIKED_LICENSES + ", the " + LICENSE
								+ " element requires a " + NAME + " attribute.");
					}
					licensingRequirements.addDislikedLicense(licenseAttribute.getValue());

				}

			}
		}

	}

	private void readCoalesceLicenses(XMLEventReader eventReader, LicensingRequirements licensingRequirements)
			throws XMLStreamException {
		String licenseName = null;
		while (eventReader.hasNext()) {
			XMLEvent event = eventReader.nextEvent();
			if (event.isEndElement()
					&& ((EndElement) event.asEndElement()).getName().getLocalPart().equals(COALESCE_LICENSES)) {
				return;
			}
			if (event.isStartElement()) {
				StartElement startElement = event.asStartElement();
				String localPart = startElement.getName().getLocalPart();
				if (LICENSE.equals(localPart)) {
					Attribute licenseAttribute = startElement.getAttributeByName(QName.valueOf(NAME));
					if (licenseAttribute == null) {
						throw new XMLStreamException("In " + COALESCE_LICENSES + ", the " + LICENSE
								+ " element requires a " + NAME + " attribute.");
					}
					licenseName = licenseAttribute.getValue();

				} else if (AKA.equals(localPart)) {
					event = eventReader.nextEvent();
					licensingRequirements.addLicenseAka(licenseName, event.asCharacters().getData());
				}

			}
		}

	}

	private void readArtifactsMissingLicenses(XMLEventReader eventReader, LicensingRequirements licensingRequirements)
			throws XMLStreamException {
		while (eventReader.hasNext()) {
			XMLEvent event = eventReader.nextEvent();
			if (event.isEndElement()
					&& ((EndElement) event.asEndElement()).getName().getLocalPart().equals(ARTIFACTS_MISSING_LICENSES)) {
				return;
			}
			if (event.isStartElement()) {
				StartElement startElement = event.asStartElement();
				String localPart = startElement.getName().getLocalPart();
				if (ARTIFACT.equals(localPart)) {
					String id = null, licenseName = null;
					// We read the attributes from this tag and add the date
					// attribute to our object

					Iterator<Attribute> attributes = startElement.getAttributes();
					while (attributes.hasNext()) {
						Attribute attribute = attributes.next();
						if (ID.equals(attribute.getName().toString())) {
							id = attribute.getValue();
						} else if (LICENSE.equals(attribute.getName().toString())) {
							licenseName = attribute.getValue();
						}

					}
					if (id == null || licenseName == null) {
						throw new XMLStreamException("When specifying " + ARTIFACTS_MISSING_LICENSES
								+ " all artifacts require both a " + LICENSE + " and " + ID + " (" + ID + "=" + id
								+ ", " + LICENSE + "=" + licenseName + ")");
					}
					licensingRequirements.addMissingLicense(id, licenseName);
				}

			}
		}
	}
}
