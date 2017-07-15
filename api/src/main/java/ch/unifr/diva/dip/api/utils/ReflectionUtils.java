package ch.unifr.diva.dip.api.utils;

import com.google.common.reflect.ClassPath;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/**
 * Java reflection utilities.
 */
public class ReflectionUtils {

	private ReflectionUtils() {
		/* nope :) */
	}

	/**
	 * List of class names to be ignored while searching for/collecting classes.
	 */
	private final static HashSet<String> classNameBlacklist = new HashSet<>();

	static {
		classNameBlacklist.add("package-info");
	}


	/**
	 * Returns the Java class by canonical name.
	 *
	 * @param name canonical name of a Java class.
	 * @return Java class or null.
	 */
	public static Class<?> getClass(String name) {
		try {
			Class<?> clazz = Class.forName(name);
			return clazz;
		} catch (ClassNotFoundException ex) {
			return null;
		}
	}

	/**
	 * Checks whether the Java class given by its canonical name exists.
	 *
	 * @param name canonical name of the Java class.
	 * @return {@code true} if the class exists, {@code false} otherwise.
	 */
	public static boolean classExists(String name) {
		try {
			Class<?> clazz = Class.forName(name);
			return true;
		} catch (ClassNotFoundException ex) {
			return false;
		}
	}

	/**
	 * Returns a list of the canonical names of all classes associated with the
	 * host/application's classloader as default. Actual classes can be
	 * retrieved from here with a call to {@code ReflectionUtils.getClass}.
	 *
	 * @return list of canonical names to found classes.
	 * @throws IOException
	 */
	public static List<String> findClasses() throws IOException {
		return findClasses(ReflectionUtils.class.getClassLoader());
	}

	/**
	 * Returns a list of the canonical names of all classes associated with the
	 * given classloader. Actual classes can be retrieved from here with a call
	 * to {@code ReflectionUtils.getClass}.
	 *
	 * @param classLoader a classloader.
	 * @return list of canonical names to found classes.
	 * @throws IOException
	 */
	public static List<String> findClasses(ClassLoader classLoader) throws IOException {
		final List<String> classes = new ArrayList<>();
		final ClassPath classpath = ClassPath.from(classLoader);
		for (ClassPath.ClassInfo classInfo : classpath.getTopLevelClasses()) {
			if (classNameBlacklist.contains(classInfo.getSimpleName())) {
				continue;
			}
			classes.add(classInfo.getName());
		}
		return classes;
	}

	/**
	 * Returns a list of the canonical names of all classes in the given package
	 * associated with the host/application's classloader as default. Actual
	 * classes can be retrieved from here with a call to
	 * {@code ReflectionUtils.getClass}.
	 *
	 * @param packageName Package name of a package loaded by the
	 * host/application's classloader.
	 * @return list of canonical names to found classes.
	 * @throws IOException
	 */
	public static List<String> findClasses(String packageName) throws IOException {
		return findClasses(packageName, ReflectionUtils.class.getClassLoader());
	}

	/**
	 * Returns a list of the canonical names of all classes in the given package
	 * associated with the given classloader. Actual classes can be retrieved
	 * from here with a call to {@code ReflectionUtils.getClass}.
	 *
	 * @param packageName Package name of a package loaded by the
	 * host/application's classloader.
	 * @param classLoader a classloader.
	 * @return list of canonical names to found classes.
	 * @throws IOException
	 */
	public static List<String> findClasses(String packageName, ClassLoader classLoader) throws IOException {
		final List<String> classes = new ArrayList<>();
		final ClassPath classpath = ClassPath.from(classLoader);
		for (ClassPath.ClassInfo classInfo : classpath.getTopLevelClasses(packageName)) {
			if (classNameBlacklist.contains(classInfo.getSimpleName())) {
				continue;
			}
			classes.add(classInfo.getName());
		}
		return classes;
	}

	/**
	 * Returns a list of the canonical names of all (sub-)packages below a given
	 * root package (including the root package itself).
	 *
	 * @param rootPackage the root package.
	 * @return list of all (sub-)packages below the given root package.
	 * @throws IOException
	 */
	public static Set<String> findPackages(String rootPackage) throws IOException {
		return findPackages(rootPackage, ReflectionUtils.class.getClassLoader());
	}

	/**
	 * Returns a list of the canonical names of all (sub-)packages below a given
	 * root package (including the root package itself).
	 *
	 * @param rootPackage the root package.
	 * @param classLoader a classloader.
	 * @return list of all (sub-)packages below the given root package.
	 * @throws IOException
	 */
	public static Set<String> findPackages(String rootPackage, ClassLoader classLoader) throws IOException {
		final Set<String> packages = new LinkedHashSet<>();
		packages.add(rootPackage);

		final ClassPath classpath = ClassPath.from(classLoader);
		for (ClassPath.ClassInfo classInfo : classpath.getTopLevelClasses()) {
			final String pkg = classInfo.getPackageName();
			if (pkg.startsWith(rootPackage)) {
				packages.add(classInfo.getPackageName());
			}
		}
		return packages;
	}

}
