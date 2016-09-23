
import spark.ModelAndView;
import spark.Session;
import spark.Spark;
import spark.template.mustache.MustacheTemplateEngine;

import java.util.HashMap;

import static spark.Spark.post;

public class Main {
    static User user;
    static HashMap<String, User> users = new HashMap<>();

    public static void main(String[] args) {

        Spark.init();

        Spark.get("/",
                ((request, response) -> {
                    Session session = request.session();
                    String name = session.attribute("loginName");
                    String password = session.attribute("loginPassword");
                    User user = users.get(name);
                    HashMap m = new HashMap<>();
                    if (user == null) {
                        return new ModelAndView(m, "index.html");
                    } else {
                        if (user.password.equals(password)) {
                            m.put("name", user.name);
                            m.put("password", user.password);
                            m.put("messages", user.messages);
                            return new ModelAndView(m, "messages.html");
                        } else {
                            return new ModelAndView(m, "index.html");
                        }
                    }
                }),
                new MustacheTemplateEngine()
        );
        Spark.post(
                "/create-user",
                ((request, response) -> {
                    String name = request.queryParams("loginName");
                    String password = request.queryParams("loginPassword");
                    User user = users.get(name);
                    if (name.length() > 0 && password.length() > 0) {
                        if (user == null) {
                            if (!(name == null || password == null)) {
                                user = new User(name, password);
                                users.put(name, user);
                            } else {
                                throw new Exception("Incorrect name/ password combo");
                            }
                        }
                    }
                    Session session = request.session();
                    session.attribute("loginName", name);
                    session.attribute("loginPassword", password);
                    response.redirect("/");
                    return "";

                })
        );

        Spark.post("/logout",
                ((request, response) -> {
                    Session session = request.session();
                    session.invalidate();
                    response.redirect("/");
                    return "";
                })
        );

        post(
                "/create-message",
                ((request, response) -> {
                    Session session = request.session();
                    String name = session.attribute("loginName");
                    User user = users.get(name);
                    String messageText = request.queryParams("message");
                    Message message = new Message(messageText);
                    user.messages.add(message);
                    response.redirect("/");
                    return "";

                })
        );

        Spark.post("/delete-message",
                ((request, response) -> {
                    Session session = request.session();
                    String name = session.attribute("loginName");
                    int messageNum = (Integer.parseInt(request.queryParams("deleteNumber")));
                    User user = users.get(name);
                    user.messages.remove(messageNum -1);
                    response.redirect("/");
                    return "";
                }));
        Spark.post("/edit-message",
                ((request, response) -> {
                    Session session = request.session();
                    String name = session.attribute("loginName");
                    int messageNum = (Integer.parseInt(request.queryParams("editMessageNum"))-1);
                    String text = request.queryParams("editMessageText");
                    User user = users.get(name);
                    Message message = user.messages.get(messageNum);
                    message.message = text;
                    response.redirect("/");
                    return "";
                }));

    }
}
