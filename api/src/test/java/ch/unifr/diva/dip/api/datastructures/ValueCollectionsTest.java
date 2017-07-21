package ch.unifr.diva.dip.api.datastructures;

import ch.unifr.diva.dip.api.utils.MathUtils;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
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
	public void testValueListEquality() {
		TestEqual<ValueList> te = new TestEqual<ValueList>(){
			@Override
			public ValueList newCollection() {
				return new ValueList();
			}

			@Override
			public void addToCollection(ValueList collection, String key, Object value) {
				collection.add(value);
			}
		};
		te.test();
	}

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
	public void testValueListSelectionEquality() {
		TestEqual<ValueListSelection> te = new TestEqual<ValueListSelection>(){
			@Override
			public ValueListSelection newCollection() {
				return new ValueListSelection();
			}

			@Override
			public void addToCollection(ValueListSelection collection, String key, Object value) {
				collection.add(value);
			}

			@Override
			public void test() {
				super.test();
				int size = a.size();
				int sel;
				for (int i = 0; i < 5; i++) {
					sel = MathUtils.randomInt(0, size);
					a.setSelection(sel);
					if (sel != b.getSelectedIndex()) {
						assertNonEqualCollection(a, b);
					}

					b.setSelection(sel);
					assertEqualCollection(a, b);
				}
			}
		};
		te.test();
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
	public void testValueMapEquality() {
		TestEqual<ValueMap> te = new TestEqual<ValueMap>(){
			@Override
			public ValueMap newCollection() {
				return new ValueMap();
			}

			@Override
			public void addToCollection(ValueMap collection, String key, Object value) {
				collection.put(key, value);
			}
		};
		te.test();
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
	public void testValueMapSelectionEquality() {
		TestEqual<ValueMapSelection> te = new TestEqual<ValueMapSelection>(){
			@Override
			public ValueMapSelection newCollection() {
				return new ValueMapSelection();
			}

			@Override
			public void addToCollection(ValueMapSelection collection, String key, Object value) {
				collection.put(key, value);
			}

			@Override
			public void test() {
				super.test();
				List<String> keys = new ArrayList<>(a.keySet());
				int size = keys.size();
				int sel;
				String key;

				for (int i = 0; i < 5; i++) {
					sel = MathUtils.randomInt(0, size);
					key = keys.get(sel);
					a.setSelection(key);
					if (!key.equals(b.getSelectedKey())) {
						assertNonEqualCollection(a, b);
					}
					b.setSelection(key);
					assertEqualCollection(a, b);
				}
			}
		};
		te.test();
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
