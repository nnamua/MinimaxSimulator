package de.uni_hannover.sra.minimax_simulator;

import de.uni_hannover.sra.minimax_simulator.io.IOUtils;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.jar.Attributes;
import java.util.jar.JarFile;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Provides methods for reading version information from JAR's manifest, Java version of the running JVM
 * and to check Java versions against each other.
 *
 * @author Martin L&uuml;ck
 */
/* According to http://stackoverflow.com/questions/1272648/reading-my-own-jars-manifest
 * this is probably possible with less complexity
 */
public class Version {

	private boolean isJar = false;
	private String moduleName = "";
	private String revisionNumber = "";
	private String versionNumber = "";
	private String buildJdk = "";
	private String buildTime = "";
	private String authorName = "";
	private String companyName = "";

	private int jvmMajor;
	private int jvmFeature;
	private int jvmUpdate;
	private int jvmBuild;

	/**
	 * Constructs a new {@code Version} instance.<br>
	 * Tries to read version information from JAR's manifest and reads the Java version of the running JVM.
	 *
	 * @param c
	 *          the class to use for getting the JAR file
	 */
	public Version(Class<?> c) {
		setJavaVersion();

		if (c == null) {
			return;
		}

		File jarName = null;
		try {
			jarName = getResourceJar(c);
			if (jarName == null) {
				return;
			}

			JarFile jarFile = null;
			try {
				jarFile = new JarFile(jarName);
				
				Attributes attrs = jarFile.getManifest().getMainAttributes();

				setBuildTime(attrs);
				setBuildJdk(attrs);
				setRevisionNumber(attrs);
				setVersionNumber(attrs);
				setModuleName(attrs);
				setAuthor(attrs);
				setCompany(attrs);

				setIsJar(true);
			} finally {
				IOUtils.closeQuietly(jarFile);
			}
		} catch (IOException e) {
			// no jar file
		}
	}

	/**
	 * Convert the specified class name to URL to get the file path of the JAR that contains the class.
	 *
	 * @param c
	 *          the class to get the JAR containing the class
	 * @return
	 *          the JAR containing the class or {@code null} if the class is not contained in a JAR
	 */
	private static File getResourceJar(Class<?> c) {
		String resource = c.getName().replace('.', '/') + ".class";

		URL url = ClassLoader.getSystemResource(resource);
		if (url != null) {
			String u = url.toString();
			if (u.startsWith("jar:file:")) {
				int idx = u.indexOf("!");
				String jarName = u.substring(4, idx);
				try {
					return new File(new URI(jarName));
				} catch (URISyntaxException e) {
					return null;
				}
			}
		}
		return null;
	}

	/**
	 * Reads the application version number from the manifest.
	 *
	 * @param attrs
	 *          the {@code Attributes} from the manifest
	 */
	private void setVersionNumber(Attributes attrs) {
		String versionNumber = attrs.getValue("Implementation-Version");
		if (versionNumber != null) {
			this.versionNumber = versionNumber;
		}
	}

	/**
	 * Reads the application build number from the manifest.
	 *
	 * @param attrs
	 *          the {@code Attributes} from the manifest
	 */
	private void setRevisionNumber(Attributes attrs) {
		String revisionNumber = attrs.getValue("Implementation-Build");
		if (revisionNumber != null) {
			this.revisionNumber = revisionNumber;
		}
	}

	/**
	 * Reads the build JDK version from the manifest.
	 *
	 * @param attrs
	 *          the {@code Attributes} from the manifest
	 */
	private void setBuildJdk(Attributes attrs) {
		String buildJdk = attrs.getValue("Build-Jdk");
		if (buildJdk != null) {
			this.buildJdk = buildJdk;
		}
/*		else
		{
			buildJdk = attrs.getValue("Created-By");
			if (buildJdk != null)
			{
				buildJdk = buildJdk;
			}
		}	*/
	}

	/**
	 * Reads the build time from the manifest.
	 *
	 * @param attrs
	 *          the {@code Attributes} from the manifest
	 */
	private void setBuildTime(Attributes attrs) {
		String buildTime = attrs.getValue("Build-Time");
		if (buildTime != null) {
			this.buildTime = buildTime;
		}
	}

	/**
	 * Reads the application's name from the manifest.
	 *
	 * @param attrs
	 *          the {@code Attributes} from the manifest
	 */
	private void setModuleName(Attributes attrs) {
		String moduleName = attrs.getValue("Implementation-Name");
		if (moduleName != null) {
			this.moduleName = moduleName;
		}
	}

	/**
	 * Reads the application's authors from the manifest.
	 *
	 * @param attrs
	 *          the {@code Attributes} from the manifest
	 */
	private void setAuthor(Attributes attrs) {
		String authorName = attrs.getValue("Additional-Author");
		if (authorName != null) {
			this.authorName = authorName;
		}
	}

	/**
	 * Reads the application's company from the manifest.
	 *
	 * @param attrs
	 *          the {@code Attributes} from the manifest
	 */
	private void setCompany(Attributes attrs) {
		String companyName = attrs.getValue("Additional-Company");
		if (companyName != null) {
			this.companyName = companyName;
		}
	}

	/**
	 * Reads the Java version of the currently running JVM.
	 */
	private void setJavaVersion() {
		String jv = System.getProperty("java.version");
		try {
			String[] p0 = jv.split("_");
			String[] p1 = p0[0].split("\\.");

			jvmMajor = Integer.parseInt(p1[0]);
			jvmFeature = Integer.parseInt(p1[1]);
			jvmUpdate = Integer.parseInt(p1[2]);

			try {
				jvmBuild = Integer.parseInt(p0[1]);
			} catch (Exception e) {
				Pattern numberPattern = Pattern.compile("[0-9]+");
				Matcher m = numberPattern.matcher(p0[1]);
				if (m.find()) {
					jvmBuild = Integer.parseInt(m.group(0));
				}
			}
		} catch (Exception e) {
			// ignore
		}
	}

	/**
	 * Sets the value of the {@code is JAR} property.
	 *
	 * @param isLoaded
	 *          whether the class was loaded from JAR or not.
	 */
	private void setIsJar(boolean isLoaded) {
		isJar = isLoaded;
	}

	/**
	 * Gets the application's build number.
	 *
	 * @return
	 *          the application's build number
	 */
	public String getRevisionNumber() {
		return revisionNumber;
	}

	/**
	 * Gets the application's version number.
	 *
	 * @return
	 *          the application's version number
	 */
	public String getVersionNumber() {
		return versionNumber;
	}

	/**
	 * Gets the JDK version the application was built with.
	 *
	 * @return
	 *          the build JDK version
	 */
	public String getBuildJdk() {
		return buildJdk;
	}

	/**
	 * Gets the application's build time.
	 *
	 * @return
	 *          the application's build time
	 */
	public String getBuildTime() {
		return buildTime;
	}

	/**
	 * Gets the application's name.
	 *
	 * @return
	 *          the name of the application
	 */
	public String getModuleName() {
		return moduleName;
	}

	/**
	 * Gets the name(s) of the application's author(s).
	 *
	 * @return
	 *          the name(s) of the application's author(s).
	 */
	public String getAuthorName() {
		return authorName;
	}

	/**
	 * Gets the name of the application's company.
	 *
	 * @return
	 *          the name of the company of the application
	 */
	public String getCompanyName() {
		return companyName;
	}

	/**
	 * Gets the value of the {@code is JAR} property.
	 *
	 * @return
	 *          {@code true} if the application was loaded from JAR, {@code false} otherwise
	 */
	public boolean isJar() {
		return isJar;
	}

	/**
	 * Gets the full information of the application's version.<br>
	 * <br>
	 * This includes the application's name, version number, build number, build time and build JDK.
	 *
	 * @return
	 *          an array containing the full information of the version
	 */
	public String[] getFullInfoStrings() {
		return new String[] {
				"[Module: " + getModuleName() + "]",
				"Version: " + getVersionNumber() + " - "
						+ "Revision: " + getRevisionNumber() + " - "
						+ "Time: " + getBuildTime() + " - "
						+ "Build JDK: " + getBuildJdk() };
	}

	/**
	 * Gets the short information of the application's version.<br>
	 * <br>
	 * This includes the application's name, version number and build number.
	 *
	 * @return
	 *          an array containing the short information of the version
	 */
	public String[] getShortInfoStrings() {
		return new String[] {
				"[Module: " + getModuleName() + "]",
				"Version: " + getVersionNumber() + " - "
						+ "Revision: " + getRevisionNumber() };
	}

	/**
	 * Gets the major version number of the currently running JVM.
	 *
	 * @return
	 *          the JVM's major version number
	 */
	public int getJvmMajor() {
		return jvmMajor;
	}

	/**
	 * Gets the feature version number of the currently running JVM.
	 *
	 * @return
	 *          the JVM's feature version number
	 */
	public int getJvmFeature() {
		return jvmFeature;
	}

	/**
	 * Gets the update version number of the currently running JVM.
	 *
	 * @return
	 *          the JVM's update version number
	 */
	public int getJvmUpdate() {
		return jvmUpdate;
	}

	/**
	 * Gets the build version number of the currently running JVM.
	 *
	 * @return
	 *          the JVM's build version number
	 */
	public int getJvmBuild() {
		return jvmBuild;
	}

	/**
	 * Checks if the Java version of the currently running JVM is equal to the specified Java version.
	 *
	 * @param major
	 *          the major version number
	 * @param feature
	 *          the feature version number
	 * @param update
	 *          the update version number
	 * @param build
	 *          the build version number
	 * @return
	 *          {@code true} if the version numbers are equal, {@code false} otherwise
	 */
	public boolean isJvmEqual(int major, int feature, int update, int build) {
		if (major == -1) {
			return true;
		}
		else if (jvmMajor != major) {
			return false;
		}

		if (feature == -1) {
			return true;
		}
		else if (jvmFeature != feature) {
			return false;
		}

		if (update == -1) {
			return true;
		}
		else if (jvmUpdate != update) {
			return false;
		}

		if (build == -1) {
			return true;
		}
		else if (jvmBuild != build) {
			return false;
		}
		return true;
	}

	/**
	 * Checks if the Java version of the currently running JVM is lower than the specified version number.
	 *
	 * @param major
	 *          the major version number
	 * @param feature
	 *          the feature version number
	 * @param update
	 *          the update version number
	 * @param build
	 *          the build version number
	 * @return
	 *          {@code true} if the current Java version is lower, {@code false} otherwise
	 */
	public boolean isJvmLower(int major, int feature, int update, int build) {
		if (major == -1) {
			return true;
		}
		else if (jvmMajor >= major) {
			return false;
		}

		if (feature == -1) {
			return true;
		}
		else if (jvmFeature >= feature) {
			return false;
		}

		if (update == -1) {
			return true;
		}
		else if (jvmUpdate >= update) {
			return false;
		}

		if (build == -1) {
			return true;
		}
		else if (jvmBuild >= build) {
			return false;
		}
		return true;
	}

	/**
	 * Checks if the Java version of the currently running JVM is lower or equal to the specified version number.
	 *
	 * @param major
	 *          the major version number
	 * @param feature
	 *          the feature version number
	 * @param update
	 *          the update version number
	 * @param build
	 *          the build version number
	 * @return
	 *          {@code true} if the current Java version of lower or equal, {@code false} otherwise
	 */
	public boolean isJvmLowerOrEqual(int major, int feature, int update, int build) {
		if (major == -1) {
			return true;
		}
		else if (jvmMajor > major) {
			return false;
		}

		if (feature == -1) {
			return true;
		}
		else if (jvmFeature > feature) {
			return false;
		}

		if (update == -1) {
			return true;
		}
		else if (jvmUpdate > update) {
			return false;
		}

		if (build == -1) {
			return true;
		}
		else if (jvmBuild > build) {
			return false;
		}
		return true;
	}

	/**
	 * Checks if the Java version of the currently running JVM is higher than the specified version number.
	 *
	 * @param major
	 *          the major version number
	 * @param feature
	 *          the feature version number
	 * @param update
	 *          the update version number
	 * @param build
	 *          the build version number
	 * @return
	 *          {@code true} if the current Java version is higher, {@code false} otherwise
	 */
	public boolean isJvmHigher(int major, int feature, int update, int build) {
		if (major == -1) {
			return true;
		}
		else if (jvmMajor <= major) {
			return false;
		}

		if (feature == -1) {
			return true;
		}
		else if (jvmFeature <= feature) {
			return false;
		}

		if (update == -1) {
			return true;
		}
		else if (jvmUpdate <= update) {
			return false;
		}

		if (build == -1) {
			return true;
		}
		else if (jvmBuild <= build) {
			return false;
		}
		return true;
	}

	/**
	 * Checks if the Java version of the currently running JVM is higher or equal to the specified version number.
	 *
	 * @param major
	 *          the major version number
	 * @param feature
	 *          the feature version number
	 * @param update
	 *          the update version number
	 * @param build
	 *          the build version number
	 * @return
	 *          {@code true} if the current Java version is higher or equal, {@code false} otherwise
	 */
	public boolean isJvmHigherOrEqual(int major, int feature, int update, int build) {
		if (major == -1) {
			return true;
		}
		else if (jvmMajor < major) {
			return false;
		}

		if (feature == -1) {
			return true;
		}
		else if (jvmFeature < feature) {
			return false;
		}

		if (update == -1) {
			return true;
		}
		else if (jvmUpdate < update) {
			return false;
		}

		if (build == -1) {
			return true;
		}
		else if (jvmBuild < build) {
			return false;
		}
		return true;
	}
}
