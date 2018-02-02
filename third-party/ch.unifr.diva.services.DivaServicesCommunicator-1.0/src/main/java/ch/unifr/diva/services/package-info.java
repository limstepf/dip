/**
 * Diva Services Communicator API. Repackaged as an OSGi bundle.
 *
 * <p>
 * Note that this version is outdated and had to be fixed to use the new URL to
 * connect to v1 of the API. v2 is available too, so a new communicator API
 * should be out soon enough. Further note that the tests had to be removed,
 * since the data directory is in an illegal location (should be moved to
 * resources...). And that deprecated Base64 decoder has been replaced with the
 * apache one. For a future version there should be service discovery, which
 * will require a mapping of Diva Services to DIP types to work.
 *
 * <p>
 * <ul>
 * <li>deprecated/unavailable API (v1): http://divaservices.unifr.ch/</li>
 * <li>API v1: http://divaservices.unifr.ch/api/v1/</li>
 * <li>API v2: http://divaservices.unifr.ch/api/v2/</li>
 * </ul>
 *
 * <p>
 * P.S. All classes have been moved a package up into the ch.unifr.diva.services
 * namespace to not litter the ch.unifr.diva namespace with the project. Should
 * probably also be "fixed" upstream in order to keep ch.unifr.diva clean.
 */
package ch.unifr.diva.services;
