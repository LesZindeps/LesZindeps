/*
 * Copyright(c) 2010 Les Zindeps.
 *
 * The code source of this project is distributed
 * under the Affero GPL GNU AFFERO GENERAL PUBLIC LICENSE
 * Version 3, 19 November 2007
 *
 * This file is part of project LesZindeps. The source code is
 * hosted on GitHub. The initial project was launched by
 * Nicolas Martignole.
 *
 * LesZindeps is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * LesZindeps is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 *
 * Please see COPYING.AGPL.txt for the full text license
 * or online http://www.gnu.org/licenses/agpl.html
 */

import models.Zindep;
import play.Logger;
import play.Play;
import play.jobs.Job;
import play.jobs.OnApplicationStart;
import play.test.Fixtures;

import java.util.List;

/**
 * Le bootstrap est execute en mode dev au demarrage pour placer quelques utilisateurs en base.
 *
 * @author Nicolas Martignole
 */
@OnApplicationStart
public class Bootstrap extends Job {

    public void doJob() {
        if (Play.mode == Play.Mode.DEV ) {
            //before tests run,we load each time the database to isolate them
            //weirdly, Zindep.count return 0 but there is a conflict in the database (data already exist)
            //datas are filled byt unit tests
            if (Zindep.count() == 0 && !Play.runingInTestMode()) {
                Fixtures.loadModels("test-datas.yml");
            }
        }

        //set to isVisible to false, when Zindep hasn't got any pictureUrl
        //useful to run only ONCE, because each Zindep update will check that pictureUrl is present
        List<Zindep> zindepsWithoutPictureUrl = Zindep.find(" pictureUrl IS NULL and isVisible = TRUE ").fetch();
        for(Zindep zindep : zindepsWithoutPictureUrl){
            zindep.isVisible = false;
            zindep.save();
        }

        
    }

}