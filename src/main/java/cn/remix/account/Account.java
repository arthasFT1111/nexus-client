package cn.remix.account;

public class Account {
    private String name;
    private String token;
    private String type;

    public Account(String name, String token, String type) {
        this.name = name;
        this.token = token;
        this.type = type;
    }

    public String getName() { return name; }
    public String getToken() { return token; }
    public String getType() { return type; }

    public void setName(String name) { this.name = name; }
    public void setToken(String token) { this.token = token; }
    public void setType(String type) { this.type = type; }
}