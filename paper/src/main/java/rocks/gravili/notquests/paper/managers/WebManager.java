package rocks.gravili.notquests.paper.managers;

import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.util.EntityUtils;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import rocks.gravili.notquests.paper.NotQuests;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.apache.http.client.HttpClient;
import rocks.gravili.notquests.paper.managers.data.Category;

import java.io.IOException;
import java.io.UnsupportedEncodingException;

public class WebManager {
    private final NotQuests main;
    Gson gson          = new Gson();

    public WebManager(final NotQuests main){
        this.main = main;


        /*Assert.assertTrue(jsonObject.isJsonObject());
        Assert.assertTrue(jsonObject.get("name").getAsString().equals("Baeldung"));
        Assert.assertTrue(jsonObject.get("java").getAsBoolean() == true);*/

    }

    String convertYamlToJson(String yaml) throws JsonProcessingException {
        ObjectMapper yamlReader = new ObjectMapper(new YAMLFactory());
        Object obj = yamlReader.readValue(yaml, Object.class);

        ObjectMapper jsonWriter = new ObjectMapper();
        return jsonWriter.writeValueAsString(obj);
    }

    public String sendRequest(JsonObject jsonObject) throws IOException {
        //String       postURL       = "editor.notquests.com/webeditornew";// put in your url
        String postURL = "https://editor.notquests.com/api/webeditors";
        HttpClient   httpClient    = HttpClientBuilder.create().build();
        HttpPost post          = new HttpPost(postURL);
        StringEntity postingString = new StringEntity(gson.toJson(jsonObject));//gson.tojson() converts your pojo to json
        post.setEntity(postingString);
        post.setHeader("Content-type", "application/json");



        HttpResponse result = httpClient.execute(post);


        return EntityUtils.toString(result.getEntity(), "UTF-8");


        //main.getLogManager().info("Response: " + response.toString());
    }

    public String openEditor() {
        JsonObject jsonObject = toJson();

        try{
            String jsonReturnObject = sendRequest(jsonObject);
            main.getLogManager().info("JSON returned: \n" + jsonReturnObject);
            return jsonReturnObject;
            //main.getLogManager().info("<success>Sent web request!");
        }catch (IOException e){
            main.getLogManager().warn("Cannot send web request!");
            e.printStackTrace();
        }

        return "Cannot send web request!";
    }

    public JsonObject toJson(){
        /*String jsonDefaultCategoryQuests = "{}";

        try{
            jsonDefaultCategoryQuests = convertYamlToJson(main.getDataManager().getDefaultCategory().getQuestsConfig().saveToString());
        }catch (JsonProcessingException e){
            main.getLogManager().warn("Cannot convert Quests YAML to web-ready JSON.");
            e.printStackTrace();
        }

        main.getLogManager().info("JSON to send: \n" + jsonDefaultCategoryQuests);*/



        JsonObject categories = new JsonObject();

        try{
            for(Category category : main.getDataManager().getCategories()){
                JsonObject categoryObject = new JsonObject();

                String ymlQuestsString = category.getQuestsConfig().saveToString();
                String ymlActionsString = category.getActionsConfig().saveToString();
                String ymlConditionsString = category.getConditionsConfig().saveToString();





                categories.add(category.getCategoryName(), categoryObject);

            }
        }catch (Exception e){
            main.getLogManager().warn("Cannot convert Quests YAML to web-ready JSON.");
            e.printStackTrace();
        }



        return categories;

    }

}
