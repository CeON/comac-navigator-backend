package pl.edu.icm.comac.vis.server;

import java.io.File;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties("comac")
public class ServerSettings {
    

    /**
     * URL of a remote Sesame repository
     */
    private String repositoryUrl;
    /**
     * Location for a local Sesame repository
     */
    private File workingDirectory;
    /**
     * Location for RDF files that can be imported to the repository
     */
    private File inputDirectory;

    public File getWorkingDirectory() {
        return workingDirectory;
    }

    public void setWorkingDirectory(File workingDirectory) {
        this.workingDirectory = workingDirectory;
    }

    public File getInputDirectory() {
        return inputDirectory;
    }

    public void setInputDirectory(File inputDirectory) {
        this.inputDirectory = inputDirectory;
    }

    public String getRepositoryUrl() {
        return repositoryUrl;
    }

    public void setRepositoryUrl(String repositoryUrl) {
        this.repositoryUrl = repositoryUrl;
    }
}
