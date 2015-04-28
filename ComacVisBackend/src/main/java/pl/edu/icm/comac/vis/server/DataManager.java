/*
 * Copyright 2015 Pivotal Software, Inc..
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package pl.edu.icm.comac.vis.server;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import javax.annotation.PreDestroy;
import org.apache.commons.io.FileUtils;
import org.openrdf.OpenRDFException;
import org.openrdf.repository.Repository;
import org.openrdf.repository.RepositoryConnection;
import org.openrdf.repository.RepositoryException;
import org.openrdf.rio.RDFFormat;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 *
 * @author Aleksander Nowinski <a.nowinski@icm.edu.pl>
 */
@Service
public class DataManager {

    private static final Logger log = org.slf4j.LoggerFactory.getLogger(DataManager.class.getName());
    @Autowired
    Repository repository;
    @Autowired
    private ServerSettings settings;

    boolean running = false;

    public void importData() {
        log.info("Request to load data.");
        if (running) {
            log.warn("Data import already..");
            return;
        }
        running = true;
        //first find files:
        List<File> inputFiles = new ArrayList<File>();
        RepositoryConnection con = null;
        try {
            File input = settings.getInputDirectory();
            log.info("Input directory: {}", input);
            if (input.isDirectory()) {
                inputFiles.addAll(FileUtils.listFiles(input, null, true));
            } else {
                inputFiles.add(input);
            }
            con = repository.getConnection();
            for (File f : inputFiles) {
                log.info("Loading data from: {}", f);
                con.add(f, "http://comac.edu.pl/", RDFFormat.TURTLE);
                log.info("Successfully loaded.");
            }
        } catch (OpenRDFException | IOException e) {
            log.warn("Unexpecte exception: {}", e);
        } finally {
            if (con != null) {
                try {
                    con.close();
                } catch (RepositoryException ex) {
                    log.warn("Error while closing: {}", ex);
                }
            }
        }
        log.info(
                "Data loading finished.");
        running = false;
    }
    
    
    @PreDestroy
    void destroyRepository() throws RepositoryException {
        log.info("Disposing respository...");
        repository.shutDown();
        log.info("Done.");
    }
}
