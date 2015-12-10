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

import net.sf.ehcache.Cache;
import net.sf.ehcache.CacheManager;
import org.openrdf.repository.Repository;
import org.openrdf.repository.RepositoryException;
import org.openrdf.repository.http.HTTPRepository;
import org.openrdf.repository.sail.SailRepository;
import org.openrdf.repository.sparql.SPARQLRepository;
import org.openrdf.sail.Sail;
import org.openrdf.sail.nativerdf.NativeStore;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import pl.edu.icm.comac.vis.server.service.SearchService;

/**
 * Creates the repository itself based on spring configuration.
 *
 * @author Aleksander Nowinski <a.nowinski@icm.edu.pl>
 */
@Configuration
@EnableConfigurationProperties(ServerSettings.class)
public class ServerConfiguration {

    private static final Logger log = org.slf4j.LoggerFactory.getLogger(ServerConfiguration.class.getName());

    @Autowired
    private ServerSettings settings;

    
    @Bean
    @Profile({"sparql", "remote"})
    SearchService buildBlazegraphSearchService(Repository r) {
        SearchService res = new SearchService();
        res.setRepo(r);
        res.setEnableBlazegraphSearch(true);
        return res;
    }
    @Bean
    @Profile("sesame")
    SearchService buildSesameSearchService(Repository r) {
        SearchService res = new SearchService();
        res.setRepo(r);
        res.setEnableBlazegraphSearch(false);
        return res;
    }
    
    @Bean
    CacheManager buildCacheManager() {
        CacheManager cm = CacheManager.getInstance();
        return cm;
    }

    @Bean(name = "idCache")
    Cache buildIdCache(CacheManager cm) {
        cm.addCache(ID_CACHE_NAME);
        return cm.getCache(ID_CACHE_NAME);
    }
    private static final String ID_CACHE_NAME = "idCache";

    @Bean
    @Profile("sesame")
    Repository buildSesameRepository(Sail sail) throws RepositoryException {
        log.info("Building sesame repository...");
        SailRepository repo = new SailRepository(sail);
        log.info("Initializing repository.");
        repo.initialize();
        return repo;
    }

    @Bean
    @Profile("sesame")
    Sail buildSailStore() {
        log.info("Building sail store...");
        Sail res = new NativeStore(settings.getWorkingDirectory(), "spoc posc opsc");
        return res;
    }

    @Bean
    @Profile("remote")
    Repository httpRepository() throws RepositoryException {
        HTTPRepository repo = new HTTPRepository(settings.getRepositoryUrl());
        repo.initialize();
        return repo;
    }

    @Bean
    @Profile("sparql")
    Repository sparqlRepository() throws RepositoryException {
        Repository repo = new SPARQLRepository(settings.getRepositoryUrl());
        repo.initialize();
        return repo;
    }
}
