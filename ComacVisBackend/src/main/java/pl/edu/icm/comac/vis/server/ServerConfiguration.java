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
import org.openrdf.repository.Repository;
import org.openrdf.repository.RepositoryException;
import org.openrdf.repository.sail.SailRepository;
import org.openrdf.sail.Sail;
import org.openrdf.sail.memory.MemoryStore;
import org.openrdf.sail.nativerdf.NativeStore;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Creates the repository itself based on spring configuration.
 * @author Aleksander Nowinski <a.nowinski@icm.edu.pl>
 */
@Configuration
public class ServerConfiguration {
    private static final Logger log = org.slf4j.LoggerFactory.getLogger(ServerConfiguration.class.getName());
    @Value("${working_dir}")
    String workingDirectory;

    @Bean
    Repository buildSesameRepository(Sail sail) throws RepositoryException {
        log.info("Building sesame repository...");
        SailRepository repo = new SailRepository(sail);
        log.info("Initializing repository.");
        repo.initialize();
        return repo;
    }

    @Bean
    Sail buildSailStore() {
        log.info("Building sail store...");
        Sail res = new NativeStore(new File(workingDirectory), "spoc posc opsc");
        return res;
    }
    
}
