package org.linuxstuff.mojo.licensing.model;

import java.io.ByteArrayInputStream;

import javax.xml.stream.XMLStreamException;

import junit.framework.TestCase;

public class LicensingRequirementsStaxParserTest extends TestCase {

	public void testEmptyXML() throws XMLStreamException {

		LicensingRequirementsStaxParser parser = new LicensingRequirementsStaxParser();
		LicensingRequirements reqs = new LicensingRequirements();
		ByteArrayInputStream is = new ByteArrayInputStream(
				"<licensing-requirements></licensing-requirements>".getBytes());
		parser.read(reqs, is);
	}

	public void testEmptyBlock() throws XMLStreamException {

		LicensingRequirementsStaxParser parser = new LicensingRequirementsStaxParser();
		LicensingRequirements reqs = new LicensingRequirements();
		ByteArrayInputStream is = new ByteArrayInputStream(
				"<licensing-requirements><artifacts-missing-licenses/></licensing-requirements>".getBytes());
		parser.read(reqs, is);
	}

	public void testMissingLicense() throws XMLStreamException {

		LicensingRequirementsStaxParser parser = new LicensingRequirementsStaxParser();
		LicensingRequirements reqs = new LicensingRequirements();
		ByteArrayInputStream is = new ByteArrayInputStream(
				"<licensing-requirements><artifacts-missing-licenses><artifact id=\"testing-id\" license=\"testing-license\"/></artifacts-missing-licenses></licensing-requirements>"
						.getBytes());
		parser.read(reqs, is);
		assertEquals(1, reqs.getLicenseNames("testing-id").size());
	}

	public void testSimpleCoalesce() throws XMLStreamException {

		LicensingRequirementsStaxParser parser = new LicensingRequirementsStaxParser();
		LicensingRequirements reqs = new LicensingRequirements();
		ByteArrayInputStream is = new ByteArrayInputStream(
				"<licensing-requirements><coalesce-licenses><license name='JAMES'><aka>one</aka><aka>two</aka><aka>three</aka></license></coalesce-licenses></licensing-requirements>"
						.getBytes());
		parser.read(reqs, is);

		assertEquals("JAMES", reqs.getCorrectLicenseName("one"));
		assertEquals("JAMES", reqs.getCorrectLicenseName("two"));
		assertEquals("JAMES", reqs.getCorrectLicenseName("three"));
		assertEquals("JAMES", reqs.getCorrectLicenseName("JAMES"));
	}

	public void testDislikedLicenses() throws XMLStreamException {

		LicensingRequirementsStaxParser parser = new LicensingRequirementsStaxParser();
		LicensingRequirements reqs = new LicensingRequirements();
		ByteArrayInputStream is = new ByteArrayInputStream(
				"<licensing-requirements><disliked-licenses><license name='JAMES'/></disliked-licenses></licensing-requirements>"
						.getBytes());
		parser.read(reqs, is);
		assertTrue(reqs.isDislikedLicense("JAMES"));
		assertFalse(reqs.isDislikedLicense("EVERYTHING ELSE"));
	}

	public void testDislikeExemption() throws XMLStreamException {

		LicensingRequirementsStaxParser parser = new LicensingRequirementsStaxParser();
		LicensingRequirements reqs = new LicensingRequirements();
		ByteArrayInputStream is = new ByteArrayInputStream(
				"<licensing-requirements><dislike-exemptions><artifact id='testing'/></dislike-exemptions></licensing-requirements>"
						.getBytes());
		parser.read(reqs, is);

		assertTrue(reqs.isDislikedExempt("testing"));
		assertFalse(reqs.isDislikedExempt("EVERYTHING ELSE"));
	}

	public void testSimpleScenario() throws XMLStreamException {

		LicensingRequirementsStaxParser parser = new LicensingRequirementsStaxParser();
		LicensingRequirements reqs = new LicensingRequirements();
		ByteArrayInputStream is = new ByteArrayInputStream(
				("<licensing-requirements>"
						+ "<artifacts-missing-licenses><artifact id='james' license='james-license'/><artifact id='testing' license='james-license'/>"
						+ "</artifacts-missing-licenses>"
						+ "<coalesce-licenses><license name='magic'><aka>james-license</aka></license></coalesce-licenses>"
						+ "<disliked-licenses><license name='magic'/></disliked-licenses>"
						+ "<dislike-exemptions><artifact id='testing'/></dislike-exemptions></licensing-requirements>")
						.getBytes());
		parser.read(reqs, is);

	}

}
