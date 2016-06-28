package ch.unifr.diva.dip.utils;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.nio.file.FileSystem;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.nio.file.WatchEvent;
import java.nio.file.WatchKey;
import java.nio.file.WatchService;
import java.util.Iterator;

/**
 * A decorated {@code Path} that can be extended right away.
 */
public class DecoratedPath implements Path {

	private final Path path;

	/**
	 * Default constructor.
	 *
	 * @param path the path to be decorated.
	 */
	public DecoratedPath(Path path) {
		this.path = path;
	}

	@Override
	public FileSystem getFileSystem() {
		return this.path.getFileSystem();
	}

	@Override
	public boolean isAbsolute() {
		return this.path.isAbsolute();
	}

	@Override
	public Path getRoot() {
		return this.path.getRoot();
	}

	@Override
	public Path getFileName() {
		return this.path.getFileName();
	}

	@Override
	public Path getParent() {
		return this.path.getParent();
	}

	@Override
	public int getNameCount() {
		return this.path.getNameCount();
	}

	@Override
	public Path getName(int i) {
		return this.path.getName(i);
	}

	@Override
	public Path subpath(int i, int i1) {
		return this.path.subpath(i, i1);
	}

	@Override
	public boolean startsWith(Path path) {
		return this.path.startsWith(path);
	}

	@Override
	public boolean startsWith(String string) {
		return this.path.startsWith(string);
	}

	@Override
	public boolean endsWith(Path path) {
		return this.path.endsWith(path);
	}

	@Override
	public boolean endsWith(String string) {
		return this.path.endsWith(string);
	}

	@Override
	public Path normalize() {
		return this.path.normalize();
	}

	@Override
	public Path resolve(Path path) {
		return this.path.resolve(path);
	}

	@Override
	public Path resolve(String string) {
		return this.path.resolve(string);
	}

	@Override
	public Path resolveSibling(Path path) {
		return this.path.resolveSibling(path);
	}

	@Override
	public Path resolveSibling(String string) {
		return this.path.resolveSibling(string);
	}

	@Override
	public Path relativize(Path path) {
		return this.path.relativize(path);
	}

	@Override
	public URI toUri() {
		return this.path.toUri();
	}

	@Override
	public Path toAbsolutePath() {
		return this.path.toAbsolutePath();
	}

	@Override
	public Path toRealPath(LinkOption... los) throws IOException {
		return this.path.toRealPath(los);
	}

	@Override
	public File toFile() {
		return this.path.toFile();
	}

	@Override
	public WatchKey register(WatchService ws, WatchEvent.Kind<?>[] kinds, WatchEvent.Modifier... mdfrs) throws IOException {
		return this.path.register(ws, kinds, mdfrs);
	}

	@Override
	public WatchKey register(WatchService ws, WatchEvent.Kind<?>... kinds) throws IOException {
		return this.path.register(ws, kinds);
	}

	@Override
	public Iterator<Path> iterator() {
		return this.path.iterator();
	}

	@Override
	public int compareTo(Path path) {
		return this.path.compareTo(path);
	}

	@Override
	public int hashCode() {
		return this.path.hashCode();
	}

	@Override
	public boolean equals(Object obj) {
		return this.path.equals(obj);
	}
	
	@Override
	public String toString() {
		return this.path.toString();
	}

}
