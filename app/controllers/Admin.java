package controllers;

import models.Propal;
import models.Zindep;
import play.data.validation.Valid;
import play.libs.OpenID;
import play.mvc.Before;
import play.mvc.Controller;

import java.util.List;

/**
 * Ce controleur permet à chaque Zindep de gérer son profil librement.
 * Une bonne partie du code vient en fait de l'eXpress-Board.
 *
 * @author Nicolas Martignole
 * @since 22 déc. 2010 21:48:49
 */
public class Admin extends Controller {
    // Protege toutes les methodes sauf index et authentification via openid

    @Before(unless = {"index", "logout",
            "authenticateOpenId"
    })
    static void checkLogin() {
        if (!session.contains("zindepId")) {
            flash.error("Merci de vous authentifier pour accéder à cette partie.");
            index();
        }
    }

    /**
     * Affiche la page d'accueil
     */
    public static void index() {
        render();
    }

    public static void logout() {
        session.remove("zindepId");
        session.remove("zindepEmail");
        flash.success("Vous avez été délogué.");
        index();

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
            session.put("zindepId", zindep.id);
            session.put("zindepEmail", zindep.email);

            flash.success("Bienvenue " + zindep.firstName);

            // Attention ne pas passer de parametre ici pr des raisons de securité
            welcome();

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
     * Cette methode est appelé d'authenticateOpenIdGoogle mais elle peut
     * aussi etre appelée directement si je garde en bookmark cette ressource.
     * Play! Framework est completement sans etat, donc on cherche le zindepId
     * et on recharge l'entité
     */
    public static void showMyProfile() {
        String id = session.get("zindepId");
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
        String id = session.get("zindepId");
        if (id == null) {
            error("Probleme avec l'authentification");
        }
        // Validation rules
        validation.required(zindep.firstName);
        validation.maxSize(zindep.firstName, 255);
        validation.required(zindep.lastName);
        validation.maxSize(zindep.lastName, 255);
        validation.maxSize(zindep.location, 255);
        validation.maxSize(zindep.bio, 2000);
        validation.maxSize(zindep.techno, 2000);

        // Handle errors
        if (validation.hasErrors()) {
            render("@showMyProfile", zindep);
        }
        Zindep existing = Zindep.findById(id);
        if (existing == null) {
            flash.error("Utilisateur non trouvé");
            index();
        }

        // c'est pourri et je pense qu'il y a un moyen plus intelligent pour le faire
        existing.lastName = zindep.lastName;
        existing.firstName = zindep.firstName;
        existing.memberSince = zindep.memberSince;
        existing.location = zindep.location;
        existing.bio = zindep.bio;
        existing.techno = zindep.techno;

        existing.save();

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

}
