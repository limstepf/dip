package ch.unifr.diva.dip.api.datastructures;

import java.io.IOException;
import javax.xml.bind.JAXBException;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

/**
 * ValueList, ValueListSelection, ValueMap, and ValueMapSelection unit tests.
 */
public class ValueCollectionsTest {

	@Rule
	public final TemporaryFolder parent = new TemporaryFolder();

	@Test
	public void testValueListMarshaller() throws IOException, JAXBException {
		TestMarshaller<ValueList> tm = new TestMarshaller<ValueList>(ValueList.class, parent) {
			@Override
			public ValueList newInstance() {
				return TestUtils.newValueList(5);
			}
		};
		tm.test();
	}

	@Test
	public void testValueListSelectionMarshaller() throws IOException, JAXBException {
		TestMarshaller<ValueListSelection> tm = new TestMarshaller<ValueListSelection>(ValueListSelection.class, parent) {
			@Override
			public ValueListSelection newInstance() {
				return TestUtils.newValueListSelection(11, 3);
			}
		};
		tm.test();
	}

	@Test
	public void testValueMapMarshaller() throws IOException, JAXBException {
		TestMarshaller<ValueMap> tm = new TestMarshaller<ValueMap>(ValueMap.class, parent) {
			@Override
			public ValueMap newInstance() {
				return TestUtils.newValueMap(5);
			}
		};
		tm.test();
	}

	@Test
	public void testValueMapSelectionMarshaller() throws IOException, JAXBException {
		TestMarshaller<ValueMapSelection> tm = new TestMarshaller<ValueMapSelection>(ValueMapSelection.class, parent) {
			@Override
			public ValueMapSelection newInstance() {
				return TestUtils.newValueMapSelection(5);
			}
		};
		tm.test();
	}

}
