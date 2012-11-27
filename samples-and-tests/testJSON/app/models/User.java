/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package models;

import com.google.gson.annotations.Expose;
import javax.persistence.Entity;

import pl.com.tt.play.modules.json.JsonRenderer.JsonIgnore;
import play.db.jpa.Model;

/**
 *
 * @author piechutm
 */
@Entity
public class User extends Model {
    public String name;
    @JsonIgnore
    public String surname;
    public int age;
}
