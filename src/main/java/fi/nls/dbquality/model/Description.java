package fi.nls.dbquality.model;

public class Description {
    private String lang;
    private String description;
    private String extraInfo;

    public void setExtraInfo(String extraInfo) { this.extraInfo = extraInfo; }

    public String getExtraInfo() { return extraInfo; }

    public String getLang() {
        return lang;
    }

    public void setLang(String lang) {
        this.lang = lang;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}
