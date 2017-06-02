package ch.unifr.diva.dip.osgi;

import ch.unifr.diva.dip.api.utils.ReflectionUtils;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Extra system packages that need to be exposed to the OSGi framework. Since
 * we're embedding an OSGi framework, we need to make sure that classes
 * available to the OSGi framework and the host application are being loaded
 * from the same class-loader. This is accomplished by having the host (or
 * system bundle) export such extra packages the OSGi framework.
 */
public class ExtraSystemPackages {

	private static final Logger log = LoggerFactory.getLogger(ExtraSystemPackages.class);
	private static List<SystemPackageBundle> config = Arrays.asList(
			/**
			 * Export all DIP API packages.
			 */
			new SystemPackageBundle(
					"1.0.0",
					findPackages("ch.unifr.diva.dip.api")
			),
			/**
			 * Export the imaging bundle.
			 */
			new SystemPackageBundle(
					"1.0.0",
					findPackages("ch.unifr.diva.dip.imaging")
			),
			/**
			 * Export glyph fonts.
			 */
			new SystemPackageBundle(
					"1.0.0",
					"ch.unifr.diva.dip.glyphs.fontawesome"
			),
			new SystemPackageBundle(
					"1.0.0",
					"ch.unifr.diva.dip.glyphs.icofont"
			),
			new SystemPackageBundle(
					"1.0.0",
					"ch.unifr.diva.dip.glyphs.mdi"
			),
			/**
			 * Export SLF4J. Exporting the simple logging facade is enough. No
			 * need to export the used logging framework itself.
			 */
			new SystemPackageBundle(
					"1.7.25",
					Arrays.asList(
							"org.slf4j",
							"org.slf4j.Logger",
							"org.slf4j.LoggerFactory"
					)
			),
			/**
			 * Export JavaFx packages. While the Java standard library is
			 * already exposed to the OSGi framework, the JavaFx packages are
			 * not. So we have to export them explicitly.
			 */
			new SystemPackageBundle(
					"2.2.0",
					Arrays.asList(
							"javafx.animation",
							"javafx.application",
							"javafx.beans",
							"javafx.beans.binding",
							"javafx.beans.property",
							"javafx.beans.property.adapter",
							"javafx.beans.value",
							"javafx.collections",
							"javafx.concurrent",
							"javafx.embed.swing",
							"javafx.embed.swt",
							"javafx.event",
							"javafx.fxml",
							"javafx.geometry",
							"javafx.scene",
							"javafx.scene.canvas",
							"javafx.scene.chart",
							"javafx.scene.control",
							"javafx.scene.control.cell",
							"javafx.scene.effect",
							"javafx.scene.image",
							"javafx.scene.input",
							"javafx.scene.layout",
							"javafx.scene.media",
							"javafx.scene.paint",
							"javafx.scene.shape",
							"javafx.scene.text",
							"javafx.scene.transform",
							"javafx.scene.web",
							"javafx.stage",
							"javafx.util",
							"javafx.util.converter",
							"javafx.fxml"
					)
			)
	);

	private static List<String> findPackages(String rootPackage) {
		try {
			return new ArrayList<>(ReflectionUtils.findPackages(rootPackage));
		} catch (IOException ex) {
			log.error("unable to locate DIP API packages at: {}", rootPackage, ex);
			return new ArrayList<>();
		}
	}

	/**
	 * Returns a list of all required extra system packages.
	 *
	 * @return list of system packages.
	 */
	public static List<SystemPackage> getSystemPackages() {
		final List<ExtraSystemPackages.SystemPackage> packages = new ArrayList<>();

		for (SystemPackageBundle bundle : config) {
			for (String pkg : bundle.packages) {
				packages.add(new SystemPackage(pkg, bundle.version));
			}
		}

		return packages;
	}

	/**
	 * Encapsulates a bundle/set of extra system packages.
	 */
	private static class SystemPackageBundle {

		/**
		 * The version of the packages.
		 */
		final public String version;

		/**
		 * List of canonical package names.
		 */
		final public List<String> packages;

		/**
		 * Creates a new system package bundle with a single class.
		 *
		 * @param version the version.
		 * @param name the canonical name of the package.
		 */
		public SystemPackageBundle(String version, String name) {
			this(version, Arrays.asList(name));
		}

		/**
		 * Creates a new system package bundle.
		 *
		 * @param version the version.
		 * @param packages the packages (a list of canonical package names).
		 */
		public SystemPackageBundle(String version, List<String> packages) {
			this.version = version;
			this.packages = packages;
		}

	}

	/**
	 * Encapsulates extra system packages.
	 */
	public static class SystemPackage {

		/**
		 * The canonical package name.
		 */
		final public String pkg;

		/**
		 * The version of the package.
		 */
		final public String version;

		/**
		 * SystemPackage constructor without min. version. Defaults to min.
		 * version="0.0.0".
		 *
		 * @param pkg Name of the system package (e.g. "javafx.animation").
		 */
		public SystemPackage(String pkg) {
			this(pkg, null);
		}

		/**
		 * SystemPackage constructor.
		 *
		 * @param pkg Name of the system package (e.g. "javafx.animation").
		 * @param version Min. version (e.g. "2.2.0").
		 */
		public SystemPackage(String pkg, String version) {
			this.pkg = pkg;
			this.version = version;
		}

		@Override
		public String toString() {
			if (version == null) {
				return pkg;
			}
			return pkg + ";version=" + version;
		}

	}

}
