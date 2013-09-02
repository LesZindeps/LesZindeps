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
package models;

import org.hibernate.annotations.GenericGenerator;
import play.db.jpa.GenericModel;
import play.db.jpa.JPABase;

import javax.persistence.*;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * représente l'état de disponibilité des zindeps lorsque l'un d'entre eux change la sienne.
 */
@Entity
public class ZindepAvailabilitiesEntry extends GenericModel {

    @Id
    @GeneratedValue(generator = "system-uuid")
    @GenericGenerator(name = "system-uuid", strategy = "uuid")
    public String id;

    public Date updateDate;
    @OneToOne
    public Zindep lastZindepModified;
    public Zindep.Availability previousAvailability;
    public Zindep.Availability currentAvailability;

    @ManyToMany
    @JoinTable(
            name = "zindepsPartTime",
            joinColumns = @JoinColumn(name = "availability_id"),
            inverseJoinColumns = @JoinColumn(name = "zindep_id")
    )
    public Set<Zindep> zindepsPartTime = new HashSet<Zindep>();
    @ManyToMany
    @JoinTable(
            name = "zindepsFullTime",
            joinColumns = @JoinColumn(name = "availability_id"),
            inverseJoinColumns = @JoinColumn(name = "zindep_id")
    )
    public Set<Zindep> zindepsFullTime = new HashSet<Zindep>();
    @ManyToMany
    @JoinTable(
            name = "zindepsNotAvailable",
            joinColumns = @JoinColumn(name = "availability_id"),
            inverseJoinColumns = @JoinColumn(name = "zindep_id")
    )
    public Set<Zindep> zindepsNotAvailable = new HashSet<Zindep>();


    public void removeZindepFromListsAndSaveIt(Zindep zindep) {
        zindepsPartTime.remove(zindep);
        zindepsFullTime.remove(zindep);
        zindepsNotAvailable.remove(zindep);

        save();
    }

    @Override
    public <T extends JPABase> T delete() {
        clearLists();
        return super.delete();
    }

    private void clearLists() {
        zindepsPartTime.clear();
        zindepsFullTime.clear();
        zindepsNotAvailable.clear();
    }

    public static void delete(Zindep zindep) {
        removeZindepFromAllZindepAvailabilitiesEntry(zindep);
        deleteZindepAvailabilitiesEntry(zindep);
    }

    private static void deleteZindepAvailabilitiesEntry(Zindep zindep) {
        String query = "from ZindepAvailabilitiesEntry z where z.lastZindepModified.id = ?";
        List<ZindepAvailabilitiesEntry> toDeleteEntry = find(query, zindep.id).fetch();

        for (ZindepAvailabilitiesEntry entry : toDeleteEntry) {
            entry.delete();
        }
    }

    private static void removeZindepFromAllZindepAvailabilitiesEntry(Zindep zindep) {
        //Clean FK
        List<ZindepAvailabilitiesEntry> allZindepAvailabilitiesEntry = findAll();
        for (ZindepAvailabilitiesEntry entry  : allZindepAvailabilitiesEntry) {
            entry.removeZindepFromListsAndSaveIt(zindep);
        }
    }
}