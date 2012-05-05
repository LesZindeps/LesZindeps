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

import play.data.validation.Email;
import play.data.validation.MaxSize;
import play.data.validation.Required;
import play.data.validation.URL;
import play.db.jpa.GenericModel;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Lob;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

import org.hibernate.annotations.Type;

import java.util.Date;

/**
 * Class used to store an old indep, who have quit the group to have an employe or to be employed ;)
 */
@Entity
public class OldZindep extends GenericModel {
    @Id
    public String id;

    @Required(message = "Email est obligatoire")
    @Email
    @MaxSize(255)
    public String email;

    @Required(message = "Ce champ est obligatoire")
    @MaxSize(255)
    public String firstName;

    @Required(message = "Ce champ est obligatoire")
    @MaxSize(255)
    public String lastName;

    public String title;

    @Temporal(TemporalType.DATE)
    public Date memberSince;

    @Required(message = "Ce champ est obligatoire")
    @Lob
    @Type(type = "org.hibernate.type.TextType")
    public String location;

    // Correspond au champ summary de LinkedIn
    @Lob
    @Type(type = "org.hibernate.type.TextType")
    public String bio;

    // Correspond au champ specialties de LinkedIn
    @Lob
    @Type(type = "org.hibernate.type.TextType")
    public String techno;

    @Email
    @MaxSize(255)
    public String emailBackup;

    @MaxSize(255)
    @URL
    public String blogUrl;

    @MaxSize(255)
    public String twitter;

    @Temporal(TemporalType.DATE)
    public Date leftGroupSince;

    public OldZindep(Zindep zindep) {
        this.id = zindep.id;
        this.bio = zindep.bio;
        this.blogUrl = zindep.blogUrl;
        this.email = zindep.email;
        this.emailBackup = zindep.emailBackup;
        this.firstName = zindep.firstName;
        this.lastName = zindep.lastName;
        this.leftGroupSince = new Date();
        this.memberSince = zindep.memberSince;
        this.techno = zindep.techno;
        this.title = zindep.title;
        this.twitter = zindep.twitter;
    }

    public Zindep toZindep() {
        Zindep result = new Zindep();

        result.bio = this.bio;
        result.blogUrl = this.blogUrl;
        result.email = this.email;
        result.emailBackup = this.emailBackup;
        result.firstName = this.firstName;
        result.lastName = this.lastName;
        result.memberSince = this.memberSince;
        result.techno = this.techno;
        result.title = this.title;
        result.twitter = this.twitter;

        return result;
    }
}
