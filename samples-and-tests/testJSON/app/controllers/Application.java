package controllers;

import pl.com.tt.play.modules.json.JsonRenderer;
import pl.com.tt.play.modules.json.JsonRenderer.JsonEndpoint;
import play.mvc.*;

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
