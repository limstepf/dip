package ch.unifr.diva.dip.core.model;

import ch.unifr.diva.dip.api.services.Processor;
import ch.unifr.diva.dip.utils.IOUtils;
import ch.unifr.diva.dip.api.utils.XmlUtils;
import ch.unifr.diva.dip.utils.ZipFileSystem;
import ch.unifr.diva.dip.api.utils.jaxb.PathAdapter;
import ch.unifr.diva.dip.core.ApplicationHandler;
import ch.unifr.diva.dip.osgi.OSGiService;
import ch.unifr.diva.dip.osgi.OSGiVersionPolicy;
import ch.unifr.diva.dip.osgi.ServiceCollection;
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
import org.osgi.framework.Version;
import org.slf4j.LoggerFactory;

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

	private static final org.slf4j.Logger log = LoggerFactory.getLogger(ProjectData.class);

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

	/**
	 * The zip file system of the project. Used to pass ZipFileSystem pointer to
	 * the Project constructor.
	 */
	@XmlTransient
	public ZipFileSystem zip;

	/**
	 * The path to the zip file system of the project.
	 */
	@XmlTransient
	public Path zipFile;

	// additional assets (loaded separately in a second step, see validate() below)
	@XmlTransient
	public PipelineData pipelines;

	// possible exception thrown while trying to read the pipeline file
	@XmlTransient
	private Exception pipelineException;

	/**
	 * The id of the default pipeline.
	 */
	public int defaultPipeline = -1;

	/**
	 * The path to the project file. Should be overwritten after reading
	 * existing project data since it could have been moved.
	 */
	@XmlJavaTypeAdapter(PathAdapter.class)
	public Path file;

	/**
	 * The name of the project.
	 */
	public String name;

	/**
	 * The list of project pages.
	 */
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
	 * @param images list of pages/images, or null.
	 * @param pipelines list of pipelines, or null.
	 * @throws java.io.IOException
	 */
	public ProjectData(String name, Path saveFile, List<Path> images, List<PipelineData.Pipeline> pipelines) throws IOException {
		this.name = name;
		this.file = saveFile;
		if (images != null) {
			addPages(images);
			this.pages.selectedPage = images.isEmpty() ? -1 : 1;
		} else {
			this.pages.selectedPage = -1;
		}
		if (pipelines != null) {
			addPipelines(pipelines);
		}
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

	/**
	 * Loads the project assets. This is implicitly done while trying to verify
	 * the project data.
	 */
	public void loadAssets() {
		final Path pipelinesXml = zip.getPath(ProjectData.PROJECT_PIPELINES_XML);
		if (zip.exists(pipelinesXml)) {
			try {
				this.pipelines = PipelineData.loadAsStream(pipelinesXml);
			} catch (IOException | JAXBException ex) {
				this.pipelineException = ex;
			}
		}
	}

	/**
	 * Returns the pipeline data list. Make sure the project data has been
	 * validated, or the assets have been manually loaded already by a call to
	 * {@code loadAssets()}.
	 *
	 * @return the pipeline data list, or null if not loaded yet.
	 */
	public List<PipelineData.Pipeline> pipelines() {
		if (pipelines == null) {
			return null;
		}
		return pipelines.list;
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

	private void addPipelines(List<PipelineData.Pipeline> pipelines) {
		for (PipelineData.Pipeline pipeline : pipelines) {
			addPipeline(pipeline);
		}
	}

	private void addPipeline(PipelineData.Pipeline pipeline) {
		if (this.pipelines == null) {
			this.pipelines = new PipelineData();
		}
		pipeline.id = newPipelineId();
		this.pipelines.addPipeline(pipeline);
	}

	private int newPipelineId() {
		return this.pipelines.list.size() + 1;
	}

	/**
	 * Returns the selected page.
	 *
	 * @return the selected page.
	 */
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

		/**
		 * Exceptions that have been thrown during validation.
		 */
		public final List<Exception> exceptions = new ArrayList<>();

		/**
		 * Checksums of modified image files, indexed by page ids.
		 */
		public final Map<Integer, String> checksums = new HashMap<>();

		/**
		 * A list of images that have been modified. I.e. the checksum doesn't
		 * match.
		 */
		public final List<Page> modifiedImages = new ArrayList<>();

		/**
		 * A list of images that have been moved.
		 */
		public final List<Page> movedImages = new ArrayList<>();

		/**
		 * A list of images that are missing/not found.
		 */
		public final List<Page> missingImages = new ArrayList<>();

		/**
		 * A map of unavailable services. Indexed by PID pointing to the set of
		 * missing versions/version strings.
		 */
		public final Map<String, Set<String>> missingServices = new HashMap<>();

		/**
		 * Creates a new validation result.
		 */
		public ValidationResult() {

		}

		/**
		 * Adds a missing/unavailable service.
		 *
		 * @param pid PID of the service.
		 * @param version version of the service.
		 */
		public void addMissingService(String pid, String version) {
			final Set<String> versions;
			if (missingServices.containsKey(pid)) {
				versions = missingServices.get(pid);
			} else {
				versions = new HashSet<>();
				missingServices.put(pid, versions);
			}
			versions.add(version);
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
	 * remaining assets (if not already done so) and checks the integritiy of
	 * the project data, that is, we already know that the project's root XML is
	 * fine, so what is checked here are mostly references to external files
	 * that could have been modified or moved in the meantime, and required OSGi
	 * services/processors.
	 *
	 * @param handler the application handler.
	 * @return validation results.
	 */
	public ValidationResult validate(ApplicationHandler handler) {
		final ValidationResult v = new ValidationResult();

		// load additional assets if not already done so...
		if (this.pipelines == null && this.pipelineException == null) {
			loadAssets();
		}

		if (this.pipelineException != null) {
			v.exceptions.add(this.pipelineException);
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
				final OSGiVersionPolicy policy = OSGiVersionPolicy.get(pipeline.versionPolicy);

				for (PipelineData.Processor processor : pipeline.processors()) {
					// retrieve service by pipeline's version policy. If a service is available it's
					// either the exact version, or an up-/downgraded one. If null, the service is
					// unavailable (and needs to be manually replaced or something...)
					final ServiceCollection<Processor> collection = handler.osgi.getProcessorCollection(processor.pid);
					if (collection != null) {
						final OSGiService<Processor> service = policy.getService(collection, new Version(processor.version));
						if (service != null) {
							final String versionByPolicy = service.version.toString();
							if (versionByPolicy.equals(processor.version)) {
								// exact same version is available
								continue;
							} else {
								// different version has been supplied, auto-update
								log.info(
										"OSGi Service {} up-/downgraded from {} to {} by version policy: {}",
										processor.pid, processor.version, versionByPolicy, policy
								);
								processor.version = versionByPolicy;
								continue;
							}
						}
					}

					// fall-through: service is not available
					v.addMissingService(processor.pid, processor.version);
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

	/**
	 * A page list object.
	 */
	@XmlRootElement
	public static class PageList {

		/**
		 * The id of the selected page.
		 */
		@XmlAttribute(name = "selected")
		public int selectedPage = 1;

		/**
		 * The list of pages.
		 */
		@XmlElement(name = "page")
		public List<Page> list = new ArrayList<>();
	}

	/**
	 * Page object.
	 */
	@XmlRootElement
	public static class Page {

		/**
		 * The id of the page.
		 */
		@XmlAttribute
		public int id = -1;

		/**
		 * The id of the used/parent pipeline.
		 */
		@XmlAttribute(name = "pipeline")
		public int pipelineId = -1;

		/**
		 * The name of the page. Get's initialized to the filename of the image
		 * file, but can be changed to anything.
		 */
		public String name;

		/**
		 * The path to the image file of the page.
		 */
		@XmlJavaTypeAdapter(PathAdapter.class)
		public Path file;

		/**
		 * The checksum of the image file of the page.
		 */
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

		/**
		 * Creates a new page object.
		 *
		 * @param id the id of the page.
		 * @param file the image of the page.
		 * @throws IOException
		 */
		public Page(int id, Path file) throws IOException {
			this(id, null, file, IOUtils.checksum(file), -1);
		}

		/**
		 * Creates a new page object.
		 *
		 * @param id the id of the page.
		 * @param file the image of the page.
		 * @param pipelineId the id of the used/parent pipeline.
		 * @throws IOException
		 */
		public Page(int id, Path file, int pipelineId) throws IOException {
			this(id, null, file, IOUtils.checksum(file), pipelineId);
		}

		/**
		 * Creates a new page object.
		 *
		 * @param id the id of the page.
		 * @param name the name of the page.
		 * @param file the path to the image file of the page.
		 * @param checksum the checksum of the image file.
		 * @param pipelineId the id of the used/parent pipeline.
		 */
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
