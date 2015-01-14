/*
 * Copyright 2014 Pivotal Software, Inc..
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

import org.openrdf.repository.Repository;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * A controller for the views dedicated to user
 *
 * @author Aleksander Nowinski <a.nowinski@icm.edu.pl>
 */
@Controller
@EnableAutoConfiguration
public class UserViewController {

    private static final Logger log = org.slf4j.LoggerFactory.getLogger(UserViewController.class.getName());

    @Autowired
    Repository sesameRepository;

    @Autowired
    DataManager dataManager;

    @RequestMapping("/")
    String home() {
        return "home";
    }

    @RequestMapping("/query")
    String sparql() {
        return "query";
    }

    @RequestMapping("/read_data")
    String readData(ModelMap model) {
        new Thread(new Runnable() {

            @Override
            public void run() {
                dataManager.importData();
            }
        }).start();
        return "home";
    }
}
