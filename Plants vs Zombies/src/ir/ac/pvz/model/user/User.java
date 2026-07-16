package ir.ac.pvz.model.user;

import java.util.ArrayList;
import java.util.List;

public class User {
    private String username;
    private String passwordHash;
    private String nickName;
    private String email;

    private Gender gender;
    private int securityQuestionId;
    private String securityAnswer;

    private int gamesCount;
    private int gameProgress;
    private int maxMuPoint;
    private int difficultyLevel;

    private PlayerWallet wallet;
    private Collection collection;
    private GreenHouse greenHouse;
    private List<News> newsList;
    private QuestLog questLog;
    private Inventory inventory;

    //Constructor

    public User(String username, String passwordHash, String nickName, String email,
                Gender gender, int securityQuestionId, String securityAnswer) {
        this.username = username;
        this.passwordHash = passwordHash;
        this.nickName = nickName;
        this.email = email;

        this.gender = gender;
        this.securityQuestionId = securityQuestionId;
        this.securityAnswer = securityAnswer;

        this.gameProgress = 0;
        this.maxMuPoint = 0;
        this.difficultyLevel = 3;

        this.wallet = new PlayerWallet();
        this.collection = new Collection();
        this.greenHouse = new GreenHouse();
        this.newsList = new ArrayList<>();
        this.questLog = new QuestLog();
        this.inventory = new Inventory();
    }

    //Getters

    public String getUsername() { return username; }

    public String getPasswordHash() { return passwordHash; }

    public String getNickName() {
        return nickName;
    }

    public String getEmail() { return email; }

    public Gender getGender() { return gender; }

    public int getSecurityQuestionId() { return securityQuestionId; }

    public String getSecurityAnswer() { return securityAnswer; }

    public int getGamesCount() { return gamesCount; }

    public int getGameProgress() { return gameProgress; }

    public int getMaxMuPoint() { return maxMuPoint; }

    public int getDifficultyLevel() {return difficultyLevel; }

    public PlayerWallet getWallet() { return wallet; }

    public Collection getCollection() { return collection; }

    public GreenHouse getGreenHouse() { return greenHouse; }

    public List<News> getNewsList() { return newsList; }

    public List<News> getUnreadNews() {
        List<News> unreadNews = new ArrayList<>();

        for (News news : this.newsList) {
            if (!news.isRead()) unreadNews.add(news);
        }

        return unreadNews;
    }

    public QuestLog getQuestLog() { return questLog; }

    public Inventory getInventory() { return inventory; }

    //Setters
    public void setUsername(String username) {
        this.username = username;
    }

    public void setPasswordHash(String passwordHash) { this.passwordHash = passwordHash; }

    public void setNickName(String nickName) { this.nickName = nickName; }

    public void setEmail(String email) { this.email = email; }

    public void setGender(Gender gender) { this.gender = gender; }

    public void setGameProgress(int gameProgress) { this.gameProgress = gameProgress; }

    public void setMaxMuPoint(int maxMuPoint) { this.maxMuPoint = maxMuPoint; }

    public boolean setDifficultyLevel(int difficultyLevel) {
        if (difficultyLevel < 1 || difficultyLevel > 5) {
            return false;
        }
        this.difficultyLevel = difficultyLevel;
        return true;
    }

    public void addGame() { this.gamesCount++; }

    public void addNews(String message, NewsType type) {
        this.newsList.add(new News(message, type));
    }
}
