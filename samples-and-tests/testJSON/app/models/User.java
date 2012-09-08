/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package models;

import com.google.gson.annotations.Expose;
import javax.persistence.Entity;
import play.db.jpa.Model;

/**
 *
 * @author piechutm
 */
@Entity
public class User extends Model {
    @Expose
    public String name;
    public String surname;
    @Expose
    public int age;
}
