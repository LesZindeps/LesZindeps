package controllers;

import models.Propal;
import models.Zindep;
import play.data.validation.Valid;
import play.libs.Codec;
import play.mvc.Controller;

import java.util.List;

/**
 * Un controleur different pour surveiller l'arrière-boutique.
 *
 * @author Nicolas Martignole
 * @since 21 déc. 2010 14:21:41
 */
public class BackOffice extends Controller {

    public static void index() {
        render();
    }


    /**
     * Affiche la liste des propals.
     */
    public static void showPropals() {
        List<Propal> listOfPropals = Propal.findAllByDate();
        render(listOfPropals);
    }

    public static void newZindep() {
        render();
    }

    /**
     * Valide et sauvegarde un nouvel indépendant
     *
     * @param zindep est le nouvel indépendant
     */
    public static void storeNewZindep(@Valid Zindep zindep) {
        // Handle errors
        if (validation.hasErrors()) {
            render("@newZindep", zindep);
        }


        zindep.validateAndSave();

        // Gravatar
       zindep.gravatarId = Codec.hexMD5(zindep.email.trim().toLowerCase());
        
        render();
    }

    public static void listZindeps() {
        List<Zindep> listOfZindeps = Zindep.findAllByName();
        render(listOfZindeps);
    }

    /**
     * Charge la fiche de l'indep spécifié
     *
     * @param zindep a editer
     * @param id a editer
     */
    public static void updateProfile(String id) {
        Zindep zindep = Zindep.findById(id);
        render(zindep);
    }

    public static void doUpdateProfile(@Valid Zindep zindep, String idEdit) {
        // Handle errors
        if (validation.hasErrors()) {
            render("@updateProfile", zindep);
        }
        Zindep existing=Zindep.findById(idEdit);
        if(existing==null){
            flash.error("Utilisateur non trouvé");
            listZindeps();
        }

        // c'est pourri et je pense qu'il y a un moyen plus intelligent pour le faire
        existing.email=zindep.email;
        existing.lastName=zindep.lastName;
        existing.firstName=zindep.firstName;
        existing.memberSince=zindep.memberSince;
        existing.location=zindep.location;

        existing.save();

        flash.success("Mise à jour effectuée");
        listZindeps();
    }

}

