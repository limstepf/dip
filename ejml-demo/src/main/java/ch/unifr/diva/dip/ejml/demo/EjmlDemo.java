package ch.unifr.diva.dip.ejml.demo;

import ch.unifr.diva.dip.api.components.ProcessorContext;
import ch.unifr.diva.dip.api.parameters.ButtonParameter;
import ch.unifr.diva.dip.api.parameters.CompositeGrid;
import ch.unifr.diva.dip.api.parameters.StringParameter;
import ch.unifr.diva.dip.api.services.Processor;
import ch.unifr.diva.dip.api.services.ProcessorBase;
import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Service;
import org.ejml.simple.SimpleMatrix;

/**
 * EJML demo plugin. The goal here is twofold: 1) how to wrap/repackage third
 * party dependencies into a nice OSGi bundle, and 2) how to actually make use
 * of them.
 *
 * <p>
 * For the first part have a look at the {@code pom.xml} of the
 * "pom-osgi-package" module, and - the used for this demo - the
 * "org.emjl-all.029" module, which has the firmer module as parent pom, and
 * then simply lists the dependencies to be wrapped into a new bundle (check
 * with the Felix Gogo Shell and the {@code felix:lb} command to see if the
 * bundle shows up).
 *
 * <p>
 * And as to the second part: just do your usual thing, list the just wrapped
 * dependencies, but do not embed them directly into the bundle which is to be
 * created for this plugin. Just don't. Everything will be fine. As long as the
 * wrapped bundle created in step 1 is in the bundle directory, this plugin will
 * be able to resolve its dependencies and become _active_. Otherwise (try it
 * out by removing the org-emjl-all-0.29.jar from the bundle directory, then hit
 * {@code felix:lb} again) the bundle of this plugin will be _installed_, and
 * only become _active_ once a bundle with those dependencies show back up
 * again.
 *
 * <p>
 * Generally it's much better to wrap needed dependencies this way, instead of
 * embbeding them directly into the bundle that requires them - given the
 * dependencies aren't already an OSGi bundle in the first place (in the end
 * that's just a matter of another manifest file in the jar). Just be carefull
 * with the versions/version ranges. OSGi is all about semantic versioning.
 * Third party dependencies maybe not so much... But if you're targeting
 * specific versions anyways, no problem. Update manually as you see fit. There
 * can be multiple ejml bundles around at the same time with different versions.
 */
@Component
@Service
public class EjmlDemo extends ProcessorBase {

	public EjmlDemo() {
		super("EJML Demo");

		// let's just have a button that creates a small diagonal matrix
		// and then output it's determinant, which should be about 6, eh.
		final StringParameter s = new StringParameter("out", "");
		final ButtonParameter b = new ButtonParameter("EJML", (c) -> {
			SimpleMatrix mat = SimpleMatrix.diag(3, 2, 1);
			s.set("det = " + mat.determinant());
		});
		final CompositeGrid grid = new CompositeGrid(
				s, b
		);
		grid.setColumnWidthConstraints(.67, .33);
		this.parameters.put("grid", grid);
	}

	@Override
	public Processor newInstance(ProcessorContext context) {
		return new EjmlDemo();
	}

	@Override
	public void init(ProcessorContext context) {

	}
}
