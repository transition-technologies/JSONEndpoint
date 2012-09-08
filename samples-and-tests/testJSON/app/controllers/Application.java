package controllers;

import controllers.json.JsonRenderer;
import controllers.json.JsonRenderer.JsonEndpoint;
import play.*;
import play.mvc.*;

import java.util.*;

import models.*;

@With(JsonRenderer.class)
public class Application extends Controller {

    public static void index() {
        render();
    }

    public static void page() {
        User model = User.all().first();
        render(model);
    }

    @JsonEndpoint
    public static void json() {
        User model = User.all().first();
        String someStr = "asd";
        render(model, someStr);
    }

    @JsonEndpoint(output = {"someStr", "number"})
    public static void jsonWithOutputExplicit() {
        User model = User.all().first();
        String someStr = "asd";
        int number = 123;
        render(model, someStr, number);
    }
}