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
package pl.edu.icm.comac.vis.server.service;

import java.io.IOException;
import javax.sql.DataSource;
import net.sf.ehcache.Cache;
import net.sf.ehcache.CacheManager;
import org.openrdf.OpenRDFException;
import org.openrdf.repository.Repository;
import org.openrdf.repository.RepositoryConnection;
import org.openrdf.repository.sail.SailRepository;
import org.openrdf.rio.RDFFormat;
import org.openrdf.sail.Sail;
import org.openrdf.sail.memory.MemoryStore;
import org.slf4j.Logger;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabase;
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabaseBuilder;
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabaseType;

/**
 *
 * @author Aleksander Nowinski <a.nowinski@icm.edu.pl>
 */
@Configuration
//@ComponentScan("pl.edu.icm.comac.vis.service")
public class TestConfig {

    private static final Logger log = org.slf4j.LoggerFactory.getLogger(TestConfig.class.getName());

    @Bean
    public DataSource dataSource() {

        // no need shutdown, EmbeddedDatabaseFactoryBean will take care of this
        EmbeddedDatabaseBuilder builder = new EmbeddedDatabaseBuilder();
        EmbeddedDatabase db = builder
                .setType(EmbeddedDatabaseType.HSQL) //.H2 or .DERBY
                .addScript("sql/prepare_database_1.00.sql")
                .build();
        return db;
    }

    @Bean
    public JdbcTemplate getJdbcTemplate(DataSource dataSource) {
        return new JdbcTemplate(dataSource);
    }

    @Bean
    public DbGraphIdService graphService() {
        return new DbGraphIdService();
    }

    @Bean
    public Repository prepareSmallRepository() throws OpenRDFException, IOException {
        log.info("Building sail store...");
        Sail sail = new MemoryStore();
        log.info("Building sesame repository...");
        SailRepository repo = new SailRepository(sail);
        log.info("Initializing repository.");
        repo.initialize();
        log.info("Loading data....");
        RepositoryConnection con = null;
        con = repo.getConnection();
        String shortTurtle = "/rdf/small.rdf";
        log.info("Loading data from: {}", shortTurtle);
        con.add(TestConfig.class.getResourceAsStream(shortTurtle), "http://comac.edu.pl/", RDFFormat.TURTLE);
        log.info("Successfully loaded.");
        return repo;
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
    NodeTypeService buildNodeTypeService() {
        NodeTypeService ns = new NodeTypeService();
        return ns;
    }
    
    @Bean
    GraphService buildGraphService() {
        return new GraphService();
    }
}
