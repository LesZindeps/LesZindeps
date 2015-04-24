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

package controllers;

import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeTokenRequest;
import com.google.api.client.googleapis.auth.oauth2.GoogleTokenResponse;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import models.Propal;
import models.Zindep;
import play.Logger;
import play.data.parsing.UrlEncodedParser;
import play.data.validation.Error;
import play.libs.OpenID;
import play.mvc.Before;
import play.mvc.Controller;

import java.util.List;
import java.util.Map;

/**
 * Ce controleur permet à chaque Zindep de gérer son profil librement.
 * Une bonne partie du code vient en fait de l'eXpress-Board.
 *
 * @author Nicolas Martignole
 * @since 22 déc. 2010 21:48:49
 */
public class Admin extends Controller {
    public static final String ZINDEP_ID = "zindepId";
    public static final NetHttpTransport TRANSPORT = new NetHttpTransport();
    public static final JsonFactory JSON_FACTORY = new JacksonFactory();

    // Protege toutes les methodes sauf index et authentification via openid
    //et authenticateAs pour les tests

    @Before(unless = {"index",
            "logout",
            "authenticateWithLinkedIn",
            "authenticateOpenId",
            "authenticateWithGSignin",
            "authenticateAs"
    })
    static void checkLogin() {
        if (!session.contains(ZINDEP_ID)) {
            flash.error("Merci de vous authentifier pour accéder à cette partie.");
            index();
        }
    }

    /**
     * Affiche la page d'accueil
     */
    public static void index() {
        if (session.contains(ZINDEP_ID)) {
            welcome();
        } else {
            render();
        }
    }

    public static void logout() {
        session.remove(ZINDEP_ID);
        session.remove("zindepEmail");
        flash.success("Vous avez été délogué.");
        index();

    }

   public static void authenticateWithGSignin() {
        try {
            //Parse the content, I not found how do better :(
            Map<String, String[]> map = new UrlEncodedParser().parse(request.body);
            String code = map.get("code")[0];

            authenticateAs(getEmail(code));
        } catch (Exception e) {
            flash.error("Erreur Google Signin authentication failed!");
            index();
        }
    }

    private static String getEmail(String code) throws Exception {
        GoogleTokenResponse tokenResponse = getGoogleToken(code);
        return tokenResponse.parseIdToken().getPayload().getEmail();
    }

    private static GoogleTokenResponse getGoogleToken(String code) throws Exception {
        String clientSecret = System.getProperty("googleClientSecret");
        return new GoogleAuthorizationCodeTokenRequest(
                TRANSPORT,
                JSON_FACTORY,
                "856980994888-o979bp449j9mqdc4ko0cp1njqtocr5cu.apps.googleusercontent.com",
                clientSecret,
                code, "postmessage").execute();
    }

    /**
     * Réalise l'authentification.
     * Le parametre action ne sert à rien ?
     */
    public static void authenticateOpenId(String action, String openid_identifier) {
        if (OpenID.isAuthenticationResponse()) {
            OpenID.UserInfo verifiedUser = OpenID.getVerifiedID();
            if (verifiedUser == null) {
                flash.error("Erreur OpenID generique");
                index();
            }

            String userEmail = verifiedUser.extensions.get("email");
            authenticateAs(userEmail);

        } else {
            if (openid_identifier == null) {
                flash.error("Param openid_identifier is null");
                index();
            }
            if (openid_identifier.trim().isEmpty()) {
                flash.error("Param openid_identifier is empty");
                index();
            }

            // Verify the id
            if (!OpenID.id(openid_identifier).required("email", "http://axschema.org/contact/email").verify()) {
                flash.put("error", "Impossible de s'authentifier avec l'URL utilisée.");
                index();
            }
        }
    }

    /**
     * this authentication bypass is only available in DEV mode
     *
     * @param userEmail
     */
    public static void authenticateAs(String userEmail) {
        if (userEmail == null) {
            flash.error("L'identification de votre compte sur le site des Zindeps s'effectue avec votre email." +
                    " Vous devez authoriser le domaine leszindeps.fr à accéder à votre email pour vous authentifier."
            );
            index();
        }

        Zindep zindep = Zindep.findByMail(userEmail);
        if (zindep == null) {
            flash.error("Désolé votre compte n'existe pas. Demandez à l'équipe d'ajouter votre email "
                    + userEmail
                    + " pour pouvoir vous authentifier avec ce compte.");
            index();
        }
        session.put(ZINDEP_ID, zindep.id);
        session.put("zindepEmail", zindep.email);

        flash.success("Bienvenue " + zindep.firstName);

        // Attention ne pas passer de parametre ici pr des raisons de securité
        welcome();
    }


    /**
     * Cette methode est appelé d'authenticateOpenIdGoogle mais elle peut
     * aussi etre appelée directement si je garde en bookmark cette ressource.
     * Play! Framework est completement sans etat, donc on cherche le zindepId
     * et on recharge l'entité
     */
    public static void showMyProfile() {
        String id = session.get(ZINDEP_ID);
        if (id == null) {
            error("Probleme avec l'authentification");
        }

        Zindep zindep = Zindep.findById(id);
        if (zindep == null) {
            error("Zindep non trouvé");
        }

        render(zindep); // cette variable zindep est celle utilisee dans la page HTML directement
    }

    /**
     * Sauvegarde les modifications
     *
     * @param zindep est une sorte de DTO
     */
    public static void doUpdateMyProfile(Zindep zindep) {
        checkAuthenticity(); // See http://www.playframework.org/documentation/1.1.1/releasenotes-1.0.2 
        String id = session.get(ZINDEP_ID);
        if (id == null) {
            error("Probleme avec l'authentification");
            return;
        }
        if (zindep.id == null) {
            flash.error("Impossible de mettre à jour votre profil.");
            showMyProfile();
            return;
        }

        if (!id.equals(zindep.id)) {
            flash.error("Vous ne pouvez mettre à jour que votre propre profil");
            showMyProfile();
            return;
        }

        // Validation rules
        validation.required(zindep.firstName);
        validation.maxSize(zindep.firstName, 255);
        validation.required(zindep.lastName);
        validation.maxSize(zindep.lastName, 255);
        validation.maxSize(zindep.location, 255);
        validation.maxSize(zindep.bio, 2000);
        validation.maxSize(zindep.techno, 2000);
        validation.valid(zindep.blogUrl);
        validation.email(zindep.emailBackup);
        validation.url(zindep.pictureUrl);
        validation.required(zindep.pictureUrl);
        validation.required(zindep.currentAvailability);


        Zindep existing = Zindep.findById(id);
        if (existing == null) {
            flash.error("Utilisateur non trouvé");
            index();
            return;
        }

        // L'email n'est pas repassé à la page d'édition
        // c'est la clé fonctionnelle. Donc on la recopie.
        zindep.email = existing.email;

        // Derniere validation globale
        validation.valid(zindep);
        if (validation.hasErrors()) {
            Map<String, List<Error>> map = validation.errorsMap();
            Logger.info("zindep updated is invalid:");
            for (List<Error> errors : map.values()) {
                for (Error error : errors) {
                    Logger.debug("key=" + error.getKey() + " message=" + error.message());
                }
            }
            render("@showMyProfile", zindep);
            return;
        }

        // Avec Play lorsque l'on passe une entité Zindep à une action, et qu'il y a
        // des cases à cocher comme isVisible, il faut reprendre la map des parametres
        // et voir si la personne a gardé la case cochée ou non.
        // C'est pas top.
        zindep.isVisible = (request.params.get("zindep.isVisible") != null);

        // Et persiste
        zindep.save();


        flash.success("Mise à jour effectuée");
        showMyProfile();
    }

    /**
     * Page d'accueil une fois authentifié
     */
    public static void welcome() {
        render();
    }

    /**
     * Affiche la liste des propals.
     */
    public static void listPropals() {
        List<Propal> listOfPropals = Propal.findAllByDate();
        render(listOfPropals);
    }

    public static void importFromLinkedIn() {
        String id = session.get(ZINDEP_ID);
        if (id == null) {
            error("Probleme avec l'authentification");
        }

        Zindep zindep = Zindep.findById(id);
        if (zindep == null) {
            error("Zindep non trouvé");
        }

        render();
    }

    /**
     *
     */
    public static void javascriptWidget() {
        String id = session.get(ZINDEP_ID);
        if (id == null) {
            error("Probleme avec l'authentification");
        }

        Zindep zindep = Zindep.findById(id);
        if (zindep == null) {
            error("Zindep non trouvé");
        }

        render(zindep);
    }

    /**
     * Cette fonction est appelée par l'API JS de LinkedIn lorsque l'utilisateur s'est bien authentifié.
     * Je m'en sers pour recopier l'id linkedin dans sa fiche.
     *
     * @param linkedInId est l'id unique de chaque utilisateur.
     */
    public static void copyLinkedInProfile(String linkedInId) {
        if (linkedInId == null) {
            flash.error("Param linkedInId missing");
            render();  // Ici comprendre return "ma page" car render() arrete l'execution de cette methode.
        }
        if (linkedInId.equals("undefined")) {
            flash.error("Erreur javascript, impossible de retrouver l'attribut linkedIn Id. Vérifiez la console JS.");
            render();
        }
        Zindep zindep = Zindep.findByLinkedInId(linkedInId);

        if (zindep == null) {
            // Recherche via la session
            Zindep fromSession = Zindep.findById(session.get(ZINDEP_ID));
            if (fromSession == null) {
                flash.error("Impossible de retrouver votre compte... vous n'etes pas authentifié ?");
                render();
            }
            fromSession.linkedInId = linkedInId;
            fromSession.save();
            flash.success("Votre compte Zindep est maintenant lié à votre compte LinkedIn.");
            render(fromSession);
        }
        render(zindep);
    }

    /**
     * Sauvegarde vers votre profil les parametres récuperés sur votre compte linkedin.
     *
     * @param firstName
     * @param lastName
     * @param title
     * @param pictureUrl
     * @param bio
     * @param techno
     */
    public static void doUpdateMyProfileFromLinkedIn(String firstName, String lastName, String title, String pictureUrl,
                                                     String bio, String techno) {
        String id = session.get(ZINDEP_ID);
        if (id == null) {
            error("Probleme avec l'authentification");
        }

        Zindep zindep = Zindep.findById(id);
        if (zindep == null) {
            error("Zindep non trouvé");
        }

        zindep.firstName = firstName;
        zindep.lastName = lastName;
        zindep.title = title;
        zindep.pictureUrl = pictureUrl;
        zindep.bio = bio;
        zindep.techno = techno;
        zindep.save();

        flash.success("Votre profil a été mis à jour à partir des données de LinkedIn");
        showMyProfile();


    }
}
