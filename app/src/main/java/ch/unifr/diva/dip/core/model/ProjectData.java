package ch.unifr.diva.dip.core.model;

import ch.unifr.diva.dip.utils.IOUtils;
import ch.unifr.diva.dip.api.utils.XmlUtils;
import ch.unifr.diva.dip.utils.ZipFileSystem;
import ch.unifr.diva.dip.api.utils.jaxb.PathAdapter;
import ch.unifr.diva.dip.core.ApplicationHandler;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import javafx.collections.ObservableList;
import javax.xml.bind.JAXBException;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

/**
 * {@code ProjectData} is just that; a pure data object (as in a struct) and not
 * the actual {@code Project} object. A {@code ProjectData} object needs to be
 * validated before constructing a {@code Project} first (for example paths to
 * external files might have to be fixed).
 *
 * <p>
 * Note that this class has been designed to produce a nice XML encoding with
 * JAXB. Hence those static inner classes and what not.
 */
@XmlRootElement
public class ProjectData {

	/**
	 * The project's root XML file.
	 */
	@XmlTransient
	public static final String PROJECT_ROOT_XML = "/project.xml";

	/**
	 * XML file containing defined processing pipelines.
	 */
	@XmlTransient
	public static final String PROJECT_PIPELINES_XML = "/pipelines.xml";

	// just used to pass ZipFileSystem pointer to the Project constructor
	@XmlTransient
	public ZipFileSystem zip;

	@XmlTransient
	public Path zipFile;

	// additional assets (loaded separately in a second step, see validate() below)
	@XmlTransient
	public PipelineData pipelines;

	public int defaultPipeline = -1;

	public List<PipelineData.Pipeline> pipelines() {
		if (pipelines == null) {
			return null;
		}
		return pipelines.list;
	}

	// file should be overwritten after reading existing project data since it
	// could have been moved
	@XmlJavaTypeAdapter(PathAdapter.class)
	public Path file;

	public String name;

	@XmlElement(name = "pages")
	public PageList pages = new PageList();


	/**
	 * Empty constructor (needed for JAXB).
	 */
	public ProjectData() {

	}

	/**
	 * Creates a new ProjectData instance. Used to create new ProjectData from
	 * scratch for a new project.
	 *
	 * @param name name of the project.
	 * @param saveFile the project file.
	 * @param images list of pages/images.
	 * @throws java.io.IOException
	 */
	public ProjectData(String name, Path saveFile, List<Path> images) throws IOException {
		this.name = name;
		this.file = saveFile;
		this.pages.selectedPage = images.isEmpty() ? -1 : 1;
		addPages(images);
	}

	/**
	 * Creates a ProjectData instance from a project. Used to marshal a living
	 * project to XML.
	 *
	 * @param project a project.
	 */
	public ProjectData(Project project) {
		this.name = project.getProjectName();
		this.file = project.getFile();
		this.defaultPipeline = project.pipelineManager().getDefaultPipelineId();
		this.pages.selectedPage = project.getSelectedPageId();
		addPages(project.pages());
	}

	// new project
	private void addPages(List<Path> images) throws IOException {
		for (Path image : images) {
			addPage(image);
		}
	}

	// new project
	private void addPage(Path image) throws IOException {
		this.pages.list.add(new Page(newPageId(), image));
	}

	// marshal to xml
	private void addPages(ObservableList<ProjectPage> pages) {
		for (ProjectPage page : pages) {
			addPage(page);
		}
	}

	// marshal to xml
	private void addPage(ProjectPage page) {
		this.pages.list.add(new Page(
				page.id,
				page.getName(),
				page.file,
				page.checksum,
				page.getPipelineId()
		));
	}

	private int newPageId() {
		return this.pages.list.size() + 1;
	}

	public int getSelectedPage() {
		return this.pages.selectedPage;
	}

	/**
	 * Returns the project's pages.
	 *
	 * @return an image set.
	 */
	public List<Page> getPages() {
		return this.pages.list;
	}

	@Override
	public String toString() {
		return this.getClass().getSimpleName()
				+ "{"
				+ "name=" + name
				+ ", file=" + file
				+ "}";
	}

	/**
	 * The validation results of project data.
	 */
	public static class ValidationResult {

		public final List<Exception> exceptions = new ArrayList<>();
		public final Map<Integer, String> checksums = new HashMap<>();
		public final List<Page> modifiedImages = new ArrayList<>();
		public final List<Page> movedImages = new ArrayList<>();
		public final List<Page> missingImages = new ArrayList<>();
		public final Set<String> missingServices = new HashSet<>();

		public ValidationResult() {

		}

		/**
		 * Returns the validation result.
		 *
		 * @return True if the project data is valid, False otherwise.
		 */
		public boolean isValid() {
			final boolean validImageFiles = modifiedImages.isEmpty()
					&& movedImages.isEmpty()
					&& missingImages.isEmpty();
			final boolean availableServices = missingServices.isEmpty();
			final boolean noExceptions = exceptions.isEmpty();

			return (validImageFiles && availableServices && noExceptions);
		}

		@Override
		public String toString() {
			return this.getClass().getSimpleName()
					+ "{"
					+ "valid=" + isValid()
					+ ", missingImages(" + missingImages.size() + ")=" + missingImages
					+ ", modifiedImages(" + modifiedImages.size() + ")=" + modifiedImages
					+ ", missingServices(" + missingServices.size() + ")=" + missingServices
					+ ", exceptions(" + exceptions.size() + ")=" + exceptions
					+ "}";
		}
	}

	/**
	 * Loads remaining assets and validates the project data. This method loads
	 * remaining assets and checks the integritiy of the project data, that is,
	 * we already know that the project's root XML is fine, so what is checked
	 * here are mostly references to external files that could have been
	 * modified or moved in the meantime.
	 *
	 * @param handler the application handler.
	 * @return validation results.
	 */
	public ValidationResult validate(ApplicationHandler handler) {
		final ValidationResult v = new ValidationResult();

		// load additional assets
		Path pipelinesXml = zip.getPath(ProjectData.PROJECT_PIPELINES_XML);
		if (zip.exists(pipelinesXml)) {
			try {
				pipelines = PipelineData.loadAsStream(pipelinesXml);
			} catch (IOException | JAXBException ex) {
				v.exceptions.add(ex);
			}
		}

		// check location and checksum of referenced image files
		for (Page image : getPages()) {
			if (Files.exists(image.file)) {
				try {
					String checksum = IOUtils.checksum(image.file);
					v.checksums.put(image.id, checksum);
					if (!image.checksum.equals(checksum)) {
						v.modifiedImages.add(image);
					}
				} catch (IOException ex) {
					v.missingImages.add(image);
				}
			} else {
				v.missingImages.add(image);
			}
		}

		// check required processors/OSGI services
		if (pipelines() != null) {
			for (PipelineData.Pipeline<ProcessorWrapper> pipeline : pipelines()) {
				for (PipelineData.Processor processor : pipeline.processors()) {
					if (handler.osgi.services.isAvailable(processor.pid)) {
						continue;
					}
					if (handler.osgi.hostServices.isAvailable(processor.pid)) {
						continue;
					}
					v.missingServices.add(processor.pid);
				}
			}
		}

		return v;
	}

	/**
	 * Returns the parent directory of the savefile.
	 *
	 * @return the parent directory of the savefile.
	 */
	public Path getParentDirectory() {
		return this.file.getParent();
	}

	/**
	 * Project factory reading/unmarshalling from an XML file.
	 *
	 * @param file an XML file.
	 * @return project data.
	 * @throws JAXBException in case of unexpected errors during unmarshalling.
	 */
	public static ProjectData load(Path file) throws JAXBException {
		return XmlUtils.unmarshal(ProjectData.class, file);
	}

	/**
	 * Project factory reading/unmarshalling from an XML file.
	 *
	 * @param stream input stream of an XML file.
	 * @return project data.
	 * @throws JAXBException in case of unexpected errors during unmarshalling.
	 */
	public static ProjectData load(InputStream stream) throws JAXBException {
		return XmlUtils.unmarshal(ProjectData.class, stream);
	}

	/**
	 * Saves/marshalls the Project to disk.
	 *
	 * @param file the XML file to store the project.
	 * @throws JAXBException in case of unexpected errors during marshalling.
	 */
	public void save(Path file) throws JAXBException {
		XmlUtils.marshal(this, file);
	}

	/**
	 * Saves/marshalls the Project to disk.
	 *
	 * @param stream output stream to an XML file.
	 * @throws JAXBException in case of unexpected errors during marshalling.
	 */
	public void save(OutputStream stream) throws JAXBException {
		XmlUtils.marshal(this, stream);
	}

	@XmlRootElement
	public static class PageList {

		@XmlAttribute(name = "selected")
		public int selectedPage = 1;

		@XmlElement(name = "page")
		public List<Page> list = new ArrayList<>();
	}

	@XmlRootElement
	public static class Page {

		@XmlAttribute
		public int id = -1;
		@XmlAttribute(name = "pipeline")
		public int pipelineId = -1;
		public String name;
		@XmlJavaTypeAdapter(PathAdapter.class)
		public Path file;
		public String checksum;

		@Override
		public String toString() {
			return this.getClass().getSimpleName()
					+ "{"
					+ "id=" + id
					+ ", pipeline=" + pipelineId
					+ ", name=" + name
					+ ", file=" + file
					+ ", checksum=" + checksum
					+ "}";
		}

		/**
		 * Empty constructor (needed for JAXB).
		 */
		public Page() {
		}

		public Page(int id, Path file) throws IOException {
			this(id, null, file, IOUtils.checksum(file), -1);
		}

		public Page(int id, Path file, int pipelineId) throws IOException {
			this(id, null, file, IOUtils.checksum(file), pipelineId);
		}

		public Page(int id, String name, Path file, String checksum, int pipelineId) {
			this.id = id;
			if (name == null) {
				this.name = getDefaultName(file);
			} else {
				this.name = name;
			}
			this.file = file;
			this.checksum = checksum;
			this.pipelineId = pipelineId;
		}

		private String getDefaultName(Path file) {
			final String defaultName = file.getFileName().toString();
			int i = defaultName.lastIndexOf('.');
			if (i > 0) {
				return defaultName.substring(0, i);
			}
			return defaultName;
		}

		public String pipelineXmlPath() {
			return String.format(ProjectPage.PIPELINE_XML_FORMAT, id);
		}

		@Override
		public int hashCode() {
			return this.file.hashCode();
		}

		@Override
		public boolean equals(Object obj) {
			if (obj == null) {
				return false;
			}
			if (getClass() != obj.getClass()) {
				return false;
			}
			final Page other = (Page) obj;
			return Objects.equals(this.file, other.file);
		}
	}

}
