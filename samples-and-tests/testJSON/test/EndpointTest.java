/*
 * Copyright (c) 2012 Transition Technologies S.A.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import models.User;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import play.mvc.Http.Header;
import play.mvc.Http.Request;
import play.mvc.Http.Response;
import play.test.Fixtures;
import play.test.FunctionalTest;

/**
 *
 * @author Marek Piechut <m.piechut@tt.com.pl>
 */
public class EndpointTest extends FunctionalTest {

    private Request request;

    @Test
    public void shouldPassIndex() {
        Response response = GET("/Application/index");
        assertStatus(200, response);
    }

    @Test
    public void shouldPassHtml() {
        Response response = GET("/Application/page");
        assertStatus(200, response);
    }

    @Test
    public void shouldReturnOnlyModel() {
        Response response = GET(request, "/Application/json");
        assertStatus(200, response);

        User user = getFromJSon(response, "model", User.class);
        assertEquals("Roman", user.name);
        assertEquals(43, user.age);
        assertNull(user.surname);
        
        String someStr = getFromJSon(response, "someStr", String.class);
        assertNull(someStr);
    }

    @Test
    public void shouldReturnOnlyDeclared() {
        Response response = GET(request, "/Application/jsonWithOutputExplicit");
        assertStatus(200, response);
        
        User user = getFromJSon(response, "model", User.class);
        assertNull(user);
        
        String someStr = getFromJSon(response, "someStr", String.class);
        assertEquals("asd", someStr);
        int number = getFromJSon(response, "number", Integer.class);
        assertEquals(123, number);
    }
    
    @Before
    public void createJsonRequest() {
        request = newRequest();
        request.headers.put("accept", new Header("accept", "application/json"));
    }
    
    @BeforeClass
    public static void loadData() {
        Fixtures.deleteDatabase();
        Fixtures.loadModels("data.yml");
    }
    
    private <T> T getFromJSon(Response response, String name, Class<T> clazz) {
        String json = new String(response.out.toByteArray());
        JsonElement elem = new JsonParser().parse(json);
        JsonElement model = elem.getAsJsonObject().get(name);
        T value = new Gson().fromJson(model, clazz);
        return value;
    }
}
